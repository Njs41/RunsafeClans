package no.runsafe.clans.commands;

import no.runsafe.clans.Clan;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class PassLeadership extends PlayerAsyncCommand
{
	public PassLeadership(IScheduler scheduler, ClanHandler clanHandler)
	{
		super("passleadership", "Make another clan member leader.", "runsafe.clans.promote", scheduler, new Player().require());
		this.clanHandler = clanHandler;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		if (!clanHandler.playerIsInClan(executor))
			return "&cYou are not in a clan.";

		if (!clanHandler.playerIsClanLeader(executor))
			return "&cYou are not the leader of your clan.";

		IPlayer targetPlayer = parameters.getValue("player") ;
		if (targetPlayer == null)
			return "&cInvalid player";

		Clan playerClan = clanHandler.getPlayerClan(executor); // The clan of the player.
		if (!clanHandler.playerIsInClan(targetPlayer, playerClan.getId()))
			return "&cThat player is not in your clan.";

		clanHandler.changeClanLeader(playerClan.getId(), targetPlayer); // Change the leader.
		return "&aYou have passed the leadership of your clan!";
	}

	private final ClanHandler clanHandler;
}
