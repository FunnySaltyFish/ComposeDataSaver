#!/bin/bash

# DataSaver Core 测试运行脚本 
# DataSaver Core Test Runner Script

set -e  # 遇到错误时退出

echo "🚀 DataSaver Core 测试运行器"
echo "=============================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 函数：打印带颜色的消息
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 函数：运行测试并处理结果
run_test() {
    local test_name="$1"
    local test_command="$2"
    
    print_status "开始运行 $test_name 测试..."
    
    if eval "$test_command"; then
        print_success "$test_name 测试通过 ✅"
        return 0
    else
        print_error "$test_name 测试失败 ❌"
        return 1
    fi
}

# 检查是否在正确的目录
if [ ! -f "build.gradle.kts" ]; then
    print_error "请在 data-saver-core 目录下运行此脚本"
    exit 1
fi

# 默认运行所有测试
RUN_ALL=true
RUN_COMMON=false
RUN_ANDROID=false
RUN_IOS=false
RUN_DESKTOP=false
RUN_WASM=false

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        --common)
            RUN_ALL=false
            RUN_COMMON=true
            shift
            ;;
        --android)
            RUN_ALL=false
            RUN_ANDROID=true
            shift
            ;;
        --ios)
            RUN_ALL=false
            RUN_IOS=true
            shift
            ;;
        --desktop)
            RUN_ALL=false
            RUN_DESKTOP=true
            shift
            ;;
        --wasm)
            RUN_ALL=false
            RUN_WASM=true
            shift
            ;;
        --all)
            RUN_ALL=true
            shift
            ;;
        --help|-h)
            echo "用法: $0 [选项]"
            echo "选项:"
            echo "  --all      运行所有平台测试 (默认)"
            echo "  --common   仅运行公共测试"
            echo "  --android  仅运行 Android 测试"
            echo "  --ios      仅运行 iOS 测试"
            echo "  --desktop  仅运行桌面测试"
            echo "  --wasm     仅运行 WASM 测试"
            echo "  --help|-h  显示此帮助信息"
            exit 0
            ;;
        *)
            print_warning "未知选项: $1"
            shift
            ;;
    esac
done

# 测试结果统计
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0

# 运行公共测试
if [ "$RUN_ALL" = true ] || [ "$RUN_COMMON" = true ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if run_test "公共测试" "../gradlew :data-saver-core:commonTest"; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
fi

# 运行 Android 测试
if [ "$RUN_ALL" = true ] || [ "$RUN_ANDROID" = true ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if run_test "Android 测试" "../gradlew :data-saver-core:testDebugUnitTest"; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
fi

# 运行 iOS 测试
if [ "$RUN_ALL" = true ] || [ "$RUN_IOS" = true ]; then
    print_status "检查 iOS 测试环境..."
    
    # 检查 Xcode 是否安装
    if command -v xcodebuild &> /dev/null; then
        # 尝试运行 iOS 模拟器测试
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if run_test "iOS 模拟器测试" "../gradlew :data-saver-core:iosSimulatorArm64Test"; then
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        print_warning "Xcode 未找到，跳过 iOS 测试"
    fi
fi

# 运行桌面测试
if [ "$RUN_ALL" = true ] || [ "$RUN_DESKTOP" = true ]; then
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
    if run_test "桌面测试" "../gradlew :data-saver-core:desktopTest"; then
        PASSED_TESTS=$((PASSED_TESTS + 1))
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
    fi
fi

# 运行 WASM 测试
if [ "$RUN_ALL" = true ] || [ "$RUN_WASM" = true ]; then
    print_status "检查 WASM 测试环境..."
    
    # 检查 Node.js 是否安装
    if command -v node &> /dev/null; then
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
        if run_test "WASM 测试" "../gradlew :data-saver-core:wasmJsTest"; then
            PASSED_TESTS=$((PASSED_TESTS + 1))
        else
            FAILED_TESTS=$((FAILED_TESTS + 1))
        fi
    else
        print_warning "Node.js 未找到，跳过 WASM 测试"
    fi
fi

# 打印测试结果汇总
echo ""
echo "=============================="
echo "📊 测试结果汇总"
echo "=============================="
echo "总测试数: $TOTAL_TESTS"
echo -e "通过: ${GREEN}$PASSED_TESTS${NC}"
echo -e "失败: ${RED}$FAILED_TESTS${NC}"

if [ $FAILED_TESTS -eq 0 ]; then
    print_success "所有测试都通过了！🎉"
    exit 0
else
    print_error "有 $FAILED_TESTS 个测试失败"
    echo ""
    echo "💡 提示:"
    echo "- 查看详细的测试报告：data-saver-core/build/reports/tests/"
    echo "- 运行特定测试获取更多信息"
    echo "- 参考 README_TESTING.md 获取故障排除指南"
    exit 1
fi 