-- 팀 공통코드 구조 기반 기본 코드
-- GENERATED ALWAYS AS IDENTITY 컬럼인 id를 직접 지정하지 않고,
-- 부모 코드를 조회하여 자식 코드를 적재합니다.

-- 상위 코드
INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order, description, state)
VALUES
    (NULL, 'PLACE_CATEGORY',   '장소 분류',   1, 1, NULL, 'ACTIVE'),
    (NULL, 'PARKING_CATEGORY', '주차장 분류', 1, 2, NULL, 'ACTIVE'),
    (NULL, 'PARKING_TYPE',     '주차장 유형',  1, 3, NULL, 'ACTIVE'),
    (NULL, 'USER_ROLE',        '사용자 권한',  1, 4, NULL, 'ACTIVE');

-- 장소 분류
INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order, description, state)
SELECT p.id, v.code, v.name, 2, v.sort_order, NULL, 'ACTIVE'
FROM tb_cmn_code p
CROSS JOIN (
    VALUES
        ('RESTAURANT', '음식점', 1),
        ('CAFE',       '카페',   2),
        ('HOSPITAL',   '병원',   3),
        ('SCHOOL',     '학교',   4),
        ('ETC',        '기타',  99)
) AS v(code, name, sort_order)
WHERE p.parent_id IS NULL
  AND p.code = 'PLACE_CATEGORY';

-- 주차장 분류: 전국주차장정보표준데이터 '주차장구분'
INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order, description, state)
SELECT p.id, v.code, v.name, 2, v.sort_order, NULL, 'ACTIVE'
FROM tb_cmn_code p
CROSS JOIN (
    VALUES
        ('PUBLIC',  '공영', 1),
        ('PRIVATE', '민영', 2)
) AS v(code, name, sort_order)
WHERE p.parent_id IS NULL
  AND p.code = 'PARKING_CATEGORY';

-- 주차장 유형: 전국주차장정보표준데이터 '주차장유형'
INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order, description, state)
SELECT p.id, v.code, v.name, 2, v.sort_order, NULL, 'ACTIVE'
FROM tb_cmn_code p
CROSS JOIN (
    VALUES
        ('OFF_STREET', '노외', 1),
        ('ON_STREET',  '노상', 2),
        ('ATTACHED',   '부설', 3)
) AS v(code, name, sort_order)
WHERE p.parent_id IS NULL
  AND p.code = 'PARKING_TYPE';

-- 사용자 권한
INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order, description, state)
SELECT p.id, v.code, v.name, 2, v.sort_order, NULL, 'ACTIVE'
FROM tb_cmn_code p
CROSS JOIN (
    VALUES
        ('CLIENT',      '일반 사용자', 1),
        ('ADMIN',       '관리자',      2),
        ('SUPER_ADMIN', '최고 관리자', 3)
) AS v(code, name, sort_order)
WHERE p.parent_id IS NULL
  AND p.code = 'USER_ROLE';
