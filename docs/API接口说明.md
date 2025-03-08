# 在线课程平台API接口说明

## 基础信息

- 接口基础路径: `/api`
- 认证方式: Bearer Token
- 响应格式: JSON
- Swagger地址: `/swagger-ui.html`

## 通用响应格式

所有接口统一使用以下响应格式：

```json
{
  "code": 200,          // 状态码，200表示成功，其他值表示失败
  "message": "操作成功",  // 消息描述
  "data": {}            // 数据，可能为空
}
```

## 错误码说明

| 错误码 | 描述 |
| ------ | ---- |
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未认证或认证失败 |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 认证接口

### 获取验证码key

获取验证码key，用于后续获取验证码图片

- 请求方式: `GET`
- 接口地址: `/api/auth/captcha/key`
- 权限要求: 无需认证

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": "123e4567-e89b-12d3-a456-426614174000"  // 验证码key
  }
  ```

### 获取验证码图片

根据验证码key获取验证码图片

- 请求方式: `GET`
- 接口地址: `/api/auth/captcha/image/{key}`
- 权限要求: 无需认证
- 路径参数:
  - `key`: 验证码key

- 响应:
  - 图片数据（JPEG格式）
  - 响应头:
    - `Content-Type: image/jpeg`
    - `Cache-Control: no-store, no-cache, must-revalidate`

### 发送邮箱验证码

发送邮箱验证码，用于用户注册

- 请求方式: `POST`
- 接口地址: `/api/auth/email-verification-code`
- 权限要求: 无需认证
- 请求参数:
  ```json
  {
    "email": "test@example.com",          // 邮箱地址
    "captchaKey": "123456",               // 验证码key
    "captchaCode": "1234"                 // 图形验证码
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

### 用户注册

注册新用户，需要先获取邮箱验证码

- 请求方式: `POST`
- 接口地址: `/api/auth/register`
- 权限要求: 无需认证
- 请求参数:
  ```json
  {
    "username": "zhangsan",               // 用户名，4-20位，只能包含字母、数字和下划线
    "password": "password123",            // 密码，6-20位
    "email": "zhangsan@example.com",      // 邮箱
    "phone": "13812345678",               // 手机号（可选）
    "captchaKey": "123456",               // 验证码key
    "captchaCode": "1234",                // 图形验证码
    "emailCode": "123456"                 // 邮箱验证码
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

### 用户登录

用户登录获取JWT令牌

- 请求方式: `POST`
- 接口地址: `/api/auth/login`
- 请求参数:
  ```json
  {
    "username": "zhangsan",               // 用户名
    "password": "password123",            // 密码
    "captchaKey": "123456",               // 验证码标识
    "captchaCode": "1234"                 // 验证码
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // 访问令牌
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // 刷新令牌
      "tokenType": "Bearer",                                     // 令牌类型
      "expiresIn": 3600000                                      // 过期时间（毫秒）
    }
  }
  ```

### 刷新令牌

刷新JWT令牌

- 请求方式: `POST`
- 接口地址: `/api/auth/refresh-token`
- 请求参数:
  ```json
  {
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."  // 刷新令牌
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",  // 新的访问令牌
      "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...", // 刷新令牌（不变）
      "tokenType": "Bearer",                                     // 令牌类型
      "expiresIn": 3600000                                      // 过期时间（毫秒）
    }
  }
  ```

### 注销

用户注销

- 请求方式: `POST`
- 接口地址: `/api/auth/logout`
- 请求头:
  - `Authorization`: Bearer Token

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

## 用户管理接口

### 获取用户列表

分页查询用户列表

- 请求方式: `GET`
- 接口地址: `/api/users`
- 权限要求: `ROLE_ADMIN`
- 请求参数:
  - `username`: 用户名（可选，模糊查询）
  - `email`: 邮箱（可选，模糊查询）
  - `phone`: 手机号（可选，模糊查询）
  - `status`: 状态（可选，0-禁用，1-正常）
  - `institutionId`: 机构ID（可选）
  - `roleId`: 角色ID（可选）
  - `pageNum`: 页码，默认1
  - `pageSize`: 每页条数，默认10

- 请求示例:
  ```
  GET /api/users?username=zhang&status=1&pageNum=1&pageSize=10
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "content": [
        {
          "id": 1,
          "username": "zhangsan",
          "email": "zhangsan@example.com",
          "phone": "13812345678",
          "avatar": "avatar.jpg",
          "nickname": "张三",
          "status": 1,
          "institutionId": null,
          "createdAt": "2023-01-01T12:00:00",
          "updatedAt": "2023-01-01T12:00:00",
          "lastLoginAt": "2023-01-01T12:00:00",
          "roles": [
            {
              "id": 1,
              "name": "普通用户",
              "code": "ROLE_USER"
            }
          ]
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": {
          "empty": true,
          "sorted": false,
          "unsorted": true
        },
        "offset": 0,
        "paged": true,
        "unpaged": false
      },
      "last": true,
      "totalElements": 1,
      "totalPages": 1,
      "size": 10,
      "number": 0,
      "sort": {
        "empty": true,
        "sorted": false,
        "unsorted": true
      },
      "first": true,
      "numberOfElements": 1,
      "empty": false
    }
  }
  ```

### 获取用户详情

根据用户ID获取用户详情

- 请求方式: `GET`
- 接口地址: `/api/users/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 用户ID

