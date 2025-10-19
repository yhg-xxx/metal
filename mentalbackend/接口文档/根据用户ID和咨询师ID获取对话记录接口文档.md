# 根据用户ID和咨询师ID获取对话记录接口文档

## 接口说明

该接口用于获取指定用户和咨询师之间的对话消息记录，支持分页查询。

## 接口路径

```
GET /api/consultation/messages/conversation
```

## 请求参数

| 参数名 | 类型 | 必填 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| userId | Long | 是 | 无 | 用户ID |
| counselorId | Long | 是 | 无 | 咨询师ID |
| limit | Integer | 否 | 50 | 每页记录数，默认50条 |
| offset | Integer | 否 | 0 | 偏移量，默认0 |

## 响应格式

### 成功响应

**HTTP状态码**: 200 OK

**响应体**: JSON数组，包含对话消息对象列表

每个消息对象包含以下字段：

| 字段名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long | 消息ID |
| appointmentId | Long | 预约ID（可选，可为null） |
| senderType | String | 发送者类型（USER：用户，COUNSELOR：咨询师） |
| messageType | String | 消息类型（TEXT：文本，IMAGE：图片，VOICE：语音，SYSTEM：系统消息） |
| content | String | 消息内容（文本消息时为文本内容，媒体消息时可为null） |
| mediaUrl | String | 媒体文件URL（仅媒体消息时有值，文本消息为null） |
| durationSeconds | Integer | 语音消息时长（仅语音消息时有值，其他消息为null） |
| sentTime | String | 发送时间（ISO 8601格式，如：2025-10-19T09:57:56） |
| readStatus | Boolean | 阅读状态（true：已读，false：未读） |
| userId | Long | 用户ID |
| counselorId | Long | 咨询师ID |
| conversationType | String | 会话类型（PRE_CONSULTATION：咨询前，IN_CONSULTATION：咨询中，FOLLOW_UP：随访） |

### 失败响应

**HTTP状态码**: 500 Internal Server Error

## 请求示例

```http
GET /api/consultation/messages/conversation?userId=26&counselorId=11&limit=50&offset=0
Accept: application/json
```

## 响应示例

### 成功

```json
[
    {
        "id": 3,
        "appointmentId": null,
        "senderType": "COUNSELOR",
        "messageType": "TEXT",
        "content": "对不起，你是个好人",
        "mediaUrl": null,
        "durationSeconds": null,
        "sentTime": "2025-10-19T09:57:56",
        "readStatus": false,
        "userId": 26,
        "counselorId": 11,
        "conversationType": "PRE_CONSULTATION"
    },
    {
        "id": 2,
        "appointmentId": null,
        "senderType": "USER",
        "messageType": "TEXT",
        "content": "我喜欢你",
        "mediaUrl": null,
        "durationSeconds": null,
        "sentTime": "2025-10-19T09:57:17",
        "readStatus": false,
        "userId": 26,
        "counselorId": 11,
        "conversationType": "PRE_CONSULTATION"
    }
]
```

### 失败

```
HTTP/1.1 500 Internal Server Error
```

## 实现说明

1. 接口通过调用 `consultationMessagesService.getConversationByUserAndCounselor()` 方法获取指定用户和咨询师之间的对话记录。
2. 系统会按消息发送时间倒序排列，最新的消息排在前面。
3. 接口支持分页查询，通过 `limit` 和 `offset` 参数控制返回的数据量。
4. 当请求参数不完整或出现异常时，返回500错误。
5. 系统会记录详细的操作日志，包括请求参数和执行结果。

## 注意事项

1. 请求参数中的 `userId` 和 `counselorId` 为必填项，且必须为有效的ID值。
2. 分页参数 `limit` 和 `offset` 用于控制返回的数据量，可根据实际需求调整。
3. 消息列表按发送时间倒序排列，最新消息在列表前面。
4. 接口目前包含了标记消息已读的注释代码，但尚未实现，未来可根据需求添加此功能。