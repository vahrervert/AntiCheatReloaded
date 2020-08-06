package com.rammelkast.anticheatreloaded.event;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityPotionEffectEvent;

import com.rammelkast.anticheatreloaded.util.VersionUtil;

public class PotionEffectListener extends EventListener {

	@EventHandler
	public void onEntityPotionEffect(EntityPotionEffectEvent event) {
		if (!(event.getEntity() instanceof Player))
			return;
		if (event.getNewEffect() == null)
			return;
		if (!VersionUtil.isLevitationEffect(event.getNewEffect()))
			return;
		getBackend().logLevitating((Player) event.getEntity(), event.getNewEffect().getDuration());
	}
	
}
