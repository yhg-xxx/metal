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

 Date: 21/10/2025 19:03:21
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for counselors
-- ----------------------------
DROP TABLE IF EXISTS `counselors`;
CREATE TABLE `counselors`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '咨询师ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '真实姓名',
  `id_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '身份证号，唯一',
  `qualification_certificate_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '资质证书URL',
  `practice_certificate_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '执业证书URL',
  `photo_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '证件照URL',
  `years_of_experience` int NULL DEFAULT 0 COMMENT '从业年限',
  `specialization` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '擅长领域，JSON格式存储，如[\"焦虑\",\"抑郁\"]',
  `therapeutic_approach` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '治疗流派，JSON格式存储，如[\"认知行为\",\"人本主义\"]',
  `introduction` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '个人介绍',
  `consultation_fee` decimal(10, 2) NOT NULL COMMENT '咨询费用',
  `rating` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '平均评分，0-5分',
  `total_sessions` int NULL DEFAULT 0 COMMENT '总咨询次数',
  `status` enum('PENDING','APPROVED','REJECTED','SUSPENDED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '审核状态：待审核/已通过/已拒绝/已暂停',
  `approved_time` datetime NULL DEFAULT NULL COMMENT '审核通过时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `id_number`(`id_number` ASC) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `counselors_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询师信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of counselors
-- ----------------------------
INSERT INTO `counselors` VALUES (1, 11, '张明', '110101198001010011', '/certs/zhangming_qualification.jpg', '/certs/zhangming_practice.jpg', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 8, '[\"焦虑情绪\", \"抑郁情绪\", \"职场压力\"]', '[\"认知行为疗法\", \"人本主义\"]', '国家二级心理咨询师，擅长认知行为疗法，帮助来访者识别和改变负面思维模式。', 300.00, 4.80, 150, 'APPROVED', '2024-01-15 10:00:00', '2025-09-30 15:42:33', '2025-10-01 16:26:45');
INSERT INTO `counselors` VALUES (2, 12, '李静', '110101198102020022', '/certs/lijing_qualification.jpg', '/certs/lijing_practice.jpg', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 12, '[\"婚姻家庭\", \"亲子关系\", \"人际关系\"]', '[\"家庭系统治疗\", \"叙事疗法\"]', '婚姻家庭咨询专家，拥有丰富的家庭关系调解经验。', 350.00, 4.90, 200, 'APPROVED', '2024-01-16 11:00:00', '2025-09-30 15:42:33', '2025-10-01 16:26:47');
INSERT INTO `counselors` VALUES (3, 13, '王伟', '110101198203030033', '/certs/wangwei_qualification.jpg', '/certs/wangwei_practice.jpg', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 6, '[\"职场压力\", \"职业规划\", \"人际关系\"]', '[\"认知行为疗法\", \"焦点解决短期治疗\"]', '专注于职场心理问题，帮助企业员工缓解工作压力。', 280.00, 4.70, 120, 'APPROVED', '2024-01-17 09:30:00', '2025-09-30 15:42:33', '2025-10-01 16:26:48');
INSERT INTO `counselors` VALUES (4, 14, '刘芳', '110101198304040044', '/certs/liufang_qualification.jpg', '/certs/liufang_practice.jpg', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 10, '[\"焦虑情绪\", \"创伤修复\", \"情绪管理\"]', '[\"心理动力学\", \"眼动脱敏与再处理\"]', '擅长处理创伤后应激障碍和焦虑障碍。', 320.00, 4.80, 180, 'APPROVED', '2024-01-18 14:00:00', '2025-09-30 15:42:33', '2025-10-01 16:26:50');
INSERT INTO `counselors` VALUES (5, 15, '陈强', '110101198405050055', '/certs/chenqiang_qualification.jpg', '/certs/chenqiang_practice.jpg', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 15, '[\"抑郁情绪\", \"生命意义\", \"存在主义问题\"]', '[\"存在主义疗法\", \"接纳与承诺疗法\"]', '资深心理咨询师，专注于抑郁症的长期治疗。', 400.00, 4.90, 250, 'APPROVED', '2024-01-19 16:00:00', '2025-09-30 15:42:33', '2025-10-01 16:26:51');
INSERT INTO `counselors` VALUES (6, 16, '杨丽', '110101198506060066', '/certs/yangli_qualification.jpg', '/certs/yangli_practice.jpg', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 7, '[\"亲子关系\", \"青少年心理\", \"学习困难\"]', '[\"游戏治疗\", \"艺术治疗\"]', '儿童青少年心理专家，善于用游戏和艺术与孩子沟通。', 300.00, 4.60, 130, 'APPROVED', '2024-01-20 10:30:00', '2025-09-30 15:42:33', '2025-10-01 16:26:53');
INSERT INTO `counselors` VALUES (7, 17, '赵刚', '110101198607070077', '/certs/zhaogang_qualification.jpg', '/certs/zhaogang_practice.jpg', 'http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg', 9, '[\"人际关系\", \"社交恐惧\", \"自信心建立\"]', '[\"认知行为疗法\", \"社交技能训练\"]', '专注于社交焦虑和人际关系问题的咨询。', 290.00, 4.70, 160, 'APPROVED', '2024-01-21 11:30:00', '2025-09-30 15:42:33', '2025-10-01 16:26:55');
INSERT INTO `counselors` VALUES (8, 18, '黄敏', '110101198708080088', '/certs/huangmin_qualification.jpg', '/certs/huangmin_practice.jpg', '/photos/huangmin.jpg', 11, '[\"婚姻家庭\", \"情感问题\", \"亲密关系\"]', '[\"情感聚焦疗法\", \"系统式家庭治疗\"]', '婚姻情感咨询师，帮助夫妻改善沟通和亲密关系。', 330.00, 4.80, 190, 'APPROVED', '2024-01-22 15:00:00', '2025-09-30 15:42:33', '2025-09-30 15:44:49');
INSERT INTO `counselors` VALUES (9, 19, '周涛', '110101198809090099', '/certs/zhoutao_qualification.jpg', '/certs/zhoutao_practice.jpg', '/photos/zhoutao.jpg', 5, '[\"职场压力\", \"职业倦怠\", \"时间管理\"]', '[\"认知行为疗法\", \"正念疗法\"]', '企业EAP咨询师，擅长压力管理和职业发展规划。', 270.00, 4.50, 100, 'APPROVED', '2024-01-23 09:00:00', '2025-09-30 15:42:33', '2025-09-30 15:44:53');
INSERT INTO `counselors` VALUES (10, 20, '吴霞', '110101198910100101', '/certs/wuxia_qualification.jpg', '/certs/wuxia_practice.jpg', '/photos/wuxia.jpg', 13, '[\"焦虑情绪\", \"惊恐发作\", \"强迫行为\"]', '[\"认知行为疗法\", \"暴露与反应阻止\"]', '焦虑障碍治疗专家，提供专业的暴露疗法。', 360.00, 4.90, 220, 'APPROVED', '2024-01-24 14:30:00', '2025-09-30 15:42:33', '2025-09-30 15:44:56');
INSERT INTO `counselors` VALUES (11, 21, '郑浩', '110101199011110111', '/certs/zhenghao_qualification.jpg', '/certs/zhenghao_practice.jpg', '/photos/zhenghao.jpg', 8, '[\"抑郁情绪\", \"睡眠问题\", \"情绪调节\"]', '[\"认知行为疗法\", \"正念认知疗法\"]', '专注于抑郁症的认知行为治疗和睡眠问题。', 310.00, 4.70, 140, 'APPROVED', '2024-01-25 16:30:00', '2025-09-30 15:42:33', '2025-09-30 15:44:59');
INSERT INTO `counselors` VALUES (12, 22, '孙悦', '110101199112120121', '/certs/sunyue_qualification.jpg', '/certs/sunyue_practice.jpg', '/photos/sunyue.jpg', 6, '[\"亲子关系\", \"儿童行为问题\", \"家长教育\"]', '[\"亲子互动疗法\", \"积极教养\"]', '儿童心理行为问题专家，提供家长培训。', 290.00, 4.60, 110, 'APPROVED', '2024-01-26 10:00:00', '2025-09-30 15:42:33', '2025-09-30 15:45:02');
INSERT INTO `counselors` VALUES (13, 23, '朱琳', '110101199213130131', '/certs/zhulin_qualification.jpg', '/certs/zhulin_practice.jpg', '/photos/zhulin.jpg', 10, '[\"人际关系\", \"边界设定\", \"沟通技巧\"]', '[\"人际心理治疗\", \"沟通分析\"]', '人际关系咨询师，帮助建立健康的个人边界。', 320.00, 4.80, 170, 'APPROVED', '2024-01-27 11:00:00', '2025-09-30 15:42:33', '2025-09-30 15:45:05');
INSERT INTO `counselors` VALUES (14, 24, '马超', '110101199314140141', '/certs/machao_qualification.jpg', '/certs/machao_practice.jpg', '/photos/machao.jpg', 7, '[\"职场压力\", \"领导力发展\", \"团队协作\"]', '[\"教练技术\", \"解决方案聚焦\"]', '企业领导力教练，结合心理学提升管理能力。', 380.00, 4.70, 150, 'APPROVED', '2024-01-28 15:30:00', '2025-09-30 15:42:33', '2025-09-30 15:45:09');
INSERT INTO `counselors` VALUES (15, 25, '林婷', '110101199415150151', '/certs/linting_qualification.jpg', '/certs/linting_practice.jpg', '/photos/linting.jpg', 4, '[\"情绪管理\", \"自我成长\", \"生活适应\"]', '[\"人本主义疗法\", \"接纳与承诺疗法\"]', '专注于个人成长和自我探索的咨询工作。', 260.00, 4.50, 80, 'APPROVED', '2024-01-29 09:30:00', '2025-09-30 15:42:33', '2025-09-30 15:45:12');

SET FOREIGN_KEY_CHECKS = 1;
