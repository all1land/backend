INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order) VALUES
    (NULL, 'PLACE_CATEGORY',   '장소 분류',   1, 1),
    (NULL, 'PARKING_CATEGORY', '주차장 분류', 1, 2),
    (NULL, 'USER_ROLE',        '사용자 권한', 1, 3);

INSERT INTO tb_cmn_code (parent_id, code, name, level, sort_order)
SELECT p.id, c.code, c.name, 2, c.sort_order
FROM (VALUES
    ('PLACE_CATEGORY',   'RESTAURANT',  '음식점',      1),
    ('PLACE_CATEGORY',   'CAFE',        '카페',        2),
    ('PLACE_CATEGORY',   'HOSPITAL',    '병원',        3),
    ('PLACE_CATEGORY',   'SCHOOL',      '학교',        4),
    ('PLACE_CATEGORY',   'ETC',         '기타',        99),
    ('PARKING_CATEGORY', 'PUBLIC',      '공영',        1),
    ('PARKING_CATEGORY', 'PRIVATE',     '민영',        2),
    ('USER_ROLE',        'CLIENT',      '일반 사용자', 1),
    ('USER_ROLE',        'ADMIN',       '관리자',      2),
    ('USER_ROLE',        'SUPER_ADMIN', '최고 관리자', 3)
) AS c(parent_code, code, name, sort_order)
JOIN tb_cmn_code p
    ON p.parent_id IS NULL
   AND p.code = c.parent_code;
