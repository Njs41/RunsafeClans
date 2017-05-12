package no.runsafe.clans.commands;

import no.runsafe.clans.Clan;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.AsyncCommand;
import no.runsafe.framework.api.command.ICommandExecutor;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.player.IPlayer;

public class LookupClan extends AsyncCommand
{
	public LookupClan(IScheduler scheduler, ClanHandler clanHandler)
	{
		super("lookup", "Lookup which clan a player is in", "runsafe.clans.lookup", scheduler, new Player().require());
		this.clanHandler = clanHandler;
	}

	@Override
	public String OnAsyncExecute(ICommandExecutor executor, IArgumentList parameters)
	{
		IPlayer targetPlayer = parameters.getValue("player");
		if (targetPlayer == null)
			return "&cInvalid player!";

		if (!clanHandler.playerIsInClan(targetPlayer))
			return "&cThat player is not in a clan.";

		Clan clan = clanHandler.getPlayerClan(targetPlayer);

		return targetPlayer.getPrettyName() + "&f has been a member of " + clan.getId() + " for " + clanHandler.getPlayerJoinString(targetPlayer) + ".";
	}

	private final ClanHandler clanHandler;
}