- 请求示例:
  ```
  GET /api/users/1
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@example.com",
      "phone": "13812345678",
      "avatar": "avatar.jpg",
      "nickname": "张三",
      "status": 1,
      "institutionId": null,
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00",
      "lastLoginAt": "2023-01-01T12:00:00",
      "roles": [
        {
          "id": 1,
          "name": "普通用户",
          "code": "ROLE_USER"
        }
      ]
    }
  }
  ```

### 创建用户

创建新用户

- 请求方式: `POST`
- 接口地址: `/api/users`
- 权限要求: `ROLE_ADMIN`
- 请求参数:
  ```json
  {
    "username": "zhangsan",               // 用户名，4-20位，只能包含字母、数字和下划线
    "password": "password123",            // 密码，6-20位
    "email": "zhangsan@example.com",      // 邮箱
    "phone": "13812345678",               // 手机号（可选）
    "avatar": "avatar.jpg",               // 头像（可选）
    "nickname": "张三",                    // 昵称（可选）
    "status": 1,                          // 状态：0-禁用，1-正常（可选，默认1）
    "institutionId": null,                // 机构ID（可选）
    "roleIds": [1]                        // 角色ID列表（可选，默认为普通用户）
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@example.com",
      "phone": "13812345678",
      "avatar": "avatar.jpg",
      "nickname": "张三",
      "status": 1,
      "institutionId": null,
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00",
      "lastLoginAt": null,
      "roles": [
        {
          "id": 1,
          "name": "普通用户",
          "code": "ROLE_USER"
        }
      ]
    }
  }
  ```

### 更新用户

更新用户信息

- 请求方式: `PUT`
- 接口地址: `/api/users/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 用户ID
- 请求参数:
  ```json
  {
    "username": "zhangsan",               // 用户名（可选）
    "password": "newpassword123",         // 密码（可选）
    "email": "new_email@example.com",     // 邮箱（可选）
    "phone": "13812345678",               // 手机号（可选）
    "avatar": "new_avatar.jpg",           // 头像（可选）
    "nickname": "新昵称",                  // 昵称（可选）
    "status": 1,                          // 状态（可选）
    "institutionId": 1,                   // 机构ID（可选）
    "roleIds": [1, 2]                     // 角色ID列表（可选）
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "email": "new_email@example.com",
      "phone": "13812345678",
      "avatar": "new_avatar.jpg",
      "nickname": "新昵称",
      "status": 1,
      "institutionId": 1,
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:30:00",
      "lastLoginAt": "2023-01-01T12:00:00",
      "roles": [
        {
          "id": 1,
          "name": "普通用户",
          "code": "ROLE_USER"
        },
        {
          "id": 2,
          "name": "管理员",
          "code": "ROLE_ADMIN"
        }
      ]
    }
  }
  ```

### 删除用户

删除用户

- 请求方式: `DELETE`
- 接口地址: `/api/users/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 用户ID

