package no.runsafe.clans.handlers;

import no.runsafe.clans.Clan;
import no.runsafe.clans.chat.ClanChannel;
import no.runsafe.clans.database.ClanInviteRepository;
import no.runsafe.clans.database.ClanMemberRepository;
import no.runsafe.clans.database.ClanRepository;
import no.runsafe.clans.events.ClanEvent;
import no.runsafe.clans.events.ClanJoinEvent;
import no.runsafe.clans.events.ClanKickEvent;
import no.runsafe.clans.events.ClanLeaveEvent;
import no.runsafe.framework.api.IConfiguration;
import no.runsafe.framework.api.IScheduler;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.event.player.IPlayerCustomEvent;
import no.runsafe.framework.api.event.player.IPlayerJoinEvent;
import no.runsafe.framework.api.event.player.IPlayerQuitEvent;
import no.runsafe.framework.api.event.plugin.IConfigurationChanged;
import no.runsafe.framework.api.hook.IPlayerDataProvider;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.event.player.RunsafeCustomEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerJoinEvent;
import no.runsafe.framework.minecraft.event.player.RunsafePlayerQuitEvent;
import no.runsafe.nchat.channel.IChannelManager;
import no.runsafe.nchat.channel.IChatChannel;
import no.runsafe.nchat.chat.InternalRealChatEvent;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.joda.time.format.PeriodFormat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

public class ClanHandler implements IConfigurationChanged, IPlayerDataProvider, IPlayerJoinEvent, IPlayerQuitEvent, IPlayerCustomEvent
{
	/**
	 * Constructor for ClanHandler.
	 * @param console Console to output data to.
	 * @param server The server.
	 * @param scheduler Used for starting tasks.
	 * @param clanRepository Clan storage.
	 * @param memberRepository Member storage.
	 * @param inviteRepository Invited player list.
	 * @param channelManager Manager of channels.
	 */
	public ClanHandler(IConsole console, IServer server, IScheduler scheduler, ClanRepository clanRepository, ClanMemberRepository memberRepository, ClanInviteRepository inviteRepository, IChannelManager channelManager)
	{
		this.console = console;
		this.server = server;
		this.scheduler = scheduler;
		this.clanRepository = clanRepository;
		this.memberRepository = memberRepository;
		this.inviteRepository = inviteRepository;
		this.channelManager = channelManager;
	}

	/**
	 * Handles configuration changes.
	 * @param config New configuration settings.
	 */
	@Override
	public void OnConfigurationChanged(IConfiguration config)
	{
		clanTagFormat = config.getConfigValueAsString("chatTag");
		LoadRostersIntoCache();
		LoadInvitesIntoCache();
	}

	/**
	 * Gets player data to be used by plugins that need it.
	 * @param player User to get data from.
	 * @return The player's current clan and their join date.
	 */
	@Override
	public Map<String, String> GetPlayerData(IPlayer player)
	{
		Map<String, String> data = new HashMap<String, String>(1);
		Clan playerClan = getPlayerClan(player);
		data.put("runsafe.clans.clan", playerClan == null ? "None" : playerClan.getId());
		data.put("runsafe.clans.joined", getPlayerJoinString(player));
		return data;
	}

	/**
	 * Gets the date the player has joined a clan in String format.
	 * @param player User to get join date from.
	 * @return Join date in the form of a string.
	 */
	public String getPlayerJoinString(IPlayer player)
	{
		return formatTime(memberRepository.getClanMemberJoinDate(player));
	}

