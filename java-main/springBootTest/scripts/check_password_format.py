# -*- coding: utf-8 -*-
"""检查 app_user.password 存储格式（明文 / BCrypt 哈希）。
用法: python check_password_format.py [用户名]
用法说明: 2026-11 密码改为 BCrypt 加密入库；存量明文账号在下次登录成功时自动升级。
密码从环境变量 DB_PASSWORD 读取（也可在 application-local.properties 中配置）。
"""
import os
import sys

import pymysql

DB_PASSWORD = os.environ.get("DB_PASSWORD", "")
if not DB_PASSWORD:
    # 回退读取项目根目录的 application-local.properties（不入库文件）
    local_path = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                              "application-local.properties")
    if os.path.exists(local_path):
        with open(local_path, encoding="utf-8") as f:
            for line in f:
                line = line.strip()
                if line.startswith("DB_PASSWORD="):
                    DB_PASSWORD = line.split("=", 1)[1].strip()
                    break
if not DB_PASSWORD:
    sys.exit("[错误] 未找到 DB_PASSWORD，请设置环境变量或配置 application-local.properties")

conn = pymysql.connect(host="localhost", user="root", password=DB_PASSWORD,
                       database="aurora", charset="utf8mb4")
cur = conn.cursor()

sql = ("SELECT username, LEFT(password, 12), LENGTH(password) FROM app_user"
       + (" WHERE username=%s" if len(sys.argv) > 1 else "")
       + " ORDER BY id")
cur.execute(sql, tuple(sys.argv[1:]))

rows = cur.fetchall()
for r in rows:
    kind = "BCrypt哈希(已加密)" if r[1].startswith("$2") else "明文(待下次登录自动升级)"
    print(f"  {r[0]}: {r[1]}...  长度={r[2]}  [{kind}]")
if not rows:
    print("  (无匹配用户)")
conn.close()
