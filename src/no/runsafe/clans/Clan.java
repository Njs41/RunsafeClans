package no.runsafe.clans;

import no.runsafe.framework.api.player.IPlayer;

import java.util.ArrayList;
import java.util.List;

public class Clan
{
	public Clan(String id, IPlayer leader, String motd)
	{
		this.id = id;
		this.leader = leader;
		this.motd = motd;
	}

	public String getMotd()
	{
		return motd;
	}

	public void setMotd(String motd)
	{
		this.motd = motd;
	}

	public void setLeader(IPlayer leader)
	{
		this.leader = leader;
	}

	public String getId()
	{
		return id;
	}

	public IPlayer getLeader()
	{
		return leader;
	}

	public List<IPlayer> getMembers()
	{
		return members;
	}

	public int getMemberCount()
	{
		return members.size();
	}

	public void addMember(IPlayer player)
	{
		members.add(player);
	}

	public void removeMember(IPlayer player)
	{
		members.remove(player);
	}

	public void addClanKills(int amount)
	{
		clanKills += amount;
	}

	public void addClanDeaths(int amount)
	{
		clanDeaths += amount;
	}

	public void addDergonKills(int amount)
	{
		dergonKills += amount;
	}

	public int getClanKills()
	{
		return clanKills;
	}

	public int getClanDeaths()
	{
		return clanDeaths;
	}

	public int getDergonKills()
	{
		return dergonKills;
	}

	private final String id;
	private IPlayer leader;
	private String motd;
	private int clanKills = 0;
	private int clanDeaths = 0;
	private int dergonKills = 0;
	private final List<IPlayer> members = new ArrayList<IPlayer>(0);
}
