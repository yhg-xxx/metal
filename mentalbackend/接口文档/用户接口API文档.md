# 用户接口API文档

## 1. 新增用户接口

### 接口地址
`POST /api/users`

### 接口描述
创建新用户，支持上传头像文件

### 请求参数
**Content-Type: multipart/form-data**

| 参数名 | 类型 | 是否必选 | 描述 |
|--------|------|----------|------|
| user | String | 是 | 用户信息JSON字符串 |
| avatar | File | 否 | 用户头像文件 |

**user JSON结构说明：**

| 字段名 | 类型 | 是否必选 | 描述 |
|--------|------|----------|------|
| username | String | 否 | 用户名 |
| password | String | 否 | 密码 |
| phone | String | 是 | 手机号（必填） |
| email | String | 否 | 邮箱 |
| nickname | String | 否 | 昵称 |
| gender | String | 否 | 性别 |
| age | Integer | 否 | 年龄 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "password": "encrypted_password",
    "phone": "13800138000",
    "email": "test@example.com",
    "nickname": "测试用户",
    "avatarUrl": "http://localhost:8080/files/download/1730000000000_avatar.jpg",
    "gender": "MALE",
    "age": 25,
    "status": "ACTIVE",
    "createdTime": "2025-10-01T15:42:38",
    "updatedTime": "2025-10-01T15:42:38"
  }
}
```

**失败响应：**
```json
{
  "code": 400,
  "msg": "用户信息不能为空"
}
```

## 2. 修改用户接口

### 接口地址
`PUT /api/users`

### 接口描述
修改现有用户信息，支持上传新的头像文件

### 请求参数
**查询参数：**
| 参数名 | 类型 | 是否必选 | 描述 |
|--------|------|----------|------|
| phone | String | 是 | 用户手机号 |

**Content-Type: multipart/form-data**

| 参数名 | 类型 | 是否必选 | 描述 |
|--------|------|----------|------|
| user | String | 否 | 用户信息JSON字符串 |
| avatar | File | 否 | 用户头像文件 |

**user JSON结构说明：**

| 字段名 | 类型 | 是否必选 | 描述 |
|--------|------|----------|------|
| username | String | 否 | 用户名 |
| password | String | 否 | 密码 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| nickname | String | 否 | 昵称 |
| avatarUrl | String | 否 | 头像URL |
| gender | String | 否 | 性别 |
| age | Integer | 否 | 年龄 |
| status | String | 否 | 状态 |

### 响应示例

**成功响应：**
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "username": "updateduser",
    "password": "updated_encrypted_password",
    "phone": "13900139000",
    "email": "updated@example.com",
    "nickname": "已更新用户",
    "avatarUrl": "http://localhost:8080/files/download/1730000100000_new_avatar.jpg",
    "gender": "MALE",
    "age": 26,
    "status": "ACTIVE",
    "createdTime": "2025-10-01T15:42:38",
    "updatedTime": "2025-10-01T16:00:00"
  }
}
```

**失败响应：**
```json
{
  "code": 404,
  "msg": "用户不存在"
}
```

## 3. 注意事项

1. 新增用户时，`user`参数为必填项，且其中的`phone`字段为必填项，其他字段和`avatar`为可选项
2. 修改用户时，`phone`查询参数为必填项，`user`和`avatar`均为可选项，但至少需要提供其中一个
3. 头像文件上传后，会使用系统现有的文件上传服务进行处理，并返回文件的访问URL
4. 所有日期时间格式为ISO 8601格式：`yyyy-MM-dd'T'HH:mm:ss`
5. 响应中的`code`字段表示操作结果，200表示成功，其他值表示失败