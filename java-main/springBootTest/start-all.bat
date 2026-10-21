@echo off
rem ============================================================
rem  一键启动 前端(login-vue) + 后端(springBootTest)
rem  用法: 双击运行, 或在 cmd 中执行 start-all.bat
rem  目录约定: 本脚本位于 java-main\springBootTest\ 下,
rem            前端 login-vue 与本目录同级 (java-main\login-vue)
rem  前端: Vite, 端口 5173    后端: Spring Boot, 端口 8081
rem  不依赖 target / node_modules: 缺失时会自动编译 / 自动 npm install
rem ============================================================
chcp 936 >nul
cd /d "%~dp0"

rem --- 清除外部工具注入的端口环境变量, 避免后端端口被篡改 ---
set "SERVER__PORT="
set "SERVER__HOST="
set "SERVER_PORT="
set "SERVER_HOST="

rem --- 检查 Java ---
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 找不到 java, 请确认 JDK 已安装并配置 PATH
    pause
    exit /b 1
)

rem --- 检查 Node ---
rem 注意: 必须用 where 检查; 直接跑 npm(实为 npm.cmd)不加 call 会移交控制权导致脚本静默中止
where npm >nul 2>&1
if errorlevel 1 (
    echo [错误] 找不到 npm, 请确认 Node.js 已安装并配置 PATH
    pause
    exit /b 1
)

rem --- 定位前端目录 (与 springBootTest 同级, 即 ..\login-vue) ---
set "FRONTEND=%~dp0..\login-vue"
if not exist "%FRONTEND%\package.json" (
    echo [错误] 找不到前端项目: %FRONTEND%
    echo [提示] 请确认 login-vue 与 springBootTest 同处于 java-main 目录下
    pause
    exit /b 1
)

rem --- 前端依赖缺失则自动安装 ---
if not exist "%FRONTEND%\node_modules" (
    echo [信息] 检测到 node_modules 缺失, 正在安装前端依赖, 请稍候...
    pushd "%FRONTEND%"
    call npm install --registry=https://registry.npmmirror.com
    if errorlevel 1 (
        echo [错误] 前端依赖安装失败
        popd
        pause
        exit /b 1
    )
    popd
)

echo [信息] 正在新窗口启动前端 login-vue (端口 5173)...
start "login-vue 前端" cmd /k "cd /d %FRONTEND% && npm run dev"

echo [信息] 正在新窗口启动后端 springBootTest (端口 8081, profile=dev)...
start "springBootTest 后端" cmd /k "cd /d %~dp0 && call mvnw.cmd spring-boot:run"

echo.
echo [信息] 前后端均已在独立窗口启动:
echo         前端: http://localhost:5173/
echo         后端: http://localhost:8081/
echo [信息] 关闭对应窗口即可停止; 也可运行 stop.bat 一键停止。
echo [信息] 本窗口 3 秒后自动关闭...
ping -n 4 127.0.0.1 >nul