	/**
	 * Handles custom events for this plugin.
	 * Events handled are: ClanJoinEvent, ClanLeaveEvent, and ClanKickEvent.
	 * @param event The event to handle.
	 */
	@Override
	public void OnPlayerCustomEvent(RunsafeCustomEvent event)
	{
		if (!(event instanceof ClanEvent))
			return;

		Clan clan = ((ClanEvent) event).getClan();
		IPlayer player = event.getPlayer();
		if (event instanceof ClanJoinEvent)
		{
			joinClanChannel(player, clan.getId());
			sendMessageToClan(clan.getId(), player.getPrettyName() + " has joined the clan.");
		}
		else if (event instanceof ClanLeaveEvent)
		{
			leaveClanChannel(player, clan.getId());
			sendMessageToClan(clan.getId(), player.getPrettyName() + " has left the clan.");
		}
		else if (event instanceof ClanKickEvent)
		{
			leaveClanChannel(player, clan.getId());
			sendMessageToClan(clan.getId(), player.getPrettyName() + " has been kicked from the clan by " + ((ClanKickEvent) event).getKicker().getPrettyName() + ".");
		}
	}

	/**
	 * Handles player joining.
	 * Will not do anything if the event is fake.
	 * @param event The event to handle.
	 */
	@Override
	public void OnPlayerJoinEvent(RunsafePlayerJoinEvent event)
	{
		if (event.isFake())
			return;
		IPlayer player = event.getPlayer(); // Grab the player.

		// Check if we have any pending invites.
		if (playerInvites.containsKey(player))
			processPendingInvites(player);

		if (playerIsInClan(player))
			processClanMemberConnected(player);
	}

	/**
	 * Handles player log-out.
	 * Will not do anything if the event is fake or if the player is not in a clan.
	 * @param event The event to handle.
	 */
	@Override
	public void OnPlayerQuit(RunsafePlayerQuitEvent event)
	{
		if (!event.isFake() && playerIsInClan(event.getPlayer()))
			processClanMemberDisconnected(event);
	}

	public void createClan(String clanID, IPlayer playerLeader)
	{
		clanID = clanID.toUpperCase(); // Make sure the clan ID is upper-case.
		if (clanExists(clanID)) return; // Be sure we don't have a clan with this name already.
		Clan newClan = new Clan(clanID, playerLeader, "Welcome to " + clanID); // Create a new clan object.
		clans.put(clanID, newClan); // Push the clan into the clan handler.
		clanRepository.persistClan(newClan); // Persist the clan in the database.
	}

	public boolean isInvalidClanName(String clanID)
	{
		// Check if we have a valid name that matches the pattern.
		return !clanNamePattern.matcher(clanID).matches();
	}

	public boolean clanExists(String clanID)
	{
		return clans.containsKey(clanID); // Do we have a clan with this name?
	}

	public void addClanMember(String clanID, IPlayer newMember)
	{
		removeAllPendingInvites(newMember); // Remove all pending invites.
		Clan clan = clans.get(clanID);;
		clan.addMember(newMember); // Add to cache.
		playerClanIndex.put(newMember, clanID); // Add to index.
		memberRepository.addClanMember(clan.getId(), newMember);
		new ClanJoinEvent(newMember, clan).Fire(); // Fire a join event.
	}

	public void kickClanMember(IPlayer player, IPlayer kicker)
	{
		Clan playerClan = getPlayerClan(player);

		if (playerClan != null)
		{
			removeClanMember(playerClan, player);
			new ClanKickEvent(player, playerClan, kicker).Fire();
		}
	}

	public void removeClanMember(IPlayer player)
	{
		Clan playerClan = getPlayerClan(player);

		if (playerClan != null)
		{
			removeClanMember(playerClan, player);
			new ClanLeaveEvent(player, playerClan).Fire();
		}
	}

	private void removeClanMember(Clan clan, IPlayer player)
	{
		String playerName = player.getName();
		clans.get(clan.getId()).removeMember(player); // Remove from cache.
		playerClanIndex.remove(player); // Remove from index.
		memberRepository.removeClanMember(player);
		new ClanLeaveEvent(player, clan).Fire(); // Fire a leave event.
	}

