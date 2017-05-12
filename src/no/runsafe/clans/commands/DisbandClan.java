package no.runsafe.clans.commands;

import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class DisbandClan extends PlayerAsyncCommand
{
	public DisbandClan(IScheduler scheduler, ClanHandler clanHandler)
	{
		super("disband", "Disband your clan", "runsafe.clans.disband", scheduler);
		this.clanHandler = clanHandler;
	}

	@Override
	public String OnAsyncExecute(IPlayer player, IArgumentList parameters)
	{
		if (!clanHandler.playerIsInClan(player))
			return "&cYou are not in a clan.";

		if (!clanHandler.playerIsClanLeader(player))
			return "&cYou are not the clan leader.";

		clanHandler.disbandClan(clanHandler.getPlayerClan(player));
		return "&aYour clan has been disbanded.";
	}

	private final ClanHandler clanHandler;
}
