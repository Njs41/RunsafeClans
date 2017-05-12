package no.runsafe.clans.database;

import no.runsafe.clans.Clan;
import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.database.*;
import no.runsafe.framework.api.player.IPlayer;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

public class ClanRepository extends Repository
{
	/**
	 * Constructor for the clan repository.
	 * @param database The database.
	 * @param server The server. Used for getting a player object from their username and or UUID.
	 */
	public ClanRepository(IDatabase database, IServer server)
	{
		this.database = database;
		this.server = server;
	}

	/**
	 * Gets a list of all persisting clans.
	 * @return List of clan names with their respective clan objects.
	 */
	public Map<String, Clan> getClans()
	{
		Map<String, Clan> clanList = new HashMap<String, Clan>(0);

		for (IRow row : database.query("SELECT `clanID`, `leader`, `motd`, `clanKills`, `clanDeaths`, `dergonKills` FROM `clans`"))
		{
			String clanName = row.String("clanID");
			Clan clan = new Clan(clanName, server.getPlayerExact(row.String("leader")), row.String("motd"));
			clan.addClanKills(row.Integer("clanKills")); // Add in kills stat
			clan.addClanDeaths(row.Integer("clanDeaths")); // Add in deaths stat
			clan.addDergonKills(row.Integer("dergonKills")); // Add dergon kills.
			clanList.put(clanName, clan);
		}
		return clanList;
	}

	/**
	 * Sets a new Message of the Day.
	 * @param clanID Clan to set the new motd of.
	 * @param motd The new Message of the Day.
	 */
	public void updateMotd(String clanID, String motd)
	{
		database.execute("UPDATE `clans` SET `motd` = ? WHERE `clanID` = ?", motd, clanID);
	}

	/**
	 * Deletes a clan.
	 * @param clanID Clan to delete.
	 */
	public void deleteClan(String clanID)
	{
		database.execute("DELETE FROM `clans` WHERE `clanID` = ?", clanID);
	}

	/**
	 * Changes a clan leader.
	 * @param clanID Clan that's under new leadership.
	 * @param leader Player to rule over the clan.
	 */
	public void changeClanLeader(String clanID, IPlayer leader)
	{
		database.execute("UPDATE `clans` SET `leader` = ? WHERE `clanID` = ?", leader.getName(), clanID);
	}

	/**
	 * Make sure the clan is loaded in the database.
	 * @param clan Object to load into the database.
	 */
	public void persistClan(Clan clan)
	{
		database.execute("INSERT INTO `clans` (`clanID`, `leader`, `created`, `motd`) VALUES(?, ?, NOW(), ?)", clan.getId(), clan.getLeader(), clan.getMotd());
	}

	/**
	 * Updates a single clan statistic.
	 * @param clanID The clan to update the statistic of.
	 * @param statistic Name of the statistic to update. Statistics include:
	 *                  clanKills, clanDeaths, and dergonKills.
	 * @param value Value to update the statistic to.
	 */
	public void updateStatistic(String clanID, String statistic, int value)
	{
		database.execute("UPDATE `clans` SET `" + statistic + "` = ? WHERE `clanID` = ?", value, clanID);
	}

	/**
	 * @return The table name where clans and their information is stored.
	 */
	@Override
	@Nonnull
	public String getTableName()
	{
		return "clans";
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
			"CREATE TABLE `clans` (" +
				"`clanID` VARCHAR(3) NOT NULL," +
				"`leader` VARCHAR(20) NOT NULL," +
				"`created` DATETIME NOT NULL," +
				"PRIMARY KEY (`clanID`)" +
			")"
		);

		update.addQueries("ALTER TABLE `clans` ADD COLUMN `motd` VARCHAR(255) NOT NULL AFTER `created`;");

		update.addQueries("ALTER TABLE `clans`" +
				"ADD COLUMN `clanKills` INT NOT NULL DEFAULT '0' AFTER `motd`," +
				"ADD COLUMN `clanDeaths` INT NOT NULL DEFAULT '0' AFTER `clanKills`;");

		update.addQueries("ALTER TABLE `clans`" +
				"ADD COLUMN `dergonKills` INT(10) UNSIGNED NOT NULL DEFAULT '0' AFTER `clanDeaths`");

		return update;
	}

	private final IServer server;
}
