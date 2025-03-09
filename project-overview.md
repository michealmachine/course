## Documentation for Online Course Platform Repository

### Overview

This repository contains the backend code for an online course platform. It is built using Spring Boot and implements features for user authentication, authorization, user and role management, and content management infrastructure. The repository is designed for AI consumption and provides a packed representation of the entire codebase for analysis and automation.

### Quick Start

1. **Prerequisites:**
    - Java 17
    - Maven
    - MySQL Database (for development environment)
    - Docker (optional, for MinIO)

2. **Clone the repository:**
   ```bash
   git clone <repository_url>
   cd online_course_mine
   ```

3. **Build the application:**
   ```bash
   ./mvnw clean install
   ```

4. **Run the application:**
   ```bash
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
   The application will be accessible at `http://localhost:8080`.

5. **Access API Documentation:**
   Visit Swagger UI at `http://localhost:8080/swagger-ui.html` to explore and test the API endpoints.

### Configuration

The application uses Spring Boot profiles for environment-specific configurations.

**Configuration Files:**

- `src/main/resources/application.yml`: Default application configuration.
- `src/main/resources/application-dev.yml`: Development environment configuration.
- `src/main/resources/application-test.yml`: Test environment configuration.

**Key Configuration Options:**

- **Database (MySQL & H2):**
    - Configuration in `application-dev.yml` and `application-test.yml`.
    - MySQL for development, H2 in-memory for testing.
    - JDBC URL, username, and password can be configured.

- **Redis:**
    - Configuration in `application-dev.yml` and `application-test.yml`.
    - Host, port, database, and password settings.
    - Used for caching, session management, and token blacklist.

- **JWT (JSON Web Token):**
    - Configuration in `src/main/java/com/zhangziqi/online_course_mine/config/security/JwtConfig.java`.
    - Secret key, access token expiration, and refresh token expiration.
    - Secret key should be securely managed, especially in production.

- **Email Service:**
    - Configuration in `application-dev.yml` and `application-test.yml`.
    - SMTP host, username, password, and properties.
    - Used for sending verification and email update codes.

- **File Storage (MinIO & AWS S3 compatible):**
    - Configuration in `src/main/java/com/zhangziqi/online_course_mine/config/MinioConfig.java` and `src/main/java/com/zhangziqi/online_course_mine/config/S3Config.java`.
    - Endpoint, access key, secret key, and bucket name for both MinIO and S3.
    - Choose between MinIO or S3 compatible storage using configuration profiles.

- **Verification Code (Captcha & Email):**
    - Captcha configuration in `src/main/java/com/zhangziqi/online_course_mine/config/KaptchaConfig.java`.
    - Email verification code settings (prefix, expiration) in `src/main/java/com/zhangziqi/online_course_mine/service/impl/EmailServiceImpl.java`.

**Environment Profiles:**

- **dev:** Development profile, using MySQL and detailed logging.
- **test:** Test profile, using H2 in-memory database and specific test configurations.

You can activate profiles using:
- `spring.profiles.active` property in `application.yml`.
- `-Dspring.profiles.active=dev` command line argument when running the application.
- `SPRING_PROFILES_ACTIVE=dev` environment variable.

### API Documentation

The API documentation is available in Swagger UI at `/swagger-ui.html` when the application is running.

**API Categories:**

#### Authentication API (`/api/auth`)

- **`GET /captcha/key`**:  Get captcha key.
- **`GET /captcha/image/{key}`**: Get captcha image by key.
- **`POST /email-verification-code`**: Send email verification code (for registration).
- **`POST /email-update-code`**: Send email update code (for email change).
- **`POST /register`**: Register a new user.
- **`POST /login`**: User login, retrieves JWT tokens.
- **`POST /refresh-token`**: Refresh JWT access token using refresh token.
- **`POST /logout`**: User logout.

#### User Management API (`/api/users`)

