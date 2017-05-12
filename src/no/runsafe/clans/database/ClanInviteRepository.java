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
	public ClanInviteRepository(IDatabase database, IServer server)
	{
		this.database = database;
		this.server = server;
	}

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

	@Deprecated
	public void clearPendingInvite(String playerName, String clanID)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `player` = ? AND `clanID` = ?", playerName, clanID);
	}

	public void clearPendingInvite(IPlayer player, String clanID)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `player` = ? AND `clanID` = ?", player.getName(), clanID);
	}

	@Deprecated
	public void clearAllPendingInvites(String playerName)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `player` = ?", playerName);
	}

	public void clearAllPendingInvites(IPlayer player)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `player` = ?", player.getName());
	}

	public void clearAllPendingInvitesForClan(String clanID)
	{
		database.execute("DELETE FROM `clan_invites` WHERE `clanID` = ?", clanID);
	}

	public void addInvite(String playerName, String clanID)
	{
		database.execute("INSERT IGNORE INTO `clan_invites` (`player`, `clanID`) VALUES(?, ?)", playerName, clanID);
	}

	@Nonnull
	@Override
	public String getTableName()
	{
		return "clan_invites";
	}

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
