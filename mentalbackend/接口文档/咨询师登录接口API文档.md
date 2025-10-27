# 咨询师登录接口API文档

## 1. 接口概述

**功能说明**：供心理咨询师账号登录系统，验证身份并获取访问令牌

**接口地址**：`/api/auth/counselor/login`

**请求方法**：`POST`

**内容类型**：`application/json`

## 2. 请求参数

请求体需包含以下JSON格式参数：

| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| username | String | 是 | 咨询师用户名，格式必须以"counselor_"开头 |
| password | String | 是 | 登录密码 |

**请求示例**：

```json
{
    "username": "counselor_zhangming",
    "password": "your_password_here"
}
```

## 3. 响应参数

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| success | Boolean | 登录是否成功 |
| token | String | 登录成功时返回的访问令牌 |
| message | String | 响应消息，登录失败时包含错误信息 |
| counselor | Object | 登录成功时返回的咨询师详细信息（见下表） |

### 咨询师信息结构（counselor字段）

| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| userId | Long | 用户ID |
| username | String | 用户名 |
| nickname | String | 用户昵称 |
| avatarUrl | String | 头像URL |
| counselorId | Long | 咨询师ID |
| realName | String | 真实姓名 |
| yearsOfExperience | Integer | 从业年限 |
| specialization | String | 擅长领域（JSON格式字符串） |
| therapeuticApproach | String | 治疗流派（JSON格式字符串） |
| introduction | String | 个人介绍 |
| consultationFee | BigDecimal | 咨询费用 |
| rating | BigDecimal | 平均评分 |
| totalSessions | Integer | 总咨询次数 |
| counselorStatus | String | 咨询师审核状态（APPROVED：已通过） |
| approvedTime | String | 审核通过时间 |

## 4. 响应示例

### 成功响应

```json
{
    "success": true,
    "token": "token_5a7b9c3d1e2f4g8h6i9j0k",
    "message": "登录成功",
    "counselor": {
        "userId": 11,
        "username": "counselor_zhangming",
        "nickname": "张明老师",
        "avatarUrl": "http://localhost:8080/files/download/1759307161163_efe5745b4caadb89fd5eade8cb165bc.jpg",
        "counselorId": 1,
        "realName": "张明",
        "yearsOfExperience": 8,
        "specialization": "[\"焦虑情绪\", \"抑郁情绪\", \"职场压力\"]",
        "therapeuticApproach": "[\"认知行为疗法\", \"人本主义\"]",
        "introduction": "国家二级心理咨询师，擅长认知行为疗法，帮助来访者识别和改变负面思维模式。",
        "consultationFee": 300.00,
        "rating": 4.80,
        "totalSessions": 150,
        "counselorStatus": "APPROVED",
        "approvedTime": "2024-01-15 10:00:00"
    }
}
```

### 失败响应

#### 用户名不存在

```json
{
    "success": false,
    "token": null,
    "message": "用户名不存在",
    "counselor": null
}
```

#### 密码错误

```json
{
    "success": false,
    "token": null,
    "message": "密码错误",
    "counselor": null
}
```

#### 非咨询师账号

```json
{
    "success": false,
    "token": null,
    "message": "该账号不是咨询师账号",
    "counselor": null
}
```

#### 账号被禁用

```json
{
    "success": false,
    "token": null,
    "message": "账号已被禁用",
    "counselor": null
}
```

#### 未通过审核

```json
{
    "success": false,
    "token": null,
    "message": "咨询师账号未通过审核",
    "counselor": null
}
```

## 5. 错误码说明

| 错误信息 | 说明 | 解决方法 |
| :--- | :--- | :--- |
| 用户名和密码不能为空 | 请求参数不完整 | 请提供有效的用户名和密码 |
| 用户名不存在 | 用户名未在系统中注册 | 请检查用户名是否输入正确 |
| 密码错误 | 密码输入错误 | 请重新输入正确的密码 |
| 该账号不是咨询师账号 | 用户名格式不正确 | 咨询师账号必须以"counselor_"开头 |
| 账号已被禁用 | 用户状态异常 | 请联系管理员处理 |
| 咨询师信息不存在 | 咨询师信息未配置 | 请联系管理员完善咨询师信息 |
| 咨询师账号未通过审核 | 咨询师审核状态为PENDING/REJECTED/SUSPENDED | 请等待审核通过或联系管理员查询审核状态 |
| 登录失败，请稍后重试 | 系统内部错误 | 请检查系统日志或稍后再试 |

## 6. 注意事项

1. 用户名必须以"counselor_"开头，否则将被判定为非咨询师账号
2. 系统支持明文密码和MD5加密密码两种验证方式
3. 只有状态为"ACTIVE"的用户和"APPROVED"状态的咨询师才能成功登录
4. 获取的token用于后续API调用的身份验证，请妥善保管
5. token有效期可在系统配置中设置，过期后需要重新登录
6. 建议在生产环境中使用HTTPS协议进行加密传输，保护密码安全

## 7. 安全措施

1. 密码验证支持多种加密方式，增强安全性
2. 登录失败有详细的错误信息提示，但不会泄露敏感信息
3. 登录过程包含详细的日志记录，便于安全审计
4. 系统对异常情况进行了合理的错误处理，避免信息泄露
5. 建议前端实现密码强度校验，增强用户密码安全性