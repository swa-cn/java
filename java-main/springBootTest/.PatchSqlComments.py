# -*- coding: utf-8 -*-
import re, pathlib

p = pathlib.Path('D:/workSpace/java/java-main/aurora.sql')
s = p.read_text(encoding='utf-8')

comments = {
    'app_user': '用户账户',
    'pet_breed': '宠物品种目录',
    'pet_category': '宠物分类目录',
    'pet_follow': '用户关注关系',
    'pet_friend': '好友关系',
    'pet_knowledge': '养宠知识文章',
    'pet_message': '用户私信消息',
    'pet_post': '社区帖子',
    'pet_post_comment': '帖子评论',
    'pet_post_favorite': '帖子收藏',
    'pet_post_like': '帖子点赞',
    'pet_post_media': '帖子媒体（图片/视频）',
    'pet_post_report': '帖子举报记录',
}

# 每张表的 CREATE TABLE ... ENGINE=... ROW_FORMAT=...; 结尾，在 ROW_FORMAT 前插入 COMMENT（若尚无）
changed = []
for table, comment in comments.items():
    # 定位该表的 CREATE TABLE 语句块（到分号结束）
    m = re.search(r"(CREATE TABLE `" + re.escape(table) + r"`\s*\(.*?;\s*\n)", s, re.S)
    if not m:
        print('未找到表:', table)
        continue
    block = m.group(1)
    if 'COMMENT =' in block:
        print('已有注释，跳过:', table)
        continue
    # 在 ROW_FORMAT = xxx 前插入 COMMENT
    new_block, n = re.subn(r"(ROW_FORMAT\s*=\s*[A-Za-z]+\s*)", "COMMENT = '" + comment + "' " + r"\1", block, count=1)
    if n == 0:
        print('未找到 ROW_FORMAT:', table)
        continue
    s = s[:m.start(1)] + new_block + s[m.end(1):]
    changed.append(table)

p.write_text(s, encoding='utf-8')
print('已更新建表脚本的表:', ', '.join(changed))
