@echo off
rem ============================================================
rem  springBootTest 一键启动脚本
rem  用法: 双击运行, 或在 cmd 中执行 start.bat
rem  注意: 本脚本不依赖 target / node_modules (每次开机它们会被删除)
rem ============================================================
chcp 936 >nul
cd /d "%~dp0"

rem --- 清除外部工具(如WorkBuddy)注入的端口环境变量, 避免端口冲突 ---
set "SERVER__PORT="
set "SERVER__HOST="
set "SERVER_PORT="
set "SERVER_HOST="

rem --- 检查 Java 环境 ---
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 找不到 java 命令, 请先安装 JDK17+ 并配置 PATH
    echo        本机 Java 位置: C:\Program Files\Zulu\zulu-21\bin
    pause
    exit /b 1
)

rem --- 首次运行会自动下载 Maven 和依赖(约1分钟), 之后走本地缓存 ---
echo [信息] 正在启动 springBootTest ... (端口 8081, profile=dev)
echo [信息] 首次启动需编译, 请耐心等待出现 Spring Boot 横幅
call mvnw.cmd spring-boot:run

echo.
echo [信息] 项目已退出
pause