- **`GET /users`**: Get paginated user list (ADMIN role required).
- **`GET /users/{id}`**: Get user details by ID (ADMIN role required).
- **`POST /users`**: Create a new user (ADMIN role required).
- **`PUT /users/{id}`**: Update user information (ADMIN role required).
- **`DELETE /users/{id}`**: Delete user by ID (ADMIN role required).
- **`PATCH /users/{id}/status`**: Modify user status (ADMIN role required).
- **`PUT /users/{id}/roles`**: Assign roles to a user (ADMIN role required).
- **`DELETE /users/batch`**: Batch delete users (ADMIN role required).
- **`GET /users/current`**: Get current user information (authenticated user required).
- **`PUT /users/current`**: Update current user profile (authenticated user required).
- **`PUT /users/current/password`**: Change current user password (authenticated user required).
- **`PUT /users/current/email`**: Update current user email (authenticated user required).
- **`POST /users/current/avatar`**: Upload/update current user avatar (authenticated user required).
- **`GET /users/basic/{userId}`**: Get basic user information by ID (public access).

#### Role Management API (`/api/roles`)

- **`GET /roles`**: Get role list (ADMIN role required).
- **`GET /roles/{id}`**: Get role details by ID (ADMIN role required).
- **`POST /roles`**: Create a new role (ADMIN role required).
- **`PUT /roles/{id}`**: Update role information (ADMIN role required).
- **`DELETE /roles/{id}`**: Delete role by ID (ADMIN role required).
- **`PUT /roles/{id}/permissions`**: Assign permissions to a role (ADMIN role required).
- **`DELETE /roles/batch`**: Batch delete roles (ADMIN role required).

#### Permission Management API (`/api/permissions`)

- **`GET /permissions`**: Get permission list (ADMIN role required).
- **`GET /permissions/{id}`**: Get permission details by ID (ADMIN role required).
- **`POST /permissions`**: Create a new permission (ADMIN role required).
- **`PUT /permissions/{id}`**: Update permission information (ADMIN role required).
- **`DELETE /permissions/{id}`**: Delete permission by ID (ADMIN role required).
- **`DELETE /permissions/batch`**: Batch delete permissions (ADMIN role required).

### Dependencies and Requirements

- **Java:** 17
- **Maven:**  For build management.
- **Spring Boot:** 3.3.9
- **Spring Security:** For authentication and authorization.
- **Spring Data JPA:** For database interaction.
- **MySQL:** Relational database for development environment.
- **H2 Database:** In-memory database for testing environment.
- **Redis:** For caching, session management, and token blacklist.
- **JWT (JSON Web Token):** For secure API authentication.
- **Kaptcha:** For generating captcha images.
- **Thymeleaf:** For email templating.
- **MinIO/S3:** For file storage.
- **OpenAPI (Swagger):** For API documentation.
- **Lombok:** For reducing boilerplate code.

Dependencies are managed using Maven, refer to `pom.xml` for detailed versions.

### Advanced Usage Examples

1. **Setting up different environments:**
   - Utilize Spring Boot profiles to manage configurations for development, testing, and production environments.
   - Create separate `application-*.yml` files for each environment and activate them using profiles.

2. **Extending User Roles and Permissions:**
   - Define new roles in `src/main/java/com/zhangziqi/online_course_mine/model/enums/RoleEnum.java`.
   - Create new permissions using the Permission Management API.
   - Assign permissions to roles and roles to users via API or database.
   - Implement custom authorization logic using `@PreAuthorize` annotations or custom security components.

3. **Integrating with Frontend Application:**
   - Configure the frontend application to communicate with the backend API using the base URL (e.g., `http://localhost:8080/api`).
   - Implement JWT token handling in the frontend to authenticate API requests.
   - Use API documentation (`/swagger-ui.html`) to understand and integrate with backend endpoints.

4. **Using File Storage for Course Content:**
   - Configure either MinIO or AWS S3 compatible storage using application properties.
   - Use `MinioService` or `S3Service` (if implemented, based on `S3Config`) to upload, download, and manage course related files (videos, documents, etc.).
   - Store file URLs in the database and serve them to users as needed.

This documentation provides a comprehensive guide to understand, set up, and use the Online Course Platform Backend Repository. For detailed API usage, please refer to the Swagger UI documentation.