- 请求示例:
  ```
  DELETE /api/users/1
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

### 修改用户状态

修改用户状态

- 请求方式: `PATCH`
- 接口地址: `/api/users/{id}/status`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 用户ID
- 请求参数:
  - `status`: 状态（0-禁用，1-正常）

- 请求示例:
  ```
  PATCH /api/users/1/status?status=0
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@example.com",
      "status": 0,
      "roles": [
        {
          "id": 1,
          "name": "普通用户",
          "code": "ROLE_USER"
        }
      ]
    }
  }
  ```

### 给用户分配角色

给用户分配角色

- 请求方式: `PUT`
- 接口地址: `/api/users/{id}/roles`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 用户ID
- 请求参数:
  ```json
  [1, 2]  // 角色ID列表
  ```

- 请求示例:
  ```
  PUT /api/users/1/roles
  [1, 2]
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "roles": [
        {
          "id": 1,
          "name": "普通用户",
          "code": "ROLE_USER"
        },
        {
          "id": 2,
          "name": "管理员",
          "code": "ROLE_ADMIN"
        }
      ]
    }
  }
  ```

### 批量删除用户

批量删除用户

- 请求方式: `DELETE`
- 接口地址: `/api/users/batch`
- 权限要求: `ROLE_ADMIN`
- 请求参数:
  ```json
  [1, 2]  // 用户ID列表
  ```

- 请求示例:
  ```
  DELETE /api/users/batch
  [1, 2]
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

## 角色管理接口

### 获取角色列表

获取角色列表

- 请求方式: `GET`
- 接口地址: `/api/roles`
- 权限要求: `ROLE_ADMIN`

- 请求示例:
  ```
  GET /api/roles
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": [
      {
        "id": 1,
        "name": "普通用户",
        "code": "ROLE_USER",
        "description": "普通用户角色",
        "createdAt": "2023-01-01T12:00:00",
        "updatedAt": "2023-01-01T12:00:00",
        "permissions": [
          {
            "id": 1,
            "name": "查看课程",
            "code": "COURSE_VIEW"
          }
        ]
      },
      {
        "id": 2,
        "name": "管理员",
        "code": "ROLE_ADMIN",
        "description": "系统管理员角色",
        "createdAt": "2023-01-01T12:00:00",
        "updatedAt": "2023-01-01T12:00:00",
        "permissions": [
          {
            "id": 1,
            "name": "查看课程",
            "code": "COURSE_VIEW"
          },
          {
            "id": 2,
            "name": "创建课程",
            "code": "COURSE_CREATE"
          }
        ]
      }
    ]
  }
  ```

### 获取角色详情

根据角色ID获取角色详情

- 请求方式: `GET`
- 接口地址: `/api/roles/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 角色ID

- 请求示例:
  ```
  GET /api/roles/1
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "name": "普通用户",
      "code": "ROLE_USER",
      "description": "普通用户角色",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00",
      "permissions": [
        {
          "id": 1,
          "name": "查看课程",
          "code": "COURSE_VIEW"
        }
      ]
    }
  }
  ```

### 创建角色

创建新角色

- 请求方式: `POST`
- 接口地址: `/api/roles`
- 权限要求: `ROLE_ADMIN`
- 请求参数:
  ```json
  {
    "name": "审核人员",                    // 角色名称
    "code": "ROLE_REVIEWER",              // 角色编码，必须以ROLE_开头
    "description": "内容审核人员角色",      // 角色描述（可选）
    "permissionIds": [1, 3, 5]           // 权限ID列表（可选）
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 3,
      "name": "审核人员",
      "code": "ROLE_REVIEWER",
      "description": "内容审核人员角色",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00",
      "permissions": [
        {
          "id": 1,
          "name": "查看课程",
          "code": "COURSE_VIEW"
        },
        {
          "id": 3,
          "name": "审核课程",
          "code": "COURSE_REVIEW"
        },
        {
          "id": 5,
          "name": "查看评论",
          "code": "COMMENT_VIEW"
        }
      ]
    }
  }
  ```

### 更新角色

更新角色信息

- 请求方式: `PUT`
- 接口地址: `/api/roles/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 角色ID
- 请求参数:
  ```json
  {
    "name": "审核专员",                    // 角色名称（可选）
    "code": "ROLE_REVIEWER",              // 角色编码（可选）
    "description": "内容审核专员角色",      // 角色描述（可选）
    "permissionIds": [1, 3, 5, 7]        // 权限ID列表（可选）
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 3,
      "name": "审核专员",
      "code": "ROLE_REVIEWER",
      "description": "内容审核专员角色",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:30:00",
      "permissions": [
        {
          "id": 1,
          "name": "查看课程",
          "code": "COURSE_VIEW"
        },
        {
          "id": 3,
          "name": "审核课程",
          "code": "COURSE_REVIEW"
        },
        {
          "id": 5,
          "name": "查看评论",
          "code": "COMMENT_VIEW"
        },
        {
          "id": 7,
          "name": "审核评论",
          "code": "COMMENT_REVIEW"
        }
      ]
    }
  }
  ```

