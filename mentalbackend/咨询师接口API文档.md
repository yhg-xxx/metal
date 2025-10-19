# 咨询师信息管理系统API接口文档

## 1. 接口概述

本文档详细描述了咨询师信息管理系统的RESTful API接口，包括咨询师信息的查询、创建、更新和删除等功能。

## 2. 基础信息

- 接口基础路径：`/api/counselors`
- 数据格式：JSON
- 字符编码：UTF-8

## 3. 接口列表

| 接口名称 | 请求方法 | 请求路径 | 功能描述 |
|---------|---------|---------|---------|
| 搜索咨询师 | POST | `/api/counselors/search` | 根据条件搜索和筛选咨询师 |
| 获取咨询师详情 | GET | `/api/counselors/{id}` | 获取指定咨询师的详细信息 |
| 创建咨询师 | POST | `/api/counselors` | 创建新的咨询师信息 |
| 更新咨询师 | PUT | `/api/counselors` | 更新咨询师信息 |
| 删除咨询师 | DELETE | `/api/counselors/{id}` | 删除指定咨询师信息 |

## 4. 详细接口说明

### 4.1 搜索咨询师

**功能描述**：根据关键词和筛选条件查询咨询师列表

**请求URL**：`/api/counselors/search`

**请求方法**：POST

**请求参数**：

```json
{
    "keyword": "string", // 搜索关键词（姓名、擅长领域等）
    "specializationTags": ["string"], // 擅长领域标签列表
    "therapeuticApproachTags": ["string"], // 治疗流派标签列表
    "serviceTypeTags": ["string"], // 服务类型标签列表
    "genderFilter": "string" // 性别筛选（MALE/FEMALE/UNKNOWN）
}
```

**响应参数**：

```json
[
    {
        "counselorId": 1, // 咨询师ID
        "userId": 10, // 用户ID
        "realName": "张医生", // 真实姓名
        "username": "doctor_zhang", // 用户名
        "phone": "13800138000", // 手机号
        "email": "zhang@example.com", // 邮箱
        "gender": "MALE", // 性别
        "age": 35, // 年龄
        "qualificationCertificateUrl": "https://example.com/certificate/1", // 资质证书URL
        "practiceCertificateUrl": "https://example.com/practice/1", // 执业证书URL
        "photoUrl": "https://example.com/photo/1", // 证件照URL
        "yearsOfExperience": 10, // 从业年限
        "specialization": "[\"焦虑\",\"抑郁\"]", // 擅长领域
        "therapeuticApproach": "[\"认知行为\",\"人本主义\"]", // 治疗流派
        "introduction": "从事心理咨询工作10年...", // 个人介绍
        "consultationFee": 200.00, // 咨询费用
        "rating": 4.80, // 平均评分
        "totalSessions": 200, // 总咨询次数
        "counselorStatus": "APPROVED", // 咨询师状态
        "serviceTypes": "[\"文字\",\"语音\",\"视频\"]", // 服务类型
        "availableDays": "[\"周一\",\"周三\",\"周五\"]", // 可用日期
        "workingHours": "{\"morning\":[\"9:00\",\"12:00\"],\"afternoon\":[\"14:00\",\"18:00\"]}", // 工作时间段
        "sessionDurations": "[30,60,90]", // 支持的咨询时长
        "maxDailySessions": 5, // 每日最大咨询次数
        "createdTime": "2023-01-01T10:00:00", // 创建时间
        "updatedTime": "2023-01-01T10:00:00" // 更新时间
    }
]
```

**状态码**：
- 200：查询成功

### 4.2 获取咨询师详情

**功能描述**：获取指定ID的咨询师详细信息

**请求URL**：`/api/counselors/{id}`

**请求方法**：GET

**请求参数**：
- id：咨询师ID（路径参数）

**响应参数**：

