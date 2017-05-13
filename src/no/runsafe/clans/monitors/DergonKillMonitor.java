package no.runsafe.clans.monitors;

import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.event.player.IPlayerCustomEvent;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;

public class DergonKillMonitor implements IPlayerCustomEvent
{
	/**
	 * Constructor for handling dergon kills.
	 * @param handler Handles clans.
	 */
	public DergonKillMonitor(ClanHandler handler)
	{
		this.handler = handler;
	}

	/**
	 * Adds a dergon kill to a clan's statistics.
	 * @param event Event to handle.
	 */
	@Override
	public void OnPlayerCustomEvent(RunsafeCustomEvent event)
	{
		if (event.getEvent().equals("runsafe.dergon.slay"))
			handler.addDergonKill(event.getPlayer());
	}

	private final ClanHandler handler;
}