### 删除角色

删除角色

- 请求方式: `DELETE`
- 接口地址: `/api/roles/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 角色ID

- 请求示例:
  ```
  DELETE /api/roles/3
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

### 给角色分配权限

给角色分配权限

- 请求方式: `PUT`
- 接口地址: `/api/roles/{id}/permissions`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 角色ID
- 请求参数:
  ```json
  [1, 2, 3, 4]  // 权限ID列表
  ```

- 请求示例:
  ```
  PUT /api/roles/1/permissions
  [1, 2, 3, 4]
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "name": "普通用户",
      "code": "ROLE_USER",
      "permissions": [
        {
          "id": 1,
          "name": "查看课程",
          "code": "COURSE_VIEW"
        },
        {
          "id": 2,
          "name": "创建课程",
          "code": "COURSE_CREATE"
        },
        {
          "id": 3,
          "name": "审核课程",
          "code": "COURSE_REVIEW"
        },
        {
          "id": 4,
          "name": "删除课程",
          "code": "COURSE_DELETE"
        }
      ]
    }
  }
  ```

### 批量删除角色

批量删除角色

- 请求方式: `DELETE`
- 接口地址: `/api/roles/batch`
- 权限要求: `ROLE_ADMIN`
- 请求参数:
  ```json
  [3, 4]  // 角色ID列表
  ```

- 请求示例:
  ```
  DELETE /api/roles/batch
  [3, 4]
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

## 权限管理接口

### 获取权限列表

获取权限列表

- 请求方式: `GET`
- 接口地址: `/api/permissions`
- 权限要求: `ROLE_ADMIN`

- 请求示例:
  ```
  GET /api/permissions
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": [
      {
        "id": 1,
        "name": "查看课程",
        "code": "COURSE_VIEW",
        "description": "查看课程信息权限",
        "url": "/api/courses",
        "method": "GET",
        "createdAt": "2023-01-01T12:00:00",
        "updatedAt": "2023-01-01T12:00:00"
      },
      {
        "id": 2,
        "name": "创建课程",
        "code": "COURSE_CREATE",
        "description": "创建课程权限",
        "url": "/api/courses",
        "method": "POST",
        "createdAt": "2023-01-01T12:00:00",
        "updatedAt": "2023-01-01T12:00:00"
      }
    ]
  }
  ```

### 获取权限详情

根据权限ID获取权限详情

- 请求方式: `GET`
- 接口地址: `/api/permissions/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 权限ID

- 请求示例:
  ```
  GET /api/permissions/1
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 1,
      "name": "查看课程",
      "code": "COURSE_VIEW",
      "description": "查看课程信息权限",
      "url": "/api/courses",
      "method": "GET",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00"
    }
  }
  ```

### 创建权限

创建新权限