	public void changeClanLeader(String clanID, IPlayer newLeader)
	{
		clans.get(clanID).setLeader(newLeader);
		clanRepository.changeClanLeader(clanID, newLeader);
		sendMessageToClan(clanID, newLeader.getPrettyName() + " has been given leadership of the clan.");
	}

	public boolean playerIsInClan(IPlayer player)
	{
		return playerClanIndex.containsKey(player);
	}

	public boolean playerIsInClan(IPlayer player, String clanID)
	{
		return playerClanIndex.containsKey(player) && playerClanIndex.get(player).equals(clanID);
	}

	public Clan getPlayerClan(IPlayer player)
	{
		return playerClanIndex.containsKey(player) ? getClan(playerClanIndex.get(player)) : null;
	}

	public Clan getClan(String clanID)
	{
		return clans.containsKey(clanID) ? clans.get(clanID) : null;
	}

	public boolean playerIsClanLeader(IPlayer player)
	{
		Clan playerClan = getPlayerClan(player);
		return playerClan != null && playerClan.getLeader().equals(player);
	}

	public boolean playerHasPendingInvite(String clanID, IPlayer player)
	{
		return playerInvites.containsKey(player) && playerInvites.get(player).contains(clanID);
	}

	public void invitePlayerToClan(String clanID, IPlayer player)
	{
		if (!playerInvites.containsKey(player))
			playerInvites.put(player, new ArrayList<String>(1));

		playerInvites.get(player).add(clanID); // Add clan invite to the player.
		inviteRepository.addInvite(player, clanID);

		NotifyNewInvite(clanID, player);
	}

	public void removeAllPendingInvites(IPlayer player)
	{
		playerInvites.remove(player); // Remove all pending invites.
		inviteRepository.clearAllPendingInvites(player); // Persist the change in database.
	}

	public void removePendingInvite(IPlayer player, String clanName)
	{
		if (playerInvites.containsKey(player))
			playerInvites.get(player).remove(clanName);

		inviteRepository.clearPendingInvite(player, clanName);
	}

	public void acceptClanInvite(String clanID, IPlayer player)
	{
		// Make sure the player has a pending invite we can accept.
		if (playerHasPendingInvite(clanID, player))
		{
			addClanMember(clanID, player); // Add the member to the clan.
			Clan playerClan = getPlayerClan(player);
			if (playerClan != null)
				sendMessageOfTheDay(player, playerClan);
		}
	}

	public void sendMessageToClan(String clanID, String message)
	{
		Clan clan = getClan(clanID); // Grab the clan.
		// Make sure said clan exists.
		if (clan != null)
			channelManager.getChannelByName(clanID).SendSystem(formatClanMessage(clanID, message));
	}

	public String formatClanMessage(String clanID, String message)
	{
		return formatClanTag(clanID) + message;
	}

	public String formatMotd(String message)
	{
		return "Message of the Day: " + message;
	}

	public void setClanMotd(String clanID, String message)
	{
		clans.get(clanID).setMotd(message);
		clanRepository.updateMotd(clanID, message);
		sendMessageToClan(clanID, formatMotd(message));
	}

	public void disbandClan(Clan clan)
	{
		String clanID = clan.getId();
		sendMessageToClan(clanID, "The clan is being disbanded by the leader.");
		PurgePendingInvites(clanID);
		PurgeMembers(clan, clanID);
		PurgeClan(clanID);
	}

	public void clanChat(IPlayer player, String message)
	{
		Clan playerClan = getPlayerClan(player);
		if (playerClan != null)
		{
			IChatChannel channel = channelManager.getChannelByName(playerClan.getId());
			channel.Send(new InternalRealChatEvent(player, message));
		}
	}

	public void addClanKill(IPlayer player)
	{
		Clan clan = getPlayerClan(player);
		if (clan != null)
		{
			clan.addClanKills(1);
			clanRepository.updateStatistic(clan.getId(), "clanKills", clan.getClanKills());
		}
	}

