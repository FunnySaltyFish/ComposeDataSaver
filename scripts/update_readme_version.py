import re
import sys
from pathlib import Path

def extract_versions():
    """从 libs.versions.toml 文件中提取 Kotlin 版本号和项目版本号"""
    try:
        # 1. 读取 libs.versions.toml
        versions_toml_path = Path("gradle/libs.versions.toml")
        if not versions_toml_path.exists():
            raise FileNotFoundError(f"版本文件不存在: {versions_toml_path}")

        versions_toml = versions_toml_path.read_text(encoding="utf-8")

        # 提取 Kotlin 版本号
        kotlin_version_match = re.search(r'kotlin\s*=\s*"([\d\.]+)"', versions_toml)
        kotlin_version = kotlin_version_match.group(1) if kotlin_version_match else None

        if not kotlin_version:
            raise ValueError("无法从 libs.versions.toml 中提取 Kotlin 版本号")

        # 提取项目版本号
        project_version_match = re.search(r'project\s*=\s*"([\d\.]+)"', versions_toml)
        project_version = project_version_match.group(1) if project_version_match else None

        if not project_version:
            raise ValueError("无法从 libs.versions.toml 中提取项目版本号")

        return kotlin_version, project_version

    except Exception as e:
        print(f"错误: 提取版本号失败 - {e}", file=sys.stderr)
        sys.exit(1)

def update_readme_version(readme_path_str: str, kotlin_version: str, project_version: str):
    """更新 README 文件中的版本号"""
    try:
        readme_path = Path(readme_path_str)
        if not readme_path.exists():
            print(f"警告: README 文件不存在: {readme_path_str}")
            return False

        original_content = readme_path.read_text(encoding="utf-8")
        updated_content = original_content

        # 替换 Kotlin 版本徽章
        kotlin_pattern = r'(Kotlin-)[\d\.]+(-[A-F0-9]+?\?logo=kotlin)'
        kotlin_replacement = rf'\g<1>{kotlin_version}\g<2>'
        updated_content = re.sub(kotlin_pattern, kotlin_replacement, updated_content)

        # 替换 implementation 代码块里的版本号（支持多个data-saver库）
        # 替换 data-saver-core 版本号
        core_pattern = r'(implementation\s+"io\.github\.FunnySaltyFish:data-saver-core:).+?(")'
        core_replacement = rf'\g<1>{project_version}\g<2>'
        updated_content = re.sub(core_pattern, core_replacement, updated_content)
        
        # 替换其他data-saver库的版本号
        other_pattern = r'(implementation\s+"io\.github\.FunnySaltyFish:data-saver-[^:]+:).+?(")'
        other_replacement = rf'\g<1>{project_version}\g<2>'
        updated_content = re.sub(other_pattern, other_replacement, updated_content)

        # 检查是否有变化
        if original_content == updated_content:
            print(f"ℹ️  {readme_path_str} 无需更新")
            return False
        else:
            readme_path.write_text(updated_content, encoding="utf-8")
            print(f"✅ {readme_path_str} 更新完成")
            return True

    except Exception as e:
        print(f"错误: 更新 {readme_path_str} 失败 - {e}", file=sys.stderr)
        return False

def main():
    """主函数"""
    print("🚀 开始更新 README 版本号...")

    # 提取版本号
    kotlin_version, project_version = extract_versions()
    print(f"📝 Kotlin 版本号: {kotlin_version}")
    print(f"📝 项目版本号: {project_version}")

    # 更新 README 文件
    updated_files = []
    for readme_path in ("README.md", "README_en.md"):
        if update_readme_version(readme_path, kotlin_version, project_version):
            updated_files.append(readme_path)

    if updated_files:
        print(f"✨ 成功更新了 {len(updated_files)} 个文件: {', '.join(updated_files)}")
    else:
        print("ℹ️  所有文件都已是最新版本")

if __name__ == "__main__":
    main()