- 请求方式: `POST`
- 接口地址: `/api/permissions`
- 权限要求: `ROLE_ADMIN`
- 请求参数:
  ```json
  {
    "name": "编辑课程",                    // 权限名称
    "code": "COURSE_EDIT",                // 权限编码
    "description": "编辑课程信息权限",      // 权限描述（可选）
    "url": "/api/courses/{id}",           // 资源URL（可选）
    "method": "PUT"                       // HTTP方法（可选）
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 3,
      "name": "编辑课程",
      "code": "COURSE_EDIT",
      "description": "编辑课程信息权限",
      "url": "/api/courses/{id}",
      "method": "PUT",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:00:00"
    }
  }
  ```

### 更新权限

更新权限信息

- 请求方式: `PUT`
- 接口地址: `/api/permissions/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 权限ID
- 请求参数:
  ```json
  {
    "name": "修改课程",                    // 权限名称（可选）
    "code": "COURSE_UPDATE",              // 权限编码（可选）
    "description": "修改课程信息权限",      // 权限描述（可选）
    "url": "/api/courses/{id}",           // 资源URL（可选）
    "method": "PUT"                       // HTTP方法（可选）
  }
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": {
      "id": 3,
      "name": "修改课程",
      "code": "COURSE_UPDATE",
      "description": "修改课程信息权限",
      "url": "/api/courses/{id}",
      "method": "PUT",
      "createdAt": "2023-01-01T12:00:00",
      "updatedAt": "2023-01-01T12:30:00"
    }
  }
  ```

### 删除权限

删除权限

- 请求方式: `DELETE`
- 接口地址: `/api/permissions/{id}`
- 权限要求: `ROLE_ADMIN`
- 路径参数:
  - `id`: 权限ID

- 请求示例:
  ```
  DELETE /api/permissions/3
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

### 批量删除权限

批量删除权限

- 请求方式: `DELETE`
- 接口地址: `/api/permissions/batch`
- 权限要求: `ROLE_ADMIN`
- 请求参数:
  ```json
  [3, 4]  // 权限ID列表
  ```

- 请求示例:
  ```
  DELETE /api/permissions/batch
  [3, 4]
  ```

- 响应示例:
  ```json
  {
    "code": 200,
    "message": "操作成功",
    "data": null
  }
  ```

## 开发说明

### 认证流程

1. 获取验证码key：调用 `/api/auth/captcha/key` 接口获取验证码key
2. 获取验证码图片：调用 `/api/auth/captcha/image/{key}` 接口获取验证码图片
3. 发送邮箱验证码：调用 `/api/auth/email-verification-code` 接口发送邮箱验证码
4. 用户注册：调用 `/api/auth/register` 接口注册用户（需要邮箱验证码）
5. 用户登录：调用 `/api/auth/login` 接口获取JWT令牌
6. 接口调用：在请求头中携带 `Authorization: Bearer {访问令牌}` 调用需要认证的接口
7. 刷新令牌：访问令牌过期时，调用 `/api/auth/refresh-token` 接口刷新令牌
8. 用户注销：调用 `/api/auth/logout` 接口注销用户

### 权限管理流程

1. 创建权限：管理员调用 `/api/permissions` 接口创建权限
2. 创建角色：管理员调用 `/api/roles` 接口创建角色
3. 给角色分配权限：管理员调用 `/api/roles/{id}/permissions` 接口给角色分配权限
4. 创建用户：管理员调用 `/api/users` 接口创建用户
5. 给用户分配角色：管理员调用 `/api/users/{id}/roles` 接口给用户分配角色

### 注意事项

1. 图形验证码有效期为5分钟，请在有效期内使用
2. 邮箱验证码有效期为5分钟，请在有效期内使用
3. 访问令牌有效期为1小时，刷新令牌有效期为7天
4. 请求头中的认证信息格式必须为 `Authorization: Bearer {访问令牌}`
5. 刷新令牌仅能使用一次，使用后会生成新的访问令牌，但刷新令牌本身不变
6. 权限编码必须以`ROLE_`开头的才是角色，其他的是普通权限
7. 系统内置了四种基本角色：普通用户(ROLE_USER)、管理员(ROLE_ADMIN)、审核人员(ROLE_REVIEWER)、机构用户(ROLE_INSTITUTION) 