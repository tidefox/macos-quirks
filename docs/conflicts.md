# Conflict check (spec §3 step 1): PENDING

**Status:** waiting for the jars. Please upload the client pack's `mcmouser-1.12.2-forge-1.0.3.jar`,
`MouseTweaks-2.10.1`, `Controlling-3.0.12.4`, the private Retina fix jar, and any sound mod in the
pack (Sound Physics, Dynamic Surroundings, AmbientSounds…).

## What Client Quirks patches (compare against this)

| Class | Member | How | Fix |
|---|---|---|---|
| `net.minecraft.client.Minecraft` | `processKeyBinds` (`func_184117_aA`): the `GuiScreen.isCtrlKeyDown()` call | `@WrapOperation` | §1.1 |
| `net.minecraft.client.Minecraft` | `runGameLoop` (`func_71411_J`): the `Display.isCloseRequested()` call | `@WrapOperation` | §1.3 |
| `net.minecraft.client.gui.inventory.GuiContainer` | `keyTyped` (`func_73869_a`): the `isCtrlKeyDown()` call | `@WrapOperation` | §1.1 |
| `net.minecraft.client.settings.KeyBinding` | `updateKeyBindState` (`func_186704_a`): `Keyboard.isKeyDown` | `@WrapOperation` | §1.4 |
| `net.minecraft.client.audio.SoundHandler` | `sndManager` (`field_147694_f`) | `@Accessor` | §1.2 |
| `net.minecraft.client.audio.SoundManager` | `loaded` (`field_148617_f`) | `@Accessor` | §1.2 |
| (Forge events) | `ClientTickEvent`, `KeyInputEvent`, `GuiScreenEvent.{KeyboardInputEvent.Pre, InitGuiEvent.Post, ActionPerformedEvent.Post}` on `GuiScreenOptionsSounds` | event handlers | §1.2, §1.4 |

`@WrapOperation` chains with other mods' `@WrapOperation`s on the same call. It only clashes with an
`@Redirect` or `@Overwrite` of the same call or method, or an ASM transformer that removes it.
In those cases our fix logs `Mixin apply failed` and turns itself off; the game still starts.

## Per jar (to fill in)

| Jar | Classes / methods patched | Overlap | Decision |
|---|---|---|---|
| mcmouser 1.0.3 | | | |
| MouseTweaks 2.10.1 | | | |
| Controlling 3.0.12.4 | | | |
| Retina fix (private) | | | |
| sound mods | | | |
