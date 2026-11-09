# Release 1: test plan for the Mac (spec §3 step 4)

CI already proves that the mod loads on a real 1.12.2 client and that every mixin applies there.
It also proves that the sound system reloads and that a server ignores the mod.
It **can't** test how macOS itself behaves. That's this list.

**Setup:** Dregora instance with `clientquirks-<version>.jar` in `mods/` and the default config.
Before you start, please note your launcher, Intel or Apple Silicon, the Java version, and the
`LWJGL Version:` line from `logs/latest.log`.
After testing, send `logs/latest.log` (search for `clientquirks`).

## A. Ctrl+Q drops the stack (§1.1)
| # | Do | Expect |
|---|---|---|
| A1 | In the world, hold a stack, press **Q** | drops **one** item |
| A2 | **Ctrl+Q** | drops the **whole stack** |
| A3 | **Cmd+Q** | drops the whole stack, and the game **does not** quit (see C) |
| A4 | Open inventory, hover a stack, **Ctrl+Q** | whole stack thrown from that slot |
| A5 | Same, plain **Q** | one item |
| A6 | Chat: type text, **Cmd+A**, **Cmd+C**, **Cmd+V** | select/copy/paste still work |
| A7 | Chat: **Ctrl+A / Ctrl+C** | whatever vanilla did before (not changed) |
| A8 | If Controlling is installed and Drop is bound with a modifier: try it | report what happens |

## B. Sound device (§1.2)
| # | Do | Expect |
|---|---|---|
| B1 | Music playing on speakers, connect **AirPods** (they become the default output) | within ~3 s, sound moves to AirPods and the toast says *Sound device reloaded* |
| B2 | Disconnect AirPods | back on speakers within ~3 s |
| B3 | Switch the output in **Control Center** without unplugging anything | follows |
| B4 | *Options → Music & Sounds → Reload sound device* | short gap, sound continues on the current device |
| B5 | Bind *Controls → Client Quirks → Reload sound device*, press it in-world and on a menu | same as B4 |
| B6 | Set `audio.autoSwitch=false` and repeat B1 | no auto reload, but B4 still works |
| B7 | `logs/latest.log` | `Automatic sound device switching uses CoreAudio` (**if it says OpenAL, JNA failed: please send the warning line**) |

Also tell us: wired or Bluetooth, and **before** this mod, did the game go silent or keep playing on the old device? (spec §4)

## C. Cmd+Q asks first (§1.3)
| # | Do | Expect |
|---|---|---|
| C1 | In a single-player world, press **Cmd+Q** | *Quit Minecraft?* screen, game paused |
| C2 | *Cancel* | back to where you were |
| C3 | **Cmd+Q**, then **Cmd+Q** again | quits; the world is saved (rejoin to check) |
| C4 | **Cmd+Q**, then *Quit Game* | quits, world saved |
| C5 | Red window button in a world | same prompt |
| C6 | Main menu, **Cmd+Q** | quits immediately |
| C7 | On a server, **Cmd+Q** | prompt says *You will be disconnected* |
| C8 | Press Cmd+Q once and don't touch anything | the prompt must **not** reopen by itself every frame (needs LWJGL to clear the close flag) |

## D. Stuck keys (§1.4)
**First, without the mod** (or with `focus.resyncKeys=false`), check whether the bug really happens:
| # | Do | Note |
|---|---|---|
| D0a | In world, **Cmd+Tab** out, come back, press **Q** | one item, or the whole stack? |
| D0b | Hold **W**, **Cmd+Tab** out, release W, come back | still walking? |

**Then with the mod (default config):**
| # | Do | Expect |
|---|---|---|
| D1 | Repeat D0a | one item |
| D2 | Repeat D0b | not walking |
| D3 | Cmd+Tab back while **still holding W** | walks after you press W again (one press) |
| D4 | Cmd+Tab away and back several times, then Ctrl+Q and Cmd+Q | A2 and A3 still work |

## E. General
- Game starts, *Mods → Client Quirks → Config* opens, changes apply without restart.
- With Mouse Tweaks, Controlling and mcmouser installed, nothing obvious breaks: inventory scrolling, key binding screen.
