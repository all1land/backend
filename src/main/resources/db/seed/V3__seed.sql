INSERT INTO tb_user (username, email, role, state, password, img_url, c_date, u_date)
VALUES ('guest', 'guest@email.com', 0, 1, 'guest', NULL, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());

INSERT INTO tb_user (username, email, role, state, password, img_url, c_date, u_date)
VALUES ('admin', 'admin@email.com', 1, 1, 'admin', NULL, UNIX_TIMESTAMP(), UNIX_TIMESTAMP());