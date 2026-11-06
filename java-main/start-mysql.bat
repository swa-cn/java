@echo off
rem ============================================================
rem  启动项目所需的 MySQL（端口 3306）
rem  用法: 右键本文件 -> 以管理员身份运行
rem  说明: 项目实际可用的实例是 MySQL81
rem        (指向 MySQL Server 8.0 的 mysqld + 8.0 的 my.ini, 端口 3306)
rem        MySQL80 指向已失效的 8.4 路径; MySQL84 指向读不到的 D:\my.ini, 均不可用
rem ============================================================
net session >nul 2>&1 || (
  echo [错误] 需要管理员权限。请右键本文件 -> 以管理员身份运行
  pause
  exit /b 1
)

echo [信息] 将 MySQL81 启动类型设为"手动"并启动...
sc config MySQL81 start= demand
net start MySQL81

echo.
echo [信息] 若提示"服务已启动"或"启动成功"即 OK。
echo        连接: localhost:3306  用户 root  密码 123456  数据库 aurora
echo        之后即可在 IDEA / 浏览器 重试连接。
echo.
echo [提示] 想开机自启可把上面 start= demand 改成 start= auto
pause
