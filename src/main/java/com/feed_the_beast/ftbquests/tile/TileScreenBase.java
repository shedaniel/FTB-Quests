package com.feed_the_beast.ftbquests.tile;

import com.feed_the_beast.ftblib.lib.tile.TileBase;

import javax.annotation.Nullable;

/**
 * @author LatvianModder
 */
public abstract class TileScreenBase extends TileBase
{
	public int getOffsetX()
	{
		return 0;
	}

	public int getOffsetY()
	{
		return 0;
	}

	public int getOffsetZ()
	{
		return 0;
	}

	@Nullable
	public abstract TileScreenCore getScreen();

	@Override
	protected boolean notifyBlock()
	{
		return !world.isRemote;
	}

	@Override
	public boolean canBeWrenched()
	{
		return false;
	}
}