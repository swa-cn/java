@echo off
rem ============================================================
rem  一键停止 后端 springBootTest(8081) 和 前端 login-vue(5173)
rem  用法: 双击运行, 或在 cmd 中执行 stop.bat
rem ============================================================
chcp 936 >nul
set "FOUND="
for %%p in (8081 5173) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr /c:":%%p " ^| findstr /c:"LISTENING"') do (
        set "FOUND=1"
        echo [信息] 正在结束占用 %%p 端口的进程 PID %%a
        taskkill /f /pid %%a >nul 2>&1
    )
)
if not defined FOUND (
    echo [信息] 未发现占用 8081 / 5173 端口的进程, 前后端当前都没有在运行
) else (
    echo [信息] 已全部停止
)
ping -n 2 127.0.0.1 >nul
