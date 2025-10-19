# 快速咨询申请接口API文档

## 1. 添加快速咨询申请接口

### 接口地址
`POST /api/quick-consultation`

### 接口描述
用户提交快速咨询申请，系统记录申请信息并设置状态为待处理，同时自动尝试智能匹配适合的咨询师，并支持图片上传和文字识别功能。

### 请求参数
**Content-Type: multipart/form-data**

| 参数名 | 类型 | 是否必选 | 描述 |
|--------|------|----------|------|
| userId | Long | 是 | 用户ID，外键关联users表 |
| problemDescription | String | 是 | 问题描述 |
| problemDuration | String | 是 | 问题持续时间 |
| preferredMethod | String | 是 | 偏好咨询方式：TEXT（文字）/VOICE（语音）/VIDEO（视频） |
| files | File[] | 否 | 上传的图片文件数组 |
| matchedCounselorId | Long | 否 | 匹配的咨询师ID，外键关联counselors表 |

### 响应示例

**成功响应（已匹配到咨询师）：**
```json
{
  "code": 200,
  "msg": "申请成功并匹配到咨询师",
  "data": {
    "id": 1,
    "userId": 123,
    "problemDescription": "最近工作压力很大，晚上失眠严重...",
    "problemDuration": "2周",
    "preferredMethod": "TEXT",
    "attachedImages": "[{\"url\":\"http://localhost:8080/files/download/1234567890_image.jpg\",\"recognizedText\":\"这是图片中的文字内容\"},{\"url\":\"http://localhost:8080/files/download/1234567891_another.jpg\",\"recognizedText\":\"这是另一张图片的文字\"}]",
    "matchedCounselorId": 456,
    "status": "MATCHED",
    "createdTime": "2025-10-14T08:30:00",
    "matchedTime": "2025-10-14T08:30:05"
  }
}
```

**成功响应（待匹配咨询师）：**
```json
{
  "code": 200,
  "msg": "申请成功，正在为您匹配咨询师",
  "data": {
    "id": 1,
    "userId": 123,
    "problemDescription": "最近工作压力很大，晚上失眠严重...",
    "problemDuration": "2周",
    "preferredMethod": "TEXT",
    "attachedImages": "[{\"url\":\"http://localhost:8080/files/download/1234567890_image.jpg\",\"recognizedText\":\"这是图片中的文字内容\"}]",
    "matchedCounselorId": null,
    "status": "PENDING",
    "createdTime": "2025-10-14T08:30:00",
    "matchedTime": null
  }
}
```

**失败响应（缺少必填字段）：**
```json
{
  "code": 400,
  "msg": "用户ID不能为空"
}
```

**失败响应（系统错误）：**
```json
{
  "code": 500,
  "msg": "快速咨询申请添加失败: 数据库连接错误"
}
```

## 2. 根据用户ID查询匹配的咨询师列表接口

### 接口地址
`GET /api/quick-consultation/matched-counselors`

### 接口描述
根据用户ID查询该用户所有已匹配的咨询师列表，系统会查询快速咨询表中userId=useId且status不是PENDING的matched_counselor_id并去重，返回完整的咨询师详细信息。

### 请求参数

| 参数名 | 类型 | 是否必选 | 描述 |
|--------|------|----------|------|
| useId | Long | 是 | 用户ID，外键关联users表 |

### 响应示例

