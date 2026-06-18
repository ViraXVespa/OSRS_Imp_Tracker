package com.imptimer;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("imptimer")
public interface ImpTimerConfig extends Config
{
	@ConfigItem(
		keyName = "showOverlay",
		name = "Show overlay",
		description = "Show the timer and imp counter overlay in the top left",
		position = 0
	)
	default boolean showOverlay()
	{
		return true;
	}

	@ConfigItem(
		keyName = "overlayTitle",
		name = "Overlay title",
		description = "Title shown above the timer",
		position = 1
	)
	default String overlayTitle()
	{
		return "Imp Timer";
	}
}