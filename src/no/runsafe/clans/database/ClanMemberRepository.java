package no.runsafe.clans.database;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.database.*;
import no.runsafe.framework.api.player.IPlayer;
import org.joda.time.DateTime;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClanMemberRepository extends Repository
{
	/**
	 * Constructor for the clan member repository.
	 * @param database The database.
	 * @param server The server. Used for getting a player object from their username and or UUID.
	 */
	public ClanMemberRepository(IDatabase database, IServer server)
	{
		this.database = database;
		this.server = server;
	}

	/**
	 * @return A list of clans, each containing a list of their members.
	 */
	public Map<String, List<IPlayer>> getClanRosters()
	{
		Map<String, List<IPlayer>> rosters = new ConcurrentHashMap<String, List<IPlayer>>(0);
		for (IRow row : database.query("SELECT `clanID`, `member` FROM `clan_members`"))
		{
			String clanName = row.String("clanID");
			if (!rosters.containsKey(clanName))
				rosters.put(clanName, new ArrayList<IPlayer>(1));

			rosters.get(clanName).add(server.getPlayerExact(row.String("member")));
		}
		return rosters;
	}

	/**
	 * Adds a member to a clan.
	 * @param clanID ID of the clan.
	 * @param player New member.
	 */
	public void addClanMember(String clanID, IPlayer player)
	{
		database.execute("INSERT INTO `clan_members` (`clanID`, `member`, `joined`) VALUES(?, ?, NOW())", clanID, player.getName());
	}

	/**
	 * Removes a member from whichever clan they are in.
	 * @param player Member to remove.
	 */
	public void removeClanMember(IPlayer player)
	{
		database.execute("DELETE FROM `clan_members` WHERE `member` = ?", player.getName());
	}

	/**
	 * Removes all clan members from a clan.
	 * @param clanID Clan to remove members from.
	 */
	public void removeAllClanMembers(String clanID)
	{
		database.execute("DELETE FROM `clan_members` WHERE `clanID` = ?", clanID);
	}

	/**
	 * Gets the date a player joined their current clan if they're in a one.
	 * @param player User to get the join date of.
	 * @return Date the player joined their current clan.
	 */
	public DateTime getClanMemberJoinDate(IPlayer player)
	{
		return database.queryDateTime("SELECT `joined` FROM `clan_members` WHERE `member` = ?", player.getName());
	}

	/**
	 * @return The table name where clan members are stored.
	 */
	@Override
	@Nonnull
	public String getTableName()
	{
		return "clan_members";
	}

	/**
	 * @return The SQL statements for upgrading the database table.
	 */
	@Override
	@Nonnull
	public ISchemaUpdate getSchemaUpdateQueries()
	{
		ISchemaUpdate update = new SchemaUpdate();

		update.addQueries(
			"CREATE TABLE `clan_members` (" +
				"`clanID` VARCHAR(3) NOT NULL," +
				"`member` VARCHAR(20) NOT NULL," +
				"PRIMARY KEY (`clanID`, `member`)" +
				")"
		);

		update.addQueries("ALTER TABLE `clan_members` ADD COLUMN `joined` DATETIME NOT NULL AFTER `member`;");

		return update;
	}

	private final IServer server;
}
