package no.runsafe.clans.monitors;

import no.runsafe.clans.Clan;
import no.runsafe.clans.events.BackstabberEvent;
import no.runsafe.clans.events.MutinyEvent;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.clans.handlers.UniverseHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.entity.IEntityDamageByEntityEvent;
import no.runsafe.framework.api.event.player.IPlayerDeathEvent;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.entity.ProjectileEntity;
import no.runsafe.framework.minecraft.entity.RunsafeEntity;
import no.runsafe.framework.minecraft.entity.RunsafeLivingEntity;
import no.runsafe.framework.minecraft.entity.RunsafeProjectile;
import no.runsafe.framework.minecraft.event.entity.RunsafeEntityDamageByEntityEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerDeathEvent;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class CombatMonitor implements IEntityDamageByEntityEvent, IPlayerDeathEvent
{
	/**
	 * Constructor for monitoring combat.
	 * @param server The server.
	 * @param scheduler Used for starting tasks.
	 * @param clanHandler Handles clans.
	 * @param universeHandler Used to check if players are in the correct world.
	 */
	public CombatMonitor(IServer server, IScheduler scheduler, ClanHandler clanHandler, UniverseHandler universeHandler)
	{
		this.server = server;
		this.scheduler = scheduler;
		this.universeHandler = universeHandler;
		this.clanHandler = clanHandler;
	}

	/**
	 * Handle the player death event.
	 * @param event Event to handle.
	 */
	@Override
	public void OnPlayerDeathEvent(RunsafePlayerDeathEvent event)
	{
		IPlayer deadPlayer = event.getEntity();

		// Check we tracked the player getting hit and they are in a clan!
		if (!track.containsKey(deadPlayer) || !clanHandler.playerIsInClan(deadPlayer))
			return;

		IPlayer killer = track.get(deadPlayer).getAttacker(); // Grab the last player to hit them.
		if (killer == null || !clanHandler.playerIsInClan(killer))
			return;

		Clan deadPlayerClan = clanHandler.getPlayerClan(deadPlayer); // Dead players clan.
		if (clanHandler.playerIsInClan(killer, deadPlayerClan.getId()))
		{
			new BackstabberEvent(killer).Fire();
			if (clanHandler.playerIsClanLeader(deadPlayer))
				new MutinyEvent(killer).Fire();
		}
		else
		{
			clanHandler.addClanKill(killer); // Stat the kill
			clanHandler.addClanDeath(deadPlayer); // Stat the death
		}
	}

	/**
	 * Deal with players attacking other players.
	 * @param event Event to handle.
	 */
	@Override
	public void OnEntityDamageByEntity(RunsafeEntityDamageByEntityEvent event)
	{
		if (!(event.getEntity() instanceof IPlayer))
			return;

		IPlayer victim = (IPlayer) event.getEntity();
		if (victim.isVanished())
			return;

		if (!universeHandler.isInClanWorld(victim))
			return;

		IPlayer source = null;
		RunsafeEntity attacker = event.getDamageActor();

		if (attacker instanceof IPlayer)
			source = (IPlayer) attacker;
		else if (attacker instanceof RunsafeProjectile)
		{
			RunsafeProjectile projectile = (RunsafeProjectile) attacker;
			if (!(projectile.getEntityType() == ProjectileEntity.Egg || projectile.getEntityType() == ProjectileEntity.Snowball))
				source = projectile.getShootingPlayer();
		}

		if (source == null || source.isVanished() || source.shouldNotSee(victim) || victim.equals(source))
			return;

		registerHit(victim, source); // Register the hit!
	}

	/**
	 * Registers a hit.
	 * @param victim Player getting attacked.
	 * @param attacker Player attacking.
	 */
	private void registerHit(final IPlayer victim, IPlayer attacker)
	{
		// Check to see if we have a timer existing.
		if (track.containsKey(victim))
			scheduler.cancelTask(track.get(victim).getTimerID()); // Cancel existing timer.
		else
			track.put(victim, new CombatTrackingNode()); // Create blank node.

		// Update the node with new information.
		track.get(victim).setAttacker(attacker).setTimerID(scheduler.startAsyncTask(new Runnable()
		{
			@Override
			public void run()
			{
				track.remove(victim); // Remove after 10 seconds.
			}
		}, 10));
	}

	/**
	 * Gets a player based on a RunsafeLivingEntity.
	 * @param entity Potential player.
	 * @return A player if the entity has the same entity ID as an online player, otherwise null.
	 */
	private IPlayer findPlayer(RunsafeLivingEntity entity)
	{
		List<IPlayer> onlinePlayers = server.getOnlinePlayers();
		for (IPlayer player : onlinePlayers)
			if (entity != null && player != null && entity.getEntityId() == player.getEntityId())
				return player;

		return null;
	}

	private final IServer server;
	private final IScheduler scheduler;
	private final ClanHandler clanHandler;
	private final UniverseHandler universeHandler;
	private final ConcurrentHashMap<IPlayer, CombatTrackingNode> track = new ConcurrentHashMap<IPlayer, CombatTrackingNode>(0);
}
