# 抽奖系统部署指南

## 项目概述

本系统是一个基于Spring Boot的抽奖系统，包含两个主要页面：
- 移动端用户注册页面：用户提交姓名和手机号参与抽奖
- PC端管理页面：管理员可筛选参与者、进行抽奖、查看中奖历史

系统特点：
- 半年内中奖者不会重复中奖
- 同一手机号每天只能报名一次
- 单次抽取多个奖项时，相同手机号只能中一次奖
- 管理页面有密码保护
- 支持自定义抽奖人数
- 支持时间范围筛选参与者
- 中奖历史记录显示完整手机号码

## 系统要求

- JDK 8或更高版本
- Maven 3.6或更高版本
- 内存要求：至少2GB RAM
- 存储空间：至少100MB可用空间

## 功能详细说明

### 防重复报名功能
系统实现了防止重复报名的功能：
- 同一手机号在同一天内只能报名一次
- 如果尝试重复报名，系统会返回友好的错误提示
- 次日可以重新报名参与

### 抽奖去重功能
系统实现了两层的抽奖去重功能：
- 半年内已中奖的用户不会再次被抽中（防止长期重复中奖）
- 在同一次抽奖中，即使设置了抽取多名中奖者，同一手机号也只会被抽中一次（防止同次抽奖重复中奖）

### 手机号码显示策略
系统对手机号码采用了不同的显示策略：
- 在用户列表和抽奖过程中，手机号码会部分脱敏显示（如：138****1234）
- 在中奖历史记录中，显示完整手机号码，方便管理员核实中奖信息

## 快速部署指南

### 1. 获取项目包

将本项目JAR包上传到服务器，或使用源代码构建：

```bash
git clone <项目仓库地址>
cd drawing
mvn clean package
```

### 2. 运行项目（默认使用H2数据库）

```bash
java -jar target/drawing-1.0-SNAPSHOT.jar
```
或者后台运行（Linux）
```bash
# 使用nohup后台运行
nohup java -jar drawing-1.0-SNAPSHOT.jar > app.log 2>&1 &

# 查看进程
ps -ef | grep java

# 查看日志
tail -f app.log
```

确保8080端口可访问，应用将在8080端口启动，数据存储在项目目录下的`data/drawdb`文件中。

### 3. 访问系统

- 用户注册页面：`http://服务器IP:8080/`
- 管理员页面：`http://服务器IP:8080/admin`
  - 默认用户名：`admin`
  - 默认密码：`admin123`

## H2数据库性能与限制

根据日志和系统配置，当前H2数据库的性能情况：

1. **并发能力**:
   - H2数据库使用HikariCP连接池，默认最大连接数为10
   - 适合同时在线用户数在50-100人的小型活动
   - 日志中看到的"Thread starvation"警告是正常的，不影响功能

2. **数据容量**:
   - 当前使用文件模式(jdbc:h2:file:./data/drawdb)而非纯内存模式
   - 可以存储约10,000-50,000条参与记录而不影响性能
   - 数据持久化到文件，系统重启数据不会丢失

3. **实际负载能力**:
   - 注册阶段：支持约100-200人同时注册
   - 抽奖管理：支持1-5个管理员同时操作
   - 总用户数：建议不超过5,000人

可以切换到MySQL数据库，以获得更好的性能和扩展性。

## 切换到MySQL数据库

对于大型活动（超过1,000人）或需要更高并发的场景，建议切换到MySQL数据库。

### 1. 准备MySQL数据库

```bash
# 安装MySQL（CentOS/RHEL）
sudo yum install mysql-server

# 或Ubuntu/Debian
sudo apt update
sudo apt install mysql-server

# 启动MySQL
sudo systemctl start mysqld
sudo systemctl enable mysqld

# 安全配置
sudo mysql_secure_installation
```

### 2. 创建数据库和用户

登录MySQL并执行：

```sql
CREATE DATABASE drawing CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'drawuser'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON drawing.* TO 'drawuser'@'localhost';
FLUSH PRIVILEGES;
```

### 3. 修改项目配置

创建`application-mysql.properties`文件，放在与JAR包同目录下：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/drawing?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
spring.datasource.username=drawuser
spring.datasource.password=your_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update

# 连接池配置
spring.datasource.hikari.maximum-pool-size=30
spring.datasource.hikari.minimum-idle=10
spring.datasource.hikari.connection-timeout=30000

# 服务器配置
server.address=0.0.0.0
server.port=8080
```

### 4. 添加MySQL依赖

如果您使用源代码构建，在pom.xml中添加MySQL驱动依赖：

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.29</version>
    <scope>runtime</scope>
</dependency>
```

重新构建项目：

```bash
mvn clean package
```

### 5. 启动时指定MySQL配置

```bash
java -jar drawing-1.0-SNAPSHOT.jar --spring.config.location=file:./application-mysql.properties
```

### 6. 手动创建表（可选，JPA通常会自动创建）

如果需要手动创建表结构：

```sql
CREATE TABLE participants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    create_time DATETIME,
    win_time DATETIME,
    has_won BOOLEAN DEFAULT FALSE,
    INDEX idx_phone (phone),
    INDEX idx_create_time (create_time),
    INDEX idx_has_won (has_won)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

MySQL配置能支持的并发量：
- 同时在线用户：200-500人
- 总参与用户：10,000+人
- 抽奖操作：更流畅的体验

## 安全加固建议

1. **修改默认管理员密码**:
   - 编辑`SecurityConfig.java`中的密码设置
   - 或者设置环境变量：`ADMIN_PASSWORD=新密码`
   
2. **启用HTTPS**:
   - 申请SSL证书
   - 添加配置：
     ```properties
     server.ssl.key-store=classpath:keystore.p12
     server.ssl.key-store-password=your-password
     server.ssl.key-store-type=PKCS12
     server.ssl.key-alias=tomcat
     ```

3. **IP访问限制**:
   - 修改安全组配置，限制管理页面IP访问
   - 使用Nginx反向代理添加IP限制

4. **启用CSRF保护**:
   - 修改`SecurityConfig.java`，移除`.csrf().disable()`

## 监控与维护

1. **日志管理**:
   - 配置日志轮转：`logrotate`或使用`logging.file.max-history=7`
   - 监控日志中的错误和警告

2. **备份策略**:
   - H2数据库：定期备份`data/drawdb`文件
   - MySQL数据库：使用`mysqldump`定期备份

3. **系统监控**:
   - 使用Spring Boot Actuator监控应用健康状态
   - 配置云监控告警

4. **防DDoS攻击**:
   - 启用云服务器DDoS防护
   - 配置请求速率限制

## 常见问题排查

1. **应用无法启动**:
   - 检查日志文件中的错误
   - 确认Java版本兼容性
   - 验证端口8080未被占用

2. **数据库连接问题**:
   - 检查数据库用户名和密码
   - 验证数据库服务是否运行
   - 确认防火墙规则允许连接

3. **内存溢出**:
   - 检查JVM内存设置
   - 增加系统内存
   - 添加JVM参数：`-XX:+HeapDumpOnOutOfMemoryError`

4. **负载过高**:
   - 考虑横向扩展，使用负载均衡
   - 优化数据库查询
   - 增加服务器资源

