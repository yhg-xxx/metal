# 咨询预约接口API文档

## 1. 创建新预约

### 接口描述
用户选定咨询师后，选择咨询类型、时长及具体时间，创建新的咨询预约。

### 请求URL
`POST /api/appointments`

### 请求头
- `Content-Type: application/json`

### 请求体 (JSON)
```json
{
  "userId": 1,
  "counselorId": 2,
  "consultationType": "VIDEO",
  "durationMinutes": 60,
  "scheduledTime": "2024-06-20T14:00:00"
}
```

### 请求参数说明
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | Long | 是 | 用户ID |
| counselorId | Long | 是 | 咨询师ID |
| consultationType | String | 是 | 咨询类型：TEXT(文字)/VOICE(语音)/VIDEO(视频) |
| durationMinutes | Integer | 是 | 咨询时长（分钟） |
| scheduledTime | String | 是 | 预约时间，格式：yyyy-MM-dd'T'HH:mm:ss |

### 响应体 (JSON)
```json
{
  "id": 1,
  "userId": 1,
  "counselorId": 2,
  "consultationType": "VIDEO",
  "durationMinutes": 60,
  "scheduledTime": "2024-06-20T14:00:00",
  "fee": 300.00,
  "status": "PENDING",
  "paymentStatus": "PENDING",
  "createdTime": "2024-06-15T10:30:00",
  "userName": "张三",
  "counselorName": "李医生"
}
```

### 响应参数说明
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| id | Long | 预约ID |
| userId | Long | 用户ID |
| counselorId | Long | 咨询师ID |
| consultationType | String | 咨询类型 |
| durationMinutes | Integer | 咨询时长 |
| scheduledTime | String | 预约时间 |
| fee | BigDecimal | 咨询费用 |
| status | String | 预约状态：PENDING(待确认)/CONFIRMED(已确认)/IN_PROGRESS(进行中)/COMPLETED(已完成)/CANCELLED(已取消)/NO_SHOW(未到场) |
| paymentStatus | String | 支付状态：PENDING(待支付)/PAID(已支付)/REFUNDED(已退款) |
| createdTime | String | 创建时间 |
| userName | String | 用户名称（扩展信息） |
| counselorName | String | 咨询师名称（扩展信息） |

### 错误码说明
| 错误码 | 说明 |
| :--- | :--- |
| 400 | 参数错误，如无效的咨询类型、咨询时长小于等于0等 |
| 404 | 咨询师不存在或未通过审核 |
| 409 | 该时间段已被预约，时间冲突 |

## 2. 获取预约详情

### 接口描述
根据预约ID获取预约详情。

### 请求URL
`GET /api/appointments/{id}`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 预约ID |

### 响应体 (JSON)
```json
{
  "id": 1,
  "userId": 1,
  "counselorId": 2,
  "consultationType": "VIDEO",
  "durationMinutes": 60,
  "scheduledTime": "2024-06-20T14:00:00",
  "actualStartTime": "2024-06-20T14:00:00",
  "actualEndTime": "2024-06-20T15:00:00",
  "fee": 300.00,
  "status": "COMPLETED",
  "paymentStatus": "PAID",
  "paymentTime": "2024-06-18T09:00:00",
  "createdTime": "2024-06-15T10:30:00",
  "userName": "张三",
  "userPhone": "13800138000",
  "counselorName": "李医生",
  "counselorPhone": "13900139000"
}
```

## 3. 获取用户的预约列表

### 接口描述
根据用户ID获取该用户的所有预约记录。

### 请求URL
`GET /api/appointments/user/{userId}`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | Long | 是 | 用户ID |

### 响应体 (JSON)
```json
[
  {
    "id": 1,
    "userId": 1,
    "counselorId": 2,
    "consultationType": "VIDEO",
    "durationMinutes": 60,
    "scheduledTime": "2024-06-20T14:00:00",
    "fee": 300.00,
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "createdTime": "2024-06-15T10:30:00",
    "counselorName": "李医生"
  }
]
```

## 4. 获取咨询师的预约列表

### 接口描述
根据咨询师ID获取该咨询师的所有预约记录。

### 请求URL
`GET /api/appointments/counselor/{counselorId}`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| counselorId | Long | 是 | 咨询师ID |

### 响应体 (JSON)
```json
[
  {
    "id": 1,
    "userId": 1,
    "counselorId": 2,
    "consultationType": "VIDEO",
    "durationMinutes": 60,
    "scheduledTime": "2024-06-20T14:00:00",
    "fee": 300.00,
    "status": "PENDING",
    "paymentStatus": "PENDING",
    "createdTime": "2024-06-15T10:30:00",
    "userName": "张三"
  }
]
```

## 5. 更新预约状态

### 接口描述
更新预约的状态。

### 请求URL
`PUT /api/appointments/{id}/status`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 预约ID |

### 查询参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| status | String | 是 | 新状态：PENDING/CONFIRMED/IN_PROGRESS/COMPLETED/CANCELLED/NO_SHOW |

### 响应体 (JSON)
```json
true
```

## 6. 更新支付状态

### 接口描述
更新预约的支付状态。

### 请求URL
`PUT /api/appointments/{id}/payment`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 预约ID |

### 查询参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| paymentStatus | String | 是 | 支付状态：PENDING/PAID/REFUNDED |

### 响应体 (JSON)
```json
true
```

## 7. 取消预约

### 接口描述
取消指定的预约。只有待确认和已确认的预约可以被取消。

### 请求URL
`PUT /api/appointments/{id}/cancel`

### 路径参数
| 参数名 | 类型 | 必填 | 说明 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 预约ID |

### 响应体 (JSON)
```json
true
```

## 8. 验证时间槽是否可用

### 接口描述
验证指定咨询师在特定时间段是否可用。

### 请求URL
`POST /api/appointments/validate-time-slot`

### 请求体 (JSON)
```json
{
  "counselorId": 2,
  "durationMinutes": 60,
  "scheduledTime": "2024-06-20T14:00:00"
}
```

### 响应体 (JSON)
```json
true
```

## 数据类型说明

### 咨询类型 (consultationType)
- `TEXT`: 文字咨询
- `VOICE`: 语音咨询
- `VIDEO`: 视频咨询

### 预约状态 (status)
- `PENDING`: 待确认
- `CONFIRMED`: 已确认
- `IN_PROGRESS`: 进行中
- `COMPLETED`: 已完成
- `CANCELLED`: 已取消
- `NO_SHOW`: 未到场

### 支付状态 (paymentStatus)
- `PENDING`: 待支付
- `PAID`: 已支付
- `REFUNDED`: 已退款

## 业务流程说明

1. **预约创建流程**：
   - 用户选择咨询师、咨询类型、时长和时间
   - 系统验证时间槽是否可用
   - 系统计算咨询费用
   - 创建预约记录，状态为待确认
   - 用户进行支付
   - 支付成功后，状态更新为已确认

2. **预约状态流转**：
   - 待确认(PENDING) → 已确认(CONFIRMED) → 进行中(IN_PROGRESS) → 已完成(COMPLETED)
   - 待确认(PENDING)/已确认(CONFIRMED) → 已取消(CANCELLED)
   - 已确认(CONFIRMED) → 未到场(NO_SHOW)

3. **支付状态流转**：
   - 待支付(PENDING) → 已支付(PAID)
   - 已支付(PAID) → 已退款(REFUNDED)（取消预约时）