package no.runsafe.clans.handlers;

import no.runsafe.framework.api.IServer;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.item.meta.RunsafeBook;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class CharterHandler
{
	/**
	 * Constructor for handling clan charters.
	 * @param server The server. Used for getting a player object from their username and or UUID.
	 */
	public CharterHandler(IServer server)
	{
		this.server = server;
	}

	/**
	 * Gives a player a clan charter in the form of a book.
	 * @param player Leader of the clan to create.
	 * @param clanName Name of the clan.
	 */
	public void givePlayerCharter(IPlayer player, String clanName)
	{
		RunsafeMeta charter = Item.Special.Crafted.WrittenBook.getItem(); // Create a book item.
		charter.setDisplayName("Leather-bound Charter"); // Give the item a name.
		charter.addLore("§7Clan: " + clanName); // Append the clan name.
		charter.addLore("§7Leader: " + player.getName()); // Append the clan leader.
		charter.addLore("§fRight-click to sign the clan charter!"); // Add some info.
		addCharterSign(charter, player); // Sign the charter.

		player.give(charter); // Give the player the charter.
	}

	/**
	 * Checks if an item is a clan charter.
	 * Only checks for "§fRight-click to sign the clan charter!" in the third lore slot.
	 * @param item Item to check.
	 * @return True if item is a clan charter, false otherwise.
	 */
	public boolean itemIsCharter(RunsafeMeta item)
	{
		List<String> lore = item.getLore();
		return lore != null && lore.size() == 3 && lore.get(2).equals("§fRight-click to sign the clan charter!");
	}

	/**
	 * Gets the name of the clan to be created.
	 * Assumes item passed to it is a legitimate clan charter.
	 * @param charter Charter to check.
	 * @return Clan name.
	 */
	public String getClanName(RunsafeMeta charter)
	{
		return getCharterValue(charter.getLore(), 0);
	}

	/**
	 * Gets the leader listed in a clan charter.
	 * Assumes item passed to it is a legitimate clan charter.
	 * @param charter Charter to check.
	 * @return Leader name.
	 */
	public IPlayer getLeader(RunsafeMeta charter)
	{
		return getCharterSigns(charter).get(0);
	}

	/**
	 * Gets the lore value stored at a specified lore number.
	 * If the lore values are null, "INVALID" will be returned.
	 * Assumes lore values are from a legitimate clan charter.
	 * @param values Lore to look through.
	 * @param index Lore number to check.
	 * @return Value stored at the specified lore number.
	 */
	private String getCharterValue(List<String> values, int index)
	{
		if (values == null)
			return "INVALID"; // This should never happen.

		return values.get(index).split("\\s")[1];
	}

	/**
	 * Gets a list of the players who have signed a charter.
	 * Assumes item passed to it is a legitimate clan charter.
	 * Assumes there is only a single valid UUID or username on each page of the charter.
	 * Only checks if what's on each page is an expected length to be a UUID or username.
	 * @param item Charter to check.
	 * @return List of players who have signed the charter.
	 */
	public List<IPlayer> getCharterSigns(RunsafeMeta item)
	{
		RunsafeBook charter = (RunsafeBook) item; // Convert item to a book.
		if (charter.hasPages()) // Check we have some pages.
		{
			List<String> charterPages = charter.getPages();
			List<IPlayer> charterSigns = new ArrayList<IPlayer>(0);
			for(String page : charterPages)
				if (page.length() == 36) // Check if what's written on the page is likely a UUID.
					charterSigns.add(server.getPlayer(UUID.fromString(page)));
				else if (page.length() <= 16) // Check if what's written on the page is likely a username.
					charterSigns.add(server.getPlayerExact(page));
			return charterSigns;
		}
		return Collections.emptyList(); // Return an empty thing.
	}

	/**
	 * Adds a player signature to a charter in the form of their UUID.
	 * Assumes item passed to it is a legitimate clan charter.
	 * @param item Charter to sign.
	 * @param player User signing the charter.
	 */
	public void addCharterSign(RunsafeMeta item, IPlayer player)
	{
		RunsafeBook charter = (RunsafeBook) item; // Convert item to a book.
		charter.addPages(player.getUniqueId().toString()); // Add the sign to the charter.
	}

	private final IServer server;
}
