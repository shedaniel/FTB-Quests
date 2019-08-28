package com.feed_the_beast.ftbquests.integration.kubejs;

import com.feed_the_beast.ftbquests.events.CustomRewardEvent;
import com.feed_the_beast.ftbquests.events.CustomTaskEvent;
import dev.latvian.kubejs.KubeJSBindingsEvent;
import dev.latvian.kubejs.documentation.DocumentationEvent;
import dev.latvian.kubejs.event.EventsJS;
import dev.latvian.kubejs.player.PlayerDataCreatedEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * @author LatvianModder
 */
public class KubeJSIntegration
{
	public static void preInit()
	{
		MinecraftForge.EVENT_BUS.register(KubeJSIntegration.class);
	}

	@SubscribeEvent
	public static void registerDocumentation(DocumentationEvent event)
	{
		event.registerPackage(KubeJSIntegration.class.getPackage());

		event.registerEvent("ftbquests.custom_task.<id>", CustomTaskEventJS.class);
		event.registerEvent("ftbquests.custom_reward.<id>", CustomRewardEventJS.class);
	}

	@SubscribeEvent
	public static void registerBindings(KubeJSBindingsEvent event)
	{
		event.add("ftbquests", new FTBQuestsKubeJSWrapper());
	}

	@SubscribeEvent
	public static void onPlayerDataCreated(PlayerDataCreatedEvent event)
	{
		event.setData("ftbquests", new FTBQuestsKubeJSPlayerData(event.getPlayerData()));
	}

	@SubscribeEvent
	public static void onCustomTask(CustomTaskEvent event)
	{
		if (!event.getTask().getQuestFile().isClient())
		{
			CustomTaskEventJS e = new CustomTaskEventJS();
			EventsJS.INSTANCE.post("ftbquests.custom_task." + event.getTask(), e);

			if (e.check != null)
			{
				event.getTask().check = new CheckWrapper(e.check);
				event.getTask().checkTimer = e.checkTimer;
			}
		}
	}

	@SubscribeEvent
	public static void onCustomReward(CustomRewardEvent event)
	{
		if (!event.getReward().getQuestFile().isClient())
		{
			EventsJS.INSTANCE.post("ftbquests.custom_reward." + event.getReward(), new CustomRewardEventJS(event.getPlayer(), event.getNotify()));
		}
	}
}