package com.imptimer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import javax.inject.Inject;
import net.runelite.client.ui.overlay.OverlayPanel;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayPriority;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;

public class ImpTimerOverlay extends OverlayPanel
{
	private final ImpTimerPlugin plugin;
	private final ImpTimerConfig config;

	@Inject
	public ImpTimerOverlay(ImpTimerPlugin plugin, ImpTimerConfig config)
	{
		this.plugin = plugin;
		this.config = config;

		setPosition(OverlayPosition.TOP_LEFT);
		setPriority(OverlayPriority.HIGH);
		setResizable(false);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (!config.showOverlay())
		{
			return null;
		}

		panelComponent.getChildren().clear();

		// Only show when a session has been started (even if time is up, to show final count)
		if (!plugin.isSessionActive() && plugin.getImpsCaught() == 0)
		{
			// Optionally show a "waiting" hint - disabled for clean look
			// TitleComponent waiting = TitleComponent.builder()
			//     .text(config.overlayTitle())
			//     .build();
			// panelComponent.getChildren().add(waiting);
			// panelComponent.getChildren().add(LineComponent.builder()
			//     .left("Set 5 boxes to start")
			//     .build());
			// return super.render(graphics);
			return null;
		}

		// Title
		panelComponent.getChildren().add(TitleComponent.builder()
			.text(config.overlayTitle())
			.build());

		// Time remaining
		String timeText = plugin.getFormattedTime();
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Time")
			.right(timeText)
			.build());

		// Imps caught
		panelComponent.getChildren().add(LineComponent.builder()
			.left("Imps")
			.right(String.valueOf(plugin.getImpsCaught()))
			.build());

		return super.render(graphics);
	}
}