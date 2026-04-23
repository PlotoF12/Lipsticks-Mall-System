# 口红商城系统 RESTful API 接口文档

> 基础路径: `/api`  
> 统一响应格式: `{ "success": boolean, "message": string, "data": T | null }`  
> 认证方式: Bearer Token (JWT)，在请求头 `Authorization: Bearer <token>` 中携带

---

## 1. 认证模块 `/api/auth`

### 1.1 用户注册

- **POST** `/api/auth/register`
- 权限: 公开
- 请求体:

```json
{
  "username": "string (必填, 最长64)",
  "password": "string (必填, 6~64位)"
}
```

- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

- 失败响应:

```json
{
  "success": false,
  "message": "用户名已存在",
  "data": null
}
```

---

### 1.2 用户登录

- **POST** `/api/auth/login`
- 权限: 公开
- 请求体:

```json
{
  "username": "string (必填)",
  "password": "string (必填)"
}
```

- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "username": "user",
    "role": "USER"
  }
}
```

- 失败响应:

```json
{
  "success": false,
  "message": "用户名或密码错误",
  "data": null
}
```

---

### 1.3 获取当前用户信息

- **GET** `/api/auth/me`
- 权限: 已登录
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "username": "user",
    "role": "USER",
    "gender": "female",
    "skinTone": "neutral",
    "skinType": "normal"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| username | string | 用户名 |
| role | string | 角色: USER / ADMIN |
| gender | string\|null | 性别: male / female |
| skinTone | string\|null | 肤色色调: warm / cool / neutral |
| skinType | string\|null | 肤质: oily / dry / normal / combination |

---

### 1.4 更新用户画像

- **PUT** `/api/auth/me`
- 权限: 已登录
- 请求体:

```json
{
  "gender": "female",
  "skinTone": "neutral",
  "skinType": "normal"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| gender | string | 否 | 最大16字符 |
| skinTone | string | 否 | 最大32字符 |
| skinType | string | 否 | 最大32字符 |

- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

---

### 1.5 修改密码

- **POST** `/api/auth/me/password`
- 权限: 已登录
- 请求体:

```json
{
  "oldPassword": "string (必填)",
  "newPassword": "string (必填, 6~64位)"
}
```

- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

---

## 2. 主页模块 `/api/home`

### 2.1 获取功能模块列表

- **GET** `/api/home/modules`
- 权限: 公开
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": [
    { "name": "Lipstick Mall", "path": "/user/mall" },
    { "name": "Product Recommend", "path": "/user/recommend" },
    { "name": "Color Visualization", "path": "/user/visualization" }
  ]
}
```

---

## 3. 商城模块 `/api/mall`

### 3.1 获取商品列表

- **GET** `/api/mall/products`
- 权限: 公开
- 查询参数:

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| keyword | string | 否 | 关键词搜索(标题/品牌/色号) |
| brand | string | 否 | 按品牌筛选 |
| category | string | 否 | 按色系分类筛选 |

- 无参数时返回全部在售商品；传入任一参数时执行搜索/筛选
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "title": "Dior 999 经典正红",
      "brand": "Dior",
      "shade": "999",
      "colorHex": "#B3123F",
      "category": "red",
      "finishType": "matte",
      "detail": "经典正红色，气场全开...",
      "price": 350,
      "stock": 50,
      "onSale": true,
      "suitableSkinTone": "warm,neutral,cool",
      "suitableGender": "female",
      "scene": "职场,派对,重要场合",
      "imageUrl": "/images/dior-999.jpg"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 商品ID |
| title | string | 商品标题 |
| brand | string\|null | 品牌 |
| shade | string\|null | 色号 |
| colorHex | string\|null | 颜色十六进制值 |
| category | string\|null | 色系分类: red / red_brown / tomato / bean_paste / pink / rose / rose_brown / nude |
| finishType | string\|null | 质地: matte / gloss / satin |
| detail | string\|null | 商品详情 |
| price | number | 价格(元) |
| stock | number | 库存 |
| onSale | boolean | 是否上架 |
| suitableSkinTone | string\|null | 适合肤色(逗号分隔) |
| suitableGender | string\|null | 适合性别(逗号分隔) |
| scene | string\|null | 适用场景(逗号分隔) |
| imageUrl | string\|null | 商品图片路径 |

---

### 3.2 获取商品详情

- **GET** `/api/mall/products/{id}`
- 权限: 公开
- 路径参数:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 商品ID |

- 成功响应: 同 3.1 中的单个商品对象
- 失败响应(商品不存在或已下架):

```json
{
  "success": false,
  "message": "商品不存在或已下架",
  "data": null
}
```

---

### 3.3 获取品牌列表

- **GET** `/api/mall/brands`
- 权限: 公开
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": ["Dior", "YSL", "Armani", "Chanel", "MAC", "Tom Ford", "NARS"]
}
```

