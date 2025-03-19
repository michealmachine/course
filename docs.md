
--- Repository Documentation ---

## Documentation for Online Course Platform Repository

This document provides a comprehensive overview of the Online Course Platform repository.

### Repository Summary

This repository contains a packed representation of the entire codebase. It is designed for AI systems to facilitate:

- Analysis
- Code review
- Automated processes

The repository is structured as follows:

1. Summary section (this document)
2. Repository information
3. Directory structure
4. Repository files with file paths and contents

This file is read-only. Modify original repository files, not this packed version. Use file paths to differentiate files. Handle sensitive information securely. Some files may be excluded due to `.gitignore` rules. Binary files are not included, but their paths are listed in the directory structure.

### Directory Structure

The repository directory structure is as follows:

```markdown
.cursorrules
.gitattributes
.gitignore
docs/
front/
src/main/java/com/zhangziqi/online_course_mine/
src/main/resources/
src/test/java/com/zhangziqi/online_course_mine/
src/main/resources/application-dev.yml
src/main/resources/application-test.yml
src/main/resources/application.yml
mvnw
mvnw.cmd
pom.xml
```

### File Documentation

#### `.cursorrules`

Provides instructions for using AI cursor tools within the repository. Includes commands for:

- Web search
- Repository context queries
- Documentation generation
- GitHub information access
- Browser automation

#### `.gitattributes`

Defines attributes for files in the repository, such as line endings.

#### `.gitignore`

Specifies intentionally untracked files that Git should ignore. Includes common IDE and build artifacts.

#### `docs/`

Contains documentation files in markdown format, including:

- 测试说明.md (Testing instructions)
- 当前进度描述.md (Current progress description)
- 课程流程设计.md (Course flow design)
- 实现进度.md (Implementation progress)
- 系统设计说明.md (System design documentation)
- 下一阶段.md (Next phase plan)
- 下一阶段开发需求.md (Next phase development requirements)
- 项目结构说明.md (Project structure documentation)
- 项目配置说明.md (Project configuration documentation)
- api.md / API接口说明.md (API documentation)
- fornt/开发进度.md / front/开发进度.md (Frontend development progress)
- fornt/前端开发规划.md (Frontend development plan)
- fornt/前端说明.md (Frontend documentation)

#### `front/`

Contains the frontend application code. Key files and directories include:

- `.cursorrules`, `.gitignore`, `components.json`, `eslint.config.mjs`, `next.config.ts`, `package.json`, `postcss.config.mjs`, `README.md`, `tsconfig.json` : Configuration and setup files for the frontend application.
- `public/`: Static assets such as images and icons.
- `src/app/`: Next.js application routing structure.
  - `(auth)/`: Authentication related pages (login, register, institution).
  - `(dashboard)/`: Dashboard pages (course management, user management, settings, etc.).
  - `preview/`: Course preview pages.
  - `institution/`: Institution specific pages.
  - `api/`: Optional API routes (BFF).
  - `layout.tsx`, `page.tsx`, `global.css`: Core layout and styling.
- `src/components/`: React components.
  - `ui/`: Reusable UI components (using Shadcn UI).
  - `dashboard/`: Dashboard specific components.
  - `preview/`: Preview specific components.
  - `providers/`: Theme and other providers.
  - `question/`: Question related components.
  - `course/`: Course related components.
- `src/hooks/`: Custom React hooks.
  - `useDebounce.ts`: Hook for debouncing function calls.
  - `useMediaUpload.ts`: Hook for media upload functionality.
- `src/lib/`: Utility libraries.
  - `utils.ts`: General utility functions.
  - `http.ts`, `request.ts`: HTTP request utilities.
- `src/services/`: API service clients.
  - `auth.ts`: Authentication service API calls.
  - `course-service.ts`, `course.ts`: Course service API calls.
  - `media-service.ts`: Media service API calls.
  - `category.ts`: Category service API calls.
  - `tag.ts`: Tag service API calls.
  - Other service files for different entities.
- `src/stores/`: Zustand state management stores.
  - `auth-store.ts`: Authentication state management.
  - `ui-store.ts`: UI related state management (theme, sidebar).
  - `course-store.ts`: Course related state management.
  - Other store files for different entities.
- `src/types/`: TypeScript type definitions.
  - `api.ts`: API response and request types.
  - `auth.ts`: Authentication related types.
  - `course.ts`: Course related types.
  - Other type definition files for different entities.
- `src/middleware.ts`: Next.js middleware for route protection.

#### `src/main/java/com/zhangziqi/online_course_mine/`

Contains the backend Java application code. Key packages include:

- `config/`: Configuration classes for Spring Boot application, security, Redis, MinIO, S3, OpenAPI, and asynchronous tasks.
- `constant/`: Constant definitions.
- `controller/`: REST controllers for handling API requests.
  - `AuthController`: Authentication endpoints (login, register, etc.).
  - `UserController`: User management endpoints.
  - `RoleController`: Role management endpoints.
  - `PermissionController`: Permission management endpoints.
  - `InstitutionController`: Institution application endpoints.
  - `InstitutionAuthController`: Institution authentication endpoints.
  - `InstitutionMemberController`: Institution member endpoints.
  - `ReviewerInstitutionController`: Institution review endpoints.
  - `StorageQuotaController`: Storage quota management endpoints.
  - `MediaController`: Media management endpoints.
  - `QuestionController`: Question management endpoints.
  - `QuestionGroupController`: Question group management endpoints.
  - `QuestionTagController`: Question tag management endpoints.
  - `CourseController`: Course management endpoints.
  - `ChapterController`: Chapter management endpoints.
  - `SectionController`: Section management endpoints.
- `exception/`: Custom exception classes and global exception handler.
- `model/`: Data models (Entities, DTOs, and VOs).
  - `entity/`: JPA entities representing database tables.
  - `dto/`: Data Transfer Objects for API requests and responses.
  - `vo/`: View Objects for API responses to frontend.
  - `enums/`: Enumerations for different statuses, types, and roles.
- `repository/`: Spring Data JPA repositories for database interaction.
- `security/`: Security related components, including JWT implementation.
  - `jwt/`: JWT token provider, filter, and blacklist service.
- `service/`: Service layer containing business logic.
  - `impl/`: Implementations of service interfaces.
- `excel/`: Components related to Excel data handling for question import.
- `util/`: Utility classes and helper functions.

#### `src/main/resources/`

Contains application resources. Key files and directories include:

- `application.yml`, `application-dev.yml`, `application-test.yml`: Spring Boot configuration files for different environments.
- `db/migration/`: Database migration scripts (Flyway).
- `templates/`: Thymeleaf templates for email and payment related views.
- `static/`: Static resources.

#### `src/test/java/com/zhangziqi/online_course_mine/`

Contains test classes for different layers of the application. Key packages include:

- `config/`: Test configuration classes.
- `controller/`: Controller tests.
- `integration/`: Integration tests.
- `repository/`: Repository tests.
- `security/jwt/`: JWT security component tests.
- `service/`: Service layer tests.

#### `mvnw`, `mvnw.cmd`, `pom.xml`

Maven wrapper scripts and project configuration file for building the backend application.

### Usage Guidelines

- Treat this file as read-only.
- Make changes in the original repository files.
- Use file paths to distinguish between files.
- Handle sensitive information with care.

### Notes

- Some files may be excluded based on `.gitignore` and Repomix configuration.
- Binary files are excluded from the packed representation, but paths are in the directory structure.

### Additional Information

No additional information is provided in the document.

--- End of Documentation ---
