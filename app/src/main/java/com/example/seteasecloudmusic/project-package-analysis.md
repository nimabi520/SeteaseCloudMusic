# SeteaseCloudMusic 项目包结构职责分析

## 项目概述

本项目是一款基于 **Jetpack Compose + Media3 + Retrofit** 构建的 Android 音乐播放器应用，采用**分层架构 + 按功能分包（feature-based）**的组织方式。

---

## 顶层包结构

```
com.example.seteasecloudmusic
├── core/          # 核心基础设施：被所有功能模块共享
├── feature/       # 按功能划分的业务模块
```

---

## 一、core — 核心层

`core` 包存放与具体业务功能无关、可被全应用复用的基础设施代码。

### 1. `core.common`
存放通用工具类、扩展函数等纯辅助代码（当前项目中未见具体文件，为预留扩展包）。

### 2. `core.design`
存放全局设计系统相关代码，如主题、颜色、字体、通用 UI 组件等（当前为预留扩展包）。

### 3. `core.model`
**职责：定义全应用共享的核心领域模型。**

| 文件 | 说明 |
|------|------|
| `Track.kt` | 定义歌曲（`Track`）、专辑（`Album`）、艺人（`Artist`）以及音质枚举（`AudioQuality`）。这些是跨功能模块通用的业务实体。 |

### 4. `core.network`
**职责：负责网络基础设施的搭建与配置。**

| 文件 | 说明 |
|------|------|
| `NetworkModule.kt` | 简单的手动依赖注入模块，负责组装 `OkHttpClient`（配置超时、拦截器）和 `Retrofit`（基础 URL、Gson 转换器），并对外暴露 `NeteaseMusicService` 实例。 |

#### `core.network.interceptor`
**职责：存放网络请求拦截器。**

| 文件 | 说明 |
|------|------|
| `AuthInterceptor.kt` | 负责在请求中附加认证相关的 Header 或参数（如 Cookie、Token）。 |

### 5. `core.player`
**职责：基于 Media3 实现全局音乐播放能力，包含后台服务与播放控制器。**

| 文件 | 说明 |
|------|------|
| `MusicService.kt` | 继承 `MediaSessionService` 的后台服务，持有 `ExoPlayer` 实例和 `MediaSession`，负责向系统暴露播放控制能力（通知栏、锁屏、耳机按键）。 |
| `MusicPlayerController.kt` | 面向 UI 层的播放控制器，封装 `MediaController` 的连接逻辑，提供 `play()`、`pause()`、`seekTo()` 等高级 API，并通过 `StateFlow<PlaybackState>` 向界面同步播放状态。 |

---

## 二、feature — 功能层

每个 `feature` 子包对应一个独立的业务功能，内部按 **data / domain / presentation** 三层进一步划分。

---

### 1. `feature.main`
**职责：应用的入口与全局导航容器。**

| 文件 | 说明 |
|------|------|
| `MainActivity.kt` | Android 入口 Activity，初始化 Compose 宿主并开启 Edge-to-Edge 沉浸式布局，挂载 `AppNavigation()`。 |
| `AppNavigation.kt` | 应用的主导航组件，构建底部毛玻璃导航栏（Liquid Glass 风格）、页面切换逻辑，以及搜索入口与迷你播放条的 UI 组合。 |

---

### 2. `feature.search`
**职责：实现音乐搜索功能，包含搜索建议、搜索结果、歌曲播放准备。**

#### `feature.search.data`
**数据层：负责与网易云音乐 API 通信及数据转换。**

| 文件 | 说明 |
|------|------|
| `NeteaseMusicService.kt` | Retrofit 接口，定义搜索、获取歌曲 URL、获取搜索建议的 HTTP 端点。 |
| `SearchRepositoryImpl.kt` | `SearchRepository` 的实现类，调用 `NeteaseMusicService`，并将接口响应模型转换为 `core.model.Track` 等领域模型。 |
| `SearchResultResponse.kt` | 搜索结果的网络响应数据类。 |
| `SearchSuggestResponse.kt` | 搜索建议的网络响应数据类。 |
| `SongResponse.kt` | 歌曲 URL 查询的网络响应数据类。 |

#### `feature.search.domain`
**领域层：定义搜索业务的抽象接口与用例。**

