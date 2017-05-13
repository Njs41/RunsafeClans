package no.runsafe.clans.monitors;

import no.runsafe.clans.handlers.CharterHandler;
import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.block.IBlock;
import no.runsafe.framework.api.event.player.IPlayerRightClick;
import no.runsafe.framework.api.player.IPlayer;
import no.runsafe.framework.minecraft.Item;
import no.runsafe.framework.minecraft.item.meta.RunsafeMeta;

import java.util.List;

public class PlayerMonitor implements IPlayerRightClick
{
	/**
	 * Constructor for monitoring player right click events.
	 * @param charterHandler Handles charters.
	 * @param clanHandler Handles clans.
	 */
	public PlayerMonitor(CharterHandler charterHandler, ClanHandler clanHandler)
	{
		this.charterHandler = charterHandler;
		this.clanHandler = clanHandler;
	}

	/**
	 * Handles players trying to use clan charters.
	 * @param player User trying to use a charter.
	 * @param usingItem Potential clan charter.
	 * @param targetBlock Required argument; not used.
	 * @return True if player isn't trying to use a charter.
	 */
	@Override
	public boolean OnPlayerRightClick(IPlayer player, RunsafeMeta usingItem, IBlock targetBlock)
	{
		// Check we are holding a charter.
		if (usingItem == null || !usingItem.is(Item.Special.Crafted.WrittenBook) || !charterHandler.itemIsCharter(usingItem))
			return true;

		if (clanHandler.playerIsInClan(player))
		{
			player.sendColouredMessage("&cYou are already in a clan, you cannot sign this.");
			player.closeInventory();
			return false;
		}

		String clanName = charterHandler.getClanName(usingItem); // Grab the clan name from the book.

		// Check we have been given a valid clan name.
		if (clanHandler.isInvalidClanName(clanName))
		{
			player.sendColouredMessage(String.format("&c'%s' is not a valid clan tag. A clan tag must be three characters using characters A-Z.", clanName));
			player.closeInventory();
			return false;
		}

		// If the clan already exists, just tell them it can't happen.
		if (clanHandler.clanExists(clanName))
		{
			player.sendColouredMessage(String.format("&cA clan named '%s' already exists.", clanName));
			player.closeInventory();
			return false;
		}

		List<IPlayer> charterSigns = charterHandler.getCharterSigns(usingItem);

		if (charterSigns.contains(player))
		{
			player.sendColouredMessage("&cYou have already signed this charter.");
			player.closeInventory();
			return false;
		}

		// If we have less than 2 signs on the charter, we should sign it!
		if (charterSigns.size() < 2)
		{
			charterHandler.addCharterSign(usingItem, player);
			player.sendColouredMessage("&aYou have signed the charter!");
		}
		else
		{
			// Make sure all signs are valid.
			for (IPlayer signedPlayer : charterSigns)
			{
				if (clanHandler.playerIsInClan(signedPlayer))
				{
					player.sendColouredMessage("&cOne or more of the signatures on this charter are invalid, get more!");
					player.closeInventory();
					return false;
				}
			}

			if (clanHandler.playerIsInClan(player))
			{
				player.sendColouredMessage("&cYou are already in a clan!");
				player.closeInventory();
				return false;
			}

			clanHandler.createClan(clanName, charterHandler.getLeader(usingItem)); // Forge the clan!

			// Add all players on the charter to the clan if they are not already in a clan.
			for (IPlayer signedPlayer : charterSigns)
				if (!clanHandler.playerIsInClan(signedPlayer))
					clanHandler.addClanMember(clanName, signedPlayer);

			clanHandler.addClanMember(clanName, player); // Add the signing player to the clan.
			clanHandler.sendMessageToClan(clanName, "Your clan has been formed!");
			player.removeExactItem(usingItem); // Remove the charter.
		}
		player.closeInventory();
		return false;
	}

	private final CharterHandler charterHandler;
	private final ClanHandler clanHandler;
}
