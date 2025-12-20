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

 Date: 19/12/2025 19:34:16
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for children
-- ----------------------------
DROP TABLE IF EXISTS `children`;
CREATE TABLE `children`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '孩子ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '关联用户ID，外键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '孩子姓名',
  `gender` enum('MALE','FEMALE','UNKNOWN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '性别：男/女/未知',
  `birth_date` date NULL DEFAULT NULL COMMENT '出生年月',
  `ethnicity` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '民族',
  `native_place` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '籍贯',
  `birth_order` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '家中排行',
  `birth_location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '出生地',
  `language_environment` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '语言环境',
  `current_school` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '现就读学校/园',
  `home_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '现家庭住址',
  `hobbies` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '孩子趣味爱好',
  `interests` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '孩子兴趣活动',
  `health_status` enum('EXCELLENT','GOOD','AVERAGE','POOR','VERY_POOR') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'AVERAGE' COMMENT '身体状态：很好/良好/普通/较差/很差',
  `health_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '若健康状态需具体描述',
  `past_diseases` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '过往病史',
  `father_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '父亲电话',
  `mother_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '母亲电话',
  `guardian_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '监护人电话',
  `is_current_operation` tinyint(1) NULL DEFAULT 0 COMMENT '当前操作孩子标识：0-否，1-是',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_is_current_operation`(`is_current_operation` ASC) USING BTREE,
  CONSTRAINT `fk_children_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '孩子基本信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for consultation_appointments
-- ----------------------------
DROP TABLE IF EXISTS `consultation_appointments`;
CREATE TABLE `consultation_appointments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '预约ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `counselor_id` bigint NOT NULL COMMENT '咨询师ID，外键关联counselors表',
  `consultation_type` enum('TEXT','VOICE','VIDEO') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '咨询类型：文字/语音/视频',
  `duration_minutes` int NOT NULL COMMENT '咨询时长（分钟）',
  `scheduled_time` datetime NOT NULL COMMENT '预约时间',
  `actual_start_time` datetime NULL DEFAULT NULL COMMENT '实际开始时间',
  `actual_end_time` datetime NULL DEFAULT NULL COMMENT '实际结束时间',
  `fee` decimal(10, 2) NOT NULL COMMENT '咨询费用',
  `status` enum('PENDING','CONFIRMED','IN_PROGRESS','COMPLETED','CANCELLED','NO_SHOW') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '预约状态：待确认/已确认/进行中/已完成/已取消/未到场',
  `payment_status` enum('PENDING','PAID','REFUNDED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '支付状态：待支付/已支付/已退款',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `counselor_id`(`counselor_id` ASC) USING BTREE,
  CONSTRAINT `consultation_appointments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `consultation_appointments_ibfk_2` FOREIGN KEY (`counselor_id`) REFERENCES `counselors` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询预约表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for consultation_messages
-- ----------------------------
DROP TABLE IF EXISTS `consultation_messages`;
CREATE TABLE `consultation_messages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID，主键自增',
  `appointment_id` bigint NULL DEFAULT NULL COMMENT '预约ID，外键关联consultation_appointments表，可为空',
  `sender_type` enum('USER','COUNSELOR') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '发送者类型：用户/咨询师',
  `message_type` enum('TEXT','IMAGE','VOICE','SYSTEM') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'TEXT' COMMENT '消息类型：文字/图片/语音/系统',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '消息内容',
  `media_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '媒体文件URL',
  `duration_seconds` int NULL DEFAULT NULL COMMENT '语音消息时长（秒）',
  `sent_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `read_status` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID，外键关联users表',
  `counselor_id` bigint NULL DEFAULT NULL COMMENT '咨询师ID，外键关联counselors表',
  `conversation_type` enum('PRE_CONSULTATION','IN_CONSULTATION','FOLLOW_UP') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PRE_CONSULTATION' COMMENT '对话类型：咨询前/咨询中/随访',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `fk_consultation_messages_user_id`(`user_id` ASC) USING BTREE,
  INDEX `fk_consultation_messages_counselor_id`(`counselor_id` ASC) USING BTREE,
  INDEX `consultation_messages_ibfk_1`(`appointment_id` ASC) USING BTREE,
  CONSTRAINT `consultation_messages_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `consultation_appointments` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_consultation_messages_counselor_id` FOREIGN KEY (`counselor_id`) REFERENCES `counselors` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_consultation_messages_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 40 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询对话记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for consultation_records
-- ----------------------------
DROP TABLE IF EXISTS `consultation_records`;
CREATE TABLE `consultation_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '记录ID，主键自增',
  `appointment_id` bigint NOT NULL COMMENT '预约ID，外键关联consultation_appointments表',
  `summary` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '咨询摘要',
  `counselor_feedback` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '咨询师反馈',
  `core_issue_tags` json NULL COMMENT '核心问题标签，JSON格式',
  `follow_up_recommendation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '后续建议',
  `counselor_submitted_time` datetime NULL DEFAULT NULL COMMENT '咨询师提交时间',
  `user_rating` int NULL DEFAULT NULL COMMENT '用户评分，1-5分',
  `user_review` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户评价内容',
  `review_time` datetime NULL DEFAULT NULL COMMENT '评价时间',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `appointment_id`(`appointment_id` ASC) USING BTREE,
  CONSTRAINT `consultation_records_ibfk_1` FOREIGN KEY (`appointment_id`) REFERENCES `consultation_appointments` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for counselor_service_settings
-- ----------------------------
DROP TABLE IF EXISTS `counselor_service_settings`;
CREATE TABLE `counselor_service_settings`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '设置ID，主键自增',
  `counselor_id` bigint NOT NULL COMMENT '咨询师ID，外键关联counselors表',
  `service_types` json NOT NULL COMMENT '服务类型，JSON格式，如[\"文字\",\"语音\",\"视频\"]',
  `available_days` json NOT NULL COMMENT '可用日期设置，JSON格式',
  `working_hours` json NOT NULL COMMENT '工作时间段，JSON格式',
  `session_durations` json NOT NULL COMMENT '支持的咨询时长，JSON格式，如[20,60]',
  `max_daily_sessions` int NULL DEFAULT 5 COMMENT '每日最大咨询次数',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `counselor_id`(`counselor_id` ASC) USING BTREE,
  CONSTRAINT `counselor_service_settings_ibfk_1` FOREIGN KEY (`counselor_id`) REFERENCES `counselors` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 31 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '咨询师服务设置表' ROW_FORMAT = Dynamic;

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
-- Table structure for distress_tags
-- ----------------------------
DROP TABLE IF EXISTS `distress_tags`;
CREATE TABLE `distress_tags`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '标签ID，主键自增',
  `tag_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标签名称，唯一',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类：情绪、人际、职场等',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '标签描述',
  `status` enum('ACTIVE','INACTIVE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ACTIVE' COMMENT '标签状态：启用/停用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `tag_name`(`tag_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '困扰标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_packages
-- ----------------------------
DROP TABLE IF EXISTS `learning_packages`;
CREATE TABLE `learning_packages`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学习包ID，主键自增',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '学习包标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '学习包描述',
  `cover_image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '封面图片URL',
  `target_tags` json NULL COMMENT '目标标签，JSON格式',
  `video_count` int NULL DEFAULT 0 COMMENT '视频数量',
  `estimated_duration_minutes` int NULL DEFAULT 0 COMMENT '预计学习时长（分钟）',
  `difficulty_level` enum('BEGINNER','INTERMEDIATE','ADVANCED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'BEGINNER' COMMENT '难度级别：初级/中级/高级',
  `status` enum('DRAFT','PUBLISHED','ARCHIVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT '状态：草稿/已发布/已归档',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习包表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for learning_videos
-- ----------------------------
DROP TABLE IF EXISTS `learning_videos`;
CREATE TABLE `learning_videos`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '视频ID，主键自增',
  `learning_package_id` bigint NOT NULL COMMENT '学习包ID，外键关联learning_packages表',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '视频描述',
  `video_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '视频文件URL',
  `thumbnail_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '缩略图URL',
  `duration_seconds` int NOT NULL COMMENT '视频时长（秒）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `status` enum('DRAFT','PUBLISHED','ARCHIVED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'DRAFT' COMMENT '状态：草稿/已发布/已归档',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `learning_package_id`(`learning_package_id` ASC) USING BTREE,
  CONSTRAINT `learning_videos_ibfk_1` FOREIGN KEY (`learning_package_id`) REFERENCES `learning_packages` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '学习视频表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID，主键自增',
  `operator_id` bigint NULL DEFAULT NULL COMMENT '操作者ID',
  `operator_type` enum('USER','COUNSELOR','ADMIN') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作者类型：用户/咨询师/管理员',
  `operation_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `target_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '目标类型',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标ID',
  `operation_details` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '操作详情',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'IP地址',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理',
  `operation_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for psychological_assessments
-- ----------------------------
DROP TABLE IF EXISTS `psychological_assessments`;
CREATE TABLE `psychological_assessments`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评估报告ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `test_record_id` bigint NOT NULL COMMENT '测试记录ID，外键关联test_records表',
  `consultation_record_id` bigint NULL DEFAULT NULL COMMENT '咨询记录ID，外键关联consultation_records表',
  `overall_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总体得分',
  `knowledge_mastery_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '知识点掌握率',
  `psychological_state_indicators` json NULL COMMENT '心理状态指标，JSON格式',
  `improvement_suggestions` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '改善建议',
  `recommended_actions` json NULL COMMENT '推荐行动，JSON格式',
  `assessment_date` date NULL DEFAULT NULL COMMENT '评估日期',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `test_record_id`(`test_record_id` ASC) USING BTREE,
  INDEX `consultation_record_id`(`consultation_record_id` ASC) USING BTREE,
  CONSTRAINT `psychological_assessments_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `psychological_assessments_ibfk_2` FOREIGN KEY (`test_record_id`) REFERENCES `test_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `psychological_assessments_ibfk_3` FOREIGN KEY (`consultation_record_id`) REFERENCES `consultation_records` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '心理评估报告表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for quick_consultation_requests
-- ----------------------------
DROP TABLE IF EXISTS `quick_consultation_requests`;
CREATE TABLE `quick_consultation_requests`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '申请ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `problem_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题描述',
  `problem_duration` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '问题持续时间',
  `preferred_method` enum('TEXT','VOICE','VIDEO') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '偏好咨询方式：文字/语音/视频',
  `attached_images` json NULL COMMENT '上传的图片URL，JSON格式',
  `matched_counselor_id` bigint NULL DEFAULT NULL COMMENT '匹配的咨询师ID，外键关联counselors表',
  `status` enum('PENDING','MATCHED','COMPLETED','CANCELLED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'PENDING' COMMENT '申请状态：待处理/已匹配/已完成/已取消',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `matched_time` datetime NULL DEFAULT NULL COMMENT '匹配时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `matched_counselor_id`(`matched_counselor_id` ASC) USING BTREE,
  CONSTRAINT `quick_consultation_requests_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `quick_consultation_requests_ibfk_2` FOREIGN KEY (`matched_counselor_id`) REFERENCES `counselors` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '快速咨询申请表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for system_configurations
-- ----------------------------
DROP TABLE IF EXISTS `system_configurations`;
CREATE TABLE `system_configurations`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID，主键自增',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键，唯一',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置描述',
  `config_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'STRING' COMMENT '配置类型：STRING/NUMBER/BOOLEAN/JSON',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `config_key`(`config_key` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_answers
-- ----------------------------
DROP TABLE IF EXISTS `test_answers`;
CREATE TABLE `test_answers`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '答题详情ID，主键自增',
  `test_record_id` bigint NOT NULL COMMENT '测试记录ID，外键关联test_records表',
  `question_id` bigint NOT NULL COMMENT '题目ID，外键关联test_questions表',
  `user_answers` json NULL COMMENT '用户答案，JSON格式',
  `is_correct` tinyint(1) NULL DEFAULT 0 COMMENT '是否正确',
  `time_spent_seconds` int NULL DEFAULT 0 COMMENT '答题用时（秒）',
  `answered_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '答题时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `test_record_id`(`test_record_id` ASC) USING BTREE,
  INDEX `question_id`(`question_id` ASC) USING BTREE,
  CONSTRAINT `test_answers_ibfk_1` FOREIGN KEY (`test_record_id`) REFERENCES `test_records` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `test_answers_ibfk_2` FOREIGN KEY (`question_id`) REFERENCES `test_questions` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测试答题详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_questions
-- ----------------------------
DROP TABLE IF EXISTS `test_questions`;
CREATE TABLE `test_questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '题目ID，主键自增',
  `learning_package_id` bigint NOT NULL COMMENT '学习包ID，外键关联learning_packages表',
  `question_type` enum('SINGLE_CHOICE','MULTIPLE_CHOICE','TRUE_FALSE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目类型：单选/多选/判断',
  `question_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '题目文本',
  `options` json NOT NULL COMMENT '选项，JSON格式',
  `correct_answers` json NOT NULL COMMENT '正确答案，JSON格式',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案解析',
  `points` int NULL DEFAULT 1 COMMENT '分值',
  `psychological_dimension` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '心理维度',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `status` enum('ACTIVE','INACTIVE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'ACTIVE' COMMENT '状态：启用/停用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `learning_package_id`(`learning_package_id` ASC) USING BTREE,
  CONSTRAINT `test_questions_ibfk_1` FOREIGN KEY (`learning_package_id`) REFERENCES `learning_packages` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测试题库表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for test_records
-- ----------------------------
DROP TABLE IF EXISTS `test_records`;
CREATE TABLE `test_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '测试记录ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `learning_package_id` bigint NOT NULL COMMENT '学习包ID，外键关联learning_packages表',
  `total_questions` int NOT NULL COMMENT '总题目数',
  `answered_questions` int NULL DEFAULT 0 COMMENT '已答题数',
  `correct_answers` int NULL DEFAULT 0 COMMENT '正确答题数',
  `score` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '得分',
  `time_spent_seconds` int NULL DEFAULT 0 COMMENT '用时（秒）',
  `time_limit_seconds` int NULL DEFAULT 900 COMMENT '时间限制（秒），默认15分钟',
  `status` enum('IN_PROGRESS','COMPLETED','TIMEOUT') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'IN_PROGRESS' COMMENT '测试状态：进行中/已完成/超时',
  `started_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
  `submitted_time` datetime NULL DEFAULT NULL COMMENT '提交时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `learning_package_id`(`learning_package_id` ASC) USING BTREE,
  CONSTRAINT `test_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `test_records_ibfk_2` FOREIGN KEY (`learning_package_id`) REFERENCES `learning_packages` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '测试记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_learning_records
-- ----------------------------
DROP TABLE IF EXISTS `user_learning_records`;
CREATE TABLE `user_learning_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '学习记录ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `learning_package_id` bigint NOT NULL COMMENT '学习包ID，外键关联learning_packages表',
  `consultation_record_id` bigint NULL DEFAULT NULL COMMENT '关联的咨询记录ID，外键关联consultation_records表',
  `progress_percentage` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '学习进度百分比',
  `total_watch_time_seconds` int NULL DEFAULT 0 COMMENT '总观看时长（秒）',
  `status` enum('IN_PROGRESS','COMPLETED','ABANDONED') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'IN_PROGRESS' COMMENT '学习状态：进行中/已完成/已放弃',
  `started_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '开始学习时间',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成学习时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `learning_package_id`(`learning_package_id` ASC) USING BTREE,
  INDEX `consultation_record_id`(`consultation_record_id` ASC) USING BTREE,
  CONSTRAINT `user_learning_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_learning_records_ibfk_2` FOREIGN KEY (`learning_package_id`) REFERENCES `learning_packages` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_learning_records_ibfk_3` FOREIGN KEY (`consultation_record_id`) REFERENCES `consultation_records` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户学习记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_psychological_profile
-- ----------------------------
DROP TABLE IF EXISTS `user_psychological_profile`;
CREATE TABLE `user_psychological_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '档案ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `current_distress_level` enum('LOW','MEDIUM','HIGH') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'LOW' COMMENT '当前困扰程度：低/中/高',
  `main_concerns` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '主要困扰描述',
  `medication_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用药史',
  `therapy_history` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '治疗史',
  `emergency_contact_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人姓名',
  `emergency_contact_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '紧急联系人电话',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `user_psychological_profile_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户心理档案表' ROW_FORMAT = Dynamic;

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
) ENGINE = InnoDB AUTO_INCREMENT = 114 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户基本信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_verification_questions
-- ----------------------------
DROP TABLE IF EXISTS `video_verification_questions`;
CREATE TABLE `video_verification_questions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '验证题ID，主键自增',
  `video_id` bigint NOT NULL COMMENT '视频ID，外键关联learning_videos表',
  `question_text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '问题文本',
  `correct_answer` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '正确答案',
  `explanation` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '答案解析',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `video_id`(`video_id` ASC) USING BTREE,
  CONSTRAINT `video_verification_questions_ibfk_1` FOREIGN KEY (`video_id`) REFERENCES `learning_videos` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频验证题表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for video_watch_records
-- ----------------------------
DROP TABLE IF EXISTS `video_watch_records`;
CREATE TABLE `video_watch_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '观看记录ID，主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID，外键关联users表',
  `video_id` bigint NOT NULL COMMENT '视频ID，外键关联learning_videos表',
  `watch_duration_seconds` int NULL DEFAULT 0 COMMENT '已观看时长（秒）',
  `total_duration_seconds` int NOT NULL COMMENT '视频总时长（秒）',
  `progress_percentage` decimal(5, 2) NULL DEFAULT 0.00 COMMENT '观看进度百分比',
  `verification_passed` tinyint(1) NULL DEFAULT 0 COMMENT '验证题是否通过',
  `last_watch_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '最后观看时间',
  `completed_time` datetime NULL DEFAULT NULL COMMENT '完成观看时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `video_id`(`video_id` ASC) USING BTREE,
  CONSTRAINT `video_watch_records_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `video_watch_records_ibfk_2` FOREIGN KEY (`video_id`) REFERENCES `learning_videos` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '视频观看记录表' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;
