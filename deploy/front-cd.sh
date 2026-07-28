#!/bin/bash

# 에러 발생 시 스크립트 실행을 즉시 중단
set -e

cd ~/front

set -a
source ~/infra/common.env
set +a
NEW_TAG="${IMAGE_TAG:-latest}"
LAST_GOOD_FILE=".last-good-tag"
OLD_TAG=$(cat "$LAST_GOOD_FILE" 2>/dev/null || echo "latest")

SERVICES=("front-1" "front-2")
PORTS=("10404" "10405")

deploy_tag() {
  local tag="$1"
  export IMAGE_TAG="$tag"
  docker compose pull

  for i in "${!SERVICES[@]}"; do
    SERVICE="${SERVICES[$i]}"
    PORT="${PORTS[$i]}"

    # nginx가 front를 직접 로드밸런싱(패시브 헬스체크)하므로 Eureka 등록/해제 절차는 더 이상 불필요 (nginx는 max_fails/fail_timeout으로 죽은 인스턴스를 스스로 우회)

    echo "${SERVICE} 재배포 (tag=${tag})"
    docker compose up -d --force-recreate "$SERVICE"

    for attempt in {1..30}; do
      if curl -sf "http://127.0.0.1:${PORT}/actuator/health" 2>/dev/null | grep -q '"status":"UP"'; then
        echo "${SERVICE} 배포 성공"
        break;
      fi

      if [ "$attempt" -eq 30 ]; then
        echo "${SERVICE} 배포 실패 (타임아웃, tag=${tag})"
        return 1;
      fi

      sleep 2;
    done
  done

  return 0
}

if deploy_tag "$NEW_TAG"; then
  echo "$NEW_TAG" > "$LAST_GOOD_FILE"
  docker image prune -f
else
  echo "새 버전(${NEW_TAG}) 배포 실패, 이전 버전(${OLD_TAG})으로 롤백"
  deploy_tag "$OLD_TAG" || echo "롤백도 실패함, 수동 확인 필요"
  docker image prune -f
  exit 1
fi
