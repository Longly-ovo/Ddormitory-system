# Dormitory System Code Map

This document is for defense and maintenance navigation only. It does not affect runtime behavior.

## Basic Architecture

- Frontend: Vue 3 + Vite + Pinia + Vue Router + Element Plus.
- Backend: Spring Boot 3 + Spring Security + JWT + MyBatis Plus.
- Database: MySQL schema is defined in `backend/src/main/resources/schema.sql`.
- API wrapper: frontend requests go through `frontend/src/api.ts` with `/api` as the base path.

## Layering Rule

- Controller: receive HTTP requests and return `ApiResponse`.
- Service: hold business logic and validation.
- Mapper: access database tables through MyBatis Plus.
- Model: current `entity` and `dto` packages.

## Defense Navigation Map

### 1. Login And Permission

- Login logic -> `AuthService.login()`
- Current user -> `AuthService.getCurrentUser()`
- Student password change -> `AuthService.changeStudentPassword()`
- Backend permission control -> `SecurityConfig` + `JwtAuthenticationFilter`
- Frontend route permission -> `frontend/src/router.ts`

### 2. Student

- Student list/search -> `StudentService.listStudents()`
- Student detail -> `StudentService.getStudentDetail()`
- Create student + account -> `StudentService.createStudentAccount()`
- Update student profile -> `StudentService.updateStudentProfile()`
- Delete student + account -> `StudentService.deleteStudentAndAccount()`

### 3. Bed

- Create bed -> `BedService.createBed()`
- Update bed -> `BedService.updateBed()`
- Delete bed -> `BedService.deleteBed()`
- Assign bed -> `BedService.assignStudentToBed()`
- Checkout/release bed -> `BedService.releaseBed()`

### 4. Dormitory Structure

- Building management -> `DormitoryStructureService` building methods
- Floor management -> `DormitoryStructureService` floor methods
- Room management -> `DormitoryStructureService` room methods
- Bed basic management -> `BedService` create/update/delete methods

### 5. Map And Dormitory View

- Floor map -> `DormitoryMapService.getFloorMap()`
- Room beds -> `DormitoryMapService.getRoomBeds()`
- Current student dormitory -> `DormitoryMapService.getCurrentStudentDormitory()`
- Current student floor map -> `DormitoryMapService.getCurrentStudentFloorMap()`
- Selected student dormitory -> `DormitoryMapService.getStudentDormitory()`

## Frontend Page To API

- `HomeView.vue`: floor map, room beds, assign bed, checkout.
- `StudentView.vue`: student list, create/update/delete student, student dormitory detail.
- `StructureView.vue`: building, floor, room, and bed structure management.
- `MyDormitoryView.vue`: current student's dormitory and floor map.
- `LoginView.vue`: login.
- `ChangePasswordView.vue`: student password change.

## Common Questions

- Where is bed assignment? `BedService.assignStudentToBed()`
- Where is checkout? `BedService.releaseBed()`
- Where is student account creation? `StudentService.createStudentAccount()`
- Where does floor map data come from? `DormitoryMapService.getFloorMap()`
- Where is backend permission configured? `SecurityConfig`
- Where is JWT parsed? `JwtAuthenticationFilter`
- Where is frontend role routing controlled? `frontend/src/router.ts`
