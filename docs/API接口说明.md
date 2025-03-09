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

## 用户自身管理接口

### 获取当前用户信息

- **URL**: `/api/users/current`
- **方法**: `GET`
- **描述**: 获取当前登录用户的详细信息
- **请求头**: `Authorization: Bearer {accessToken}`
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@example.com",
      "nickname": "张三",
      "phone": "13800138000",
      "avatar": "https://example.com/avatars/default.png",
      "status": 1,
      "createdAt": "2023-01-01T12:00:00Z",
      "lastLoginAt": "2023-01-01T12:00:00Z",
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
- **错误响应**:
  - 401 Unauthorized: 未授权

### 更新当前用户信息

- **URL**: `/api/users/current`
- **方法**: `PUT`
- **描述**: 更新当前登录用户的个人资料
- **请求头**: `Authorization: Bearer {accessToken}`
- **请求体**:
  ```json
  {
    "nickname": "新昵称",
    "phone": "13812345678"
  }
  ```
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": {
      "id": 1,
      "username": "zhangsan",
      "email": "zhangsan@example.com",
      "nickname": "新昵称",
      "phone": "13812345678",
      "avatar": "https://example.com/avatars/default.png",
      "updatedAt": "2023-01-01T13:00:00Z"
    }
  }
  ```
- **错误响应**:
  - 400 Bad Request: 参数错误
  - 401 Unauthorized: 未授权

### 修改当前用户密码

- **URL**: `/api/users/current/password`
- **方法**: `PUT`
- **描述**: 修改当前登录用户的密码
- **请求头**: `Authorization: Bearer {accessToken}`
- **请求体**:
  ```json
  {
    "oldPassword": "Password123",
    "newPassword": "NewPassword456",
    "confirmPassword": "NewPassword456"
  }
  ```
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "密码修改成功",
    "data": null
  }
  ```
- **错误响应**:
  - 400 Bad Request: 参数错误或新密码与确认密码不匹配
  - 401 Unauthorized: 未授权或旧密码不正确

### 上传用户头像

- **URL**: `/api/users/current/avatar`
- **方法**: `POST`
- **描述**: 上传并设置当前用户的头像
- **请求头**: `Authorization: Bearer {accessToken}`
- **请求体**: `multipart/form-data` 类型，包含名为 `avatar` 的文件字段
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "头像上传成功",
    "data": {
      "avatarUrl": "https://example.com/avatars/user1/avatar.jpg"
    }
  }
  ```
- **错误响应**:
  - 400 Bad Request: 文件格式不支持或文件大小超限
  - 401 Unauthorized: 未授权

### 获取邮箱更新验证码

- **URL**: `/api/users/current/email-code`
- **方法**: `POST`
- **描述**: 向用户新邮箱发送验证码，用于更新邮箱地址
- **请求头**: `Authorization: Bearer {accessToken}`
- **请求体**:
  ```json
  {
    "newEmail": "new-email@example.com"
  }
  ```
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "验证码已发送到新邮箱，请查收",
    "data": null
  }
  ```
- **错误响应**:
  - 400 Bad Request: 邮箱格式不正确或已被其他用户使用
  - 401 Unauthorized: 未授权
  - 429 Too Many Requests: 请求频率过高

### 更新用户邮箱

- **URL**: `/api/users/current/email`
- **方法**: `PUT`
- **描述**: 使用验证码更新用户邮箱
- **请求头**: `Authorization: Bearer {accessToken}`
- **请求体**:
  ```json
  {
    "newEmail": "new-email@example.com",
    "verificationCode": "123456"
  }
  ```
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "邮箱更新成功",
    "data": {
      "email": "new-email@example.com",
      "updatedAt": "2023-01-01T14:00:00Z"
    }
  }
  ```
- **错误响应**:
  - 400 Bad Request: 参数错误
  - 401 Unauthorized: 未授权
  - 403 Forbidden: 验证码错误
  - 410 Gone: 验证码已过期

## 系统设置接口

### 获取系统配置 (管理员)

- **URL**: `/api/admin/settings`
- **方法**: `GET`
- **描述**: 获取系统配置信息
- **请求头**: `Authorization: Bearer {accessToken}`
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "siteName": "在线课程平台",
      "siteDescription": "提供优质的在线课程学习平台",
      "contactEmail": "contact@example.com",
      "registrationEnabled": true,
      "maintenanceMode": false,
      "fileStorageType": "minio",
      "maxUploadSize": 10485760
    }
  }
  ```
- **错误响应**:
  - 401 Unauthorized: 未授权
  - 403 Forbidden: 无权限

### 更新系统配置 (管理员)

