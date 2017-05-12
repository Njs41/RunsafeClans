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
	public ClanRepository(IDatabase database, IServer server)
	{
		this.database = database;
		this.server = server;
	}

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

	public void updateMotd(String clanID, String motd)
	{
		database.execute("UPDATE `clans` SET `motd` = ? WHERE `clanID` = ?", motd, clanID);
	}

	public void deleteClan(String clanID)
	{
		database.execute("DELETE FROM `clans` WHERE `clanID` = ?", clanID);
	}

	public void changeClanLeader(String clanID, IPlayer leader)
	{
		database.execute("UPDATE `clans` SET `leader` = ? WHERE `clanID` = ?", leader.getName(), clanID);
	}

	public void persistClan(Clan clan)
	{
		database.execute("INSERT INTO `clans` (`clanID`, `leader`, `created`, `motd`) VALUES(?, ?, NOW(), ?)", clan.getId(), clan.getLeader(), clan.getMotd());
	}

	public void updateStatistic(String clanID, String statistic, int value)
	{
		database.execute("UPDATE `clans` SET `" + statistic + "` = ? WHERE `clanID` = ?", value, clanID);
	}

	@Override
	@Nonnull
	public String getTableName()
	{
		return "clans";
	}

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
