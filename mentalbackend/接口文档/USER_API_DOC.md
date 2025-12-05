# 用户相关接口文档

## 1. 手机号注册状态检查接口

### 接口路径
`GET /api/users/check/{phone}`

### 功能描述
查询指定手机号在远程数据库中的注册状态，仅返回存在性信息

### 请求参数
| 参数名 | 类型 | 位置 | 必填 | 描述 |
|--------|------|------|------|------|
| phone | String | 路径参数 | 是 | 待查询的手机号 |

### 请求示例
```bash
GET /api/users/check/13812345678
```

### 响应格式
- 成功响应：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "exists": true/false
  }
}
```

- 失败响应：
```json
{
  "code": 400/500,
  "message": "错误信息",
  "data": null
}
```

### 响应示例
- 手机号已注册：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "exists": true
  }
}
```

- 手机号未注册：
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "exists": false
  }
}
```

### 错误码说明
| 错误码 | 描述 |
|--------|------|
| 400 | 无效手机号格式（空手机号） |
| 500 | 服务器异常 |

## 2. 用户注册接口

### 接口路径
`POST /api/users/register`

### 功能描述
新用户注册功能，实现用户信息的创建与存储

### 请求体
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | MD5加密后的密码 |
| username | String | 否 | 用户名（如未提供需自动生成） |
| nickname | String | 否 | 昵称 |
| email | String | 否 | 邮箱 |
| avatarUrl | String | 否 | 头像URL |
| gender | String | 否 | 性别（MALE/FEMALE） |
| age | Integer | 否 | 年龄 |

### 请求体示例
```json
{
  "phone": "13812345678",
  "password": "e10adc3949ba59abbe56e057f20f883e",
  "username": "用户12345",
  "nickname": "我的昵称"
}
```

### 响应格式
- 成功响应：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "用户12345",
    "phone": "13812345678",
    "email": null,
    "nickname": "我的昵称",
    "avatarUrl": null,
    "gender": null,
    "age": null,
    "status": "ACTIVE",
    "createdTime": "2025-12-04T12:00:00",
    "updatedTime": "2025-12-04T12:00:00"
  }
}
```

- 失败响应：
```json
{
  "code": 400/409/500,
  "message": "错误信息",
  "data": null
}
```

### 响应示例
- 注册成功：
```json
{
  "code": 200,
  "message": "注册成功",
  "data": {
    "id": 1,
    "username": "用户138123",
    "phone": "13812345678",
    "email": null,
    "nickname": null,
    "avatarUrl": null,
    "gender": null,
    "age": null,
    "status": "ACTIVE",
    "createdTime": "2025-12-04T20:30:45",
    "updatedTime": "2025-12-04T20:30:45"
  }
}
```

### 错误码说明
| 错误码 | 描述 |
|--------|------|
| 400 | 请求参数缺失或无效（用户信息为空、手机号为空、密码为空） |
| 409 | 手机号已注册 |
| 500 | 注册失败（服务器异常） |

## 3. 用户登录接口

### 接口路径
`POST /api/users/login`

### 功能描述
验证用户手机号和密码并返回用户信息

### 请求体
| 参数名 | 类型 | 必填 | 描述 |
|--------|------|------|------|
| phone | String | 是 | 手机号 |
| password | String | 是 | MD5加密后的密码 |

### 请求体示例
```json
{
  "phone": "13812345678",
  "password": "e10adc3949ba59abbe56e057f20f883e"
}
```

### 响应格式
- 成功响应：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "用户138123",
    "phone": "13812345678",
    "email": null,
    "nickname": null,
    "avatarUrl": null,
    "gender": null,
    "age": null,
    "status": "ACTIVE",
    "createdTime": "2025-12-04T12:00:00",
    "updatedTime": "2025-12-04T12:00:00"
  }
}
```

- 失败响应：
```json
{
  "code": 400/401/500,
  "message": "错误信息",
  "data": null
}
```

### 响应示例
- 登录成功：
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "id": 1,
    "username": "用户138123",
    "phone": "13812345678",
    "email": null,
    "nickname": null,
    "avatarUrl": null,
    "gender": null,
    "age": null,
    "status": "ACTIVE",
    "createdTime": "2025-12-04T20:30:45",
    "updatedTime": "2025-12-04T20:30:45"
  }
}
```

- 用户不存在：
```json
{
  "code": 401,
  "message": "用户不存在",
  "data": null
}
```

- 密码错误：
```json
{
  "code": 401,
  "message": "密码错误",
  "data": null
}
```

### 错误码说明
| 错误码 | 描述 |
|--------|------|
| 400 | 请求参数缺失（手机号为空、密码为空） |
| 401 | 认证失败（用户不存在、密码错误） |
| 500 | 登录失败（服务器异常） |

## 接口开发约束说明

1. **认证系统区分**：用户登录系统与现有咨询师登录系统完全独立，两套认证逻辑互不影响

2. **响应格式统一**：所有接口响应均使用Result.java工具类统一包装，包含code、message和data字段

3. **密码安全存储**：用户密码采用MD5哈希算法存储，禁止明文存储

4. **输入验证机制**：所有接口均实现完善的输入验证，确保参数有效性

5. **错误处理机制**：针对不同业务场景返回相应的错误码和错误信息，便于客户端处理