---

### 3.4 获取色系分类列表

- **GET** `/api/mall/categories`
- 权限: 公开
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": ["red", "red_brown", "tomato", "bean_paste", "pink", "rose", "rose_brown", "nude"]
}
```

---

## 4. 推荐模块 `/api/recommend`

### 4.1 获取推荐列表

- **GET** `/api/recommend`
- 权限: 公开(登录后可获得个性化推荐)
- 查询参数:

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| target | string | 否 | girlfriend | 推荐目标: girlfriend(为女朋友推荐) / self(为自己推荐) |

- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "productId": 1,
      "title": "Dior 999 经典正红",
      "brand": "Dior",
      "shade": "999",
      "colorHex": "#B3123F",
      "category": "red",
      "price": 350,
      "reason": "Dior 999 经典正红——女性日常场景适配，经典正红色百搭不挑人，最稳妥选择"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| productId | number | 商品ID |
| title | string | 商品标题 |
| brand | string | 品牌 |
| shade | string | 色号 |
| colorHex | string | 颜色十六进制值 |
| category | string | 色系分类 |
| price | number | 价格(元) |
| reason | string | 推荐理由 |

---

## 5. 颜色可视化模块 `/api/visualization`

### 5.1 获取全部色板

- **GET** `/api/visualization/palette`
- 权限: 公开
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "productId": 1,
      "title": "Dior 999 经典正红",
      "brand": "Dior",
      "shade": "999",
      "colorHex": "#B3123F",
      "category": "red"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| productId | number | 商品ID |
| title | string | 商品标题 |
| brand | string | 品牌 |
| shade | string | 色号 |
| colorHex | string | 颜色十六进制值 |
| category | string | 色系分类 |

---

### 5.2 按品牌分组色板

- **GET** `/api/visualization/palette/by-brand`
- 权限: 公开
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "Dior": [
      { "productId": 1, "title": "...", "brand": "Dior", "shade": "999", "colorHex": "#B3123F", "category": "red" }
    ],
    "YSL": [...]
  }
}
```

---

### 5.3 按色系分组色板

