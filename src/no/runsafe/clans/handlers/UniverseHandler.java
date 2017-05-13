package no.runsafe.clans.handlers;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IUniverse;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.player.IPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniverseHandler implements IConfigurationChanged
{
	/**
	 * Constructor for the universe handler.
	 */
	public UniverseHandler()
	{
	}

	/**
	 * Checks if a player is in a clan world.
	 * @param player User to check if is in the right world.
	 * @return True if and only if the player is in a clan world listed in the configs file.
	 */
	public boolean isInClanWorld(IPlayer player)
	{
		IUniverse universe = player.getUniverse();
		return (universe == null || !clanUniverses.contains(universe.getName()));
	}

	/**
	 * Handles configuration changes.
	 * @param config New configurations.
	 */
	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		clanUniverses.clear();
		Collections.addAll(clanUniverses, config.getConfigValueAsString("clanUniverse").split(","));
	}

	private List<String> clanUniverses = new ArrayList<String>(0);
}
