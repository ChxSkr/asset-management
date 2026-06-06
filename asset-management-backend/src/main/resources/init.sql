-- ============================================
-- 数字资产管理系统 (DAM) 数据库初始化脚本
-- 数据库: dam_db
-- ============================================

CREATE DATABASE IF NOT EXISTS `dam_db` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `dam_db`;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
  `role` VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN/USER',
  `real_name` VARCHAR(50) NOT NULL COMMENT '真实姓名',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '电话',
  `avatar_url` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1正常/0禁用',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 资产表
-- ============================================
CREATE TABLE IF NOT EXISTS `asset` (
  `asset_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '资产ID',
  `asset_name` VARCHAR(200) NOT NULL COMMENT '资产名称',
  `description` TEXT COMMENT '描述',
  `file_type` VARCHAR(20) NOT NULL COMMENT '文件类型',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
  `upload_user_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `category_id` BIGINT DEFAULT NULL COMMENT '分类ID',
  `current_version` INT NOT NULL DEFAULT 1 COMMENT '当前版本号',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '是否删除：0正常/1已删除',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`asset_id`),
  KEY `idx_upload_user` (`upload_user_id`),
  KEY `idx_category` (`category_id`),
  KEY `idx_is_deleted` (`is_deleted`),
  KEY `idx_file_type` (`file_type`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产表';

-- ============================================
-- 3. 分类表
-- ============================================
CREATE TABLE IF NOT EXISTS `category` (
  `category_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `category_name` VARCHAR(100) NOT NULL COMMENT '分类名称',
  `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父分类ID，0表示根分类',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序号',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================
-- 4. 标签表
-- ============================================
CREATE TABLE IF NOT EXISTS `tag` (
  `tag_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
  `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`tag_id`),
  UNIQUE KEY `uk_tag_name` (`tag_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

-- ============================================
-- 5. 资产-标签关联表
-- ============================================
CREATE TABLE IF NOT EXISTS `asset_tag` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产ID',
  `tag_id` BIGINT NOT NULL COMMENT '标签ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_asset_tag` (`asset_id`, `tag_id`),
  KEY `idx_tag_id` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产-标签关联表';

-- ============================================
-- 6. 资产版本表
-- ============================================
CREATE TABLE IF NOT EXISTS `asset_version` (
  `version_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '版本ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产ID',
  `version_number` INT NOT NULL COMMENT '版本号',
  `file_size` BIGINT NOT NULL COMMENT '文件大小（字节）',
  `storage_path` VARCHAR(500) NOT NULL COMMENT '存储路径',
  `upload_user_id` BIGINT NOT NULL COMMENT '上传用户ID',
  `version_note` VARCHAR(500) DEFAULT NULL COMMENT '版本说明',
  `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`version_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_version_number` (`asset_id`, `version_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资产版本表';

-- ============================================
-- 7. 下载记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `download_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `asset_id` BIGINT NOT NULL COMMENT '资产ID',
  `user_id` BIGINT NOT NULL COMMENT '下载用户ID',
  `download_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  PRIMARY KEY (`log_id`),
  KEY `idx_asset_id` (`asset_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='下载记录表';

-- ============================================
-- 8. 操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `operation_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT NOT NULL COMMENT '操作用户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `description` VARCHAR(500) NOT NULL COMMENT '操作描述',
  `operation_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_time` (`operation_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- ============================================
-- 初始化数据：管理员账号通过系统注册功能创建
-- 或手动执行以下SQL（需将密码替换为BCrypt加密后的值）
-- BCrypt在线生成: https://www.bcrypt-generator.com/
-- ============================================
-- INSERT INTO `user` (`username`, `password`, `role`, `real_name`, `email`, `status`)
-- VALUES ('admin', 'BCRYPT_HASH_HERE', 'ADMIN', '系统管理员', 'admin@dam.com', 1);