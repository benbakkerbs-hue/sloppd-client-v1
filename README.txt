sloppd client 1.0.2 — Minecraft Java 1.21.11 / Fabric

This is an editable source project, not a precompiled JAR.

Target setup:
- Minecraft Java Edition 1.21.11
- Fabric Loader 0.18.1+
- Java 21
- Fabric API 0.141.6+1.21.11
- Yarn mappings 1.21.11+build.3
- Fabric Loom remap plugin 1.14.10

Rendering port:
- Uses Fabric's 1.21.11 world-render events in the `rendering.v1.world`
  package.
- Reads world/entity/block data in END_EXTRACTION.
- Stores immutable frame snapshots.
- Draws those snapshots in BEFORE_DEBUG_RENDER using the new
  WorldRenderContext matrices/consumer accessors.

Build via GitHub Actions:
1. Create a GitHub repository.
2. Upload the CONTENTS of this folder to the repository root.
3. Open Actions.
4. Select "Build sloppd client 1.21.11".
5. Choose Run workflow.
6. When the workflow succeeds, download the
   "sloppd-client-1.21.11" artifact.
7. Place the mod JAR in your Fabric instance's mods folder.

In game:
- Right Shift opens/closes the ClickGUI.
- The project source remains editable; rebuild after changes.

Verification:
- Source and dependency configuration have been ported for 1.21.11.
- This environment did not run Gradle, so no successful compile or
  in-game launch has been confirmed yet.
- If GitHub Actions reports a compile error, share the log and the
  failing source can be corrected.
