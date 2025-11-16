# 孩子信息接口API文档

## 接口概述
本文档描述了孩子信息管理相关的RESTful API接口，包括新增孩子、修改孩子信息、设置当前操作孩子和获取当前操作孩子等功能。

## 基础路径
所有接口的基础路径为：`/api/children`

## 接口列表

### 1. 新增孩子信息

#### 接口描述
创建一个新的孩子信息记录。

#### 请求URL
`POST /api/children`

#### 请求参数
请求体 (JSON格式)：
| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | Long | 是 | 用户ID |
| name | String | 是 | 孩子姓名 |
| gender | String | 是 | 性别 |
| birthYearMonth | String | 否 | 出生年月 |
| ethnicity | String | 否 | 民族 |
| householdRegister | String | 否 | 户籍 |
| birthOrder | String | 否 | 家中排行 |
| birthPlace | String | 否 | 出生地 |
| languageEnvironment | String | 否 | 语言环境 |
| currentSchool | String | 否 | 就读学校/园 |
| homeAddress | String | 否 | 现家庭住址 |
| habits | String | 否 | 孩子睡眠爱好 |
| interestActivities | String | 否 | 孩子兴趣活动 |
| healthStatus | String | 否 | 身体状态 |
| healthDescription | String | 否 | 身体状态具体描述 |
| pastIllness | String | 否 | 过往病史 |
| pastIllnessDescription | String | 否 | 过往病史具体描述 |
| fatherPhone | String | 否 | 父亲电话 |
| motherPhone | String | 否 | 母亲电话 |
| guardianPhone | String | 否 | 监护人电话 |

#### 返回参数
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| code | Integer | 状态码，200表示成功，400表示参数错误，500表示服务器错误 |
| msg | String | 响应消息 |
| data | Object | 孩子信息对象 |

#### 示例请求
```json
{
  "userId": 1,
  "name": "小明",
  "gender": "男",
  "birthYearMonth": "2018-05",
  "ethnicity": "汉族",
  "currentSchool": "第一幼儿园"
}
```

#### 示例响应
成功：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "小明",
    "gender": "男",
    "birthYearMonth": "2018-05",
    "ethnicity": "汉族",
    "currentSchool": "第一幼儿园",
    "isCurrentOperation": true
  }
}
```

失败 - 参数错误：
```json
{
  "code": 400,
  "msg": "孩子姓名不能为空"
}
```

### 2. 修改孩子信息

#### 接口描述
更新指定孩子的信息。

#### 请求URL
`PUT /api/children`

#### 请求参数
请求体 (JSON格式)：
| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| id | Long | 是 | 孩子ID |
| name | String | 否 | 孩子姓名 |
| gender | String | 否 | 性别 |
| birthYearMonth | String | 否 | 出生年月 |
| ethnicity | String | 否 | 民族 |
| householdRegister | String | 否 | 户籍 |
| birthOrder | String | 否 | 家中排行 |
| birthPlace | String | 否 | 出生地 |
| languageEnvironment | String | 否 | 语言环境 |
| currentSchool | String | 否 | 就读学校/园 |
| homeAddress | String | 否 | 现家庭住址 |
| habits | String | 否 | 孩子睡眠爱好 |
| interestActivities | String | 否 | 孩子兴趣活动 |
| healthStatus | String | 否 | 身体状态 |
| healthDescription | String | 否 | 身体状态具体描述 |
| pastIllness | String | 否 | 过往病史 |
| pastIllnessDescription | String | 否 | 过往病史具体描述 |
| fatherPhone | String | 否 | 父亲电话 |
| motherPhone | String | 否 | 母亲电话 |
| guardianPhone | String | 否 | 监护人电话 |

#### 返回参数
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| code | Integer | 状态码，200表示成功，400表示参数错误，404表示孩子不存在，500表示服务器错误 |
| msg | String | 响应消息 |
| data | Object | 更新后的孩子信息对象 |

#### 示例请求
```json
{
  "id": 1,
  "name": "小明",
  "currentSchool": "第二幼儿园"
}
```

#### 示例响应
成功：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "小明",
    "gender": "男",
    "currentSchool": "第二幼儿园"
  }
}
```

失败 - 孩子不存在：
```json
{
  "code": 404,
  "msg": "孩子信息不存在或更新失败"
}
```

### 3. 设置当前操作孩子

#### 接口描述
设置用户当前正在操作的孩子。

#### 请求URL
`PUT /api/children/current`

#### 请求参数
查询参数：
| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | Long | 是 | 用户ID |
| childId | Long | 是 | 孩子ID |

#### 返回参数
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| code | Integer | 状态码，200表示成功，400表示参数错误，404表示孩子不存在或不属于该用户，500表示服务器错误 |
| msg | String | 响应消息 |

#### 示例请求
```
PUT /api/children/current?userId=1&childId=1
```

#### 示例响应
成功：
```json
{
  "code": 200,
  "msg": "success"
}
```

失败 - 孩子不属于该用户：
```json
{
  "code": 404,
  "msg": "设置当前操作孩子失败，孩子不存在或不属于该用户"
}
```

### 4. 获取当前操作孩子

#### 接口描述
获取用户当前正在操作的孩子信息。

#### 请求URL
`GET /api/children/current`

#### 请求参数
查询参数：
| 参数名 | 类型 | 必选 | 说明 |
| :--- | :--- | :--- | :--- |
| userId | Long | 是 | 用户ID |

#### 返回参数
| 参数名 | 类型 | 说明 |
| :--- | :--- | :--- |
| code | Integer | 状态码，200表示成功，400表示参数错误，404表示未设置当前操作孩子，500表示服务器错误 |
| msg | String | 响应消息 |
| data | Object | 当前操作的孩子信息对象 |

#### 示例请求
```
GET /api/children/current?userId=1
```

#### 示例响应
成功：
```json
{
  "code": 200,
  "msg": "success",
  "data": {
    "id": 1,
    "userId": 1,
    "name": "小明",
    "gender": "男",
    "birthYearMonth": "2018-05",
    "ethnicity": "汉族",
    "currentSchool": "第二幼儿园",
    "isCurrentOperation": true,
    "habits": "喜欢听故事入睡",
    "interestActivities": "绘画、搭积木",
    "healthStatus": "良好"
  }
}
```

失败 - 未设置当前操作孩子：
```json
{
  "code": 404,
  "msg": "未设置当前操作孩子"
}
```

## 错误码说明
| 错误码 | 说明 |
| :--- | :--- |
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 注意事项
1. 新增孩子信息时，userId、name和gender为必填字段
2. 修改孩子信息时，id为必填字段
3. 设置和获取当前操作孩子时，userId为必填字段
4. 每个用户只能有一个当前操作的孩子
5. 当新增孩子时，该孩子会自动成为用户的当前操作孩子