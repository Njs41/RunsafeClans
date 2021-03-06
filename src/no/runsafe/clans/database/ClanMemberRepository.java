package no.runsafe.clans.database;

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
	public ClanMemberRepository(IDatabase database)
	{
		this.database = database;
	}

	public Map<String, List<String>> getClanRosters()
	{
		Map<String, List<String>> rosters = new ConcurrentHashMap<String, List<String>>(0);
		for (IRow row : database.query("SELECT `clanID`, `member` FROM `clan_members`"))
		{
			String clanName = row.String("clanID");
			if (!rosters.containsKey(clanName))
				rosters.put(clanName, new ArrayList<String>(1));

			rosters.get(clanName).add(row.String("member"));
		}
		return rosters;
	}

	public void addClanMember(String clanID, String playerName)
	{
		database.execute("INSERT INTO `clan_members` (`clanID`, `member`, `joined`) VALUES(?, ?, NOW())", clanID, playerName);
	}

	public void removeClanMemberByName(String playerName)
	{
		database.execute("DELETE FROM `clan_members` WHERE `member` = ?", playerName);
	}

	public void removeAllClanMembers(String clanID)
	{
		database.execute("DELETE FROM `clan_members` WHERE `clanID` = ?", clanID);
	}

	public DateTime getClanMemberJoinDate(IPlayer player)
	{
		return database.queryDateTime("SELECT `joined` FROM `clan_members` WHERE `member` = ?", player.getName());
	}

	@Override
	@Nonnull
	public String getTableName()
	{
		return "clan_members";
	}

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
}
