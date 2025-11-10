# 航点优化 API（Spring Boot 版本）

基于 Spring Boot 的无人机航点优化服务，使用先进的算法包括网格扫描、集合覆盖优化和 TSP 路径规划。

## 目录

- [功能特性](#功能特性)
- [算法概述](#算法概述)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [API 文档](#api-文档)
- [前端集成](#前端集成)
- [参数说明](#参数说明)
- [项目结构](#项目结构)
- [开发指南](#开发指南)

## 功能特性

- 🎯 **基于网格的候选点生成**：使用栅格扫描算法生成航点候选位置
- 📐 **集合覆盖优化**：使用贪心集合覆盖算法减少航点数量，同时保持覆盖范围
- 🗺️ **TSP 路径规划**：使用旅行商问题（TSP）求解器优化航点序列（OR-Tools）
- 📷 **相机模型支持**：支持多种相机模式（wide、medium_tele、tele），精确计算 GSD
- ⚙️ **可配置参数**：可调整重叠率、飞行高度、云台俯仰角等参数
- 🌐 **RESTful API**：基于 Spring Boot 的 REST 接口
- 📊 **统计信息**：返回优化统计信息，包括减少率和覆盖率

## 算法概述

优化过程分为三个主要阶段：

### 1. 网格候选点生成
- 根据飞行高度和云台俯仰角计算相机覆盖范围
- 使用栅格扫描生成候选航点网格
- 过滤候选点，仅保留多边形边界内的点
- 支持蛇形扫描模式以提高覆盖效率

### 2. 集合覆盖优化（可选）
- 将多边形划分为单元网格
- 计算覆盖矩阵：哪些候选点覆盖哪些单元
- 使用贪心算法选择覆盖所有单元的最少航点
- 在保持完整覆盖的同时显著减少航点数量

### 3. TSP 路径优化（可选）
- 计算所选航点之间的距离矩阵
- 使用 OR-Tools 求解旅行商问题（回退到最近邻算法）
- 对航点进行排序以最小化总飞行路径距离

## 环境要求

- Java 21+
- Maven 3.6+

## 快速开始

### 安装依赖

```bash
cd java-route
mvn clean install
```

### 启动服务

```bash
mvn spring-boot:run
```

或者先编译再运行：

```bash
mvn clean package
java -jar target/java-route-0.0.1-SNAPSHOT.jar
```

服务默认启动在 `http://localhost:9527`

### 可用端点

- **健康检查**：http://localhost:9527/health
- **根路径**：http://localhost:9527/
- **优化接口**：http://localhost:9527/api/v1/wayline/optimize

### 配置说明

配置文件位于 `src/main/resources/application.properties`：

```properties
# 服务器配置
server.port=9527
server.address=0.0.0.0

# API配置
app.api.title=航点优化API
app.api.version=1.0.0

# CORS配置
app.cors.allowed-origins=*

# 算法默认参数
app.algorithm.default-overlap-front=0.6
app.algorithm.default-overlap-side=0.4
app.algorithm.default-camera-mode=wide
app.algorithm.default-cell-size-factor=0.5
```

## API 文档

### POST `/api/v1/wayline/optimize`

优化多边形区域的航点。

#### 请求体

```json
{
  "polygon_coords": [
    {"longitude": 116.3974, "latitude": 39.9093},
    {"longitude": 116.4074, "latitude": 39.9093},
    {"longitude": 116.4074, "latitude": 39.9193},
    {"longitude": 116.3974, "latitude": 39.9193}
  ],
  "flight_height_m": 100,
  "gsd_cm": 2.5,
  "overlap_front": 0.6,
  "overlap_side": 0.4,
  "gimbal_pitch": -90,
  "main_angle": 0,
  "camera_mode": "wide",
  "use_set_cover": true,
  "use_tsp": true
}
```

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "waypoints": [
      {
        "lng": 116.3974,
        "lat": 39.9093,
        "height": 100,
        "pitch": -90
      }
    ],
    "stats": {
      "total_candidates": 150,
      "selected_waypoints": 45,
      "reduction_rate": 0.7,
      "coverage_rate": 1.0
    }
  }
}
```

#### 响应码说明

- `code: 0` - 成功
- `code: 400` - 请求参数错误
- `code: 500` - 服务器内部错误

### GET `/health`

健康检查接口，返回服务状态。

#### 响应

```json
{
  "status": "ok",
  "service": "wayline-optimizer",
  "version": "1.0.0"
}
```

## 前端集成

### TypeScript 接口定义

```typescript
interface PolygonPoint {
  longitude: number
  latitude: number
}

interface WaypointOptimizeRequest {
  polygon_coords: PolygonPoint[]
  flight_height_m: number
  gsd_cm?: number
  overlap_front?: number
  overlap_side?: number
  gimbal_pitch?: number
  main_angle?: number
  camera_mode?: string
  use_set_cover?: boolean
  use_tsp?: boolean
}

interface OptimizedWaypoint {
  lng: number
  lat: number
  height: number
  pitch?: number
}

interface OptimizationStats {
  total_candidates: number
  selected_waypoints: number
  reduction_rate: number
  coverage_rate: number
}

interface WaypointOptimizeResponse {
  code: number
  message: string
  data: {
    waypoints: OptimizedWaypoint[]
    stats: OptimizationStats
  }
}
```

### 使用示例

```typescript
import axios from 'axios'

const OPTIMIZER_BASE_URL = 'http://localhost:9527'

async function optimizeWaypoints(params: WaypointOptimizeRequest): Promise<WaypointOptimizeResponse> {
  const url = `${OPTIMIZER_BASE_URL}/api/v1/wayline/optimize`
  
  try {
    const response = await axios.post<WaypointOptimizeResponse>(url, params, {
      headers: {
        'Content-Type': 'application/json',
      },
      timeout: 30000,
    })
    
    if (response.data.code !== 0) {
      throw new Error(response.data.message || '优化失败')
    }
    
    return response.data
  } catch (error: any) {
    console.error('航点优化API调用失败:', error)
    throw error
  }
}

// 调用示例
const params: WaypointOptimizeRequest = {
  polygon_coords: [
    { longitude: 116.3974, latitude: 39.9093 },
    { longitude: 116.4074, latitude: 39.9093 },
    { longitude: 116.4074, latitude: 39.9193 },
    { longitude: 116.3974, latitude: 39.9193 }
  ],
  flight_height_m: 100,
  gsd_cm: 2.5,
  overlap_front: 0.6,
  overlap_side: 0.4,
  gimbal_pitch: -90,
  main_angle: 0,
  camera_mode: 'wide',
  use_set_cover: true,
  use_tsp: true
}

const result = await optimizeWaypoints(params)
console.log(`生成了 ${result.data.waypoints.length} 个航点`)
console.log(`减少率: ${(result.data.stats.reduction_rate * 100).toFixed(1)}%`)
```

### 配置说明

**注意**：后端已配置为使用 `snake_case` 命名策略（通过 `JacksonConfig`），因此前端可以直接使用下划线命名，无需转换。

默认的基础URL是 `http://localhost:9527`。如需修改，请在前端代码中更新 `OPTIMIZER_BASE_URL` 常量。

## 参数说明

### 必需参数

- **polygon_coords**：多边形顶点数组（至少3个点）
  - 每个点格式：`{longitude: number, latitude: number}`
- **flight_height_m**：飞行高度（米）

### 可选参数

- **gsd_cm**：地面采样距离（厘米/像素，用于验证）
- **overlap_front**：前向重叠率（0.0-1.0，默认：0.6）
- **overlap_side**：侧向重叠率（0.0-1.0，默认：0.4）
- **gimbal_pitch**：云台俯仰角（度，-90到-45，默认：-90）
  - -90°：正射（垂直向下）
  - -45°：倾斜
- **main_angle**：主飞行方向角度（度，默认：0）
- **camera_mode**：相机模式（`'wide'`、`'medium_tele'`、`'tele'`，默认：`'wide'`）
- **use_set_cover**：启用集合覆盖优化（默认：true）
- **use_tsp**：启用TSP路径优化（默认：true）

### 相机模式

- **wide**：广角镜头（24mm焦距）
- **medium_tele**：中长焦镜头（70mm焦距）
- **tele**：长焦镜头（168mm焦距）

## 项目结构
```
java-route/
├── src/
│   ├── main/
│   │   ├── java/com/example/java_route/
│   │   │   ├── config/
│   │   │   │   ├── AppConfig.java          # 应用配置
│   │   │   │   ├── CorsConfig.java         # CORS配置
│   │   │   │   └── JacksonConfig.java      # JSON命名策略配置（snake_case）
│   │   │   ├── controller/
│   │   │   │   └── WaylineController.java  # REST API控制器
│   │   │   ├── core/
│   │   │   │   ├── CameraModel.java        # 相机模型和GSD计算
│   │   │   │   ├── CameraProfile.java      # 相机配置
│   │   │   │   ├── CandidateGenerator.java # 网格候选点生成
│   │   │   │   ├── CoverageMapper.java     # 覆盖单元映射
│   │   │   │   ├── MissionOptimizer.java   # 主优化逻辑
│   │   │   │   ├── SetCoverSolver.java     # 集合覆盖算法
│   │   │   │   └── TspSolver.java          # TSP求解器
│   │   │   ├── dto/
│   │   │   │   ├── PolygonPoint.java       # 多边形顶点DTO
│   │   │   │   ├── Waypoint.java           # 航点DTO
│   │   │   │   ├── WaypointOptimizeRequest.java  # 优化请求DTO
│   │   │   │   └── WaypointOptimizeResponse.java # 优化响应DTO
│   │   │   ├── utils/
│   │   │   │   ├── CoordinatesUtils.java   # 坐标转换工具
│   │   │   │   └── GeometryUtils.java      # 几何工具
│   │   │   └── JavaRouteApplication.java   # Spring Boot主类
│   │   └── resources/
│   │       └── application.properties      # 应用配置文件
│   └── test/
│       └── java/                           # 测试代码
├── pom.xml                                 # Maven配置文件
└── README.md                               # 本文档
```

### 主要依赖

- **Spring Boot 3.5.7** - Web框架
- **Lombok** - 减少样板代码
- **OR-Tools 9.8.3296** - TSP求解器
- **JTS Topology Suite 1.19.0** - 几何计算（替代 Python 的 Shapely）
- **Apache Commons Math 3.6.1** - 数值计算

### JSON 命名策略

项目已配置为使用 `snake_case` 命名策略（在 `JacksonConfig` 中），与 Python 版本保持一致。这意味着：

- 请求/响应中的字段名使用下划线：`polygon_coords`、`flight_height_m`
- Java 类中的字段名使用驼峰：`polygonCoords`、`flightHeightM`
- Jackson 会自动进行转换