```json
{
    "counselorId": 1, // 咨询师ID
    "userId": 10, // 用户ID
    "realName": "张医生", // 真实姓名
    "username": "doctor_zhang", // 用户名
    "phone": "13800138000", // 手机号
    "email": "zhang@example.com", // 邮箱
    "gender": "MALE", // 性别
    "age": 35, // 年龄
    "qualificationCertificateUrl": "https://example.com/certificate/1", // 资质证书URL
    "practiceCertificateUrl": "https://example.com/practice/1", // 执业证书URL
    "photoUrl": "https://example.com/photo/1", // 证件照URL
    "yearsOfExperience": 10, // 从业年限
    "specialization": "[\"焦虑\",\"抑郁\"]", // 擅长领域
    "therapeuticApproach": "[\"认知行为\",\"人本主义\"]", // 治疗流派
    "introduction": "从事心理咨询工作10年...", // 个人介绍
    "consultationFee": 200.00, // 咨询费用
    "rating": 4.80, // 平均评分
    "totalSessions": 200, // 总咨询次数
    "counselorStatus": "APPROVED", // 咨询师状态
    "serviceTypes": "[\"文字\",\"语音\",\"视频\"]", // 服务类型
    "availableDays": "[\"周一\",\"周三\",\"周五\"]", // 可用日期
    "workingHours": "{\"morning\":[\"9:00\",\"12:00\"],\"afternoon\":[\"14:00\",\"18:00\"]}", // 工作时间段
    "sessionDurations": "[30,60,90]", // 支持的咨询时长
    "maxDailySessions": 5, // 每日最大咨询次数
    "createdTime": "2023-01-01T10:00:00", // 创建时间
    "updatedTime": "2023-01-01T10:00:00" // 更新时间
}
```

**状态码**：
- 200：查询成功
- 404：咨询师不存在

### 4.3 创建咨询师

**功能描述**：创建新的咨询师信息

**请求URL**：`/api/counselors`

**请求方法**：POST

**请求参数**：

```json
{
    "username": "doctor_li", // 用户名
    "password": "encrypted_password", // 加密后的密码
    "phone": "13900139000", // 手机号
    "email": "li@example.com", // 邮箱
    "nickname": "李医生", // 用户昵称
    "gender": "FEMALE", // 性别
    "age": 32, // 年龄
    "realName": "李医生", // 真实姓名
    "idNumber": "310101199001011234", // 身份证号
    "qualificationCertificateUrl": "https://example.com/certificate/2", // 资质证书URL
    "practiceCertificateUrl": "https://example.com/practice/2", // 执业证书URL
    "photoUrl": "https://example.com/photo/2", // 证件照URL
    "yearsOfExperience": 8, // 从业年限
    "specialization": "[\"婚姻家庭\",\"青少年心理\"]", // 擅长领域
    "therapeuticApproach": "[\"家庭系统\",\"精神分析\"]", // 治疗流派
    "introduction": "专注于婚姻家庭咨询领域...", // 个人介绍
    "consultationFee": 180.00, // 咨询费用
    "serviceTypes": "[\"文字\",\"语音\"]", // 服务类型
    "availableDays": "[\"周二\",\"周四\",\"周六\"]", // 可用日期
    "workingHours": "{\"morning\":[\"10:00\",\"12:00\"],\"afternoon\":[\"15:00\",\"19:00\"]}", // 工作时间段
    "sessionDurations": "[45,60]", // 支持的咨询时长
    "maxDailySessions": 4 // 每日最大咨询次数
}
```

**响应参数**：

```json
true // 创建成功返回true，失败返回false
```

**状态码**：
- 200：创建成功

### 4.4 更新咨询师

**功能描述**：更新指定咨询师的信息

**请求URL**：`/api/counselors`

**请求方法**：PUT

**请求参数**：

