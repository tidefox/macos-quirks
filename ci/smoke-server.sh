#!/usr/bin/env bash
# Boots a real Forge 1.12.2 dedicated server with FermiumBooter + Client Quirks and
# proves the mod is harmless there: the coremod must not enqueue its (client-only)
# mixins, FML must not load the @Mod, and the log must be free of errors.
#
# usage: ci/smoke-server.sh <server-dir> <clientquirks.jar> <fermiumbooter.jar>
#   <server-dir> must contain an installed Forge server (forge-*.jar + libraries/).
set -uo pipefail

DIR="$(readlink -f "$1")"; MOD="$(readlink -f "$2")"; FERMIUM="$(readlink -f "$3")"
HERE="$(cd "$(dirname "$0")" && pwd)"
BOOT_TIMEOUT="${BOOT_TIMEOUT:-600}"

for f in "$MOD" "$FERMIUM"; do
  [ -f "$f" ] || { echo "::error::missing jar: $f"; exit 1; }
done
[ -d "$DIR" ] || { echo "::error::missing server dir: $DIR"; exit 1; }

cd "$DIR"
rm -rf mods logs world config crash-reports
mkdir -p mods
cp "$FERMIUM" mods/ && cp "$MOD" mods/ || { echo "::error::could not copy jars into mods/"; exit 1; }
echo "eula=true" > eula.txt
# No Forge update check: it needs the network and its failure is a stack trace.
mkdir -p config
printf 'version_checking {\n    B:Global=false\n}\n' > config/forge.cfg
cat > server.properties <<'PROPS'
online-mode=false
level-type=FLAT
generate-structures=false
spawn-npcs=false
spawn-animals=false
spawn-monsters=false
view-distance=3
max-tick-time=-1
server-port=25565
snooper-enabled=false
motd=clientquirks smoke test
PROPS

FORGE_JAR=$(ls forge-1.12.2-*.jar | grep -v installer | head -1)
echo "Starting $FORGE_JAR with mods: $(ls mods | tr '\n' ' ')"

rm -f stdin.fifo; mkfifo stdin.fifo
java -Xms1G -Xmx2G -Dfml.queryResult=confirm -jar "$FORGE_JAR" nogui < stdin.fifo > console.log 2>&1 &
PID=$!
exec 3> stdin.fifo

LOG=console.log
status=0
t=0
while ! grep -qE 'Done \([0-9.]+s\)! For help' "$LOG" 2>/dev/null; do
  if ! kill -0 "$PID" 2>/dev/null; then echo "::error::server exited before it was up"; status=1; break; fi
  sleep 2; t=$((t+2))
  if [ "$t" -ge "$BOOT_TIMEOUT" ]; then echo "::error::server did not come up in ${BOOT_TIMEOUT}s"; status=1; break; fi
done

echo "stop" >&3
for _ in $(seq 1 90); do kill -0 "$PID" 2>/dev/null || break; sleep 2; done
if kill -0 "$PID" 2>/dev/null; then echo "::error::server did not stop"; kill -9 "$PID"; status=1; fi
exec 3>&-
wait "$PID" 2>/dev/null

echo "---------------- checks ----------------"
ALL=$(mktemp)    # what to look for / forbid: includes debug.log
INFO=$(mktemp)   # what to scan for errors: INFO and above only (debug.log TRACE names exception classes)
cat console.log logs/latest.log 2>/dev/null > "$INFO"
cat "$INFO" logs/debug.log 2>/dev/null > "$ALL"

require() {
  if grep -qE "$1" "$ALL"; then echo "ok:   $2"; else echo "::error::missing: $2 ($1)"; status=1; fi
}
forbid() {
  if grep -qE "$1" "$ALL"; then echo "::error::unexpected: $2 ($1)"; grep -nE "$1" "$ALL" | head -5; status=1; else echo "ok:   $2"; fi
}
require 'Dedicated server: Client Quirks is client-only, not enqueueing mixins\.clientquirks\.json' "coremod stayed out of the server"
forbid  'Enqueued mixins\.clientquirks\.json' "no client mixins enqueued on the server"
forbid  'Mixin applied: io\.github\.tidefox\.clientquirks' "no client mixin applied on the server"
forbid  'Client Quirks .* ready' "@Mod not initialised on the server (clientSideOnly)"
require 'Disabling mod clientquirks it is client side only' "FML disabled the @Mod (clientSideOnly)"
require 'Stopping server' "server stopped cleanly"

BAD=$(grep -nE 'Mixin(Apply|Transformer)Error|InvalidMixinException|InvalidInjectionException|InvalidAccessorException|Mixin apply.*failed|/(ERROR|FATAL)\]|Exception|^\s+at [a-zA-Z_$]' "$INFO" \
      | grep -vEf "$HERE/log-allowlist.txt" || true)
if [ -n "$BAD" ]; then
  echo "::error::problems found in server log:"
  echo "$BAD" | head -80
  status=1
else
  echo "ok:   no mixin errors or exceptions in log"
fi
if [ -f logs/fermiumbooter.log ] && grep -qE 'Mixin Error' logs/fermiumbooter.log; then
  echo "::error::logs/fermiumbooter.log reports mixin errors:"; head -60 logs/fermiumbooter.log; status=1
fi
[ -d crash-reports ] && { echo "::error::crash report written"; cat crash-reports/* | head -80; status=1; }

exit $status
