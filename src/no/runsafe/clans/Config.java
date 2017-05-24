package no.runsafe.clans;

import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Config implements IConfigurationChanged
{
	@Override
	public void OnConfigurationChanged(IConfiguration configuration)
	{
		// Get maximum clan size.
		clanSize = configuration.getConfigValueAsInt("clanSize");

		// Get clan score information.
		clanMemberScore = configuration.getConfigValueAsInt("ranking.clanMember");
		clanKillScore = configuration.getConfigValueAsInt("ranking.clanKill");
		clanDergonKillScore = configuration.getConfigValueAsInt("ranking.dergonKill");

		// Get all worlds in the clan universe.
		clanUniverse.clear();
		Collections.addAll(clanUniverse, configuration.getConfigValueAsString("clanUniverse").split(","));
	}

	public int getClanSize()
	{
		return clanSize;
	}

	public int getClanMemberScore()
	{
		return clanMemberScore;
	}

	public int getClanKillScore()
	{
		return clanKillScore;
	}

	public int getClanDergonKillScore()
	{
		return clanDergonKillScore;
	}

	public List<String> getClanUniverse()
	{
		return clanUniverse;
	}

	private int clanSize;
	private int clanMemberScore;
	private int clanKillScore;
	private int clanDergonKillScore;
	private List<String> clanUniverse = new ArrayList<String>(0);
}
