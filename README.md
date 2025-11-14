# **电信管理系统 (Telecom System)**


## 项目简介

这是一个电信管理系统，用于管理电信业务相关功能。


## 功能特性

### 企业用户
- *登录验证*

- *用户管理*

- *信息修改*

-  *流量峰期分析*

### 普通用户

- *登录验证*

- *套餐查看*

- *套餐修改*

- *充值*


## 技术栈

***HTML/CSS/JavaScript***

***Java***


## 安装和使用

启动 TelecomSystemApplication.java  

## 当前进度

已完成后端基本功能开发，前端页面设计中。
（运行项目需要配置数据库连接，先在postgre服务器下新建telecom_system数据库，再运行相关 SQL 脚本，位于 `SqlScript/` 目录下）  

（测试代码（test）和开发代码（main）的数据库配置均是telecom_system数据库，但是测试代码会回滚数据，不会污染原始数据）  

## 项目结构
telecom-system/  
├── src/main/java/com/telecom_system/  
│   ├── TelecomSystemApplication.java          # Spring Boot 主类  
│   ├── config/                                # 配置类  
│   │   ├── CorsConfig.java  
│   │   ├── SwaggerConfig.java  
│   │   └── SecurityConfig.java (可选)  
│   ├── entity/                                # 实体类  
│   │   ├── UserInfo.java  
│   │   ├── AdminInfo.java  
│   │   ├── PackageInfo.java  
│   │   ├── LoginInfo.java  
│   ├── repository/                            # 数据访问层  
│   │   ├── UserRepository.java  
│   │   ├── AdminRepository.java  
│   │   ├── PackageRepository.java  
│   │   ├── LoginInfoRepository.java  
│   │   └── StatisticsRepository.java  
│   ├── service/                               # 业务逻辑层  
│   │   ├── UserService.java  
│   │   ├── AdminService.java  
│   │   ├── PackageService.java  
│   │   ├── LoginService.java  
│   │   ├── LoginInfoService.java  
│   │   └── StatisticsService.java  
│   ├── controller/                            # 控制层  
│   │   ├── AuthController.java  
│   │   ├── UserController.java  
│   │   ├── AdminController.java  
│   │   ├── PackageController.java  
│   │   ├── LoginInfoController.java  
│   │   └── StatisticsController.java  
│   └── exception/                             # 异常处理 (可选)  
│       ├── GlobalExceptionHandler.java  
│       ├── BusinessException.java  
│       └── ErrorResponse.java  
├── src/main/resources/  
│   ├── application.properties                 # 主配置文件  
│   ├── application-dev.properties             # 开发环境配置  
│   ├── application-prod.properties            # 生产环境配置  
│   ├── static/                                # 静态资源  
│   │   └── index.html (前端页面)  
│   └── templates/                             # 模板文件  
├── src/test/java/com/telecom_system/          # 测试代码  
│   ├── service/  
│   │   ├── UserServiceTest.java  
│   │   └── LoginServiceTest.java  
│   └── controller/  
│       ├── UserControllerTest.java  
│       └── AuthControllerTest.java  
├── sql/                                       # SQL 脚本  
│   ├── init_schema.sql                        # 初始化表结构  
│   ├── init_data.sql                          # 初始化数据  
│   └── views_and_functions.sql                # 视图和函数  
├── pom.xml (Maven) 或 build.gradle (Gradle)   # 构建配置  
├── Dockerfile (可选)                          # Docker 配置  
└── README.md                                  # 项目说明  



