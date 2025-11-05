-- 向学习包表插入三条示例数据
INSERT INTO learning_packages (title, description, cover_image_url, target_tags, video_count, estimated_duration_minutes, difficulty_level, status, created_time, updated_time)
VALUES 
('心理健康入门', '适合初学者的心理健康基础知识课程，包含心理学基本概念、心理健康标准和常见心理问题识别方法。', 'http://example.com/images/mental_health_basic.jpg', '["心理健康","入门","心理知识"]', 10, 120, 'BEGINNER', 'PUBLISHED', NOW(), NOW()),

('情绪管理进阶', '深入学习情绪管理技巧，包括压力应对、情绪调节和积极心理学应用，适合有一定心理学基础的学习者。', 'http://example.com/images/emotion_management.jpg', '["情绪管理","压力应对","积极心理学"]', 15, 180, 'INTERMEDIATE', 'PUBLISHED', NOW(), NOW()),

('心理咨询技术专题', '专业心理咨询师必备的技术培训课程，涵盖个案概念化、咨询技巧和治疗方法，适合心理咨询从业者。', 'http://example.com/images/counseling_techniques.jpg', '["心理咨询","治疗技术","专业成长"]', 20, 240, 'ADVANCED', 'DRAFT', NOW(), NOW());

-- 如需直接运行单条插入语句，可使用以下格式
-- INSERT INTO learning_packages (title, description, cover_image_url, target_tags, video_count, estimated_duration_minutes, difficulty_level, status)
-- VALUES ('标题', '描述', '图片URL', '["标签1","标签2"]', 视频数量, 预计时长, '难度级别', '状态');