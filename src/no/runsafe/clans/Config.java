package no.runsafe.clans;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;

public class Config implements IConfigurationChanged
{
	/**
	 * Handles configuration changes.
	 * @param configuration New configuration.
	 */
	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		clanSize = configuration.getConfigValueAsInt("clanSize");
	}

	/**
	 * @return Maximum size of a clan.
	 */
	public int getClanSize()
	{
		return clanSize;
	}

	private int clanSize;
}
