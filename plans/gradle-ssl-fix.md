# Gradle 同步 SSL 证书错误解决方案

## 问题描述

在 Windows 上执行 Gradle 同步时出现错误：
```
Cause: unable to find valid certification path to requested target
```

而在 Mac 上开发时没有出现这个问题。

## 问题原因分析

这个错误是典型的 **SSL 证书信任问题**，可能的原因包括：

### 1. Java 证书库差异
- Mac 和 Windows 上的 Android Studio 使用不同的 JDK
- Windows JDK 的证书库（cacerts）可能缺少腾讯云镜像的 SSL 根证书
- 不同版本的 JDK 对证书信任链的处理方式不同

### 2. 网络环境差异
- Windows 可能在公司网络/代理环境下
- 代理服务器可能进行 SSL 中间人检查，导致证书链被替换
- 防火墙或安全软件可能拦截 HTTPS 请求

### 3. 系统时间问题
- Windows 系统时间不正确会导致证书有效期验证失败

### 4. 镜像源问题
- 腾讯云镜像的 SSL 证书可能存在问题
- 镜像源暂时不可用或证书过期

---

## 解决方案

### 方案一：更换为阿里云镜像（推荐）

阿里云镜像在中国大陆更稳定，SSL 证书问题较少。

修改 [`settings.gradle.kts`](settings.gradle.kts)：

```kotlin
pluginManagement {
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin") }
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url = uri("https://maven.aliyun.com/repository/google") }
        maven { url = uri("https://maven.aliyun.com/repository/public") }
        google()
        mavenCentral()
    }
}

rootProject.name = "SeteaseCloudMusic"
include(":app")
```

### 方案二：导入证书到 Java 证书库

如果必须使用腾讯云镜像，可以手动导入证书：

1. **下载证书**：
   - 在浏览器中访问 `https://mirrors.cloud.tencent.com`
   - 点击地址栏锁图标 → 查看证书 → 导出证书（Base64 编码）

2. **导入到 Java 证书库**：
   ```cmd
   # 找到 Android Studio 使用的 JDK 路径
   # 通常在: C:\Program Files\Android\Android Studio\jbr
   
   # 导入证书（以管理员身份运行）
   "C:\Program Files\Android\Android Studio\jbr\bin\keytool" -import -alias tencent-mirror -keystore "C:\Program Files\Android\Android Studio\jbr\lib\security\cacerts" -file tencent-mirror.cer
   
   # 默认密码: changeit
   ```

3. **重启 Android Studio**

### 方案三：检查并修复系统时间

```cmd
# 检查系统时间
date /t
time /t

# 如果时间不正确，同步网络时间
w32tm /resync
```

### 方案四：禁用 SSL 验证（仅用于调试，不推荐生产）

在 [`gradle.properties`](gradle.properties) 中添加：
```properties
systemProp.https.protocols=TLSv1.2,TLSv1.3
org.gradle.jvmargs=-Djavax.net.ssl.trustAll=true
```

### 方案五：检查代理设置

如果使用代理：

1. **检查 Gradle 代理配置**：
   编辑 `~/.gradle/gradle.properties`（全局）或项目 `gradle.properties`：
   ```properties
   systemProp.http.proxyHost=your-proxy-host
   systemProp.http.proxyPort=your-proxy-port
   systemProp.https.proxyHost=your-proxy-host
   systemProp.https.proxyPort=your-proxy-port
   ```

2. **或者临时禁用代理**：
   ```properties
   systemProp.http.proxyHost=
   systemProp.http.proxyPort=
   systemProp.https.proxyHost=
   systemProp.https.proxyPort=
   ```

### 方案六：清理 Gradle 缓存

```cmd
# 删除 Gradle 缓存
rmdir /s /q "%USERPROFILE%\.gradle\caches"
rmdir /s /q "%USERPROFILE%\.gradle\wrapper"

# 在 Android Studio 中：File → Invalidate Caches → Invalidate and Restart
```

---

## 推荐操作顺序

1. ✅ **首先尝试方案一**：更换为阿里云镜像（最简单有效）
2. 如果方案一无效，**检查系统时间**（方案三）
3. 如果仍有问题，**清理 Gradle 缓存**（方案六）
4. 最后考虑**导入证书**（方案二）

---

## 注意事项

- AGP 9.0.0 是非常新的版本，确保你的 Android Studio 版本兼容
- 如果问题持续，可以尝试降级 AGP 版本到稳定版（如 8.5.0）
- 在中国大陆开发 Android 项目，使用镜像源是常见做法，但需要选择稳定的镜像