```json
{
    "counselorId": 2, // 咨询师ID
    "userId": 11, // 用户ID
    "username": "doctor_li", // 用户名
    "phone": "13900139000", // 手机号
    "email": "li@example.com", // 邮箱
    "nickname": "李医生", // 用户昵称
    "gender": "FEMALE", // 性别
    "age": 33, // 年龄
    "realName": "李医生", // 真实姓名
    "idNumber": "310101199001011234", // 身份证号
    "qualificationCertificateUrl": "https://example.com/certificate/2", // 资质证书URL
    "practiceCertificateUrl": "https://example.com/practice/2", // 执业证书URL
    "photoUrl": "https://example.com/photo/2", // 证件照URL
    "yearsOfExperience": 9, // 从业年限
    "specialization": "[\"婚姻家庭\",\"青少年心理\"]", // 擅长领域
    "therapeuticApproach": "[\"家庭系统\",\"精神分析\"]", // 治疗流派
    "introduction": "专注于婚姻家庭咨询领域...", // 个人介绍
    "consultationFee": 200.00, // 咨询费用
    "serviceTypes": "[\"文字\",\"语音\",\"视频\"]", // 服务类型
    "availableDays": "[\"周二\",\"周四\",\"周六\"]", // 可用日期
    "workingHours": "{\"morning\":[\"10:00\",\"12:00\"],\"afternoon\":[\"15:00\",\"19:00\"]}", // 工作时间段
    "sessionDurations": "[45,60,90]", // 支持的咨询时长
    "maxDailySessions": 5 // 每日最大咨询次数
}
```

**响应参数**：

```json
true // 更新成功返回true，失败返回false
```

**状态码**：
- 200：更新成功

### 4.5 删除咨询师

**功能描述**：删除指定ID的咨询师信息

**请求URL**：`/api/counselors/{id}`

**请求方法**：DELETE

**请求参数**：
- id：咨询师ID（路径参数）

**响应参数**：

```json
true // 删除成功返回true，失败返回false
```

**状态码**：
- 200：删除成功
- 404：咨询师不存在

## 5. 数据类型说明

### 5.1 状态枚举

- 用户状态（userStatus）：
  - ACTIVE：活跃
  - INACTIVE：非活跃
  - BANNED：封禁

- 咨询师状态（counselorStatus）：
  - PENDING：待审核
  - APPROVED：已通过
  - REJECTED：已拒绝
  - SUSPENDED：已暂停

- 性别（gender）：
  - MALE：男
  - FEMALE：女
  - UNKNOWN：未知

### 5.2 JSON格式字段

以下字段以JSON字符串格式存储：
- specialization（擅长领域）：例如 `["焦虑","抑郁"]`
- therapeuticApproach（治疗流派）：例如 `["认知行为","人本主义"]`
- serviceTypes（服务类型）：例如 `["文字","语音","视频"]`
- availableDays（可用日期）：例如 `["周一","周三","周五"]`
- workingHours（工作时间段）：例如 `{"morning":["9:00","12:00"],"afternoon":["14:00","18:00"]}`
- sessionDurations（支持的咨询时长）：例如 `[30,60,90]`

## 6. 示例请求

### 6.1 搜索咨询师示例

**请求**：
```http
POST /api/counselors/search HTTP/1.1
Content-Type: application/json

{
    "keyword": "焦虑",
    "specializationTags": ["焦虑","抑郁"],
    "serviceTypeTags": ["视频"],
    "genderFilter": "MALE"
}
```

**响应**：
```http
HTTP/1.1 200 OK
Content-Type: application/json

[
    {
        "counselorId": 1,
        "userId": 10,
        "realName": "张医生",
        "username": "doctor_zhang",
        "phone": "13800138000",
        "email": "zhang@example.com",
        "gender": "MALE",
        "age": 35,
        "qualificationCertificateUrl": "https://example.com/certificate/1",
        "practiceCertificateUrl": "https://example.com/practice/1",
        "photoUrl": "https://example.com/photo/1",
        "yearsOfExperience": 10,
        "specialization": "[\"焦虑\",\"抑郁\"]",
        "therapeuticApproach": "[\"认知行为\",\"人本主义\"]",
        "introduction": "从事心理咨询工作10年...",
        "consultationFee": 200.00,
        "rating": 4.80,
        "totalSessions": 200,
        "counselorStatus": "APPROVED",
        "serviceTypes": "[\"文字\",\"语音\",\"视频\"]",
        "availableDays": "[\"周一\",\"周三\",\"周五\"]",
        "workingHours": "{\"morning\":[\"9:00\",\"12:00\"],\"afternoon\":[\"14:00\",\"18:00\"]}",
        "sessionDurations": "[30,60,90]",
        "maxDailySessions": 5,
        "createdTime": "2023-01-01T10:00:00",
        "updatedTime": "2023-01-01T10:00:00"
    }
]
```