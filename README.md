# sky-take-out

一个基于 Spring Boot + MyBatis 的外卖后端项目（Maven 多模块），包含管理端与用户端核心业务：下单支付、订单流转、报表统计、工作台、WebSocket 提醒等。

## 项目结构

```text
sky-take-out
├─ sky-common   # 常量、异常、工具类、通用配置属性
├─ sky-pojo     # DTO / Entity / VO 模型
└─ sky-server   # Spring Boot 服务（controller/service/mapper/config）
```

- 启动类：`sky-server/src/main/java/com/sky/SkyApplication.java`
- 主配置：`sky-server/src/main/resources/application.yml`
- Mapper XML：`sky-server/src/main/resources/mapper/*.xml`

## 技术栈

- Java 17
- Spring Boot 2.7.3
- MyBatis + PageHelper
- MySQL + Druid
- Redis
- JWT
- Knife4j（API 文档）
- WebSocket
- Apache POI（Excel 报表导出）

## 环境要求

- JDK 17（推荐）
- Maven 3.8+
- MySQL 8+
- Redis 6+

> 注意：JDK 21 在当前依赖组合下可能出现 Lombok/Javac 兼容问题，建议使用 JDK 17。

## 快速启动

### 1) 配置本地参数

项目使用 `spring.profiles.active=dev`，请准备：

- `sky-server/src/main/resources/application-dev.yml`

可参考模板（如存在）：
- `sky-server/src/main/resources/application-dev.yml.template`

关键配置项包括：

- `sky.datasource.*`（MySQL）
- `sky.redis.*`
- `sky.alioss.*`（如需）
- `sky.wechat.*`（如需）
- `sky.shop.address`（门店地址）
- `sky.baidu.ak`（百度地图服务端 AK）

### 2) 编译

```bash
mvn -pl sky-server -am -DskipTests compile
```

### 3) 运行

```bash
mvn -pl sky-server spring-boot:run
```

服务默认端口：`8080`

## 常用命令

### 编译 / 打包

```bash
mvn -DskipTests compile
mvn clean package -DskipTests
mvn -pl sky-server -am -DskipTests compile
```

### 测试（项目当前无已提交测试文件）

```bash
mvn test
mvn -pl sky-server test
mvn -pl sky-server -Dtest=OrderServiceImplTest test
mvn -pl sky-server -Dtest=OrderServiceImplTest#shouldRejectOutOfRangeAddress test
```

## 核心功能

### 订单能力

- 用户提交订单、支付、取消、再来一单
- 15 分钟未支付自动取消（定时任务）
- 每天凌晨 2 点自动将配送中订单置为已完成
- 订单催单（用户端）+ 管理端 WebSocket 实时提醒

### 配送范围校验

- 下单时进行配送距离校验（5km）
- 通过百度地图地理编码 + 路线距离实现
- 校验失败时按策略降级放行（不阻断下单）

### 报表统计（管理端）

- 营业额统计：`/admin/report/turnoverStatistics`
- 用户统计：`/admin/report/userStatistics`
- 订单统计：`/admin/report/ordersStatistics`
- 销量 Top10：`/admin/report/top10`
- 运营数据导出（近30天 Excel）：`/admin/report/export`

### 工作台

- 今日数据总览（营业额、有效订单、完成率、客单价、新增用户）
- 订单概览（待接单/待派送/派送中/已完成/已取消）
- 菜品总览、套餐总览

## WebSocket

- 连接地址：`ws://<host>:8080/ws/{sid}`
- 推荐管理端使用：`/ws/1`
- 消息类型：
  - `type=1`：来单提醒（支付成功）
  - `type=2`：催单提醒

## API 文档

项目引入 Knife4j，启动后可访问：

- `http://localhost:8080/doc.html`

## 数据库说明

- 数据库名示例：`sky_take_out`
- 统计相关口径默认按订单状态判断：
  - 有效订单：`status = 5（已完成）`

## 开发注意事项

- 项目为多模块 Maven 结构，尽量在对应模块内修改代码。
- 避免提交真实密钥、AK/SK、数据库密码等敏感信息。
- 业务异常优先使用 `BaseException` 体系，统一由全局异常处理返回。
- 修改后建议至少执行：
  - `mvn -pl sky-server -am -DskipTests compile`

## License

仅用于学习与内部开发使用。
