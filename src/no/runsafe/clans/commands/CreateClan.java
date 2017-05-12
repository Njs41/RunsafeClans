package no.runsafe.clans.commands;

import no.runsafe.clans.handlers.CharterHandler;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.command.argument.IArgumentList;
import no.runsafe.framework.api.command.argument.RequiredArgument;
import no.runsafe.framework.api.command.player.PlayerAsyncCommand;
import no.runsafe.framework.api.player.IPlayer;

public class CreateClan extends PlayerAsyncCommand
{
	public CreateClan(IScheduler scheduler, ClanHandler clanHandler, CharterHandler charterHandler)
	{
		super("create", "Create a clan", "runsafe.clans.create", scheduler, new RequiredArgument("clanTag"));
		this.clanHandler = clanHandler;
		this.charterHandler = charterHandler;
	}

	@Override
	public String OnAsyncExecute(IPlayer executor, IArgumentList parameters)
	{
		String clanName = parameters.get("clanTag").toUpperCase();

		// Check we have been given a valid clan name.
		if (clanHandler.isInvalidClanName(clanName))
			return String.format("&c'%s' is not a valid clan tag. A clan tag must be three characters using characters A-Z.", clanName);

		// Make sure there is not a clan with that name already existing.
		if (clanHandler.clanExists(clanName))
			return String.format("&cA clan named '%s' already exists.", clanName);

		if (clanHandler.playerIsInClan(executor))
			return "&cYou are already in a clan!";

		charterHandler.givePlayerCharter(executor, clanName); // Give them a charter.
		return "&aCharter created! Get two other people to sign it to create your clan!";
	}

	private final ClanHandler clanHandler;
	private final CharterHandler charterHandler;
}
