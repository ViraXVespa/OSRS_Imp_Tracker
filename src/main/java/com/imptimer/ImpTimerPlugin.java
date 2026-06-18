package com.imptimer;

import com.google.inject.Provides;
import java.time.Duration;
import java.time.Instant;
import javax.inject.Inject;

import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.ItemDespawned;
import net.runelite.api.events.ItemQuantityChanged;
import net.runelite.api.events.ItemSpawned;
import net.runelite.client.chat.ChatCommandManager;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Imp Timer",
	description = "1-hour timer for Magic Box imp catching sessions. Starts at 5 boxes. Tracks Imp-in-a-box pickups.",
	tags = {"hunter", "imp", "magic box", "timer", "impling", "osrs"}
)
public class ImpTimerPlugin extends Plugin
{
	private static final int MAGIC_BOX_ID = 10025;
	private static final int IMP_IN_BOX_ID = 10027;   // Imp-in-a-box
	private static final int IMP_IN_BOX_1_ID = 10028; // Imp-in-a-box(1)

	private static final long TIMER_DURATION_MS = 60 * 60 * 1000L; // 1 hour

	@Inject
	private Client client;

	@Inject
	private ImpTimerConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ImpTimerOverlay overlay;

	@Inject
	private ChatCommandManager chatCommandManager;

	private boolean sessionActive = false;

	private Instant timerStart = null;
	private int impsCaught = 0;
	private int magicBoxesOnGround = 0;
	private int lastImpCount = 0;

	@Override
	protected void startUp()
	{
		overlayManager.add(overlay);
		chatCommandManager.registerCommand("imptimer", this::onImpTimerChatCommand);
		chatCommandManager.registerCommand("impreset", this::onImpTimerChatCommand);
		// Imp Timer started
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(overlay);
		chatCommandManager.unregisterCommand("imptimer");
		chatCommandManager.unregisterCommand("impreset");
		resetSession();
		// Imp Timer stopped
	}

	@Provides
	ImpTimerConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(ImpTimerConfig.class);
	}

	private void onImpTimerChatCommand(String command, String args)
	{
		args = args == null ? "" : args.trim().toLowerCase();

		if (args.isEmpty() || "reset".equals(args))
		{
			resetSession();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Imp Timer: Session reset.", null);
		}
		else if ("start".equals(args) || "force".equals(args))
		{
			forceStartSession();
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Imp Timer: Session force started.", null);
		}
		else if ("status".equals(args))
		{
			if (sessionActive)
			{
				long remaining = getRemainingMillis();
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "",
					"Imp Timer: " + formatTime(remaining) + " left, Imps: " + impsCaught, null);
			}
			else
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Imp Timer: No active session.", null);
			}
		}
	}

	public void resetSession()
	{
		sessionActive = false;
		timerStart = null;
		impsCaught = 0;
		magicBoxesOnGround = 0;
		lastImpCount = 0;
	}

	private void forceStartSession()
	{
		sessionActive = true;
		timerStart = Instant.now();
		impsCaught = 0;
		lastImpCount = countImpBoxesInInventory();
	}

	private void checkStartSession()
	{
		if (!sessionActive && magicBoxesOnGround >= 5)
		{
			forceStartSession();
			// Imp Timer session auto-started
			if (client.getGameState() == GameState.LOGGED_IN)
			{
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Imp Timer started! 1 hour session.", null);
			}
		}
	}

	@Subscribe
	public void onItemSpawned(ItemSpawned event)
	{
		if (event.getItem().getId() == MAGIC_BOX_ID)
		{
			magicBoxesOnGround += event.getItem().getQuantity();
			checkStartSession();
		}
	}

	@Subscribe
	public void onItemDespawned(ItemDespawned event)
	{
		if (event.getItem().getId() == MAGIC_BOX_ID)
		{
			magicBoxesOnGround = Math.max(0, magicBoxesOnGround - event.getItem().getQuantity());
		}
	}

	@Subscribe
	public void onItemQuantityChanged(ItemQuantityChanged event)
	{
		if (event.getItem().getId() == MAGIC_BOX_ID)
		{
			int diff = event.getNewQuantity() - event.getOldQuantity();
			magicBoxesOnGround = Math.max(0, magicBoxesOnGround + diff);
			checkStartSession();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() != InventoryID.INVENTORY.getId() || !sessionActive)
		{
			return;
		}

		int current = countImpBoxesInInventory(event.getItemContainer());
		if (current > lastImpCount)
		{
			impsCaught += (current - lastImpCount);
		}
		lastImpCount = current;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOGIN_SCREEN ||
			event.getGameState() == GameState.HOPPING ||
			event.getGameState() == GameState.CONNECTION_LOST)
		{
			resetSession();
		}
	}

	public long getRemainingMillis()
	{
		if (!sessionActive || timerStart == null)
		{
			return 0;
		}
		long elapsed = Duration.between(timerStart, Instant.now()).toMillis();
		return Math.max(0, TIMER_DURATION_MS - elapsed);
	}

	public int getImpsCaught()
	{
		return impsCaught;
	}

	public String getFormattedTime()
	{
		return formatTime(getRemainingMillis());
	}

	public boolean isSessionActive()
	{
		return sessionActive;
	}

	public boolean isTimerActive()
	{
		return sessionActive && getRemainingMillis() > 0;
	}

	private int countImpBoxesInInventory()
	{
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		return countImpBoxesInInventory(container);
	}

	private int countImpBoxesInInventory(ItemContainer container)
	{
		if (container == null)
		{
			return 0;
		}

		int total = 0;
		for (Item item : container.getItems())
		{
			if (item.getId() == IMP_IN_BOX_ID || item.getId() == IMP_IN_BOX_1_ID)
			{
				total += item.getQuantity();
			}
		}
		return total;
	}

	private String formatTime(long millis)
	{
		if (millis <= 0)
		{
			return "00:00";
		}
		long totalSeconds = millis / 1000;
		long minutes = totalSeconds / 60;
		long seconds = totalSeconds % 60;
		return String.format("%02d:%02d", minutes, seconds);
	}
}