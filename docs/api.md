{
  "openapi": "3.0.1",
  "info": {
    "title": "在线课程平台API",
    "description": "在线课程平台的RESTful API文档",
    "contact": {
      "name": "在线课程平台团队",
      "url": "https://example.com",
      "email": "support@example.com"
    },
    "license": {
      "name": "MIT License",
      "url": "https://opensource.org/licenses/MIT"
    },
    "version": "1.0.0"
  },
  "servers": [
    {
      "url": "http://localhost:8080",
      "description": "Generated server url"
    }
  ],
  "security": [
    {
      "Bearer Authentication": []
    }
  ],
  "tags": [
    {
      "name": "角色管理",
      "description": "角色查询、创建、编辑、删除等功能"
    },
    {
      "name": "用户管理",
      "description": "用户查询、创建、编辑、删除等功能"
    },
    {
      "name": "认证接口",
      "description": "包括注册、登录、刷新令牌等接口"
    },
    {
      "name": "权限管理",
      "description": "权限查询、创建、编辑、删除等功能"
    }
  ],
  "paths": {
    "/api/users/{id}": {
      "get": {
        "tags": [
          "用户管理"
        ],
        "summary": "获取用户详情",
        "description": "根据用户ID获取用户详情",
        "operationId": "getUserById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "用户ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "用户管理"
        ],
        "summary": "更新用户",
        "description": "更新用户信息",
        "operationId": "updateUser",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "用户ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "用户管理"
        ],
        "summary": "删除用户",
        "description": "根据用户ID删除用户",
        "operationId": "deleteUser",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "用户ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "204": {
            "description": "No Content",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/users/{id}/roles": {
      "put": {
        "tags": [
          "用户管理"
        ],
        "summary": "给用户分配角色",
        "description": "给用户分配角色",
        "operationId": "assignRoles",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "用户ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "uniqueItems": true,
                "type": "array",
                "description": "角色ID列表",
                "items": {
                  "type": "integer",
                  "format": "int64"
                }
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/users/current": {
      "get": {
        "tags": [
          "用户管理"
        ],
        "summary": "获取当前用户信息",
        "description": "获取当前登录用户的详细信息",
        "operationId": "getCurrentUser",
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "用户管理"
        ],
        "summary": "更新当前用户信息",
        "description": "更新当前登录用户的个人信息",
        "operationId": "updateCurrentUser",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserProfileDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/users/current/password": {
      "put": {
        "tags": [
          "用户管理"
        ],
        "summary": "修改密码",
        "description": "修改当前用户密码",
        "operationId": "changePassword",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/ChangePasswordDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/users/current/email": {
      "put": {
        "tags": [
          "用户管理"
        ],
        "summary": "更新邮箱",
        "description": "更新当前用户邮箱（需验证码）",
        "operationId": "updateEmail",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/EmailUpdateDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/roles/{id}": {
      "get": {
        "tags": [
          "角色管理"
        ],
        "summary": "获取角色详情",
        "description": "根据角色ID获取角色详情",
        "operationId": "getRoleById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "角色ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultRoleVO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "角色管理"
        ],
        "summary": "更新角色",
        "description": "更新角色信息",
        "operationId": "updateRole",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "角色ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RoleDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultRoleVO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "角色管理"
        ],
        "summary": "删除角色",
        "description": "根据角色ID删除角色",
        "operationId": "deleteRole",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "角色ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "204": {
            "description": "No Content",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/roles/{id}/permissions": {
      "put": {
        "tags": [
          "角色管理"
        ],
        "summary": "给角色分配权限",
        "description": "给角色分配权限",
        "operationId": "assignPermissions",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "角色ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "uniqueItems": true,
                "type": "array",
                "description": "权限ID列表",
                "items": {
                  "type": "integer",
                  "format": "int64"
                }
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultRoleVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/permissions/{id}": {
      "get": {
        "tags": [
          "权限管理"
        ],
        "summary": "获取权限详情",
        "description": "根据权限ID获取权限详情",
        "operationId": "getPermissionById",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "权限ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultPermissionVO"
                }
              }
            }
          }
        }
      },
      "put": {
        "tags": [
          "权限管理"
        ],
        "summary": "更新权限",
        "description": "更新权限信息",
        "operationId": "updatePermission",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "权限ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/PermissionDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultPermissionVO"
                }
              }
            }
          }
        }
      },
      "delete": {
        "tags": [
          "权限管理"
        ],
        "summary": "删除权限",
        "description": "根据权限ID删除权限",
        "operationId": "deletePermission",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "权限ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "204": {
            "description": "No Content",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/users": {
      "get": {
        "tags": [
          "用户管理"
        ],
        "summary": "分页查询用户列表",
        "description": "根据条件分页查询用户列表",
        "operationId": "getUserList",
        "parameters": [
          {
            "name": "queryDTO",
            "in": "query",
            "required": true,
            "schema": {
              "$ref": "#/components/schemas/UserQueryDTO"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultPageUserVO"
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "用户管理"
        ],
        "summary": "创建用户",
        "description": "创建新用户",
        "operationId": "createUser",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/UserDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "201": {
            "description": "Created",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/users/current/avatar": {
      "post": {
        "tags": [
          "用户管理"
        ],
        "summary": "上传头像",
        "description": "上传当前用户头像",
        "operationId": "uploadAvatar",
        "requestBody": {
          "content": {
            "multipart/form-data": {
              "schema": {
                "required": [
                  "file"
                ],
                "type": "object",
                "properties": {
                  "file": {
                    "type": "string",
                    "format": "binary"
                  }
                }
              }
            }
          }
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultMapStringString"
                }
              }
            }
          }
        }
      }
    },
    "/api/roles": {
      "get": {
        "tags": [
          "角色管理"
        ],
        "summary": "获取角色列表",
        "description": "获取所有角色列表",
        "operationId": "getRoleList",
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultListRoleVO"
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "角色管理"
        ],
        "summary": "创建角色",
        "description": "创建新角色",
        "operationId": "createRole",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RoleDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "201": {
            "description": "Created",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultRoleVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/permissions": {
      "get": {
        "tags": [
          "权限管理"
        ],
        "summary": "获取权限列表",
        "description": "获取所有权限列表",
        "operationId": "getPermissionList",
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultListPermissionVO"
                }
              }
            }
          }
        }
      },
      "post": {
        "tags": [
          "权限管理"
        ],
        "summary": "创建权限",
        "description": "创建新权限",
        "operationId": "createPermission",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/PermissionDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "201": {
            "description": "Created",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultPermissionVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/register": {
      "post": {
        "tags": [
          "认证接口"
        ],
        "summary": "用户注册",
        "description": "注册新用户",
        "operationId": "register",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RegisterDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "201": {
            "description": "Created",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/refresh-token": {
      "post": {
        "tags": [
          "认证接口"
        ],
        "summary": "刷新令牌",
        "description": "刷新JWT令牌",
        "operationId": "refreshToken",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/RefreshTokenDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultJwtTokenDTO"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/logout": {
      "post": {
        "tags": [
          "认证接口"
        ],
        "summary": "注销",
        "description": "用户注销",
        "operationId": "logout",
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/login": {
      "post": {
        "tags": [
          "认证接口"
        ],
        "summary": "用户登录",
        "description": "用户登录获取JWT令牌",
        "operationId": "login",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/LoginDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultJwtTokenDTO"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/email-verification-code": {
      "post": {
        "tags": [
          "认证接口"
        ],
        "summary": "发送邮箱验证码",
        "description": "发送邮箱验证码（用于用户注册）",
        "operationId": "sendEmailVerificationCode",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/EmailVerificationDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/email-update-code": {
      "post": {
        "tags": [
          "认证接口"
        ],
        "summary": "发送邮箱更新验证码",
        "description": "发送邮箱更新验证码（用于更换邮箱）",
        "operationId": "sendEmailUpdateCode",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "$ref": "#/components/schemas/EmailVerificationDTO"
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/users/{id}/status": {
      "patch": {
        "tags": [
          "用户管理"
        ],
        "summary": "修改用户状态",
        "description": "修改用户状态（0-禁用，1-正常）",
        "operationId": "updateUserStatus",
        "parameters": [
          {
            "name": "id",
            "in": "path",
            "description": "用户ID",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          },
          {
            "name": "status",
            "in": "query",
            "description": "状态（0-禁用，1-正常）",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int32"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/users/basic/{userId}": {
      "get": {
        "tags": [
          "用户管理"
        ],
        "summary": "获取用户基本信息",
        "description": "获取用户基本信息（用于前端展示）",
        "operationId": "getBasicUserInfo",
        "parameters": [
          {
            "name": "userId",
            "in": "path",
            "required": true,
            "schema": {
              "type": "integer",
              "format": "int64"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultUserVO"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/captcha/key": {
      "get": {
        "tags": [
          "认证接口"
        ],
        "summary": "获取验证码key",
        "description": "获取验证码key，用于后续获取验证码图片",
        "operationId": "getCaptchaKey",
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultString"
                }
              }
            }
          }
        }
      }
    },
    "/api/auth/captcha/image/{key}": {
      "get": {
        "tags": [
          "认证接口"
        ],
        "summary": "获取验证码图片",
        "description": "根据验证码key获取对应的验证码图片",
        "operationId": "getCaptchaImage",
        "parameters": [
          {
            "name": "key",
            "in": "path",
            "description": "验证码key",
            "required": true,
            "schema": {
              "type": "string"
            }
          }
        ],
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "200": {
            "description": "OK"
          }
        }
      }
    },
    "/api/users/batch": {
      "delete": {
        "tags": [
          "用户管理"
        ],
        "summary": "批量删除用户",
        "description": "批量删除用户",
        "operationId": "batchDeleteUsers",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "type": "array",
                "description": "用户ID列表",
                "items": {
                  "type": "integer",
                  "format": "int64"
                }
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "204": {
            "description": "No Content",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/roles/batch": {
      "delete": {
        "tags": [
          "角色管理"
        ],
        "summary": "批量删除角色",
        "description": "批量删除角色",
        "operationId": "batchDeleteRoles",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "type": "array",
                "description": "角色ID列表",
                "items": {
                  "type": "integer",
                  "format": "int64"
                }
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "204": {
            "description": "No Content",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    },
    "/api/permissions/batch": {
      "delete": {
        "tags": [
          "权限管理"
        ],
        "summary": "批量删除权限",
        "description": "批量删除权限",
        "operationId": "batchDeletePermissions",
        "requestBody": {
          "content": {
            "application/json": {
              "schema": {
                "type": "array",
                "description": "权限ID列表",
                "items": {
                  "type": "integer",
                  "format": "int64"
                }
              }
            }
          },
          "required": true
        },
        "responses": {
          "401": {
            "description": "Unauthorized",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "403": {
            "description": "Forbidden",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          },
          "204": {
            "description": "No Content",
            "content": {
              "*/*": {
                "schema": {
                  "$ref": "#/components/schemas/ResultVoid"
                }
              }
            }
          }
        }
      }
    }
  },
  "components": {
    "schemas": {
      "ResultVoid": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "type": "object"
          }
        }
      },
      "UserDTO": {
        "required": [
          "email",
          "username"
        ],
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "用户ID",
            "format": "int64"
          },
          "username": {
            "maxLength": 20,
            "minLength": 4,
            "pattern": "^[a-zA-Z0-9_]+$",
            "type": "string",
            "description": "用户名",
            "example": "zhangsan"
          },
          "password": {
            "type": "string",
            "description": "密码"
          },
          "email": {
            "type": "string",
            "description": "邮箱",
            "example": "zhangsan@example.com"
          },
          "phone": {
            "pattern": "^1[3-9]\\d{9}$",
            "type": "string",
            "description": "手机号",
            "example": "13812345678"
          },
          "avatar": {
            "type": "string",
            "description": "头像"
          },
          "nickname": {
            "type": "string",
            "description": "昵称"
          },
          "status": {
            "type": "integer",
            "description": "状态：0-禁用，1-正常",
            "format": "int32"
          },
          "institutionId": {
            "type": "integer",
            "description": "机构ID",
            "format": "int64"
          },
          "roleIds": {
            "uniqueItems": true,
            "type": "array",
            "description": "角色ID列表",
            "items": {
              "type": "integer",
              "description": "角色ID列表",
              "format": "int64"
            }
          }
        },
        "description": "用户数据"
      },
      "Permission": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "createdAt": {
            "type": "string",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "format": "date-time"
          },
          "name": {
            "type": "string"
          },
          "code": {
            "type": "string"
          },
          "description": {
            "type": "string"
          },
          "url": {
            "type": "string"
          },
          "method": {
            "type": "string"
          }
        }
      },
      "ResultUserVO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "$ref": "#/components/schemas/UserVO"
          }
        }
      },
      "Role": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "format": "int64"
          },
          "createdAt": {
            "type": "string",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "format": "date-time"
          },
          "name": {
            "type": "string"
          },
          "code": {
            "type": "string"
          },
          "description": {
            "type": "string"
          },
          "permissions": {
            "uniqueItems": true,
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/Permission"
            }
          }
        },
        "description": "角色列表"
      },
      "UserVO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "用户ID",
            "format": "int64"
          },
          "username": {
            "type": "string",
            "description": "用户名"
          },
          "email": {
            "type": "string",
            "description": "邮箱"
          },
          "phone": {
            "type": "string",
            "description": "手机号"
          },
          "avatar": {
            "type": "string",
            "description": "头像"
          },
          "nickname": {
            "type": "string",
            "description": "昵称"
          },
          "status": {
            "type": "integer",
            "description": "状态：0-禁用，1-正常",
            "format": "int32"
          },
          "institutionId": {
            "type": "integer",
            "description": "机构ID",
            "format": "int64"
          },
          "createdAt": {
            "type": "string",
            "description": "创建时间",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "description": "更新时间",
            "format": "date-time"
          },
          "lastLoginAt": {
            "type": "string",
            "description": "最后登录时间",
            "format": "date-time"
          },
          "roles": {
            "uniqueItems": true,
            "type": "array",
            "description": "角色列表",
            "items": {
              "$ref": "#/components/schemas/Role"
            }
          }
        },
        "description": "用户信息"
      },
      "UserProfileDTO": {
        "type": "object",
        "properties": {
          "nickname": {
            "type": "string",
            "description": "昵称",
            "example": "张三"
          },
          "phone": {
            "pattern": "^1[3-9]\\d{9}$",
            "type": "string",
            "description": "手机号",
            "example": "13800138000"
          }
        },
        "description": "用户个人信息更新请求"
      },
      "ChangePasswordDTO": {
        "required": [
          "confirmPassword",
          "newPassword",
          "oldPassword"
        ],
        "type": "object",
        "properties": {
          "oldPassword": {
            "type": "string",
            "description": "旧密码",
            "example": "oldPassword123"
          },
          "newPassword": {
            "pattern": "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,20}$",
            "type": "string",
            "description": "新密码",
            "example": "newPassword123"
          },
          "confirmPassword": {
            "type": "string",
            "description": "确认密码",
            "example": "newPassword123"
          }
        },
        "description": "密码修改请求"
      },
      "EmailUpdateDTO": {
        "required": [
          "emailCode",
          "newEmail",
          "password"
        ],
        "type": "object",
        "properties": {
          "newEmail": {
            "type": "string",
            "description": "新邮箱",
            "example": "newemail@example.com"
          },
          "emailCode": {
            "type": "string",
            "description": "邮箱验证码",
            "example": "123456"
          },
          "password": {
            "type": "string",
            "description": "当前密码",
            "example": "password123"
          }
        },
        "description": "邮箱更新请求"
      },
      "RoleDTO": {
        "required": [
          "code",
          "name"
        ],
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "角色ID",
            "format": "int64"
          },
          "name": {
            "maxLength": 50,
            "minLength": 2,
            "type": "string",
            "description": "角色名称",
            "example": "系统管理员"
          },
          "code": {
            "maxLength": 50,
            "minLength": 4,
            "pattern": "^ROLE_[A-Z0-9_]+$",
            "type": "string",
            "description": "角色编码",
            "example": "ROLE_ADMIN"
          },
          "description": {
            "type": "string",
            "description": "角色描述"
          },
          "permissionIds": {
            "uniqueItems": true,
            "type": "array",
            "description": "权限ID列表",
            "items": {
              "type": "integer",
              "description": "权限ID列表",
              "format": "int64"
            }
          }
        },
        "description": "角色数据"
      },
      "ResultRoleVO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "$ref": "#/components/schemas/RoleVO"
          }
        }
      },
      "RoleVO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "角色ID",
            "format": "int64"
          },
          "name": {
            "type": "string",
            "description": "角色名称"
          },
          "code": {
            "type": "string",
            "description": "角色编码"
          },
          "description": {
            "type": "string",
            "description": "角色描述"
          },
          "createdAt": {
            "type": "string",
            "description": "创建时间",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "description": "更新时间",
            "format": "date-time"
          },
          "permissions": {
            "uniqueItems": true,
            "type": "array",
            "description": "权限列表",
            "items": {
              "$ref": "#/components/schemas/Permission"
            }
          }
        },
        "description": "角色信息"
      },
      "PermissionDTO": {
        "required": [
          "code",
          "name"
        ],
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "权限ID",
            "format": "int64"
          },
          "name": {
            "maxLength": 50,
            "minLength": 2,
            "type": "string",
            "description": "权限名称",
            "example": "用户查询"
          },
          "code": {
            "maxLength": 50,
            "minLength": 4,
            "pattern": "^[A-Z0-9_]+$",
            "type": "string",
            "description": "权限编码",
            "example": "USER_QUERY"
          },
          "description": {
            "type": "string",
            "description": "权限描述"
          },
          "url": {
            "type": "string",
            "description": "资源URL"
          },
          "method": {
            "type": "string",
            "description": "HTTP方法",
            "example": "GET"
          }
        },
        "description": "权限数据"
      },
      "PermissionVO": {
        "type": "object",
        "properties": {
          "id": {
            "type": "integer",
            "description": "权限ID",
            "format": "int64"
          },
          "name": {
            "type": "string",
            "description": "权限名称"
          },
          "code": {
            "type": "string",
            "description": "权限编码"
          },
          "description": {
            "type": "string",
            "description": "权限描述"
          },
          "url": {
            "type": "string",
            "description": "资源URL"
          },
          "method": {
            "type": "string",
            "description": "HTTP方法"
          },
          "createdAt": {
            "type": "string",
            "description": "创建时间",
            "format": "date-time"
          },
          "updatedAt": {
            "type": "string",
            "description": "更新时间",
            "format": "date-time"
          }
        },
        "description": "权限信息"
      },
      "ResultPermissionVO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "$ref": "#/components/schemas/PermissionVO"
          }
        }
      },
      "ResultMapStringString": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "type": "object",
            "additionalProperties": {
              "type": "string"
            }
          }
        }
      },
      "RegisterDTO": {
        "required": [
          "captchaCode",
          "captchaKey",
          "email",
          "emailCode",
          "password",
          "username"
        ],
        "type": "object",
        "properties": {
          "username": {
            "maxLength": 20,
            "minLength": 4,
            "pattern": "^[a-zA-Z0-9_]+$",
            "type": "string",
            "description": "用户名",
            "example": "zhangsan"
          },
          "password": {
            "maxLength": 20,
            "minLength": 6,
            "type": "string",
            "description": "密码",
            "example": "password123"
          },
          "email": {
            "type": "string",
            "description": "邮箱",
            "example": "zhangsan@example.com"
          },
          "phone": {
            "pattern": "^1[3-9]\\d{9}$",
            "type": "string",
            "description": "手机号",
            "example": "13812345678"
          },
          "captchaKey": {
            "type": "string",
            "description": "验证码Key",
            "example": "123456"
          },
          "captchaCode": {
            "type": "string",
            "description": "验证码",
            "example": "1234"
          },
          "emailCode": {
            "maxLength": 6,
            "minLength": 6,
            "pattern": "^\\d{6}$",
            "type": "string",
            "description": "邮箱验证码",
            "example": "123456"
          }
        },
        "description": "注册请求"
      },
      "RefreshTokenDTO": {
        "required": [
          "refreshToken"
        ],
        "type": "object",
        "properties": {
          "refreshToken": {
            "type": "string",
            "description": "刷新令牌",
            "example": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
          }
        },
        "description": "刷新令牌请求"
      },
      "JwtTokenDTO": {
        "type": "object",
        "properties": {
          "accessToken": {
            "type": "string"
          },
          "refreshToken": {
            "type": "string"
          },
          "tokenType": {
            "type": "string"
          },
          "expiresIn": {
            "type": "integer",
            "format": "int64"
          }
        }
      },
      "ResultJwtTokenDTO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "$ref": "#/components/schemas/JwtTokenDTO"
          }
        }
      },
      "LoginDTO": {
        "required": [
          "captchaCode",
          "captchaKey",
          "password",
          "username"
        ],
        "type": "object",
        "properties": {
          "username": {
            "type": "string",
            "description": "用户名",
            "example": "zhangsan"
          },
          "password": {
            "type": "string",
            "description": "密码",
            "example": "password123"
          },
          "captchaKey": {
            "type": "string",
            "description": "验证码Key",
            "example": "123456"
          },
          "captchaCode": {
            "type": "string",
            "description": "验证码",
            "example": "1234"
          }
        },
        "description": "登录请求"
      },
      "EmailVerificationDTO": {
        "required": [
          "captchaCode",
          "captchaKey",
          "email"
        ],
        "type": "object",
        "properties": {
          "email": {
            "type": "string",
            "description": "邮箱",
            "example": "zhangsan@example.com"
          },
          "captchaKey": {
            "type": "string",
            "description": "验证码Key",
            "example": "123456"
          },
          "captchaCode": {
            "type": "string",
            "description": "验证码",
            "example": "1234"
          }
        },
        "description": "邮箱验证码请求"
      },
      "UserQueryDTO": {
        "type": "object",
        "properties": {
          "username": {
            "type": "string",
            "description": "用户名"
          },
          "email": {
            "type": "string",
            "description": "邮箱"
          },
          "phone": {
            "type": "string",
            "description": "手机号"
          },
          "status": {
            "type": "integer",
            "description": "状态：0-禁用，1-正常",
            "format": "int32"
          },
          "institutionId": {
            "type": "integer",
            "description": "机构ID",
            "format": "int64"
          },
          "roleId": {
            "type": "integer",
            "description": "角色ID",
            "format": "int64"
          },
          "pageNum": {
            "type": "integer",
            "description": "页码",
            "format": "int32",
            "default": 1
          },
          "pageSize": {
            "type": "integer",
            "description": "每页条数",
            "format": "int32",
            "default": 10
          }
        },
        "description": "用户查询参数"
      },
      "PageUserVO": {
        "type": "object",
        "properties": {
          "totalElements": {
            "type": "integer",
            "format": "int64"
          },
          "totalPages": {
            "type": "integer",
            "format": "int32"
          },
          "size": {
            "type": "integer",
            "format": "int32"
          },
          "content": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/UserVO"
            }
          },
          "number": {
            "type": "integer",
            "format": "int32"
          },
          "sort": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/SortObject"
            }
          },
          "first": {
            "type": "boolean"
          },
          "last": {
            "type": "boolean"
          },
          "numberOfElements": {
            "type": "integer",
            "format": "int32"
          },
          "pageable": {
            "$ref": "#/components/schemas/PageableObject"
          },
          "empty": {
            "type": "boolean"
          }
        }
      },
      "PageableObject": {
        "type": "object",
        "properties": {
          "offset": {
            "type": "integer",
            "format": "int64"
          },
          "sort": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/SortObject"
            }
          },
          "paged": {
            "type": "boolean"
          },
          "pageNumber": {
            "type": "integer",
            "format": "int32"
          },
          "pageSize": {
            "type": "integer",
            "format": "int32"
          },
          "unpaged": {
            "type": "boolean"
          }
        }
      },
      "ResultPageUserVO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "$ref": "#/components/schemas/PageUserVO"
          }
        }
      },
      "SortObject": {
        "type": "object",
        "properties": {
          "direction": {
            "type": "string"
          },
          "nullHandling": {
            "type": "string"
          },
          "ascending": {
            "type": "boolean"
          },
          "property": {
            "type": "string"
          },
          "ignoreCase": {
            "type": "boolean"
          }
        }
      },
      "ResultListRoleVO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/RoleVO"
            }
          }
        }
      },
      "ResultListPermissionVO": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "type": "array",
            "items": {
              "$ref": "#/components/schemas/PermissionVO"
            }
          }
        }
      },
      "ResultString": {
        "type": "object",
        "properties": {
          "code": {
            "type": "integer",
            "format": "int32"
          },
          "message": {
            "type": "string"
          },
          "data": {
            "type": "string"
          }
        }
      }
    },
    "securitySchemes": {
      "Bearer Authentication": {
        "type": "http",
        "scheme": "bearer",
        "bearerFormat": "JWT"
      }
    }
  }
}