package com.fenglin.springboottest.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 图形验证码服务（内存实现，无第三方依赖）。
 * - generate():  生成 4 位验证码图片，返回 {captchaId, image(data URI)}。
 * - verify():    校验并「一次性消费」——无论成功失败都立即删除，防止同一张图被反复试错。
 * - 内存 Map 自带 TTL 清理，单机部署够用；若将来多实例部署，可改为 Redis 存储。
 */
@Service
public class CaptchaService {

    /** 去掉易混淆字符后的候选集（I/L/1、O/0、Z/2，以及 S/5） */
    private static final String CODE_CHARS = "ABCDEFGHJKMNPQRSTUVWXY346789";
    private static final int CODE_LENGTH = 4;
    private static final int WIDTH = 116;
    private static final int HEIGHT = 40;
    /** 有效期：默认 5 分钟，可通过 auth.captcha.ttl-ms 配置 */
    @Value("${auth.captcha.ttl-ms:300000}")
    private long ttlMs;

    /** Q 版可爱风糖果色系（画字符用） */
    private static final Color[] CANDY_COLORS = {
            new Color(0xF78FB3), // 粉
            new Color(0x7E6BC4), // 紫
            new Color(0x63B3ED), // 蓝
            new Color(0x53BC81), // 绿
            new Color(0xF6AD55), // 橙
            new Color(0xE85D75)  // 莓红
    };

    /** id -> 验证码实体 */
    private record Entry(String code, long expiresAt) {}

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    /** 生成一张验证码：返回 {captchaId, image}，image 为可直接用于 <img src> 的 data URI */
    public Map<String, String> generate() {
        String id = UUID.randomUUID().toString().replace("-", "");
        String code = randomCode();
        store.put(id, new Entry(code, System.currentTimeMillis() + ttlMs));
        cleanup();
        return Map.of("captchaId", id, "image", drawToDataUri(code));
    }

    /** 校验验证码（大小写不敏感），一次性消费：用过即删，防重放 */
    public boolean verify(String captchaId, String input) {
        cleanup();
        Entry entry = captchaId == null ? null : store.remove(captchaId);
        if (entry == null) {
            return false; // 不存在或已被使用过
        }
        if (System.currentTimeMillis() > entry.expiresAt()) {
            return false; // 已过期
        }
        return entry.code().equalsIgnoreCase(input == null ? "" : input.trim());
    }

    /** 清理所有过期验证码 */
    private void cleanup() {
        long now = System.currentTimeMillis();
        List<String> expired = new ArrayList<>();
        store.forEach((id, e) -> {
            if (e.expiresAt() < now) expired.add(id);
        });
        expired.forEach(store::remove);
    }

    private String randomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(CODE_CHARS.charAt(ThreadLocalRandom.current().nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

    /** 用 BufferedImage 画验证码并转成 data:image/png;base64,xxx */
    private String drawToDataUri(String code) {
        BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        ThreadLocalRandom rnd = ThreadLocalRandom.current();

        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 背景：浅色 + 底部渐变，保持可爱风格
            g.setColor(new Color(0xFFF7FA));
            g.fillRect(0, 0, WIDTH, HEIGHT);

            // 背景圆点（装饰，浅色不干扰）
            for (int i = 0; i < 12; i++) {
                g.setColor(new Color(0xFFE3EE));
                int r = 3 + rnd.nextInt(5);
                g.fillOval(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT), r, r);
            }

            // 每个字符独立颜色 + 小幅旋转 + 上下偏移
            Font font = new Font(Font.SANS_SERIF, Font.BOLD, 30);
            g.setFont(font);
            int step = (WIDTH - 12) / CODE_LENGTH;
            for (int i = 0; i < CODE_LENGTH; i++) {
                char c = code.charAt(i);
                g.setColor(CANDY_COLORS[rnd.nextInt(CANDY_COLORS.length)]);
                double theta = (rnd.nextDouble() - 0.5) * 0.4; // ±11°，太大人和机器都难认
                int x = 8 + i * step + rnd.nextInt(3);
                int y = HEIGHT / 2 + 6 + (rnd.nextInt(5) - 2);
                g.rotate(theta, x, y);
                g.drawChars(new char[]{c}, 0, 1, x - 8, y - 10);
                g.rotate(-theta, x, y);
            }

            // 干扰线：2 条半透明，轻干扰即可
            g.setStroke(new BasicStroke(1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 2; i++) {
                Color c = CANDY_COLORS[rnd.nextInt(CANDY_COLORS.length)];
                g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 90));
                g.drawLine(rnd.nextInt(WIDTH / 2), rnd.nextInt(HEIGHT),
                        WIDTH / 2 + rnd.nextInt(WIDTH / 2), rnd.nextInt(HEIGHT));
            }

            // 前景噪点：细碎、少量
            for (int i = 0; i < 10; i++) {
                g.setColor(new Color(0x8E7CC3, false));
                g.fillOval(rnd.nextInt(WIDTH), rnd.nextInt(HEIGHT), 1, 1);
            }
        } finally {
            g.dispose();
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ImageIO.write(img, "png", out);
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            throw new IllegalStateException("图形验证码生成失败", e);
        }
    }
}
