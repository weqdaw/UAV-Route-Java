# Waypoint Optimization API (Spring Boot Version)

A drone waypoint optimization service based on Spring Boot, using advanced algorithms including grid scanning, set coverage optimization, and TSP path planning.

## Features

- **Grid-based Candidate Point Generation**: Generates waypoint candidate locations using a grid scanning algorithm.
- **Set Coverage Optimization**: Reduces the number of waypoints while maintaining coverage using a greedy set coverage algorithm.
- **TSP Path Planning**: Optimizes waypoint sequences using a Traveling Salesman Problem (TSP) solver (OR-Tools).
- **Camera Model Support**: Supports multiple camera modes (wide, medium_tele, tele), accurately calculating GSD.
- **Configurable Parameters**: Adjustable parameters such as overlap rate, flight altitude, and gimbal pitch angle.
- **RESTful API**: REST interface based on Spring Boot.
- **Statistics**: Returns optimization statistics, including reduction rate and coverage.
  
  ## Algorithm Overview
  
  The optimization process consists of three main stages:

### 1. Grid Candidate Point Generation

- Calculate camera coverage based on flight altitude and gimbal pitch angle
- Generate candidate waypoint mesh using raster scanning
- Filter candidate points, retaining only those within polygon boundaries
- Support serpentine scanning mode to improve coverage efficiency

### 2. Set Coverage Optimization (Optional)

- Divide the polygon into a cell mesh
- Calculate the coverage matrix: which candidate points cover which cells
- Select the waypoint with the fewest cells covering all cells using a greedy algorithm
- Significantly reduce the number of waypoints while maintaining complete coverage

### 3. TSP Path Optimization (Optional)

- Calculate the distance matrix between selected waypoints
- Solve the Traveling Salesman Problem using OR-Tools (back to nearest neighbor algorithm)
- Sort waypoints to minimize the total flight path distance

## Environment Requirements

- Java 21+
- Maven 3.6+

## Quick Start

### Install Dependencies

```bash
cd java-route
mvn clean install
```

### Start the service

```bash
mvn spring-boot:run
```

Or compile first and then run:

```bash
mvn clean package
java -jar target/java-route-0.0.1-SNAPSHOT.jar
```

The service starts at `http://localhost:9527` by default.

### Available endpoints

- **Health check**: http://localhost:9527/health
- **Root path**: http://localhost:9527/
- **Optimize API**: http://localhost:9527/api/v1/wayline/optimize
  
  ### Configuration instructions
  
  The configuration file is located at `src/main/resources/application.properties`:

```properties
# Server configuration
server.port=9527
server.address=0.0.0.0

# API Configuration
app.api.title=Waypoint Optimization API
app.api.version=1.0.0

# CORS Configuration
app.cors.allowed-origins=*

# Algorithm Default Parameters
app.algorithm.default-overlap-front=0.6
app.algorithm.default-overlap-side=0.4
app.algorithm.default-camera-mode=wide
app.algorithm.default-cell-size-factor=0.5
```

## API Documentation

### POST `/api/v1/wayline/optimize`

Optimizes waypoints in polygonal regions.

