#!/system/bin/sh
# Watch only the imported ARM64 overlay. Never signal the game.
pid="$1"
deadline="$2"
expected="$3"
target="/data/local/tmp/zig_liberdade_exec"
hashfile="/data/local/tmp/zaid_key.sha256"
log="/data/local/tmp/zaid_license_watch.log"
case "$pid" in ''|*[!0-9]*) exit 2 ;; esac
case "$deadline" in ''|*[!0-9]*) exit 2 ;; esac
case "$expected" in ''|*[!0-9a-f]*) exit 2 ;; esac

same_process() {
    [ -d "/proc/$pid" ] || return 1
    ex="$(readlink "/proc/$pid/exe" 2>/dev/null)"
    [ "$ex" = "$target" ] || [ "$ex" = "$target (deleted)" ]
}
close_overlay() {
    same_process || return 0
    kill -TERM "$pid" 2>/dev/null || :
    sleep 1
    same_process && kill -KILL "$pid" 2>/dev/null || :
}
while same_process; do
    now="$(/system/bin/date +%s 2>/dev/null)" || now=''
    case "$now" in ''|*[!0-9]*)
        echo "clock_error; closing" >> "$log"
        close_overlay
        exit 1
        ;;
    esac
    if [ "$now" -ge "$deadline" ]; then
        echo "license_expired; closing" >> "$log"
        close_overlay
        exit 0
    fi
    actual="$(cat "$hashfile" 2>/dev/null)" || actual=''
    if [ "$actual" != "$expected" ]; then
        echo "license_removed; closing" >> "$log"
        close_overlay
        exit 0
    fi
    remaining=$((deadline - now))
    if [ "$remaining" -le 60 ]; then sleep 1; else sleep 5; fi
done
