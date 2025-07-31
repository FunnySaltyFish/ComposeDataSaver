# 版本号自动更新脚本

## 概述

这个脚本用于自动从构建文件中提取版本号，并同步更新 README 文件中的相关内容。

## 文件说明

- `update_readme_version.py` - 主要的版本号更新脚本
- `README.md` - 本说明文档

## 脚本功能

### 自动提取版本号

脚本会从以下文件中提取版本号：

1. **Kotlin 版本号**: 从 `gradle/libs.versions.toml` 中提取
   ```toml
   kotlin = "2.2.0"
   ```

2. **项目版本号**: 从 `gradle/libs.versions.toml` 中提取
   ```toml
   project = "1.2.2"
   ```

### 自动更新 README

脚本会更新以下文件中的版本号：

- `README.md` (中文版)
- `README_en.md` (英文版)

更新内容包括：
- Kotlin 版本徽章: `[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.2.0-B125EA?logo=kotlin)]`
- 依赖引入代码: `implementation("io.github.FunnySaltyFish:data-saver-core:1.2.2")`

## 使用方法

### 手动运行

在项目根目录下运行：

```bash
python scripts/update_readme_version.py
```

### 自动化集成

脚本已集成到 GitHub Actions 工作流中：

1. **发布时自动更新** (`.github/workflows/publish.yml`)
   - 在创建 release 时自动运行
   - 更新 README 文件并提交更改
   - 然后发布到 Maven Central

2. **版本一致性检查** (`.github/workflows/version-check.yml`)
   - 在推送到主分支时检查版本号一致性
   - 如果发现不一致会提示需要更新

## 输出示例

```
🚀 开始更新 README 版本号...
📝 Kotlin 版本号: 2.2.0
📝 项目版本号: 1.2.2
✅ README.md 更新完成
✅ README_en.md 更新完成
✨ 成功更新了 2 个文件: README.md, README_en.md
```

## 错误处理

脚本包含完善的错误处理机制：

- 文件不存在检查
- 版本号提取失败检查
- 文件读写权限检查
- 详细的错误信息输出

## 注意事项

1. 确保项目结构符合预期（gradle/libs.versions.toml 文件位置正确）
2. 脚本需要在项目根目录下运行
3. 需要 Python 3.x 环境
4. 确保有文件读写权限 