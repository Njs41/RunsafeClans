package no.runsafe.clans.commands;

import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class LeaveClan extends PlayerAsyncCommand
{
	public LeaveClan(IScheduler scheduler, ClanHandler clanHandler)
	{
		super("leave", "Leave the clan you are in", "runsafe.clans.leave", scheduler);
		this.clanHandler = clanHandler;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		if (!clanHandler.playerIsInClan(executor))
			return "&cYou are not in a clan.";

		if (clanHandler.playerIsClanLeader(executor))
			return "&cYou cannot leave your clan, disband it first!";

		clanHandler.removeClanMember(executor);
		return "&aYou have left the clan.";
	}

	private final ClanHandler clanHandler;
}
