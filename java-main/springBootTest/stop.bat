@echo off
rem ============================================================
rem  一键停止 后端 springBootTest(8081) 和 前端 login-vue(5173)
rem  用法: 双击运行, 或在 cmd 中执行 stop.bat
rem  说明: 同一 PID 占用多个端口时只结束一次 (KILLED_ 前缀去重)
rem ============================================================
chcp 936 >nul
set "FOUND="
for %%p in (8081 5173) do (
    for /f "tokens=5" %%a in ('netstat -ano ^| findstr /c:":%%p " ^| findstr /c:"LISTENING"') do (
        call :kill %%p %%a
    )
)
if not defined FOUND (
    echo [信息] 未发现占用 8081 / 5173 端口的进程, 前后端当前都没有在运行
) else (
    echo [信息] 已全部停止
)
ping -n 2 127.0.0.1 >nul
exit /b 0

:kill
rem %1=端口  %2=PID
if defined KILLED_%2 (
    echo [信息] PID %2 已被结束, 跳过端口 %1 的重复处理
    exit /b 0
)
set "FOUND=1"
set "KILLED_%2=1"
echo [信息] 正在结束占用 %1 端口的进程 PID %2
taskkill /f /pid %2 >nul 2>&1
exit /b 0
