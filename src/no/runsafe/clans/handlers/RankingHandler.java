package no.runsafe.clans.handlers;

import no.runsafe.clans.Clan;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;

import java.util.*;

public class RankingHandler implements IConfigurationChanged
{
	/**
	 * Constructor for handling clan ranks.
	 * @param clanHandler Object that handles clans.
	 */
	public RankingHandler(ClanHandler clanHandler)
	{
		this.clanHandler = clanHandler;
	}

	/**
	 * @return List of clan names ordered from highest ranked first to lowest ranked last.
	 */
	public List<String> getRankingRoster()
	{
		Map<String, Clan> clanMap = clanHandler.getClans();
		Map<String, Integer> roster = new HashMap<String, Integer>(clanMap.size());

		for (Map.Entry<String, Clan> clanNode : clanMap.entrySet())
		{
			Clan clan = clanNode.getValue();
			int score = ((clan.getMemberCount() * clanMemberScore) + (clan.getClanKills() * clanKillScore)) - (clan.getClanDeaths() * (clanKillScore / 2)) + (clan.getDergonKills() * clanDergonKillScore);
			roster.put(clan.getId(), score);
		}

		LinkedHashMap<String, Integer> sorted = sortByValues(roster); // Sort the stuff!
		List<String> ordered = new ArrayList<String>(sorted.size());

		List<Map.Entry<String, Integer>> holder = new ArrayList<Map.Entry<String, Integer>>(sorted.entrySet());
		for (int i = holder.size() - 1; i >= 0; i--)
			ordered.add(holder.get(i).getKey());

		return ordered;
	}

	/**
	 * Used to sort clans.
	 * @param map List to sort.
	 * @param <K> Clan name.
	 * @param <V> Score to sort by.
	 * @return Sorted list.
	 */
	public static <K extends Comparable,V extends Comparable> LinkedHashMap<K,V> sortByValues(Map<K,V> map)
	{
		List<Map.Entry<K,V>> entries = new LinkedList<Map.Entry<K,V>>(map.entrySet());

		Collections.sort(entries, new Comparator<Map.Entry<K,V>>()
		{
			@Override
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2)
			{
				return o1.getValue().compareTo(o2.getValue());
			}
		});

		LinkedHashMap<K,V> sortedMap = new LinkedHashMap<K,V>();

		for(Map.Entry<K,V> entry: entries)
			sortedMap.put(entry.getKey(), entry.getValue());

		return sortedMap;
	}

	/**
	 * Handles configuration changes.
	 * @param config New configurations.
	 */
	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		clanMemberScore = config.getConfigValueAsInt("ranking.clanMember");
		clanKillScore = config.getConfigValueAsInt("ranking.clanKill");
		clanDergonKillScore = config.getConfigValueAsInt("ranking.dergonKill");
	}

	private int clanMemberScore;
	private int clanKillScore;
	private int clanDergonKillScore;
	private final ClanHandler clanHandler;
}
