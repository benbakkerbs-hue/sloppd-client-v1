sloppd client 1.1.0 — Minecraft Java 1.21.11 / Fabric

Target setup:
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.18.1+
- Java 21
- Fabric API 0.141.6+1.21.11
- Yarn mappings 1.21.11+build.3
- Fabric Loom remap plugin 1.14.10

Included modules:
- Combat information HUD: CPS counter, keystrokes, armor durability,
  potion effects, target health, and vanilla attack cooldown percentage.
- Movement: Sprint, Sprint HUD, and movement speed HUD.
- Render: Fullbright, Entity ESP, Storage ESP, Block ESP, Tracers,
  and current Chunk Borders.
- World information: chunk coordinates, current-chunk block-entity count,
  and Prime Sus Chunk density indicator.
- Prime Sus Chunk is intentionally limited to singleplayer and examines
  only the player's current loaded chunk. It reports a simple local
  container-density heuristic; it is not a definitive detector.

Controls:
- Right Shift opens/closes the ClickGUI.
- Click a module row to toggle it.
- Drag category headers to move panels.

Build:
- GitHub Actions workflow: "Build sloppd client 1.21.11".
- On a successful run, download the "sloppd-client-1.21.11" artifact.
- Extract the ZIP and place the regular mod JAR (not the sources JAR)
  in the Fabric instance's mods folder.

Notes:
- HUD modules are informational only; no combat automation is included.
- A successful compile confirms the code builds, not that every module
  has been verified in a live Minecraft session.
