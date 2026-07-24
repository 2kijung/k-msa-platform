#!/usr/bin/env bash
# DuckDNS 공인 IP 자동 갱신 스크립트
# 사용법: 아래 두 변수를 채운 후 실행
#   ./scripts/duckdns-update.sh

DUCKDNS_TOKEN="be0defc9-dee6-44a9-a179-d9d903c403b6"
DUCKDNS_DOMAIN="k-devops"

LOG_FILE="/tmp/duckdns.log"

RESULT=$(curl -s "https://www.duckdns.org/update?domains=${DUCKDNS_DOMAIN}&token=${DUCKDNS_TOKEN}&ip=")
echo "$(date '+%Y-%m-%d %H:%M:%S') $RESULT" >> "$LOG_FILE"

if [ "$RESULT" = "OK" ]; then
  exit 0
else
  echo "DuckDNS 업데이트 실패: $RESULT" >&2
  exit 1
fi
