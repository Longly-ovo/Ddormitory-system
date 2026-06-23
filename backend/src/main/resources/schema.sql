CREATE TABLE IF NOT EXISTS `user` (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'ADMIN',
    enabled TINYINT(1) NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SET @role_column_exists = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'user' AND COLUMN_NAME = 'role'
);
SET @add_role_sql = IF(
    @role_column_exists = 0,
    'ALTER TABLE `user` ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT ''ADMIN'' AFTER nickname',
    'SELECT 1'
);
PREPARE add_role_statement FROM @add_role_sql;
EXECUTE add_role_statement;
DEALLOCATE PREPARE add_role_statement;
UPDATE `user` SET role = 'ADMIN' WHERE role IS NULL OR role = '';

CREATE TABLE IF NOT EXISTS building (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS floor (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    building_id BIGINT NOT NULL,
    floor_no INT NOT NULL,
    name VARCHAR(50) NOT NULL,
    CONSTRAINT uk_floor_building_no UNIQUE (building_id, floor_no),
    CONSTRAINT fk_floor_building FOREIGN KEY (building_id) REFERENCES building(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    floor_id BIGINT NOT NULL,
    room_no VARCHAR(20) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    CONSTRAINT uk_room_floor_no UNIQUE (floor_id, room_no),
    CONSTRAINT fk_room_floor FOREIGN KEY (floor_id) REFERENCES floor(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS student (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_no VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    gender VARCHAR(10),
    phone VARCHAR(30),
    college VARCHAR(100),
    major VARCHAR(100),
    class_name VARCHAR(100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS bed (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    bed_no VARCHAR(20) NOT NULL,
    student_id BIGINT NULL,
    CONSTRAINT uk_bed_room_no UNIQUE (room_id, bed_no),
    CONSTRAINT uk_bed_student UNIQUE (student_id),
    CONSTRAINT fk_bed_room FOREIGN KEY (room_id) REFERENCES room(id) ON DELETE RESTRICT,
    CONSTRAINT fk_bed_student FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
