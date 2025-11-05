# 学习包接口API文档

## 1. 获取所有学习包列表

### 1.1 接口说明
获取系统中所有的学习包列表。

### 1.2 请求URL
`GET /api/learning-packages`

### 1.3 请求参数
无

### 1.4 响应数据

#### 成功响应
```json
{
    "code": 200,
    "msg": "success",
    "data": [
        {
            "id": 1,
            "title": "心理健康入门",
            "description": "适合初学者的心理健康基础知识课程，包含心理学基本概念、心理健康标准和常见心理问题识别方法。",
            "coverImageUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
            "targetTags": "[\"心理健康\", \"入门\", \"心理知识\"]",
            "videoCount": 10,
            "estimatedDurationMinutes": 120,
            "difficultyLevel": "BEGINNER",
            "status": "PUBLISHED",
            "createdTime": "2025-11-04T21:14:17",
            "updatedTime": "2025-11-04T21:16:09"
        },
        {
            "id": 2,
            "title": "情绪管理进阶",
            "description": "深入学习情绪管理技巧，包括压力应对、情绪调节和积极心理学应用，适合有一定心理学基础的学习者。",
            "coverImageUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
            "targetTags": "[\"情绪管理\", \"压力应对\", \"积极心理学\"]",
            "videoCount": 15,
            "estimatedDurationMinutes": 180,
            "difficultyLevel": "INTERMEDIATE",
            "status": "PUBLISHED",
            "createdTime": "2025-11-04T21:14:17",
            "updatedTime": "2025-11-04T21:16:11"
        },
        {
            "id": 3,
            "title": "心理咨询技术专题",
            "description": "专业心理咨询师必备的技术培训课程，涵盖个案概念化、咨询技巧和治疗方法，适合心理咨询从业者。",
            "coverImageUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
            "targetTags": "[\"心理咨询\", \"治疗技术\", \"专业成长\"]",
            "videoCount": 20,
            "estimatedDurationMinutes": 240,
            "difficultyLevel": "ADVANCED",
            "status": "DRAFT",
            "createdTime": "2025-11-04T21:14:17",
            "updatedTime": "2025-11-04T21:16:14"
        }
    ]
}
```

#### 失败响应
```json
{
    "code": 500,
    "msg": "获取学习包列表失败: 错误信息"
}
```

## 2. 添加学习包

### 2.1 接口说明
添加新的学习包，支持上传封面图片。

### 2.2 请求URL
`POST /api/learning-packages`

### 2.3 请求参数
请求类型为`multipart/form-data`，包含以下部分：

| 参数名 | 类型 | 必填 | 描述 |
| :--- | :--- | :--- | :--- |
| package | String | 是 | 学习包JSON字符串 |
| coverImage | File | 否 | 封面图片文件 |

#### package JSON格式
```json
{
    "title": "学习包标题", // 必填
    "description": "学习包描述", // 可选
    "targetTags": "[\"标签1\", \"标签2\"]", // 可选，JSON格式字符串
    "videoCount": 10, // 可选，默认0
    "estimatedDurationMinutes": 120, // 可选，默认0
    "difficultyLevel": "BEGINNER", // 可选，默认BEGINNER，可选值：BEGINNER, INTERMEDIATE, ADVANCED
    "status": "DRAFT" // 可选，默认DRAFT，可选值：DRAFT, PUBLISHED, ARCHIVED
}
```

### 2.4 响应数据

#### 成功响应
```json
{
    "code": 200,
    "msg": "success",
    "data": {
        "id": 1,
        "title": "心理健康入门",
        "description": "适合初学者的心理健康基础知识课程",
        "coverImageUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
        "targetTags": "[\"心理健康\", \"入门\", \"心理知识\"]",
        "videoCount": 10,
        "estimatedDurationMinutes": 120,
        "difficultyLevel": "BEGINNER",
        "status": "DRAFT",
        "createdTime": "2025-11-04T21:14:17",
        "updatedTime": "2025-11-04T21:14:17"
    }
}
```

#### 失败响应

##### 参数错误
```json
{
    "code": 400,
    "msg": "学习包信息不能为空"
}
```

```json
{
    "code": 400,
    "msg": "学习包标题不能为空"
}
```

##### 服务器错误
```json
{
    "code": 500,
    "msg": "学习包创建失败: 错误信息"
}
```

## 3. 数据类型说明

### 3.1 难度级别（difficulty_level）
- BEGINNER: 初级
- INTERMEDIATE: 中级
- ADVANCED: 高级

### 3.2 状态（status）
- DRAFT: 草稿
- PUBLISHED: 已发布
- ARCHIVED: 已归档

### 3.3 目标标签（target_tags）
JSON格式的字符串，包含标签数组，例如：`"[\"焦虑\",\"抑郁\",\"压力管理\"]"`

## 4. 示例代码

### 4.1 获取所有学习包列表（使用fetch API）

```javascript
fetch('/api/learning-packages')
  .then(response => response.json())
  .then(data => {
    if (data.code === 200) {
      console.log('学习包列表:', data.data);
    } else {
      console.error('获取失败:', data.msg);
    }
  })
  .catch(error => console.error('请求错误:', error));
```

### 4.2 添加学习包（使用fetch API）

```javascript
// 创建FormData对象
const formData = new FormData();

// 添加学习包JSON数据
const packageData = {
  title: '心理健康入门',
  description: '适合初学者的心理健康基础知识课程',
  targetTags: '[\"心理健康\", \"入门\", \"心理知识\"]',
  videoCount: 10,
  estimatedDurationMinutes: 120,
  difficultyLevel: 'BEGINNER',
  status: 'DRAFT'
};
formData.append('package', JSON.stringify(packageData));

// 如果有封面图片
// formData.append('coverImage', coverImageFile);

// 发送请求
fetch('/api/learning-packages', {
  method: 'POST',
  body: formData
})
.then(response => response.json())
.then(data => {
  if (data.code === 200) {
    console.log('学习包创建成功:', data.data);
  } else {
    console.error('创建失败:', data.msg);
  }
})
.catch(error => console.error('请求错误:', error));
```