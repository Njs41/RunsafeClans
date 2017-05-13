package no.runsafe.clans.chat;

import no.runsafe.clans.handlers.ClanHandler;
import no.runsafe.framework.api.log.IConsole;
import no.runsafe.nchat.channel.BasicChatChannel;
import no.runsafe.nchat.channel.IChannelManager;

import javax.annotation.Nullable;

public class ClanChannel extends BasicChatChannel
{
	/**
	 * Constructor for clan chat channels.
	 * @param console Place to output data.
	 * @param manager Channel manager.
	 * @param name What the channel is clled.
	 * @param handler Handles clans.
	 */
	public ClanChannel(IConsole console, IChannelManager manager, String name, ClanHandler handler)
	{
		super(console, manager, name);
		this.handler = handler;
	}

	@Nullable
	@Override
	public String getCustomTag()
	{
		return handler.formatClanTag(getName());
	}

	private final ClanHandler handler;
}
