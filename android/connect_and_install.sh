#!/usr/bin/env bash
# Pair, connect, and install Aria on a device over Wi-Fi debugging.
#
# Usage:
#   ./connect_and_install.sh <pair_ip:port> <pairing_code> <connect_ip:port> [backend_url]
#
# Where to find the values (Settings -> Developer options -> Wireless debugging):
#   pair_ip:port    -> shown after tapping "Pair device with pairing code"
#   pairing_code    -> the 6-digit code shown on that same screen
#   connect_ip:port -> shown on the main "Wireless debugging" screen (this is the
#                      address adb actually connects to once paired)
#   backend_url     -> optional, e.g. http://192.168.1.50:3000 (your computer's LAN IP
#                      running the backend). Defaults to http://10.0.2.2:3000, which
#                      only works in the emulator, NOT on a real device.
#
# Requires: adb on PATH, this script run from a machine on the same Wi-Fi as the phone.

set -euo pipefail

if [ "$#" -lt 3 ]; then
  echo "Usage: $0 <pair_ip:port> <pairing_code> <connect_ip:port> [backend_url]"
  exit 1
fi

PAIR_ADDR="$1"
PAIRING_CODE="$2"
CONNECT_ADDR="$3"
BACKEND_URL="${4:-}"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "Pairing with $PAIR_ADDR ..."
adb pair "$PAIR_ADDR" "$PAIRING_CODE"

echo "Connecting to $CONNECT_ADDR ..."
adb connect "$CONNECT_ADDR"

echo "Connected devices:"
adb devices

cd "$SCRIPT_DIR"
if [ -n "$BACKEND_URL" ]; then
  echo "Building and installing (backendUrl=$BACKEND_URL) ..."
  ./gradlew installDebug -PbackendUrl="$BACKEND_URL"
else
  echo "Building and installing (using default backendUrl, only works for the emulator) ..."
  ./gradlew installDebug
fi

echo "Done. Aria should now be installed on the device."
