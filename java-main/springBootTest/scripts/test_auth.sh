#!/usr/bin/env bash
# 认证接口端到端测试（图形验证码版，与前端 api/auth.js 契约一致）
# 前提：后端已在 8081 启动，MySQL aurora 库已建好
#
# 注意：「注册成功」的完整路径需要人眼看图识别验证码，无法完全自动化。
# 本脚本覆盖可自动化的部分；完整注册请在浏览器人工验证一次。
#
# 沙箱说明：本机沙箱限制 curl 只能写工作目录（写 /dev/null 会返回 23 并被 set -e 中止），
# 因此 -o 一律指向当前目录的 .curl_sink，跑完自动删除。
set -e
BASE=http://localhost:8081/api/auth
SINK=./.curl_sink
trap 'rm -f "$SINK"' EXIT
PASS="Passw0rd!"

echo "========== 0) 健康检查 =========="
curl -s -o "$SINK" -w "GET /api/auth/check-username -> HTTP %{http_code}\n" \
  -X POST "$BASE/check-username" -H "Content-Type: application/json" -d '{"username":"__noop__"}'

echo "========== 1) 获取图形验证码 =========="
CAP=$(curl -s "$BASE/captcha" -w "\n[HTTP %{http_code}]")
echo "$CAP" | sed 's/\("image":"data:image\/png;base64,\)[^"]*/\1...<省略>/'
CID=$(echo "$CAP" | sed -n 's/.*"captchaId":"\([^"]*\)".*/\1/p')
echo "captchaId=$CID"
if ! echo "$CAP" | grep -q '"image":"data:image/png;base64,'; then
  echo "!! 验证码图片格式异常"
  exit 1
fi

echo "========== 2) 错误验证码注册（期望 400）=========="
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$BASE/register" -H "Content-Type: application/json" \
  -d "{\"username\":\"autotest_$(date +%s)\",\"password\":\"$PASS\",\"captchaId\":\"$CID\",\"captchaCode\":\"ZZZZ\"}"

echo "========== 3) 验证码一次性：同 id 复用（期望 400，已被作废）=========="
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$BASE/register" -H "Content-Type: application/json" \
  -d "{\"username\":\"autotest2_$(date +%s)\",\"password\":\"$PASS\",\"captchaId\":\"$CID\",\"captchaCode\":\"ZZZZ\"}"

echo "========== 4) 登录：账号不存在（期望 401 notfound）=========="
curl -s -w "\n[HTTP %{http_code}]\n" -X POST "$BASE/login" -H "Content-Type: application/json" \
  -d "{\"account\":\"__no_such_user__\",\"password\":\"$PASS\"}"

echo "========== 5) 用户名占用检测 =========="
curl -s -X POST "$BASE/check-username" -H "Content-Type: application/json" -d '{"username":"__noop__"}'; echo

echo "=========================================================="
echo "以上自动化用例已跑完。"
echo "「完整注册（正确验证码）」需在浏览器人工验证："
echo "  打开 http://localhost:5173 -> 立即注册 -> 看图输入验证码 -> 创建账号 -> 登录"
