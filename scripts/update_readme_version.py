import re
import sys
from pathlib import Path


def extract_version_config():
    """从 libs.versions.toml 文件中提取 README 同步所需的配置"""
    try:
        versions_toml_path = Path("gradle/libs.versions.toml")
        if not versions_toml_path.exists():
            raise FileNotFoundError(f"版本文件不存在: {versions_toml_path}")

        versions_toml = versions_toml_path.read_text(encoding="utf-8")

        def extract_string(key: str) -> str:
            match = re.search(
                rf'(?m)^\s*{re.escape(key)}\s*=\s*"([^"\r\n]+)"\s*$',
                versions_toml,
            )
            if not match:
                raise ValueError(f"无法从 libs.versions.toml 中提取 {key} 配置")
            return match.group(1)

        return {
            "kotlin_version": extract_string("kotlin"),
            "project_version": extract_string("project"),
            "project_group": extract_string("group"),
        }

    except Exception as e:
        print(f"错误: 提取版本配置失败 - {e}", file=sys.stderr)
        sys.exit(1)


def update_readme_version(
    readme_path_str: str,
    kotlin_version: str,
    project_version: str,
    project_group: str,
):
    """更新 README 文件中的版本号"""
    try:
        readme_path = Path(readme_path_str)
        if not readme_path.exists():
            print(f"警告: README 文件不存在: {readme_path_str}")
            return False

        original_content = readme_path.read_text(encoding="utf-8")
        updated_content = original_content

        kotlin_pattern = r'(Kotlin-)([^?\r\n]+)(-[A-Za-z0-9]+(?:\?logo=kotlin\b[^)]*))'
        kotlin_replacement = rf'\g<1>{kotlin_version}\g<2>'
        updated_content, kotlin_badge_count = re.subn(
            kotlin_pattern,
            kotlin_replacement,
            updated_content,
        )

        dependency_pattern = re.compile(
            r'((?:implementation|api)\s*\(?\s*["\'])'
            r'((?:io\.github\.funnysaltyfish)|(?:com\.github\.funnysaltyfish))'
            r':'
            r'(data-saver(?:-[^:"\'\s\)]+)?)'
            r':'
            r'([^"\')\s]+)'
            r'(["\']\s*\)?)',
            flags=re.IGNORECASE,
        )

        def replace_dependency(match: re.Match[str]) -> str:
            return (
                f"{match.group(1)}"
                f"{project_group}:{match.group(3)}:{project_version}"
                f"{match.group(5)}"
            )

        updated_content, dependency_count = dependency_pattern.subn(
            replace_dependency,
            updated_content,
        )

        if original_content == updated_content:
            if dependency_count == 0:
                print(f"⚠️  {readme_path_str} 未识别到可更新的 data-saver 依赖")
            elif kotlin_badge_count == 0:
                print(f"ℹ️  {readme_path_str} 未找到 Kotlin 版本徽章，已跳过")
            print(f"ℹ️  {readme_path_str} 无需更新")
            return False

        readme_path.write_text(updated_content, encoding="utf-8")
        print(
            f"✅ {readme_path_str} 更新完成"
            f" (依赖 {dependency_count} 处, Kotlin 徽章 {kotlin_badge_count} 处)"
        )
        return True

    except Exception as e:
        print(f"错误: 更新 {readme_path_str} 失败 - {e}", file=sys.stderr)
        return False

def main():
    """主函数"""
    print("🚀 开始更新 README 版本号...")

    version_config = extract_version_config()
    print(f"📝 Kotlin 版本号: {version_config['kotlin_version']}")
    print(f"📝 项目版本号: {version_config['project_version']}")
    print(f"📝 项目 Group Id: {version_config['project_group']}")

    updated_files = []
    for readme_path in ("README.md", "README_en.md"):
        if update_readme_version(readme_path, **version_config):
            updated_files.append(readme_path)

    if updated_files:
        print(f"✨ 成功更新了 {len(updated_files)} 个文件: {', '.join(updated_files)}")
    else:
        print("ℹ️  所有文件都已是最新版本")

if __name__ == "__main__":
    main()
