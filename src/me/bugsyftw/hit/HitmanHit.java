package me.bugsyftw.hit;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Logger;

import me.bugsyftw.hit.uuid.UUIDManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class HitmanHit extends JavaPlugin implements Listener {

	public static Logger log = Logger.getLogger("Minecraft");
	private Economy econ = null;

	public static HitmanHit hh;

	private Permission p_create = new Permission("hit.create");
	private Permission p_del = new Permission("hit.del");
	private Permission p_list = new Permission("hit.list");

	public static HitmanHit getHitmanHit() {
		return hh;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ != null;
	}

	public void onEnable() {
		if (!setupEconomy()) {
			log.severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		hh = this;
		saveConfig();
		log.info("HitmanHit has been Enabled!");
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this, this);
		pm.addPermission(p_create);
		pm.addPermission(p_del);
		pm.addPermission(p_list);
		getConfig().options().copyDefaults(true);
		saveConfig();
		try {
			HitManager.getHitManager().loadHits();
		} catch (Exception e) {
			// Nothing
		}

		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}

	public void onDisable() {
		log.info("HitmanHit has been Disabled!");
		saveConfig();
		PluginManager pm = getServer().getPluginManager();
		pm.removePermission(p_create);
		pm.removePermission(p_del);
		pm.removePermission(p_list);
	}

	@SuppressWarnings("deprecation")
	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
		if (commandLabel.equalsIgnoreCase("hit")) {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Wrong Syntax: " + ChatColor.RED + "/hit create/delete/list");
				return false;
			} else {
				if (args[0].equalsIgnoreCase("create")) {
					if (sender instanceof Player) {
						if (sender.hasPermission("hit.create")) {
							if (args.length == 1 || args.length == 2) {
								sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Wrong Syntax: " + ChatColor.RED + "/hit create <name> <ammount> (<number_of_hits> Optional)");
							} else if (args.length == 3) {
								if (isInteger(args[2])) {
									if (Bukkit.getPlayer(args[1]) != null) {
										Player pl = Bukkit.getPlayer(args[1]);
										UUID uuid = UUIDManager.getUUIDFromPlayer(pl.getName());
										if (HitManager.getHit(uuid) == null) {
											if (getConfig().getBoolean("Create") == false) {
												if (Integer.parseInt(args[2]) >= getMinimum()) {
													Player p = (Player) sender;
													if (econ.getBalance(p.getName()) >= Double.parseDouble(args[2])) {
														EconomyResponse r = econ.withdrawPlayer(p.getName(), Double.parseDouble(args[2]));
														if (r.transactionSuccess()) {
															Hit hit = new Hit(uuid, Integer.parseInt(args[2]), 1);
															Bukkit.broadcastMessage(ChatColor.YELLOW + "A [Hit] has been placed on '" + ChatColor.RED + UUIDManager.getPlayerFromUUID(uuid) + "' " + ChatColor.YELLOW + "for an ammount of " + ChatColor.GREEN + args[2] + "$" + ChatColor.YELLOW + " (Hits: " + hit.getNumberOfHits() + ")");
															p.sendMessage(ChatColor.GREEN + "We have Withdraw " + r.amount + "$ from your bank to create the Hit");
														} else {
															p.sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
														}
													} else {
														p.sendMessage(ChatColor.RED + "You do not have enough money to create this Hit");
														return false;
													}
												} else {
													sender.sendMessage(ChatColor.RED + "The" + ChatColor.UNDERLINE + ChatColor.RED + " Minimum" + ChatColor.RED + " ammount of reward needs to be " + ChatColor.RED + ChatColor.UNDERLINE + "Higher or Exact than" + ChatColor.GREEN + ChatColor.BOLD.toString() + getMinimum() + ChatColor.GREEN + "$");
												}
											} else if (getConfig().getBoolean("Create") == true) {
												if (Integer.parseInt(args[2]) >= getMinimum()) {
													Player p = (Player) sender;
													if (econ.getBalance(p.getName()) >= Double.parseDouble(args[2])) {
														EconomyResponse r = econ.withdrawPlayer(p.getName(), Double.parseDouble(args[2]));
														if (r.transactionSuccess()) {
															Hit hit = new Hit(uuid, Integer.parseInt(args[2]), 1);
															Bukkit.broadcastMessage(ChatColor.YELLOW + "A [Hit] has been placed on '" + ChatColor.RED + UUIDManager.getPlayerFromUUID(uuid) + "' " + ChatColor.YELLOW + "for an ammount of " + ChatColor.GREEN + args[2] + "$" + ChatColor.YELLOW + " (Hits: " + hit.getNumberOfHits() + ")");
															p.sendMessage(ChatColor.GREEN + "We have Withdraw " + r.amount + "$ from your bank to create the Hit");
														} else {
															p.sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
														}
													} else {
														p.sendMessage(ChatColor.RED + "You do not have enough money to create this Hit");
														return false;
													}
												} else {
													sender.sendMessage(ChatColor.RED + "The" + ChatColor.UNDERLINE + ChatColor.RED + " Minimum" + ChatColor.RED + " ammount of reward needs to be " + ChatColor.RED + ChatColor.UNDERLINE + "Higher or Exact than" + ChatColor.GREEN + ChatColor.BOLD.toString() + getMinimum() + ChatColor.GREEN + "$");
												}
											}
										} else {
											sender.sendMessage(ChatColor.RED + "A Hit already exists on that player");
										}
									} else {
										sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + args[1] + " is not Online!");
										return false;
									}
								} else {
									sender.sendMessage(ChatColor.RED + "The 2 argument (<ammount>), must be a number!");
									return false;
								}
							} else if (args.length == 4) {
								if (isInteger(args[3])) {
									if (isInteger(args[2])) {
										if (Bukkit.getPlayer(args[1]) != null) {
											Player pl = Bukkit.getPlayer(args[1]);
											UUID uuid = UUIDManager.getUUIDFromPlayer(pl.getName());
											if (HitManager.getHit(uuid) == null) {
												if (getConfig().getBoolean("Create") == false) {
													if (Integer.parseInt(args[2]) >= getMinimum()) {
														Player p = (Player) sender;
														if (econ.getBalance(p.getName()) >= Double.parseDouble(args[2])) {
															EconomyResponse r = econ.withdrawPlayer(p.getName(), Double.parseDouble(args[2]));
															if (r.transactionSuccess()) {
																Hit hit = new Hit(uuid, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
																Bukkit.broadcastMessage(ChatColor.YELLOW + "A [Hit] has been placed on '" + ChatColor.RED + args[1] + "' " + ChatColor.YELLOW + "for an ammount of " + ChatColor.GREEN + args[2] + "$" + ChatColor.YELLOW + " (Hits: " + hit.getNumberOfHits() + ")");
																p.sendMessage(ChatColor.GREEN + "We have Withdraw " + r.amount + "$ from your bank to create the Hit");
															} else {
																p.sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
															}
														} else {
															p.sendMessage(ChatColor.RED + "You do not have enough money to create this Hit");
															return false;
														}
													} else {
														sender.sendMessage(ChatColor.RED + "The" + ChatColor.UNDERLINE + ChatColor.RED + " Minimum" + ChatColor.RED + " ammount of reward needs to be " + ChatColor.RED + ChatColor.UNDERLINE + "Higher or Exact than" + ChatColor.GREEN + ChatColor.BOLD.toString() + getMinimum() + ChatColor.GREEN + "$");
													}
												} else if (getConfig().getBoolean("Create") == true) {
													if (Integer.parseInt(args[2]) >= getMinimum()) {
														Player p = (Player) sender;
														if (econ.getBalance(p.getName()) >= Double.parseDouble(args[2])) {
															EconomyResponse r = econ.withdrawPlayer(p.getName(), Double.parseDouble(args[2]));
															if (r.transactionSuccess()) {
																Hit hit = new Hit(uuid, Integer.parseInt(args[2]), Integer.parseInt(args[3]));
																Bukkit.broadcastMessage(ChatColor.YELLOW + "A [Hit] has been placed on '" + ChatColor.RED + args[1] + "' " + ChatColor.YELLOW + "for an ammount of " + ChatColor.GREEN + args[2] + "$" + ChatColor.YELLOW + " (Hits: " + hit.getNumberOfHits() + ")");
																p.sendMessage(ChatColor.GREEN + "We have Withdraw " + r.amount + "$ from your bank to create the Hit");
															} else {
																p.sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
															}
														} else {
															p.sendMessage(ChatColor.RED + "You do not have enough money to create this Hit");
															return false;
														}
													} else {
														sender.sendMessage(ChatColor.RED + "The" + ChatColor.UNDERLINE + ChatColor.RED + " Minimum" + ChatColor.RED + " ammount of reward needs to be " + ChatColor.RED + ChatColor.UNDERLINE + "Higher or Exact than" + ChatColor.GREEN + ChatColor.BOLD.toString() + getMinimum() + ChatColor.GREEN + "$");
													}
												}
											} else {
												sender.sendMessage(ChatColor.RED + "A Hit already exists on that player");
											}
										} else {
											sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + args[1] + " is not Online!");
											return false;
										}
									} else {
										sender.sendMessage(ChatColor.RED + "The 2 argument (<ammount>), must be a number!");
										return false;
									}
								} else {
									sender.sendMessage(ChatColor.RED + "The 3 argument (<number_of_hits>), must be a number!");
									return false;
								}
							}
						} else {
							sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
							return false;
						}
					}
				} else if (args[0].equalsIgnoreCase("delete")) {
					// hits delete <name>
					if (sender.hasPermission(p_del)) {
						if (args.length == 0 || args.length == 1) {
							sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "Wrong Syntax: " + ChatColor.RED + "/hit delete <player>");
							return false;
						}
						UUID uuid = UUIDManager.getUUIDFromPlayer(args[1]);
						if (HitManager.getHit(uuid) != null) {
							sender.sendMessage(ChatColor.YELLOW + "You have deleted the Hit on " + HitManager.getHit(uuid).getCurrentName());
							HitManager.getHit(uuid).delete();
						} else {
							sender.sendMessage(ChatColor.RED + "There is not Hit on " + args[1]);
						}
					} else {
						sender.sendMessage(ChatColor.DARK_RED + "You do not have permission");
						return false;
					}
				} else if (args[0].equalsIgnoreCase("list")) {
					if (sender.hasPermission(p_list)) {
						if (HitManager.getHitsMap().size() == 0) {
							sender.sendMessage(ChatColor.YELLOW + "There are not hits!");
							return false;
						}
						sender.sendMessage(ChatColor.RED + ChatColor.BOLD.toString() + "«*-*|*-*|*-*|*-*|*-|Hits|*-*|*-*|*-*|*-*|*-*|»");
						for (Entry<String, Hit> h : HitManager.getHitsMap().entrySet()) {
							String name = UUIDManager.getPlayerFromUUID(h.getValue().getUUID());
							if (Bukkit.getPlayer(name) != null) {
								sender.sendMessage(ChatColor.RED + name + ChatColor.YELLOW + " has a [Hit] of " + ChatColor.GREEN + h.getValue().getReward() + "$" + ChatColor.YELLOW + " (Hits: " + h.getValue().getNumberOfHits() + ")" + ChatColor.GREEN + " - ONLINE");
							} else {
								sender.sendMessage(ChatColor.RED + name + ChatColor.YELLOW + " has a [Hit] of " + ChatColor.GREEN + h.getValue().getReward() + "$" + ChatColor.YELLOW + " (Hits: " + h.getValue().getNumberOfHits() + ")" + ChatColor.RED + " - OFFLINE");
							}
						}
					}
				}
			}
		}
		return false;
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent e) {

		Player p = e.getEntity();
		if (HitManager.getHitsMap().containsKey(UUIDManager.getUUIDFromPlayer(p.getName()).toString())) {
			e.setDeathMessage("");
			Hit h = HitManager.getHit(UUIDManager.getUUIDFromPlayer(p.getName()));
			if (h.getNumberOfHits() == 1) {
				Bukkit.broadcastMessage(ChatColor.YELLOW + "A [Hit] has been completed by '" + ChatColor.RED + p.getKiller().getName() + "' " + ChatColor.YELLOW + "for an ammount of " + ChatColor.GREEN + h.getReward() + "$");
				EconomyResponse r = econ.depositPlayer(p.getKiller(), h.getReward());
				if (r.transactionSuccess()) {
					p.getKiller().sendMessage(ChatColor.GREEN + "You have been granted " + h.getReward() + "$ for completing the hit on " + ChatColor.RED + ChatColor.BOLD.toString() + p.getName());
				} else {
					p.getKiller().sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
				}
				h.delete();
			} else {
				h.setNumberOfHits(h.getNumberOfHits() - 1);
				EconomyResponse r = econ.depositPlayer(p.getKiller(), h.getReward());
				if (r.transactionSuccess()) {
					p.getKiller().sendMessage(ChatColor.GREEN + "You have been granted " + h.getReward() + "$ for completing the hit on " + ChatColor.RED + ChatColor.BOLD.toString() + p.getName());
				} else {
					p.getKiller().sendMessage(ChatColor.RED + String.format("An error occured: %s", r.errorMessage));
				}
				Bukkit.broadcastMessage(ChatColor.YELLOW + "A [Hit] has been completed by '" + ChatColor.RED + p.getKiller().getName() + "' " + ChatColor.YELLOW + "for an ammount of " + ChatColor.GREEN + h.getReward() + "$" + ChatColor.YELLOW + " (Hits: " + h.getNumberOfHits() + ")");
			}
		}
	}

	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public int getMinimum() {
		return getConfig().getInt("Minimum");
	}
}
