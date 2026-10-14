#!/system/bin/sh
# Invoked after the Android app has checked Supabase over HTTPS.
src="$1"
deadline="$2"
hash="$3"
watcher="$4"
target="/data/local/tmp/zig_liberdade_exec"
pidfile="/data/local/tmp/zaid_overlay.pid"
hashfile="/data/local/tmp/zaid_key.sha256"
log="/data/local/tmp/overlay.log"
watchlog="/data/local/tmp/zaid_license_watch.log"
case "$deadline" in ''|*[!0-9]*) echo 'Invalid expiry'; exit 2 ;; esac
case "$hash" in ''|*[!0-9a-f]*) echo 'Invalid hash'; exit 2 ;; esac
[ "${#hash}" -eq 64 ] || exit 2
[ -r "$src" ] && [ -r "$watcher" ] || { echo 'Missing imported ELF or watcher'; exit 3; }
now="$(/system/bin/date +%s)"
[ "$now" -lt "$deadline" ] || { echo 'License expired'; exit 4; }
pm path com.dts.freefireth >/dev/null 2>&1 || { echo 'Free Fire package com.dts.freefireth not installed'; exit 5; }

cp "$src" "$target.tmp" || exit 6
chmod 700 "$target.tmp" || exit 6
mv -f "$target.tmp" "$target" || exit 6
umask 077
printf '%s\n' "$hash" > "$hashfile" || exit 6

if command -v setsid >/dev/null 2>&1 && command -v nohup >/dev/null 2>&1; then
    setsid nohup "$target" > "$log" 2>&1 < /dev/null &
elif command -v nohup >/dev/null 2>&1; then
    nohup "$target" > "$log" 2>&1 < /dev/null &
else
    "$target" > "$log" 2>&1 < /dev/null &
fi
pid="$!"
sleep 1
matches() {
    testpid="$1"
    [ -d "/proc/$testpid" ] || return 1
    actual="$(readlink "/proc/$testpid/exe" 2>/dev/null)" || actual=''
    [ "$actual" = "$target" ] || [ "$actual" = "$target (deleted)" ]
}
if ! matches "$pid"; then
    pid=''
    for p in /proc/[0-9]*; do
        testpid="${p#/proc/}"
        if matches "$testpid"; then pid="$testpid"; break; fi
    done
fi
[ -n "$pid" ] || { echo 'Overlay failed to launch; see overlay.log'; exit 7; }
printf '%s\n' "$pid" > "$pidfile" || exit 8
nohup /system/bin/sh "$watcher" "$pid" "$deadline" "$hash" >> "$watchlog" 2>&1 < /dev/null &
watchpid="$!"
sleep 1
if ! kill -0 "$watchpid" 2>/dev/null; then
    kill -TERM "$pid" 2>/dev/null || :
    echo 'Expiry watcher failed; stopped overlay'
    exit 9
fi
echo "Overlay running PID=$pid; watcher PID=$watchpid"
monkey -p com.dts.freefireth -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1 || :