- **GET** `/api/visualization/palette/by-category`
- 权限: 公开
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "red": [
      { "productId": 1, "title": "...", "brand": "Dior", "shade": "999", "colorHex": "#B3123F", "category": "red" }
    ],
    "nude": [...]
  }
}
```

---

## 6. 试妆模块 `/api/tryon`

### 6.1 上传照片试妆

- **POST** `/api/tryon/upload`
- 权限: 已登录
- Content-Type: `multipart/form-data`
- 请求参数:

| 参数 | 类型 | 位置 | 必填 | 说明 |
|------|------|------|------|------|
| productId | number | Query | 是 | 试妆口红商品ID |
| file | File | Form (part名: file) | 是 | 用户脸部图片 |

- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": {
    "productId": 1,
    "productTitle": "Dior 999 经典正红",
    "colorHex": "#B3123F",
    "input": "face.jpg",
    "output": "tryon_1710000000000_1.png",
    "message": "已为口红色号【999】(#B3123F)生成试妆效果。当前为演示模式，实际部署可对接面部识别与口红叠加算法。"
  }
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| productId | number | 商品ID |
| productTitle | string | 商品标题 |
| colorHex | string | 口红颜色值 |
| input | string | 上传的原始文件名 |
| output | string | 试妆结果文件名 |
| message | string | 试妆结果说明 |

---

## 7. 管理端 - 商品管理 `/api/admin/products`

> 以下接口均需要 ADMIN 角色

### 7.1 获取全部商品(含下架)

- **GET** `/api/admin/products`
- 权限: ADMIN
- 成功响应: 同 3.1 商品对象数组，但包含已下架商品

---

### 7.2 上架新商品

- **POST** `/api/admin/products`
- 权限: ADMIN
- 请求体:

```json
{
  "title": "string (必填, 最长128)",
  "brand": "string (可选, 最长64)",
  "shade": "string (可选, 最长64)",
  "colorHex": "string (可选, 最长16)",
  "category": "string (可选, 最长32)",
  "finishType": "string (可选, 最长32)",
  "detail": "string (可选, 最长2000)",
  "price": "number (必填)",
  "stock": "number (必填)",
  "onSale": "boolean (必填)",
  "suitableSkinTone": "string (可选, 最长64)",
  "suitableGender": "string (可选, 最长32)",
  "scene": "string (可选, 最长128)",
  "imageUrl": "string (可选, 最长512)"
}
```

- 成功响应: 返回创建的商品对象

---

### 7.3 编辑商品

- **PUT** `/api/admin/products/{id}`
- 权限: ADMIN
- 路径参数:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 商品ID |

- 请求体: 同 7.2
- 成功响应: 返回更新后的商品对象

---

## 8. 管理端 - 用户管理 `/api/admin/users`

> 以下接口均需要 ADMIN 角色

### 8.1 获取用户列表

- **GET** `/api/admin/users`
- 权限: ADMIN
- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": [
    {
      "id": 1,
      "username": "user",
      "role": "USER",
      "enabled": true,
      "gender": "female",
      "skinTone": "neutral",
      "skinType": "normal"
    }
  ]
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |
| username | string | 用户名 |
| role | string | 角色 |
| enabled | boolean | 是否启用 |
| gender | string\|null | 性别 |
| skinTone | string\|null | 肤色色调 |
| skinType | string\|null | 肤质 |

---

### 8.2 编辑用户

- **PUT** `/api/admin/users/{id}`
- 权限: ADMIN
- 路径参数:

| 参数 | 类型 | 说明 |
|------|------|------|
| id | number | 用户ID |

- 请求体:

```json
{
  "enabled": true,
  "role": "USER",
  "gender": "female",
  "skinTone": "neutral",
  "skinType": "normal"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| enabled | boolean | 否 | 是否启用 |
| role | string | 否 | 角色, 最长16字符 |
| gender | string | 否 | 性别, 最长16字符 |
| skinTone | string | 否 | 肤色色调, 最长32字符 |
| skinType | string | 否 | 肤质, 最长32字符 |

- 成功响应:

```json
{
  "success": true,
  "message": "OK",
  "data": null
}
```

---

## 9. 错误码说明

| HTTP状态码 | success | message | 说明 |
|-----------|---------|---------|------|
| 200 | true | OK | 请求成功 |
| 200 | false | 具体错误信息 | 业务逻辑错误 |
| 401 | - | - | 未登录或Token过期 |
| 403 | - | - | 权限不足(如普通用户访问ADMIN接口) |
| 404 | - | - | 接口不存在 |

---

## 10. 数据枚举值参考

### 色系分类 (category)

| 值 | 中文名 |
|----|--------|
| red | 正红 |
| red_brown | 红棕 |
| tomato | 番茄红 |
| bean_paste | 豆沙 |
| pink | 粉色 |
| rose | 玫瑰 |
| rose_brown | 玫瑰棕 |
| nude | 裸色 |

### 质地 (finishType)

| 值 | 中文名 |
|----|--------|
| matte | 哑光 |
| gloss | 滋润 |
| satin | 缎面 |

### 肤色色调 (skinTone)

| 值 | 中文名 |
|----|--------|
| warm | 暖调 |
| cool | 冷调 |
| neutral | 中性 |

### 肤质 (skinType)

| 值 | 中文名 |
|----|--------|
| oily | 油性 |
| dry | 干性 |
| normal | 中性 |
| combination | 混合 |

### 性别 (gender)

| 值 | 中文名 |
|----|--------|
| male | 男 |
| female | 女 |

### 角色 (role)

| 值 | 中文名 |
|----|--------|
| USER | 普通用户 |
| ADMIN | 管理员 |
