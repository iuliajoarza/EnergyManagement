#!/usr/bin/env bash
# Usage: ./run_simulator.sh <DEVICE_ID>
set -euo pipefail
if [ $# -lt 1 ]; then
  echo "Usage: $0 <DEVICE_ID>" >&2
  exit 1
fi
DEVICE_ID="$1"
IMAGE="ds2025_30242_joarza_iulia_maria-simulator"
NETWORK="ds2025_30242_joarza_iulia_maria_proxy-network"
CONTAINER_NAME="sim_${DEVICE_ID}"

# Verify device exists in microserviceDevice DB before starting
echo "Verifying device ${DEVICE_ID} exists in testdb2.device..."
EXISTS=$(docker exec db2 psql -U iulia -d testdb2 -t -A -c "SELECT COUNT(*) FROM device WHERE id = '${DEVICE_ID}';" | tr -d '\r' | tr -d ' ')
if [ -z "$EXISTS" ] || [ "$EXISTS" = "0" ]; then
  echo "Error: Device ${DEVICE_ID} does not exist in testdb2.device. Refusing to start simulation." >&2
  echo "Hint: Check available devices via: docker exec db2 psql -U iulia -d testdb2 -c \"SELECT id, name FROM device;\"" >&2
  exit 1
fi

# Run detached container; remove container name if already exists
if docker ps -a --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
  echo "Container ${CONTAINER_NAME} already exists. Removing..."
  docker rm -f "${CONTAINER_NAME}" >/dev/null 2>&1 || true
fi

echo "Starting simulator for device ${DEVICE_ID}..."
docker run -d --name "${CONTAINER_NAME}" --network "${NETWORK}" -e SIM_DEVICE_ID="${DEVICE_ID}" "${IMAGE}" python -u simulator.py --device "${DEVICE_ID}"

echo "Tail logs with: docker logs -f ${CONTAINER_NAME}"