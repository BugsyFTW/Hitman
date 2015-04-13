package me.bugsyftw.hit;

import java.util.UUID;

import me.bugsyftw.hit.uuid.UUIDManager;

public class Hit {
	
	private UUID uuid;
	private int reward;
	private int numberofhits;
	
	public Hit(UUID uuid, int reward, int number_of_hits) {
		this.uuid = uuid;
		this.reward = reward;
		this.numberofhits = number_of_hits;
		HitmanHit.getHitmanHit().getConfig().set("Hit." + uuid.toString(), uuid.toString());
		HitmanHit.getHitmanHit().getConfig().set("Hit." + uuid.toString() + ".reward", reward);
		HitmanHit.getHitmanHit().getConfig().set("Hit." + uuid.toString() + ".number_hits", number_of_hits);
		HitManager.getHitsMap().put(uuid.toString(), this);
		HitmanHit.getHitmanHit().saveConfig();
	}
	
	public Hit(UUID uuid) {
		this.uuid = uuid;
		HitManager.getHitsMap().put(uuid.toString(), this);
	}
	
	public void delete() {
		HitManager.getHitsMap().remove(getUUID().toString());
		HitmanHit.getHitmanHit().getConfig().set("Hit." + uuid.toString(), null);
	}
	
	public UUID getUUID() {
		return uuid;
	}
	
	public String getCurrentName() {
		return UUIDManager.getPlayerFromUUID(getUUID());
	}
	
	public int getReward() {
		return reward;
	}
	
	public void setReward(int reward) {
		this.reward = reward;
	}

	public int getNumberOfHits() {
		return numberofhits;
	}
	
	public void setNumberOfHits(int numberofhits) {
		this.numberofhits = numberofhits;
	}
}
