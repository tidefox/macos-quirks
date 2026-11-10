#!/usr/bin/env bash
# Boots the real (obfuscated) Forge 1.12.2 client with FermiumBooter + the release jar
# on a virtual display, lets the self-test reload the sound system, and checks that
# every mixin applied. Needs Xvfb, xrandr (LWJGL2 lists display modes with it) and Mesa.
#
# usage: ci/smoke-client.sh [extra gradle args...]
set -uo pipefail
cd "$(dirname "$0")/.."
LOG="${CLIENT_LOG:-build/smoke-client.log}"
mkdir -p "$(dirname "$LOG")"
rm -rf run/obfuscated/logs run/obfuscated/crash-reports

timeout "${CLIENT_TIMEOUT:-900}" xvfb-run -a -s "-screen 0 1280x720x24 +extension RANDR" \
  ./gradlew runObfClient -Pselftest "$@" > "$LOG" 2>&1
rc=$?
echo "runObfClient exit code: $rc"

ALL=$(mktemp)
cat "$LOG" run/obfuscated/logs/latest.log 2>/dev/null > "$ALL"
status=0
require() {
  if grep -qE "$1" "$ALL"; then echo "ok:   $2"; else echo "::error::missing: $2 ($1)"; status=1; fi
}
require '\[clientquirks\]: (Enqueued|Added) mixins\.clientquirks\.json' "mixin config handed to Mixin"
for m in MixinMinecraft MixinGuiContainer MixinKeyBinding SoundHandlerAccessor SoundManagerAccessor; do
  require "Mixin applied: io\.github\.tidefox\.clientquirks\.mixin\.$m" "$m applied"
done
require 'Client Quirks .* ready' "@Mod initialised"
require '\[selftest\] sound reload OK' "sound system reloaded through the accessor"
require '\[selftest\] done, quitting' "client quit normally"

BAD=$(grep -nE 'Mixin(Apply|Transformer)Error|InvalidMixinException|InvalidInjectionException|InvalidAccessorException|Critical injection failure|\[selftest\].*did not|Game crashed' "$ALL" || true)
if [ -n "$BAD" ]; then echo "::error::problems in client log:"; echo "$BAD" | head -40; status=1; else echo "ok:   no mixin errors"; fi
# Any exception mentioning our package is ours to fix.
OURS=$(grep -nE -B2 -A8 'Exception' "$ALL" | grep -E 'io\.github\.tidefox\.clientquirks' | grep -vE 'Mixin applied|\[clientquirks\]' || true)
if [ -n "$OURS" ]; then echo "::error::exception involving clientquirks:"; echo "$OURS" | head -40; status=1; fi
[ -d run/obfuscated/crash-reports ] && { echo "::error::crash report written"; head -80 run/obfuscated/crash-reports/*; status=1; }
exit $status
