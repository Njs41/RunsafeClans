package no.runsafe.clans.database;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.database.*;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClanInviteRepository extends Repository
{
	/**
	 * Constructor for the clan invite repository.
	 * @param database The database.
	 * @param server The server. Used for getting a player object from their username and or UUID.
	 */
	public ClanInviteRepository(IDatabase database, IServer server)
	{
		this.database = database;
		this.server = server;
	}

	/**
	 * @return A list of players being invited to clans, each having a list of clans they are being invited to.
	 */
	@Deprecated
	public Map<String, List<String>> getPendingInviteNames()
	{
		Map<String, List<String>> map = new HashMap<String, List<String>>(0);

		for (IRow row : database.query("SELECT `clanID`, `player` FROM `clan_invites`"))
		{
			String playerName = row.String("player");
			if (!map.containsKey(playerName))
				map.put(playerName, new ArrayList<String>(0));

			map.get(playerName).add(row.String("clanID"));
		}

		return map;
	}

	/**
	 * @return A list of players being invited to clans, each having a list of clans they are being invited to.
	 */
	public Map<IPlayer, List<String>> getPendingInvites()
	{
		Map<IPlayer, List<String>> map = new HashMap<IPlayer, List<String>>(0);

		for (IRow row : database.query("SELECT `clanID`, `player` FROM `clan_invites`"))
		{
			IPlayer player = server.getPlayerExact(row.String("player"));
			if (!map.containsKey(player))
				map.put(player, new ArrayList<String>(0));

			map.get(player).add(row.String("clanID"));
		}

		return map;
	}

	/**
	 * Clears a pending invite.
	 * @param player User being invited.
	 * @param clanID Clan user is being invited to.
	 */
	public void clearPendingInvite(IPlayer player, String clanID)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `player` = ? AND `clanID` = ?", player.getName(), clanID);
	}

	/**
	 * Deletes all of a player's pending invites.
	 * @param player User to delete invites from.
	 */
	public void clearAllPendingInvites(IPlayer player)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `player` = ?", player.getName());
	}

	/**
	 * Deletes all of a clan's pending invites.
	 * @param clanID Clan to delete pending invites from.
	 */
	public void clearAllPendingInvitesForClan(String clanID)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `clanID` = ?", clanID);
	}

	/**
	 * Invites a player to a clan.
	 * @param player Player to invite.
	 * @param clanID Clan the user is being invited into.
	 */
	public void addInvite(IPlayer player, String clanID)
	{
		database.execute("INSERT IGNORE INTO `clan_invites` (`player`, `clanID`) VALUES(?, ?)", player.getName(), clanID);
	}

	/**
	 * @return The table name where clan invites are stored.
	 */
	@Nonnull
	@Override
	public String getTableName()
	{
		return "clan_invites";
	}

	/**
	 * @return The SQL statements for upgrading the database table.
	 */
	@Nonnull
	@Override
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE `clan_invites` (" +
				"`clanID` VARCHAR(3) NOT NULL," +
				"`player` VARCHAR(20) NOT NULL," +
				"PRIMARY KEY (`clanID`, `player`)" +
			")"
		);

		return update;
	}

	private final IServer server;
}