**成功响应：**
```json
{
  "msg": "查询成功", 
  "code": 200, 
  "data": [ 
    {
      "userId": 21,
      "username": "counselor_zhenghao",
      "phone": "13900139011",
      "email": "zhenghao@psy.com",
      "nickname": "郑浩老师",
      "avatarUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
      "gender": "MALE",
      "age": 38,
      "userStatus": "ACTIVE",
      "counselorId": 11,
      "realName": "郑浩",
      "idNumber": "110101199011110111",
      "qualificationCertificateUrl": "/certs/zhenghao_qualification.jpg",
      "practiceCertificateUrl": "/certs/zhenghao_practice.jpg",
      "photoUrl": "/photos/zhenghao.jpg",
      "yearsOfExperience": 8,
      "specialization": "[\"抑郁情绪\", \"睡眠问题\", \"情绪调节\"]",
      "therapeuticApproach": "[\"认知行为疗法\", \"正念认知疗法\"]",
      "introduction": "专注于抑郁症的认知行为治疗和睡眠问题。",
      "consultationFee": 310.00,
      "rating": 4.70,
      "totalSessions": 140,
      "counselorStatus": null,
      "approvedTime": "2024-01-25T16:30:00",
      "createdTime": "2025-09-30T15:45:37",
      "updatedTime": "2025-09-30T15:45:37",
      "serviceSettingsId": 26,
      "serviceTypes": "[\"VOICE\", \"VIDEO\"]",
      "availableDays": "[\"MONDAY\", \"WEDNESDAY\", \"FRIDAY\", \"SUNDAY\"]",
      "workingHours": "{\"end\": \"23:00\", \"start\": \"15:00\"}",
      "sessionDurations": "[60, 90]",
      "maxDailySessions": 3,
      "keyword": null,
      "specializationTags": null,
      "therapeuticApproachTags": null,
      "serviceTypeTags": null,
      "genderFilter": null
    }
  ]
}
```

**成功响应（无数据）：**
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": []
}
```

**失败响应（参数错误）：**
```json
{
  "code": 400,
  "msg": "用户ID不能为空"
}
```

**失败响应（系统错误）：**
```json
{
  "code": 500,
  "msg": "查询失败: 数据库查询错误"
}
```

## 3. 智能匹配咨询师功能说明

### 功能描述
系统会根据用户提交的快速咨询申请信息（包括问题描述和图片中的识别文字），智能提取关键词并分析心理问题类型，然后匹配擅长对应领域的咨询师。

### 匹配规则
1. 系统从问题描述和图片OCR识别文本中提取关键词
2. 根据关键词将问题分类到不同的心理领域（如焦虑情绪、抑郁情绪、职场压力等）
3. 查询擅长对应领域的已审核通过的咨询师
4. 计算每个咨询师的匹配分数，选择分数最高的咨询师进行匹配
5. 更新咨询申请状态为MATCHED，并记录匹配时间

### 支持的心理领域
- 焦虑情绪：焦虑、紧张、担忧等相关问题
- 抑郁情绪：抑郁、情绪低落、兴趣减退等相关问题
- 职场压力：工作压力、职业倦怠、职场人际关系等相关问题
- 婚姻家庭：婚姻问题、家庭矛盾、亲密关系等相关问题
- 亲子关系：亲子沟通、青少年教育、儿童心理等相关问题
- 人际关系：社交障碍、人际沟通、边界设置等相关问题
- 情绪管理：情绪调节、压力管理、愤怒控制等相关问题
- 睡眠问题：失眠、睡眠质量差、作息不规律等相关问题

## 4. 注意事项

1. 添加咨询申请接口会自动设置申请状态为`PENDING`（待处理），并尝试智能匹配咨询师
2. 如果成功匹配到咨询师，状态会自动更新为`MATCHED`
3. 创建时间和匹配时间会自动设置为当前时间
4. `preferredMethod`字段支持的值为：`TEXT`（文字）、`VOICE`（语音）、`VIDEO`（视频）
5. 所有日期时间格式为ISO 8601格式：`yyyy-MM-dd'T'HH:mm:ss`
6. 响应中的`code`字段表示操作结果，200表示成功，其他值表示失败
7. `attachedImages`字段存储JSON格式的字符串，包含上传图片的URL和识别的文字内容
8. 上传图片时，系统会自动调用图片文字识别功能，即使识别失败也不会影响主流程
9. 查询匹配咨询师接口会过滤掉状态为PENDING的记录，并对咨询师ID进行去重处理