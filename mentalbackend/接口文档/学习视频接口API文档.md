# 学习视频接口API文档

## 1. 根据学习包ID获取视频列表

### 请求URL
`GET /api/learning-videos/package/{learningPackageId}`

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| learningPackageId | Long | 是 | 学习包ID |

### 响应数据格式
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "learningPackageId": 1,
      "title": "心理健康基础知识介绍",
      "description": "本视频主要介绍心理健康的基本概念、重要性以及常见的心理健康标准。通过本视频的学习，您将对心理健康有初步的认识。",
      "videoUrl": "http://localhost:8080/files/download/1762327301462_8105be9ab84bb6924f4d0b5d4866f08e.mp4",
      "thumbnailUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
      "durationSeconds": 1800,
      "sortOrder": 1,
      "status": "PUBLISHED",
      "createdTime": "2025-11-05T15:16:00"
    },
    {
      "id": 2,
      "learningPackageId": 1,
      "title": "情绪识别与管理技巧",
      "description": "本视频详细讲解如何识别不同的情绪状态，以及在日常生活中如何有效地管理和调节自己的情绪，保持良好的心理状态。",
      "videoUrl": "http://localhost:8080/files/download/1762327301462_8105be9ab84bb6924f4d0b5d4866f08e.mp4",
      "thumbnailUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
      "durationSeconds": 2100,
      "sortOrder": 2,
      "status": "PUBLISHED",
      "createdTime": "2025-11-05T15:16:00"
    }
  ]
}
```

### 错误响应
```json
{
  "code": 500,
  "message": "获取失败: 错误信息"
}
```

## 2. 根据视频ID获取视频详情

### 请求URL
`GET /api/learning-videos/{id}`

### 请求参数
| 参数名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 视频ID |

### 响应数据格式
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "id": 1,
    "learningPackageId": 1,
    "title": "心理健康基础知识介绍",
    "description": "本视频主要介绍心理健康的基本概念、重要性以及常见的心理健康标准。通过本视频的学习，您将对心理健康有初步的认识。",
    "videoUrl": "http://localhost:8080/files/download/1762327301462_8105be9ab84bb6924f4d0b5d4866f08e.mp4",
    "thumbnailUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
    "durationSeconds": 1800,
    "sortOrder": 1,
    "status": "PUBLISHED",
    "createdTime": "2025-11-05T15:16:00"
  }
}
```

### 错误响应
```json
{
  "code": 404,
  "message": "视频不存在"
}
```

```json
{
  "code": 500,
  "message": "获取失败: 错误信息"
}
```

## 数据类型说明

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long | 视频ID |
| learningPackageId | Long | 学习包ID |
| title | String | 视频标题 |
| description | String | 视频描述 |
| videoUrl | String | 视频文件URL，格式为：http://localhost:8080/files/download/{文件ID}_{文件名}.{扩展名} |
| thumbnailUrl | String | 缩略图URL，格式为：http://localhost:8080/files/download/{文件ID}_{文件名}.{扩展名} |
| durationSeconds | Integer | 视频时长（秒） |
| sortOrder | Integer | 排序顺序 |
| status | String | 状态（DRAFT:草稿, PUBLISHED:已发布, ARCHIVED:已归档） |
| createdTime | String | 创建时间（ISO 8601格式，如：2025-11-05T15:16:00） |

## 示例代码

### JavaScript示例
```javascript
// 获取学习包视频列表
async function getVideosByPackageId(learningPackageId) {
  try {
    const response = await fetch(`/api/learning-videos/package/${learningPackageId}`);
    const data = await response.json();
    if (data.code === 200) {
      console.log('视频列表:', data.data);
      // 示例：处理视频数据
      data.data.forEach(video => {
        console.log(`视频标题: ${video.title}, 时长: ${Math.floor(video.durationSeconds / 60)}:${(video.durationSeconds % 60).toString().padStart(2, '0')}`);
        console.log(`视频链接: ${video.videoUrl}`);
        console.log(`缩略图链接: ${video.thumbnailUrl}`);
      });
      return data.data;
    } else {
      console.error('获取失败:', data.message);
    }
  } catch (error) {
    console.error('请求异常:', error);
  }
}

// 获取视频详情
async function getVideoById(id) {
  try {
    const response = await fetch(`/api/learning-videos/${id}`);
    const data = await response.json();
    if (data.code === 200) {
      console.log('视频详情:', data.data);
      const video = data.data;
      console.log(`视频标题: ${video.title}`);
      console.log(`视频描述: ${video.description}`);
      console.log(`视频链接: ${video.videoUrl}`);
      console.log(`缩略图链接: ${video.thumbnailUrl}`);
      console.log(`时长: ${Math.floor(video.durationSeconds / 60)}:${(video.durationSeconds % 60).toString().padStart(2, '0')}`);
      console.log(`状态: ${video.status}`);
      console.log(`创建时间: ${video.createdTime}`);
      return data.data;
    } else {
      console.error('获取失败:', data.message);
    }
  } catch (error) {
    console.error('请求异常:', error);
  }
}
```

### Java示例
```java
// 获取学习包视频列表
public List<LearningVideos> getVideosByPackageId(Long learningPackageId) {
    String url = "/api/learning-videos/package/" + learningPackageId;
    try {
        // 使用RestTemplate发送请求
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            if (body.get("code").equals(200)) {
                // 解析data字段
                List<LearningVideos> videos = parseVideosFromResponse(body.get("data"));
                return videos;
            }
        }
    } catch (Exception e) {
        logger.error("获取学习包视频列表失败: {}", e.getMessage(), e);
    }
    return Collections.emptyList();
}

// 获取视频详情
public LearningVideos getVideoById(Long id) {
    String url = "/api/learning-videos/" + id;
    try {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, Map.class);
        
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            Map<String, Object> body = response.getBody();
            if (body.get("code").equals(200)) {
                // 解析data字段为单个视频对象
                return parseVideoFromResponse(body.get("data"));
            }
        }
    } catch (Exception e) {
        logger.error("获取视频详情失败: {}", e.getMessage(), e);
    }
    return null;
}

// 辅助方法：解析视频列表
private List<LearningVideos> parseVideosFromResponse(Object data) {
    // 实现JSON解析逻辑
    // ...
    return videos;
}

// 辅助方法：解析单个视频
private LearningVideos parseVideoFromResponse(Object data) {
    // 实现JSON解析逻辑
    // ...
    return video;
}
```