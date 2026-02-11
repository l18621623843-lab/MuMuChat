// 顶层构建文件，配置所有子项目/模块的通用选项
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