- **URL**: `/api/admin/settings`
- **方法**: `PUT`
- **描述**: 更新系统配置信息
- **请求头**: `Authorization: Bearer {accessToken}`
- **请求体**:
  ```json
  {
    "siteName": "优质在线教育平台",
    "siteDescription": "提供高质量的在线课程学习体验",
    "contactEmail": "support@example.com",
    "registrationEnabled": true,
    "maintenanceMode": false,
    "maxUploadSize": 20971520
  }
  ```
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": {
      "siteName": "优质在线教育平台",
      "siteDescription": "提供高质量的在线课程学习体验",
      "contactEmail": "support@example.com",
      "registrationEnabled": true,
      "maintenanceMode": false,
      "maxUploadSize": 20971520,
      "updatedAt": "2023-01-01T13:00:00Z"
    }
  }
  ```
- **错误响应**:
  - 400 Bad Request: 参数错误
  - 401 Unauthorized: 未授权
  - 403 Forbidden: 无权限

## 课程相关接口

### 获取课程分类列表（待实现）

- **URL**: `/api/categories`
- **方法**: `GET`
- **描述**: 获取所有课程分类
- **请求参数**: 无
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": [
      {
        "id": 1,
        "name": "计算机科学",
        "code": "computer-science",
        "parentId": null,
        "level": 1,
        "children": [
          {
            "id": 2,
            "name": "编程语言",
            "code": "programming-languages",
            "parentId": 1,
            "level": 2,
            "children": []
          },
          {
            "id": 3,
            "name": "数据库",
            "code": "databases",
            "parentId": 1,
            "level": 2,
            "children": []
          }
        ]
      },
      {
        "id": 4,
        "name": "数学",
        "code": "mathematics",
        "parentId": null,
        "level": 1,
        "children": []
      }
    ]
  }
  ```

### 获取课程标签列表（待实现）

- **URL**: `/api/tags`
- **方法**: `GET`
- **描述**: 获取所有课程标签
- **请求参数**:
  - `size`: 返回标签数量，默认为20
  - `popular`: 是否返回热门标签，默认为false
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": [
      {
        "id": 1,
        "name": "Java",
        "courseCount": 42
      },
      {
        "id": 2,
        "name": "Spring Boot",
        "courseCount": 38
      },
      {
        "id": 3,
        "name": "JavaScript",
        "courseCount": 56
      }
    ]
  }
  ```

### 获取课程列表（待实现）

- **URL**: `/api/courses`
- **方法**: `GET`
- **描述**: 获取课程列表
- **请求参数**:
  - `page`: 页码，默认为0
  - `size`: 每页大小，默认为10
  - `sort`: 排序字段，默认为createdAt,desc
  - `categoryId`: 按分类筛选
  - `tagId`: 按标签筛选
  - `keyword`: 搜索关键词
  - `price`: 价格区间，格式为"min,max"，如"0,100"
  - `level`: 难度级别，值为1(初级)、2(中级)或3(高级)
- **成功响应** (200 OK):
  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "content": [
        {
          "id": 1,
          "title": "Spring Boot实战入门到精通",
          "summary": "全面讲解Spring Boot框架的使用方法和最佳实践",
          "coverUrl": "https://example.com/covers/spring-boot.jpg",
          "price": 199.00,
          "discountPrice": 149.00,
          "level": 2,
          "totalDuration": 1240,
          "studentCount": 1205,
          "rating": 4.8,
          "categoryId": 2,
          "categoryName": "编程语言",
          "teacherName": "张教授",
          "tags": [
            {
              "id": 2,
              "name": "Spring Boot"
            },
            {
              "id": 8,
              "name": "Java"
            }
          ]
        }
      ],
      "pageable": {
        "pageNumber": 0,
        "pageSize": 10,
        "sort": [
          {
            "direction": "DESC",
            "property": "createdAt"
          }
        ]
      },
      "totalElements": 42,
      "totalPages": 5,
      "last": false,
      "size": 10,
      "number": 0,
      "sort": {
        "sorted": true,
        "unsorted": false,
        "empty": false
      },
      "numberOfElements": 10,
      "first": true,
      "empty": false
    }
  }
  ```

## 访问权限说明

对API接口的访问权限采用基于角色的访问控制(RBAC)策略：

- **公开接口**: 不需要任何权限即可访问，如注册、登录、公共课程列表等
- **用户接口**: 需要普通用户权限(`ROLE_USER`)，如个人信息管理、课程观看等
- **机构接口**: 需要机构权限(`ROLE_INSTITUTION`)，如创建和管理课程等
- **管理员接口**: 需要管理员权限(`ROLE_ADMIN`)，如用户管理、系统设置等

## 附录：错误码说明

| 错误码 | 描述                       |
|--------|----------------------------|
| 40001  | 请求参数错误               |
| 40002  | 表单验证失败               |
| 40003  | 数据不存在                 |
| 40004  | 用户名或密码错误           |
| 40005  | 账号被锁定                 |
| 40006  | 验证码错误或已过期         |
| 40007  | 文件上传失败               |
| 40008  | 操作频率超限               |
| 40009  | 数据已存在                 |
| 40010  | 数据关联，无法删除         |
| 50001  | 系统内部错误               |
| 50002  | 数据库操作失败             |
| 50003  | 第三方服务调用失败         | 