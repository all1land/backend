-- 지도 화면 경계 조회는 경위도 직사각형을 사용하므로 geometry 표현식 인덱스를 추가한다.
CREATE INDEX idx_place_active_location_geometry
    ON tb_place USING GIST ((location::geometry))
    WHERE deleted_at IS NULL;