	public void addClanDeath(IPlayer player)
	{
		Clan clan = getPlayerClan(player);
		if (clan != null)
		{
			clan.addClanDeaths(1);
			clanRepository.updateStatistic(clan.getId(), "clanDeaths", clan.getClanDeaths());
		}
	}

	public void addDergonKill(IPlayer player)
	{
		Clan clan = getPlayerClan(player);
		if (clan != null)
		{
			String clanID = clan.getId();
			clan.addDergonKills(1);
			clanRepository.updateStatistic(clanID, "dergonKills", clan.getDergonKills());
			sendMessageToClan(clanID, "The clan has slain a dergon!");
		}
	}

	public String formatClanTag(String name)
	{
		return String.format(clanTagFormat, name);
	}

	public Map<String, Clan> getClans()
	{
		return clans;
	}

	public void joinClanChannel(IPlayer player, String id)
	{
		IChatChannel clanChannel = channelManager.getChannelByName(id);
		if (clanChannel == null)
		{
			clanChannel = new ClanChannel(console, channelManager, id, this);
			channelManager.registerChannel(clanChannel);
		}
		clanChannel.Join(player);
	}

	public void leaveClanChannel(IPlayer player, String id)
	{
		IChatChannel clanChannel = channelManager.getChannelByName(id);
		if (clanChannel != null)
			clanChannel.Leave(player);
	}

	private String formatTime(DateTime time)
	{
		if (time == null)
			return "null";

		Period period = new Period(time, DateTime.now(), output_format);
		return PeriodFormat.getDefault().print(period);
	}

	private void processClanMemberDisconnected(RunsafePlayerQuitEvent event)
	{
		Clan playerClan = getPlayerClan(event.getPlayer());
		leaveClanChannel(event.getPlayer(), playerClan.getId());
	}

	private void LoadInvitesIntoCache()
	{
		playerInvites.clear();
		playerInvites.putAll(inviteRepository.getPendingInvites()); // Grab pending invites from the database.
		List<String> invalidClans = new ArrayList<String>(0);

		for (Map.Entry<IPlayer, List<String>> inviteNode : playerInvites.entrySet())
		{
			for (String clanName : inviteNode.getValue()) // Loop through all the invites and check they are valid.
			{
				if (!clanExists(clanName)) // Check the clan exists.
				{
					invalidClans.add(clanName);
					console.logError("Invalid clan invite found: %s - Marking for purge!", clanName);
				}
			}
		}

		// Process invalid clans found in invites and purge!
		for (String invalidClan : invalidClans)
			inviteRepository.clearAllPendingInvitesForClan(invalidClan);

		for (Map.Entry<IPlayer, List<String>> inviteNode : playerInvites.entrySet())
			inviteNode.getValue().removeAll(invalidClans);
	}

	private void LoadRostersIntoCache()
	{
		int memberCount = 0; // Keep track of how many members we have.
		clans.clear();
		clans.putAll(clanRepository.getClans()); // Populate a list of clans.
		playerClanIndex.clear(); // Clear the index.
		Map<String, List<IPlayer>> rosters = memberRepository.getClanRosters(); // Get rosters.

		// Process the clan rosters into the handler.
		for (Map.Entry<String, List<IPlayer>> roster : rosters.entrySet())
		{
			String clanName = roster.getKey(); // Grab the name of the clan.
			if (clans.containsKey(clanName))
			{
				// We have clan members, add them to the clan.
				for (IPlayer clanMember : roster.getValue())
				{
					playerClanIndex.put(clanMember, clanName); // Map the player to the clan index.
					clans.get(clanName).addMember(clanMember); // Add the member to the clan.
					memberCount++; // Increase our counter.
				}
			}
			else
			{
				// We have clan members for a non-existent clan, remove them.
				memberRepository.removeAllClanMembers(clanName);
				console.logError("Purging %s members from invalid clan: %s", roster.getValue().size(), clanName);
			}
		}

		// Output some statistics from our clan loading.
		console.logInformation("Loaded %s clans with %s members.", clans.size(), memberCount);
	}

