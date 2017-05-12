package no.runsafe.clans.commands;

import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.ILocation;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerCommand;
import no.runsafe.framework.api.player.IPlayer;

public class ClanFlare extends PlayerCommand
{
	public ClanFlare(ClanHandler handler)
	{
		super("flare", "Broadcast your location in clan chat", "runsafe.clans.flare");
		this.handler = handler;
	}

	@Override
	public String OnExecute(IPlayer executor, IArgumentList parameters)
	{
		if (!handler.playerIsInClan(executor))
			return "&cYou are not in a clan.";

		ILocation location = executor.getLocation();
		if (location == null)
			return "&cYou are nowhere.";

		handler.clanChat(executor, String.format(
				"Assistance required at X: %s, Y: %s, Z: %s!",
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ()
		));
		return null;
	}

	private final ClanHandler handler;
}