| 文件 | 说明 |
|------|------|
| `SearchRepository.kt` | 搜索仓库的抽象接口，声明 `searchTracks()`、`getTrackUrl()`、`getSearchSuggestions()`。 |
| `SearchSuggestions.kt` | 定义搜索建议的领域模型（包含歌曲、艺人、歌单、全匹配词）。 |
| `SearchMusicUseCase.kt` | 执行正式搜索的用例。 |
| `GetSearchSuggestionsUseCase.kt` | 获取搜索建议的用例。 |
| `GetTrackUrlUseCase.kt` | 获取指定歌曲可播放 URL 的用例。 |
| `PrepareTrackForPlaybackUseCase.kt` | 播放前准备工作用例（如拼接 URL、校验可播放性）。 |

#### `feature.search.presentation`
**表现层：负责搜索相关的 UI 与状态管理。**

| 文件 | 说明 |
|------|------|
| `SearchScreen.kt` | 搜索页面的 Compose UI，包含分类 Tab、搜索结果列表（歌曲/专辑/艺人/歌单）、加载/空态/错误态的展示。 |
| `SearchViewModel.kt` | 搜索页面的状态持有者，管理 `SearchUiState`，处理用户输入、搜索提交、建议请求、重试与清空等交互逻辑，内置防抖与请求取消机制。 |

---

### 3. `feature.auth`
**职责：实现用户认证功能，支持手机号、邮箱、验证码、二维码、游客等多种登录方式。**

#### `feature.auth.data`
**数据层：负责认证相关的网络请求与响应模型。**

| 文件 | 说明 |
|------|------|
| `AuthService.kt` | Retrofit 接口，定义登录、二维码、验证码、刷新会话等认证的 HTTP 端点。 |
| `data.model/` | 认证接口的数据响应类，如 `LoginResponse`、`QrCodeResponse`、`QrKeyResponse`、`QrStatusResponse`。 |

#### `feature.auth.domain`
**领域层：定义认证业务的抽象与模型。**

| 文件 | 说明 |
|------|------|
| `domain.model/` | 认证领域模型，包括 `AuthSession`（会话信息）、`LoginMethod`（登录方式枚举）、`QrLoginStart`（二维码登录启动信息）、`QrPollResult`（轮询结果）、`QrStatus`（二维码状态）。 |
| `domain.repository/AuthRepository.kt` | 认证仓库的抽象接口，声明各种登录方式、二维码轮询、状态观察等方法。 |

#### `feature.auth.usecase`
**用例层：将认证能力封装为独立的可复用用例。**

| 文件 | 说明 |
|------|------|
| `PhoneLoginUseCase.kt` | 手机号密码登录。 |
| `EmailLoginUseCase.kt` | 邮箱登录。 |
| `SendCaptchaUseCase.kt` / `VerifyCaptchaUseCase.kt` | 发送与校验验证码。 |
| `StartQrLoginUseCase.kt` / `PollQrStatusUseCase.kt` | 启动二维码登录与轮询状态。 |
| `GuestLoginUseCase.kt` | 游客登录。 |
| `RefreshSessionUseCase.kt` | 刷新会话。 |
| `ObserveAuthStateUseCase.kt` | 观察登录状态变化。 |
| `AuthInputValidator.kt` | 登录输入的校验逻辑。 |

---

### 4. `feature.home`
**职责：主页模块（当前为预留包，待实现主页内容）。**

### 5. `feature.library`
**职责：音乐库/我的音乐模块（当前为预留包，待实现歌单、收藏等内容）。**

### 6. `feature.player`
**职责：全屏播放器页面模块（当前为预留包，待实现全屏播放控制 UI）。**

### 7. `feature.radio`
**职责：电台模块（当前为预留包，待实现电台相关功能）。**

---

## 三、架构设计特点

1. **按功能分包（Feature-based）**
   - 每个业务功能（search、auth、home 等）拥有独立的 `data/domain/presentation` 三层，便于后续模块化（如 Dynamic Feature Modules）。

2. **分层清晰**
   - `data` 层只关心网络/数据库细节；
   - `domain` 层只定义业务规则与抽象；
   - `presentation` 层只处理 UI 状态与用户交互。

3. **依赖方向向内**
   - `presentation` → `domain` → `data`，`core` 被所有层依赖，符合 Clean Architecture 的依赖规则。

4. **core 层共享基础设施**
   - 网络配置、Media3 播放服务、核心数据模型全部下沉到 `core`，避免各功能模块重复建设。
