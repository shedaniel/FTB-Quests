package com.feed_the_beast.ftbquests.quest.reward;

import com.feed_the_beast.ftbquests.net.FTBQuestsNetHandler;
import com.feed_the_beast.ftbquests.net.MessageDisplayItemRewardToast;
import com.feed_the_beast.ftbquests.quest.Quest;
import com.feed_the_beast.ftbquests.util.NBTUtils;
import com.feed_the_beast.mods.ftbguilibrary.config.ConfigGroup;
import com.feed_the_beast.mods.ftbguilibrary.icon.Icon;
import com.feed_the_beast.mods.ftbguilibrary.icon.ItemIcon;
import com.feed_the_beast.mods.ftbguilibrary.widget.WrappedIngredient;
import me.shedaniel.architectury.hooks.ItemStackHooks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author LatvianModder
 */
public class ItemReward extends Reward
{
	public ItemStack item;
	public int count;
	public int randomBonus;
	public boolean onlyOne;

	public ItemReward(Quest quest, ItemStack is)
	{
		super(quest);
		item = is;
		count = 1;
		randomBonus = 0;
		onlyOne = false;
	}

	public ItemReward(Quest quest)
	{
		this(quest, new ItemStack(Items.APPLE));
	}

	@Override
	public RewardType getType()
	{
		return RewardTypes.ITEM;
	}

	@Override
	public void writeData(CompoundTag nbt)
	{
		super.writeData(nbt);
		NBTUtils.write(nbt, "item", item);

		if (count > 1)
		{
			nbt.putInt("count", count);
		}

		if (randomBonus > 0)
		{
			nbt.putInt("random_bonus", randomBonus);
		}

		if (onlyOne)
		{
			nbt.putBoolean("only_one", true);
		}
	}

	@Override
	public void readData(CompoundTag nbt)
	{
		super.readData(nbt);
		item = NBTUtils.read(nbt, "item");

		count = nbt.getInt("count");

		if (count == 0)
		{
			count = item.getCount();
			item.setCount(1);
		}

		randomBonus = nbt.getInt("random_bonus");
		onlyOne = nbt.getBoolean("only_one");
	}

	@Override
	public void writeNetData(FriendlyByteBuf buffer)
	{
		super.writeNetData(buffer);
		FTBQuestsNetHandler.writeItemType(buffer, item);
		buffer.writeVarInt(count);
		buffer.writeVarInt(randomBonus);
		buffer.writeBoolean(onlyOne);
	}

	@Override
	public void readNetData(FriendlyByteBuf buffer)
	{
		super.readNetData(buffer);
		item = FTBQuestsNetHandler.readItemType(buffer);
		count = buffer.readVarInt();
		randomBonus = buffer.readVarInt();
		onlyOne = buffer.readBoolean();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public void getConfig(ConfigGroup config)
	{
		super.getConfig(config);
		config.addItemStack("item", item, v -> item = v, ItemStack.EMPTY, true, false).setNameKey("ftbquests.reward.ftbquests.item");
		config.addInt("count", count, v -> count = v, 1, 1, 8192);
		config.addInt("random_bonus", randomBonus, v -> randomBonus = v, 0, 0, 8192).setNameKey("ftbquests.reward.random_bonus");
		config.addBool("only_one", onlyOne, v -> onlyOne = v, false);
	}

	@Override
	public void claim(ServerPlayer player, boolean notify)
	{
		if (onlyOne && player.inventory.contains(item))
		{
			return;
		}

		int size = count + player.level.random.nextInt(randomBonus + 1);

		while (size > 0)
		{
			int s = Math.min(size, item.getMaxStackSize());
			ItemStackHooks.giveItem(player, ItemStackHooks.copyWithCount(item, s));
			size -= s;
		}

		if (notify)
		{
			new MessageDisplayItemRewardToast(item, size).sendTo(player);
		}
	}

	@Override
	public boolean automatedClaimPre(BlockEntity tileEntity, List<ItemStack> items, Random random, UUID playerId, @Nullable ServerPlayer player)
	{
		int size = count + random.nextInt(randomBonus + 1);

		while (size > 0)
		{
			int s = Math.min(size, item.getMaxStackSize());
			ItemStack copy = item.copy();
			copy.setCount(s);
			items.add(copy);
			size -= s;
		}

		return true;
	}

	@Override
	public void automatedClaimPost(BlockEntity tileEntity, UUID playerId, @Nullable ServerPlayer player)
	{
	}

	@Override
	@Environment(EnvType.CLIENT)
	public MutableComponent getAltTitle()
	{
		return new TextComponent((count > 1 ? (randomBonus > 0 ? (count + "-" + (count + randomBonus) + "x ") : (count + "x ")) : "")).append(item.getHoverName());
	}

	@Override
	@Environment(EnvType.CLIENT)
	public Icon getAltIcon()
	{
		if (item.isEmpty())
		{
			return super.getAltIcon();
		}

		ItemStack copy = item.copy();
		copy.setCount(1);
		return ItemIcon.getItemIcon(copy);
	}

	@Override
	@Environment(EnvType.CLIENT)
	public boolean addTitleInMouseOverText()
	{
		return !getTitle().getString().equals(getAltTitle().getString());
	}

	@Nullable
	@Override
	@Environment(EnvType.CLIENT)
	public Object getIngredient()
	{
		return new WrappedIngredient(item).tooltip();
	}

	@Override
	@Environment(EnvType.CLIENT)
	public String getButtonText()
	{
		if (randomBonus > 0)
		{
			return count + "-" + (count + randomBonus);
		}

		return Integer.toString(count);
	}
}