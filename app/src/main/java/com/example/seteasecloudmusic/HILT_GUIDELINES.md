# Hilt 注入 vs 手动 new：后续开发判断指南

## 核心原则

**Hilt 不是让项目里「完全不用 new」，而是把「有配置成本、生命周期需要统一管理、依赖链较深」的对象交给框架托管。**

依赖注入解决的是对象**怎么来**的问题：
- 构造参数很多、层层依赖时，手动 new 很累。
- 生命周期（单例、Activity 级、ViewModel 级）需要与系统同步时，自己管理容易漏或重复创建。
- 创建过程涉及网络客户端配置、线程池、数据库初始化时，统一在 Module 里组装更清晰。

## 什么时候交给 Hilt？

| 类型 | 本项目例子 | 决策 | 原因 |
|------|-----------|------|------|
| **ViewModel** | `SearchViewModel` | ✅ 注入 | 与界面生命周期绑定，依赖 UseCase 和 Repository，Hilt 自动生成工厂 |
| **Repository / DataSource** | `SearchRepositoryImpl` | ✅ 注入 | 依赖 Retrofit 服务，适合用 `@Binds` 绑定接口与实现 |
| **UseCase** | `SearchMusicUseCase`、`GetSearchSuggestionsUseCase`、`PrepareTrackForPlaybackUseCase` | ✅ 注入 | 单一职责、数量多，注入后上层（ViewModel）无需知道构造细节 |
| **网络/第三方客户端** | `Retrofit`、`OkHttpClient`、`NeteaseMusicService` | ✅ 用 Module @Provides | 创建有配置成本，需要统一设置超时、拦截器、转换器 |
| **全局控制器** | `MusicPlayerController` | ✅ 注入（`@Singleton`） | 需要跨页面共享状态，生命周期跟随应用 |
| **需要 Context 的依赖** | `@ApplicationContext Context` | ✅ 注入 | Hilt 自动提供 Application/Activity Context，避免内存泄漏 |

## 什么时候自己 new 就行？

| 类型 | 本项目例子 | 决策 | 原因 |
|------|-----------|------|------|
| **数据类 / UI 状态** | `SearchUiState`、`Track`、`PlaybackState`、`BottomNavItem` | ❌ 手动 new | 纯数据载体，无生命周期，new 一下成本极低 |
| **Compose 内部临时状态** | `mutableStateOf`、`Animatable` | ❌ 用 `remember` | Compose 自己的状态系统比 Hilt 更合适 |
| **简单的本地工具/常量** | `AuthInputValidator`（如果无外部依赖） | ❌ `object` 或直接 new | 无状态、无生命周期要求，注入反而增加样板代码 |
| **一次性临时对象** | `Interceptor { chain -> ... }`（仅在某 Module 内部使用） | ❌ 局部 new | 只在组装 OkHttpClient 时使用一次，不需要暴露到全局依赖图 |

## 快速判断流程

```
这个对象需要以下特性吗？
├── 生命周期很长（单例 / Activity 级 / ViewModel 级）
│   └── 是  → 交给 Hilt
├── 构造参数复杂（依赖其他 Repository/UseCase/Service）
│   └── 是  → 交给 Hilt
├── 创建有配置成本（Retrofit、数据库、线程池）
│   └── 是  → 交给 Hilt（Module @Provides）
├── 是数据类 / UI 状态 / 临时变量
│   └── 是  → 手动 new / remember
└── 是无状态的纯粹工具方法集合
    └── 是  → object 或直接 new
```

## 常见误区

1. **「用了 Hilt 就不能写 new」**
   - 错误。数据类、局部 DTO、不可复用的中间对象，仍然可以并且应该直接 new。

2. **「所有工具类都加 @Inject constructor」**
   - 没必要。如果一个类没有外部依赖，也不参与生命周期管理，写成一个 `object` 或普通类即可。

3. **「CoroutineDispatcher 也注入」**
   - 可以注入（通过 `@Qualifier` 区分 Main/IO），但如果只是为了测试可替换性，不注入、直接写 `Dispatchers.IO` 也是常见做法。本项目当前已将 `MusicPlayerController` 的调度器作为内部字段处理。

4. **「Module 里手动调用其他 @Provides 方法」**
   - 不建议。比如 `provideNeteaseMusicService()` 应该直接接收 `Retrofit` 参数，让 Dagger 自动注入，而不是在方法体里手动 `provideHttpClient()` 和 `provideRetrofit()`。

## 一句话总结

> **构造复杂、周期长、要共享 → Hilt 注入。**  
> **数据临时、状态局部、工具简单 → 直接 new / remember / object。**
