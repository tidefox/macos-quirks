# Client Quirks: keyboard and audio fixes for Forge 1.12.2 (mostly macOS)

A **client-only** Forge 1.12.2 mod (built for RLCraft Dregora, but it has no dependency on any pack mod).
Servers never need it. Players without it are unaffected (`clientSideOnly`, `acceptableRemoteVersions = "*"`).

| Fix | Problem | Platforms |
|---|---|---|
| **Ctrl+Q drops the stack** | On macOS, vanilla only accepts **Cmd**+Q, and Cmd+Q also quits the app. Physical Ctrl+Q now drops the whole stack, both in the world and from the hovered slot in inventories. Cmd+C/V/A in text fields are untouched. | macOS |
| **Sound follows the output device** | Switching to AirPods, headphones or HDMI left sound on the old device (or silent) until restart. The mod reloads just the sound system (about 1 s, not F3+T). It does this automatically when the default output changes, or on demand with a key (unbound by default) or the **Reload sound device** button on *Music & Sounds*. | all (auto-detect best on macOS) |
| **Cmd+Q asks first** | Cmd+Q (or the red window button) closed the game straight away. In a world it now shows *Quit Minecraft?*. A second Cmd+Q, or *Quit Game*, quits normally and saves the world. In menus it quits as usual. | macOS (option for Windows/Linux) |
| **No stuck keys after Cmd+Tab** | If a key-up is lost while the window is unfocused, the game thinks Cmd, Ctrl or W is still held: you keep walking, and Q throws whole stacks. Keys that still look held when focus returns are ignored until they are actually pressed or released again. | all |

Every fix has its own toggle in `config/clientquirks.cfg`, also editable in-game: *Mods → Client Quirks → Config*.
Mac-only fixes do nothing on other systems.

## Requirements

- Minecraft 1.12.2, Forge 14.23.5.x (tested with 2860)
- **FermiumBooter ≥ 1.4.1**, which provides Mixin and MixinExtras. MixinBooter is not needed.

## Install

Drop `clientquirks-<version>.jar` into the **client's** `mods/` next to FermiumBooter.
Download it from the GitHub Release, or from the `clientquirks-jar` artifact of a CI run.
Putting it on a server does nothing (CI checks that), but there's no reason to.

## Config (`config/clientquirks.cfg`)

| Category | Option | Default | |
|---|---|---|---|
| drop | `physicalCtrl` | `true` | macOS: Ctrl counts as Cmd for "drop whole stack" |
| audio | `button` | `true` | Button on *Music & Sounds* |
| audio | `autoSwitch` | `true` | Reload when the default output device changes |
| audio | `toast` | `true` | Toast after a reload |
| audio | `debug` | `false` | Log every device check (for bug reports) |
| quit | `confirm` | `true` | macOS: confirm Cmd+Q / close while in a world |
| quit | `confirmEverywhere` | `false` | Also confirm on Windows/Linux |
| focus | `resyncKeys` | `true` | Ignore keys that look stuck after focus returns |

## How it works

Early (vanilla-target) mixins, all prefixed `cq$`, with `required: false` and `defaultRequire: 0`:
if another mod already rewrote one of these spots, that fix switches itself off and the game still starts.

| Mixin | Target | Purpose |
|---|---|---|
| `MixinMinecraft` | `processKeyBinds` → `GuiScreen.isCtrlKeyDown()` | in-world drop |
| | `runGameLoop` → `Display.isCloseRequested()` | quit confirmation |
| `MixinGuiContainer` | `keyTyped` → `isCtrlKeyDown()` (bytecode owner `GuiContainer`) | drop from slot |
| `MixinKeyBinding` | `updateKeyBindState` → `Keyboard.isKeyDown` | stale keys don't come back when you return to the game |
| `SoundHandlerAccessor`, `SoundManagerAccessor` | `sndManager`, `loaded` | sound reload |

**Sound device detection** runs on the client tick every 2 s, so it can never race a sound unload:
- **macOS:** CoreAudio `kAudioHardwarePropertyDefaultOutputDevice` through JNA. If JNA can't load (for example, a JNA without Apple Silicon natives), it falls back to the OpenAL checks below and logs a warning.
- **Windows/Linux:** OpenAL's `ALC_DEFAULT_DEVICE_SPECIFIER`, plus `ALC_EXT_disconnect` on the open device.
- A new device must be stable for 1 s, and the mod never reloads more than once every 5 s.

**Coremod loading:** if FermiumBooter is already on the classpath when our coremod is constructed, the config goes through `FermiumRegistryAPI`. Usually it isn't, because `clientquirks-*.jar` sorts before `fermiumbooter-*.jar`. In that case it is added with `Mixins.addConfiguration` in `injectData`, before Mixin selects its configs. On a dedicated server nothing is enqueued.

## Build

```bash
./gradlew build
```

Gradle runs on Java 25 (RetroFuturaGradle 2.x). Compiling uses a Java 8 toolchain, which foojay downloads if it's missing.

### Test on a real client

```bash
./gradlew runClient
```

That's the dev client. `./gradlew runObfClient` runs the release jar plus FermiumBooter like a real install.
Both pass `-Dmixin.debug.verify=true -Dmixin.debug.countInjections=true`, so an injection that misses fails loudly.

### CI

| Job | What it proves |
|---|---|
| `build` | compiles, unit tests (key, quit, drop and debounce logic), jar has refmap and manifest, bundles no Mixin |
| `smoke-client` | boots the **real obfuscated client** on Xvfb with the release jar and FermiumBooter. Every mixin must apply. A self-test (`-Pselftest`) reloads the sound system and checks it comes back, then quits |
| `smoke-server` | boots a Forge 2860 dedicated server with the jar. Coremod and `@Mod` must stay inert, with no errors in the log |

What CI can't check is how macOS itself behaves: Cmd+Q, CoreAudio, and lost key-ups.
That's what [`docs/test-plan.md`](docs/test-plan.md) is for.

## Credits

Prior art: [MacOS Input Fixes](https://github.com/hamarb123/MCMacOSInputFixes) by hamarb123 (BSD-3, Fabric 1.14+). No code is borrowed.
