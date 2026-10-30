# 좌표 기반 주차장 추천

## API
POST /api/routes/integrated/recommendations
Content-Type: application/json

## 요청
- origin: 사용자 출발 좌표, latitude/longitude
- departurePoi: 출발 측 POI 좌표, latitude/longitude
- destination: 목적지 좌표, latitude/longitude
- radius: 필수, 0 초과~5000 미터
- limit: 필수, 1~10

## 추천 기준
- 목적지 반경 내 모든 후보의 기존 통합 경로를 계산한다.
- 전체 시간, 전체 거리, 주차장 ID 오름차순으로 정렬한다.
- 전체 후보 평가 후 limit을 적용한다.
- 평가 후보는 최대 50개이며 초과하면 COMMON_400_001로 실패한다.
- 후보가 없으면 빈 목록을 성공 응답으로 반환한다.
- 후보 경로 하나라도 실패하면 기존 오류를 전달하고 전체 요청을 실패 처리한다.
- 실패 후보를 조용히 제외하거나 부분 추천을 반환하지 않는다.

## 응답 data
- candidateCount: 조회 후보 수
- evaluatedCount: 정상 계산한 후보 수
- recommendations: 추천 순위 목록
- 각 항목: rank, parkingId, name, totalDistanceMeters, totalDurationSeconds

## 경로 표시
선택한 recommendations 항목의 parkingId를 기존 통합 경로 API에 전달한다.
POST /api/routes/integrated/coordinates

## 오류
- 좌표 및 반경/limit 검증: COMMON_400_002
- 후보 수 초과: COMMON_400_001
- 실제 경로 실패: 기존 WALKING_ROUTE 또는 DRIVING_ROUTE 오류 전달

## 제한
- 실시간 주차 가능 여부, 주차 요금, 실시간 교통 상황을 추천에 반영하지 않는다.
- 최적은 검색 반경 내 후보 중 자체 예상 전체 이동 시간이 최소라는 의미이다.
- 출발 측 POI는 호출자가 좌표를 제공한다.
- 순차 계산 방식이며 후보 수에 따라 지연이 증가할 수 있다.
- TMAP은 비교 검증용이며 추천 계산에는 사용하지 않는다.
- DB 마이그레이션 및 기존 도보/차량 알고리즘 변경 없음

## 자동 테스트
    .\gradlew.bat test

## 실제 실행 검증
8082번 서버를 실행한 후 수행한다.
    node scripts/verify-route-completion.mjs

결과는 build/route-completion과 프로젝트 밖 route-comparison-results에 저장한다.
실제 실행 전에는 완료로 기록하지 않으며 실패 결과도 보존한다.
