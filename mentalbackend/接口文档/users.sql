/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306_1
 Source Server Type    : MySQL
 Source Server Version : 80037 (8.0.37)
 Source Host           : localhost:3306
 Source Schema         : psychological_consultation_system

 Target Server Type    : MySQL
 Target Server Version : 80037 (8.0.37)
 File Encoding         : 65001

 Date: 21/10/2025 19:03:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键自增',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名，唯一',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密后的密码',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号，唯一',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱地址，唯一',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户昵称',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` enum('MALE','FEMALE','UNKNOWN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'UNKNOWN' COMMENT '性别：男/女/未知',
  `age` int NULL DEFAULT NULL COMMENT '年龄',
  `status` enum('ACTIVE','INACTIVE','BANNED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ACTIVE' COMMENT '账号状态：活跃/非活跃/封禁',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 38 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户基本信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'user001', '$2a$10$ABC123', '13800138001', 'user001@example.com', '小明同学', '/avatars/user001.jpg', 'MALE', 25, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (2, 'user002', '$2a$10$ABC124', '13800138002', 'user002@example.com', '小芳', '/avatars/user002.jpg', 'FEMALE', 28, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (3, 'user003', '$2a$10$ABC125', '13800138003', 'user003@example.com', '职场新人', '/avatars/user003.jpg', 'MALE', 22, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (4, 'user004', '$2a$10$ABC126', '13800138004', 'user004@example.com', '幸福妈妈', '/avatars/user004.jpg', 'FEMALE', 35, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (5, 'user005', '$2a$10$ABC127', '13800138005', 'user005@example.com', '追梦人', '/avatars/user005.jpg', 'MALE', 30, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (6, 'user006', '$2a$10$ABC128', '13800138006', 'user006@example.com', '心灵探索', '/avatars/user006.jpg', 'FEMALE', 26, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (7, 'user007', '$2a$10$ABC129', '13800138007', 'user007@example.com', '阳光少年', '/avatars/user007.jpg', 'MALE', 19, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (8, 'user008', '$2a$10$ABC130', '13800138008', 'user008@example.com', '静心', '/avatars/user008.jpg', 'FEMALE', 32, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (9, 'user009', '$2a$10$ABC131', '13800138009', 'user009@example.com', '勇敢的心', '/avatars/user009.jpg', 'MALE', 27, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (10, 'user010', '$2a$10$ABC132', '13800138010', 'user010@example.com', '微笑面对', '/avatars/user010.jpg', 'FEMALE', 29, 'ACTIVE', '2025-09-30 15:42:13', '2025-09-30 15:42:13');
INSERT INTO `users` VALUES (11, 'counselor_zhangming', '$2a$10$ABC133', '13900139001', 'zhangming@psy.com', '张明老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'MALE', 38, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:02');
INSERT INTO `users` VALUES (12, 'counselor_lijing', '$2a$10$ABC134', '13900139002', 'lijing@psy.com', '李静老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 42, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:04');
INSERT INTO `users` VALUES (13, 'counselor_wangwei', '$2a$10$ABC135', '13900139003', 'wangwei@psy.com', '王伟老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'MALE', 36, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:05');
INSERT INTO `users` VALUES (14, 'counselor_liufang', '$2a$10$ABC136', '13900139004', 'liufang@psy.com', '刘芳老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 40, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:06');
INSERT INTO `users` VALUES (15, 'counselor_chenqiang', '$2a$10$ABC137', '13900139005', 'chenqiang@psy.com', '陈强老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'MALE', 45, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:08');
INSERT INTO `users` VALUES (16, 'counselor_yangli', '$2a$10$ABC138', '13900139006', 'yangli@psy.com', '杨丽老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 37, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:09');
INSERT INTO `users` VALUES (17, 'counselor_zhaogang', '$2a$10$ABC139', '13900139007', 'zhaogang@psy.com', '赵刚老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'MALE', 39, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:10');
INSERT INTO `users` VALUES (18, 'counselor_huangmin', '$2a$10$ABC140', '13900139008', 'huangmin@psy.com', '黄敏老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 41, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:11');
INSERT INTO `users` VALUES (19, 'counselor_zhoutao', '$2a$10$ABC141', '13900139009', 'zhoutao@psy.com', '周涛老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'MALE', 33, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:13');
INSERT INTO `users` VALUES (20, 'counselor_wuxia', '$2a$10$ABC142', '13900139010', 'wuxia@psy.com', '吴霞老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 43, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:14');
INSERT INTO `users` VALUES (21, 'counselor_zhenghao', '$2a$10$ABC143', '13900139011', 'zhenghao@psy.com', '郑浩老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'MALE', 38, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:15');
INSERT INTO `users` VALUES (22, 'counselor_sunyue', '$2a$10$ABC144', '13900139012', 'sunyue@psy.com', '孙悦老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 36, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:17');
INSERT INTO `users` VALUES (23, 'counselor_zhulin', '$2a$10$ABC145', '13900139013', 'zhulin@psy.com', '朱琳老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 40, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:19');
INSERT INTO `users` VALUES (24, 'counselor_machao', '$2a$10$ABC146', '13900139014', 'machao@psy.com', '马超老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'MALE', 35, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:20');
INSERT INTO `users` VALUES (25, 'counselor_linting', '$2a$10$ABC147', '13900139015', 'linting@psy.com', '林婷老师', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 'FEMALE', 32, 'ACTIVE', '2025-09-30 15:42:13', '2025-10-01 17:30:22');
INSERT INTO `users` VALUES (26, '开心的小狗', '', '+15551234567', ' zzz', 'zt', 'http://localhost:8080/files/download/1759394187138_JPEG_20251002_083625_2325940260094199469.jpg', 'FEMALE', 34, 'ACTIVE', '2025-10-02 14:26:15', '2025-10-20 10:42:10');
INSERT INTO `users` VALUES (34, '张同宁', '', '19832158160', '3465068841@qq.com', '张同宁', 'http://localhost:8080/files/download/1759737729450_JPEG_20251006_160206_1308311242906756927.jpg', 'FEMALE', 21, 'ACTIVE', '2025-10-06 13:50:54', '2025-10-18 17:35:05');
INSERT INTO `users` VALUES (36, '磁石2', '1', '19566181388', NULL, '张同宁', 'http://localhost:8080/files/download/1759738111462_JPEG_20251006_160829_7380794833892332888.jpg', 'FEMALE', 21, 'ACTIVE', '2025-10-06 16:08:01', '2025-10-06 16:08:31');

SET FOREIGN_KEY_CHECKS = 1;
