#!/system/bin/sh
target="/data/local/tmp/zig_liberdade_exec"
pidfile="/data/local/tmp/zaid_overlay.pid"
hashfile="/data/local/tmp/zaid_key.sha256"
pid="$(cat "$pidfile" 2>/dev/null)" || pid=''
case "$pid" in ''|*[!0-9]*) pid='' ;; esac
if [ -n "$pid" ]; then
    actual="$(readlink "/proc/$pid/exe" 2>/dev/null)" || actual=''
    if [ "$actual" = "$target" ] || [ "$actual" = "$target (deleted)" ]; then
        kill -TERM "$pid" 2>/dev/null || :
        sleep 1
        actual="$(readlink "/proc/$pid/exe" 2>/dev/null)" || actual=''
        if [ "$actual" = "$target" ] || [ "$actual" = "$target (deleted)" ]; then
            kill -KILL "$pid" 2>/dev/null || :
        fi
    fi
fi
rm -f "$pidfile" "$hashfile"
echo 'Overlay detenido (si estaba activo).'
