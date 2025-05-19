# Assignment 1

## Register
1. 前端界面 (登录/注册页面)
   位置：`docs-web/src/main/webapp/src/app/docs/`
2. 后端REST API
   位置：`docs-web/src/main/java/com/sismics/docs/rest/resource/UserResource.java`
3. 核心业务逻辑
   位置：`docs-core/src/main/java/com/sismics/docs/core/`
   * `dao/` - 用户数据访问层 
     * `UserDao.java`
     ```java
     /**
     * Creates a new user.
     *
     * @param user User to create
     * @param userId User ID
     * @return User ID
     * @throws Exception e
     */
     public String create(User user, String userId) throws Exception{}
     ```
   * `service/` - 用户服务层 
   * `model/jpa/` - 用户实体类 
   * `util/authentication/` - 认证相关工具
4. 数据库相关 位置：`docs-core/src/main/resources/db/update/`
5. 邮件模板
   位置：`docs-core/src/main/resources/email_template/`

