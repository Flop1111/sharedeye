# Shared Eye

A small **client-side Fabric mod** for a co-op Minecraft world. Everyone in the world installs it,
so nobody is being fooled.

Targets **Minecraft Java 1.21.1**. There is no Minecraft version "26.3" - send the real version
string from the bottom-left of the title screen and I will retarget it.

## Features

| Feature | Where | Notes |
|---|---|---|
| Container ESP | Menu / X | Boxes around chests, barrels, shulkers, hoppers, furnaces, dispensers, brewers, crafters, pots, ender chests. Boxes only: a chest's contents are not sent to the client until you open it. |
| Light ESP | Menu / L | Every torch, lantern and campfire in range. |
| Artificial light finder | Menu | Only lights with no built blocks around them - an isolated torch in the middle of nowhere. That is the closest a client can get to "not naturally placed": the server never labels who placed a block. |
| Amethyst finder | Menu | Budding amethyst (the geode core) with a vertical beam, plus every bud and cluster. Budding amethyst is the block that regrows the crystals, so a beam on it = a geode you can farm. |
| Base hints | Menu / B | Scores clusters of beds, bells, tables, anvils, furnaces, chests, lecterns, composters and lights, then lists the 3 nearest with distance, bearing and coords. |
| Freecam | F6 | Detached camera. WASD, Space/Shift, Ctrl for fast. Your body stays put. |
| Menu | Right Shift | See below. |
| HUD | H | Toggle the overlay. |
| Force rescan | R | Re-scan immediately. |

## The menu

Right Shift opens a dark panel with everything as toggles and live sliders:

- toggles: containers, lights, artificial lights, amethyst, base hints, HUD
- sliders: scan radius (1-12 chunks), scan interval (20-200 ticks), base score threshold (5-60),
  freecam speed (0.2-3.0)
- buttons: rescan now, freecam on/off

Esc or Right Shift closes it. Every setting writes straight to `config/sharedeye.json`, so edits
in the menu and edits in the file are the same thing. All keys are rebindable in
Options -> Controls -> Shared Eye.

## Why "not naturally placed lights" is only a heuristic

A client only knows what the server has sent, and the server never labels a block as "generated"
or "placed by a player". There is no reliable client-side signal for that. The artificial light
finder marks a light as "isolated" when there is no scored block (bed, bell, table, chest, furnace,
lectern, composter...) within `artificialRadius` blocks. A torch alone on a hillside shows up; a
village full of torches does not. It is a hint, not a detector.

## Hard limit on the freecam

You only see chunks the client has loaded, and the client loads chunks around your **body**, not
around the camera. Flying 500 blocks away shows void. No client-side mod can fix this - it is a
server-side data limit. If you need true "see everything", use spectator mode (you own the world)
or raise the server view distance.

## Install (both players)

1. **JDK 21** installed.
2. **Fabric Loader** for 1.21.1 from https://fabricmc.net/use/installer/ - run it, pick 1.21.1,
   install. A "fabric-loader-1.21.1" profile appears in the launcher.
3. **Fabric API** for 1.21.1 from https://modrinth.com/mod/fabric-api/versions?g=1.21.1 - put the
   jar in the mods folder.
4. Put `sharedeye-0.2.0.jar` in the same mods folder.
5. Launch the Fabric profile. Both of you do this.

Mods folder:
- Windows: `%APPDATA%\.minecraft\mods`
- macOS: `~/Library/Application Support/minecraft/mods`
- Linux: `~/.minecraft/mods`

The mod will refuse to load on the wrong Minecraft version - that is the "requires minecraft
~1.21.1" error, not a bug.

## Build from source

```
gradle wrapper --gradle-version 8.10
gradle build
```

Or import the folder as a Gradle project in IntelliJ IDEA and run `build`. Jar lands in
`build/libs/`.

## Publishing on Modrinth

1. Sign in at https://modrinth.com and open the dashboard, **Create a project**.
2. Fill in name, slug, summary, and a description. Set **Environment: Client** and
   **Client-side: Required** (it does nothing on a server).
3. On the project page, **Create a version**: upload `sharedeye-0.2.0.jar`, set version
   `0.2.0`, channel **Release**, loaders **Fabric**, game version **1.21.1**.
4. Under dependencies, add **Fabric API** as *required*. Modrinth reads `fabric.mod.json` and will
   suggest it.
5. License: MIT (already in the jar).
6. Submit. Projects with a new account usually go through a short moderation queue.

Two honest notes. Modrinth's content rules do restrict tools that read as multiplayer cheats, and
an ESP mod is right on that line - describe it as a co-op overlay for a private world, keep the
"install it on every client" framing, and if it gets rejected just ship the jar from GitHub
Releases instead. Also, publishing means strangers can install it on public servers, so keep the
description accurate about what it does.

## Honest status

I could not compile this: no JDK, no Gradle, no shell in my sandbox. The code is written against
the 1.21.1 Yarn mappings, but the first build may surface one or two mapping renames. Paste the
error and it is usually a one-line fix.

Fragile spots, in order:
- `mixin/KeyboardInputMixin.java` (freezes body movement while freecamming) - field names.
- `mixin/CameraMixin.java` - `Camera.update` signature shifts between minor versions.
- `MenuScreen.java` - `DrawContext` / `Screen` signatures.
- `Scanner.java` - `WorldChunk.getBlockEntities()` and `ChunkSection.hasAny`.

## License

MIT.
