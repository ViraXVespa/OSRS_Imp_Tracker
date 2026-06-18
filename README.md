# Imp Timer - RuneLite Plugin

Tracks a 1-hour imp catching session for OSRS using Magic Boxes.

## Features
- Automatically starts a 1-hour timer the moment you have **5 Magic Box traps** placed on the ground (item ID 10025).
- Counts **Imp-in-a-box** items (10027 and 10028) collected (inventory increases) during the active hour.
- Displays a clean countdown timer + imps caught counter in the **top-left** of the game screen.

## Requirements
- 71 Hunter
- Magic boxes (bought from Hunter shop in Yanille or similar)
- Typical strategy: Place 5 boxes around imp spawns.

## Installation (Plugin Hub style - Recommended)

1. Go to https://github.com/runelite/example-plugin (or use the generate button on the plugin-hub template).
2. Click "Use this template" → Create a new repository (you must be logged into GitHub).
3. Clone your new repo locally.
4. Replace the example Java files with the three files in this folder (adjust package name if you want).
5. Build with Gradle:
   ```bash
   ./gradlew build
   ```
6. The built jar will be in `build/libs/`.
7. Put the jar into your RuneLite `plugins` folder (or use the in-client "Plugin Hub" local install if supported, or for local dev put in runelite's external plugin dir).
8. Restart RuneLite and enable "Imp Timer".

Alternative for quick testing (advanced):
- Clone the full runelite/runelite repo.
- Copy these sources into `runelite-client/src/main/java/com/imptimer/`
- Add to the plugins list and recompile the client.

## In-Game Usage

- Set up **5 Magic Boxes** on the ground → timer auto-starts (1 hour).
- The overlay appears in the top left showing:
  ```
  Imp Timer
  Time: 59:59
  Imps: 0
  ```
- Pick up Imp-in-a-box items → the counter increases.
- Use chat commands:
  - `/imptimer reset` — Reset the timer and counters.
  - `/imptimer start` — Force-start the timer immediately (for testing).
- When the hour ends, the timer shows `00:00` and keeps the final imp count until you reset.

## Config
Open the RuneLite plugin settings for "Imp Timer":
- Enable/disable the overlay.
- (Basic version; more options can be added later.)

## Notes & Limitations
- The ground item count is event-driven (spawns/despawns). It works best when you place boxes while the plugin is active.
- It counts any Imp-in-a-box pickups during the hour (even if from other players' traps, but unlikely in practice).
- Does not track XP or other hunter actions.
- Works alongside the built-in Ground Items and Hunter plugins.

## Files (standard layout)
```
ImpTimer/
├── README.md
├── src/
│   └── main/
│       └── java/
│           └── com/
│               └── imptimer/
│                   ├── ImpTimerPlugin.java
│                   ├── ImpTimerOverlay.java
│                   └── ImpTimerConfig.java
```

Copy the entire `com/imptimer` package into your template project under `src/main/java/`.

## Development Tips
- Item IDs are hardcoded for Magic box (10025) and Imp-in-a-box (10027 + 10028).
- The 1-hour duration is fixed at 3600000 ms.
- To change display position or style, edit the overlay (uses `OverlayPanel` + `LineComponent`).

Enjoy your imp sessions! Any improvements welcome.

## License
BSD-2-Clause (same as RuneLite).