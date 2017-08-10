CREATE TABLE `tb_character` (
  `id`             INT(11)      NOT NULL AUTO_INCREMENT,
  `character_name` VARCHAR(63)  NOT NULL,
  `image_name`     VARCHAR(255) NOT NULL,
  `quality`        VARCHAR(7)   NOT NULL,
  `update_time`    DATETIME     NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `tb_character_distribution` (
  `id`              INT(11)     NOT NULL AUTO_INCREMENT,
  `chapter_name`    VARCHAR(31) NOT NULL,
  `battle_name`     VARCHAR(31) NOT NULL,
  `round_name`      VARCHAR(31) NOT NULL,
  `character_name`  VARCHAR(31) NOT NULL,
  `character_count` TINYINT(4)  NOT NULL,
  `update_time`     DATETIME    NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `tb_msg` (
  `id`       INT(11) NOT NULL AUTO_INCREMENT
  COMMENT '主键ID',
  `username` VARCHAR(255)     DEFAULT NULL,
  `email`    VARCHAR(255)     DEFAULT NULL,
  `msg`      TEXT,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `tb_tip` (
  `id`          INT(11)      NOT NULL AUTO_INCREMENT,
  `content`     VARCHAR(255) NOT NULL,
  `ref`         VARCHAR(63)  NOT NULL,
  `update_time` DATETIME     NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
