CREATE TABLE IF NOT EXISTS tb_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role TINYINT NOT NULL DEFAULT 0 COMMENT '0=client, 1=admin, 2=superadmin',
    state TINYINT NOT NULL DEFAULT 1 COMMENT '0=inactive, 1=active, 2=suspended',
    password VARCHAR(255) NOT NULL,
    img_url VARCHAR(255) DEFAULT NULL,
    c_date BIGINT NOT NULL,
	u_date BIGINT NOT NULL,

    CONSTRAINT uk_user_username UNIQUE (username),
    CONSTRAINT uk_user_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;