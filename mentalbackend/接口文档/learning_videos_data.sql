-- 向学习视频表插入三条示例数据
INSERT INTO learning_videos (learning_package_id, title, description, video_url, thumbnail_url, duration_seconds, sort_order, status, created_time)
VALUES 
(1, '心理健康基础知识介绍', '本视频主要介绍心理健康的基本概念、重要性以及常见的心理健康标准。通过本视频的学习，您将对心理健康有初步的认识。', 'http://example.com/videos/mental_health_intro.mp4', 'http://example.com/images/mental_health_intro_thumb.jpg', 1800, 1, 'PUBLISHED', NOW()),

(1, '情绪识别与管理技巧', '本视频详细讲解如何识别不同的情绪状态，以及在日常生活中如何有效地管理和调节自己的情绪，保持良好的心理状态。', 'http://example.com/videos/emotion_management.mp4', 'http://example.com/images/emotion_management_thumb.jpg', 2100, 2, 'PUBLISHED', NOW()),

(2, '压力应对与放松训练', '本视频介绍常见的压力来源，以及专业的压力评估方法，同时教授几种实用的放松训练技巧，帮助您有效应对生活中的各种压力。', 'http://example.com/videos/stress_management.mp4', 'http://example.com/images/stress_management_thumb.jpg', 2400, 1, 'DRAFT', NOW());

-- 如需直接运行单条插入语句，可使用以下格式
-- INSERT INTO learning_videos (learning_package_id, title, description, video_url, thumbnail_url, duration_seconds, sort_order, status)
-- VALUES (学习包ID, '视频标题', '视频描述', '视频URL', '缩略图URL', 时长(秒), 排序, '状态');