#### Request body

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
"use_set_cover": true, "use_tsp": true
}
```

#### Response

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

#### Response Code Explanation

- `code: 0` - Success
- `code: 400` - Incorrect request parameters
- `code: 500` - Internal server error

### GET `/health`

The health check interface returns the service status.

#### Response

```json
{ 
"status": "ok", 
"service": "wayline-optimizer", 
"version": "1.0.0"
}
```

## Front-end integration

### TypeScript interface definition

```typescript
interface PolygonPoint { 
longitude:number 
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

interface OptimizationStats { total_candidates: number 
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

### Usage examples

```typescript
import axios from 'axios'
const OPTIMIZER_BASE_URL = 'http://localhost:9527'
async function optimizeWaypoints(params: WaypointOptimizeRequest): Promise<WaypointOptimizeResponse> { 
const url = `${OPTIMIZER_BASE_URL}/api/v1/wayline/optimize` 
try { 
const response = await axios.post<WaypointOptimizeResponse>(url, params, { 
headers: { 'Content-Type': 'application/json',
},
timeout: 30000,
})
if (response.data.code !== 0) {
throw new Error(response.data.message || 'Optimization failed')
}
return response.data
} catch (error: any) {
console.error('Waypoint optimization API call failed:', error)
throw error
}
}
// Example call
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
console.log(`Generated ${result.data.waypoints.length} waypoints`)
console.log(`Reduction rate: ${(result.data.stats.reduction_rate * 100).toFixed(1)}%`)
```

### Configuration Instructions

**Note**: The backend is configured to use the `snake_case` naming strategy (via (JacksonConfig), so the frontend can directly use underscores in the naming without conversion.
The default base URL is `http://localhost:9527`. To modify it, update the `OPTIMIZER_BASE_URL` constant in the frontend code.

## Parameter Description

### Required Parameters

- **polygon_coords**: Array of polygon vertices (at least 3 points)
  
- Format for each point: `{longitude: number, latitude: number}`
  
- **flight_height_m**: Flight altitude (meters)
  

###### Optional Parameters

- **gsd_cm**: Ground sampling distance (cm/pixel, for verification)
  
- **overlap_front**: Forward overlap ratio (0.0-1.0, default: 0.6)
  
- **overlap_side**: Lateral overlap ratio (0.0-1.0, default: 0.4)
  
- **gimbal_pitch**: Gimbal pitch angle (degrees, -90 to -45, default: -90)
  
- -90°: Orthographic (vertically downward)
  
- -45°: Tilt
  
- **main_angle**: Main flight direction angle (degrees, default: 0)
  
- **camera_mode**: Camera mode (`'wide'`, `'medium_tele'`, `'tele'`, default: `'wide'`)
  
- **use_set_cover**: Enable set coverage optimization (default: true)
  
- **use_tsp**: Enable TSP path optimization (default: true)
  

### Camera Modes

- **wide**: Wide-angle lens (24mm focal length)
  
- **medium_tele**: Medium telephoto lens (70mm focal length)
  
- **tele**: Telephoto lens (168mm focal length)
  

## Project Structure

```
java-route/
├── src/
│ ├── main/
│ │ ├── java/com/example/java_route/
│ │ │ ├── config/
│ │ │ │ ├── AppConfig.java # Application configuration
│ │ │ │ ├── CorsConfig.java # CORS configuration
│ │ │ │ └── JacksonConfig.java # JSON naming strategy configuration (snake_case)
│ │ │ ├── controller/
│ │ │ │ └── WaylineController.java # REST API controller
│ │ │ ├── core/
│ │ │ │ ├── CameraModel.java # Camera model and GSD calculation
│ │ │ │ ├── CameraProfile.java # Camera configuration
│ │ │ │ ├── CandidateGenerator.java # Mesh candidate point generation
│ │ │ │ ├── CoverageMapper.java # Coverage cell mapping
│ │ │ │ ├── MissionOptimizer.java # Main optimization logic
│ │ │ │ ├── SetCoverSolver.java # Set cover algorithm
│ │ │ │ └── TspSolver.java # TSP solver
│ │ │ ├── dto/
│ │ │ │ ├── PolygonPoint.java # Polygon vertex DTO
│ │ │ │ ├── Waypoint.java # Waypoint DTO
│ │ │ │ ├── WaypointOptimizeRequest.java # Optimize request DTO
│ │ │ │ └── WaypointOptimizeResponse.java # Optimize response DTO
│ │ │ ├── utils/
│ │ │ │ ├── CoordinatesUtils.java # Coordinate transformation tool
│ │ │ │ └── GeometryUtils.java # Geometry tool
│ │ │ └── JavaRouteApplication.java # Spring Boot main class
│ │ └── resources/
│ │ └── application.properties # Application configuration file
│ └── test/
│ └── java/ # Test code
├── pom.xml # Maven configuration file
└── README.md # This document
```

### Main Dependencies
- **Spring Boot 3.5.7** - Web framework
- **Lombok** - Reduce boilerplate code
**OR-Tools 9.8.3296** - TSP solver
**JTS Topology Suite 1.19.0** - Geometric computation (an alternative to Shapely for Python)
**Apache Commons Math 3.6.1** - Numerical computation
### JSON Naming Strategy
The project is configured to use the `snake_case` naming strategy (in `JacksonConfig`), consistent with the Python version. This means:
- Field names in requests/responses use underscores: `polygon_coords`, `flight_height_m`
- Field names in Java classes use camelCase: `polygonCoords`, `flightHeightM`
- Jackson will automatically perform the conversion.
