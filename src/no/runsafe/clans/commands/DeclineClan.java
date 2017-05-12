package no.runsafe.clans.commands;

import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class DeclineClan extends PlayerAsyncCommand
{
	public DeclineClan(IScheduler scheduler, ClanHandler clanHandler)
	{
		super("decline", "Decline a clan invitation", "runsafe.clans.decline", scheduler, new RequiredArgument("clan"));
		this.clanHandler = clanHandler;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		String clanName = parameters.get("clan").toUpperCase();
		if (clanHandler.playerHasPendingInvite(clanName, executor))
		{
			clanHandler.removePendingInvite(executor, clanName);
			return "&cInvitation to " + clanName + " declined.";
		}
		return "&cYou have not been invited to that clan.";
	}

	private final ClanHandler clanHandler;
}
