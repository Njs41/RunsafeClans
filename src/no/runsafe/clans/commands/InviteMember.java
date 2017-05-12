package no.runsafe.clans.commands;

import no.runsafe.clans.Clan;
import no.runsafe.clans.Config;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.Player;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class InviteMember extends PlayerAsyncCommand
{
	public InviteMember(IScheduler scheduler, ClanHandler clanHandler, Config config)
	{
		super("invite", "Invite a member to the clan", "runsafe.clans.invite", scheduler, new Player().require());
		this.clanHandler = clanHandler;
		this.config = config;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		if (!clanHandler.playerIsInClan(executor))
			return "&cYou are not in a clan.";

		if (!clanHandler.playerIsClanLeader(executor))
			return "&cYou are not the clan leader, you cannot invite players.";

		IPlayer targetPlayer = parameters.getValue("player") ;
		if (targetPlayer == null)
			return "&cInvalid player.";

		if (clanHandler.playerIsInClan(targetPlayer))
			return "&cThat player is already in a clan.";

		Clan clan = clanHandler.getPlayerClan(executor); // Grab the players clan.

		if (clan.getMemberCount() >= config.getClanSize())
			return "&cYour clan is full! Remove someone before inviting more.";

		if (clanHandler.playerHasPendingInvite(clan.getId(), targetPlayer))
			return "&cThat player has already been invited to this clan.";

		clanHandler.invitePlayerToClan(clan.getId(), targetPlayer); // Invite the player.
		return "&aInvite sent!";
	}

	private final ClanHandler clanHandler;
	private final Config config;
}