package com.feed_the_beast.ftbquests.net.edit;

import com.feed_the_beast.ftblib.lib.io.DataIn;
import com.feed_the_beast.ftblib.lib.io.DataOut;
import com.feed_the_beast.ftblib.lib.net.MessageToServer;
import com.feed_the_beast.ftblib.lib.net.NetworkWrapper;
import com.feed_the_beast.ftbquests.FTBQuests;
import com.feed_the_beast.ftbquests.quest.QuestReward;
import com.feed_the_beast.ftbquests.quest.ServerQuestFile;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

/**
 * @author LatvianModder
 */
public class MessageEditReward extends MessageToServer
{
	private int uid;
	private boolean team;
	private ItemStack stack;

	public MessageEditReward()
	{
	}

	public MessageEditReward(int i, boolean t, ItemStack is)
	{
		uid = i;
		team = t;
		stack = is;
	}

	@Override
	public NetworkWrapper getWrapper()
	{
		return FTBQuestsEditNetHandler.EDIT;
	}

	@Override
	public void writeData(DataOut data)
	{
		data.writeInt(uid);
		data.writeBoolean(team);
		data.writeNBT(stack.isEmpty() ? null : stack.serializeNBT());
	}

	@Override
	public void readData(DataIn data)
	{
		uid = data.readInt();
		team = data.readBoolean();
		NBTTagCompound nbt = data.readNBT();
		stack = nbt == null ? ItemStack.EMPTY : new ItemStack(nbt);
	}

	@Override
	public void onMessage(EntityPlayerMP player)
	{
		if (FTBQuests.canEdit(player))
		{
			QuestReward q = ServerQuestFile.INSTANCE.allRewards.get(uid);

			if (q != null)
			{
				q.team = team;
				q.stack = stack;

				if (q.stack.isEmpty())
				{
					q.quest.rewards.remove(q);
					ServerQuestFile.INSTANCE.allRewards.remove(q.uid);
				}

				ServerQuestFile.INSTANCE.save();
				new MessageEditRewardResponse(uid, team, stack).sendToAll();
			}
		}
	}
}