	private void processClanMemberConnected(final IPlayer player)
	{
		final Clan playerClan = getPlayerClan(player);
		if (playerClan != null)
		{
			scheduler.startAsyncTask(new Runnable()
			{
				@Override
				public void run()
				{
					if (player.isOnline())
					{
						joinClanChannel(player, playerClan.getId());
						sendMessageOfTheDay(player, playerClan);
					}
				}
			}, 3);
		}
	}

	private void sendMessageOfTheDay(IPlayer player, Clan playerClan)
	{
		player.sendColouredMessage(formatClanMessage(playerClan.getId(), formatMotd(playerClan.getMotd())));
	}

	private void processPendingInvites(final IPlayer player)
	{
		final List<String> invites = playerInvites.get(player);

		if (invites.isEmpty())
			playerInvites.remove(player);
		else
			scheduler.startAsyncTask(new Runnable()
			{
				@Override
				public void run()
				{
					NotifyPendingInvites(player, invites);
				}
			}, 3);
	}

	private void NotifyNewInvite(String clanID, IPlayer player)
	{
		if (player.isOnline()) // If the player is online, inform them about the invite!
			player.sendColouredMessage("&aYou have been invited to join the '%1$s' clan. Use \"/clan join %1$s\" to join!", clanID);
	}

	private void NotifyPendingInvites(IPlayer player, List<String> invites)
	{
		if (player.isOnline())
		{
			player.sendColouredMessage("&aYou have %d pending clan invite(s): %s", invites.size(), StringUtils.join(invites, ", "));
			player.sendColouredMessage("&aUse \"/clan join <clanTag>\" to join one of them!");
		}
	}

	private void PurgeClan(String clanID)
	{
		clanRepository.deleteClan(clanID); // Delete the clan from the database.
		clans.remove(clanID); // Delete the clan from the cache.
	}

	private void PurgeMembers(Clan clan, String clanID)
	{
		memberRepository.removeAllClanMembers(clanID); // Wipe the roster.
		for (IPlayer clanMember : clan.getMembers())
		{
			playerClanIndex.remove(clanMember); // Remove the players clan index.
			memberRepository.removeClanMember(clanMember);
			new ClanLeaveEvent(clanMember, clan).Fire(); // Fire a leave event.
		}
	}

	private void PurgePendingInvites(String clanID)
	{
		// Check all pending invites and remove any for this clan.
		inviteRepository.clearAllPendingInvitesForClan(clanID); // Clear all pending invites.
		for (Map.Entry<IPlayer, List<String>> invite : playerInvites.entrySet())
			if (invite.getValue().contains(clanID))
				playerInvites.get(invite.getKey()).remove(clanID); // Remove the invite from deleted clan.
	}

	private String clanTagFormat;
	/* List of clans. String: Clan name. Clan: The clan's object. */
	private final Map<String, Clan> clans = new ConcurrentHashMap<String, Clan>(0);
	/* List of clan members and the clan they are in. IPlayer: Clan member. String: Clan ID. */
	private final Map<IPlayer, String> playerClanIndex = new ConcurrentHashMap<IPlayer, String>(0);
	/* List of player invites. IPlayer: Invited player. List<String>: List of clan IDs being invited to.*/
	private final Map<IPlayer, List<String>> playerInvites = new ConcurrentHashMap<IPlayer, List<String>>(0);
	private final IConsole console;
	private final IServer server;
	private final IScheduler scheduler;
	private final ClanRepository clanRepository;
	private final ClanMemberRepository memberRepository;
	private final ClanInviteRepository inviteRepository;
	private final Pattern clanNamePattern = Pattern.compile("^[A-Z]{3}$");
	private final PeriodType output_format = PeriodType.standard().withMillisRemoved().withSecondsRemoved();
	private final IChannelManager channelManager;
}
