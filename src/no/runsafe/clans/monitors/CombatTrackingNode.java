package no.runsafe.clans.monitors;

import no.runsafe.framework.api.player.IPlayer;

public class CombatTrackingNode
{
	/**
	 * @return The attacking player.
	 */
	public IPlayer getAttacker()
	{
		return attacker;
	}

	/**
	 * Sets the attacking player.
	 * @param attacker The attacking player.
	 * @return this.
	 */
	public CombatTrackingNode setAttacker(IPlayer attacker)
	{
		this.attacker = attacker;
		return this;
	}

	/**
	 * @return ID of the combat timer.
	 */
	public int getTimerID()
	{
		return timerID;
	}

	/**
	 * Constructor for the combat tracking node.
	 * @param timerID ID of the combat timer.
	 * @return this.
	 */
	public CombatTrackingNode setTimerID(int timerID)
	{
		this.timerID = timerID;
		return this;
	}

	private IPlayer attacker;
	private int timerID;
}
