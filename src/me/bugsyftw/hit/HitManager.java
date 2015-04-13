package me.bugsyftw.hit;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class HitManager {

	private static HitManager hm = new HitManager();

	public static HashMap<String, Hit> hits = new HashMap<String, Hit>();

	public static HitManager getHitManager() {
		return hm;
	}

	public static Hit getHit(UUID uuid) {
		for (Hit g : getHitsCollection()) {
			if (g.getUUID().toString().equals(uuid.toString())) {
				return g;
			}
		}
		return null;
	}

	public void loadHits() {
		for (String h : HitmanHit.getHitmanHit().getConfig().getConfigurationSection("Hit").getKeys(false)) {
			Hit hit = new Hit(UUID.fromString(h));
			hit.setReward(HitmanHit.getHitmanHit().getConfig().getInt("Hit." + hit.getUUID().toString() + ".reward"));
			hit.setNumberOfHits(HitmanHit.getHitmanHit().getConfig().getInt("Hit." + hit.getUUID().toString() + ".number_hits"));
		}
	}

	public static HashMap<String, Hit> getHitsMap() {
		return hits;
	}

	public static Collection<Hit> getHitsCollection() {
		return hits.values();
	}
}
