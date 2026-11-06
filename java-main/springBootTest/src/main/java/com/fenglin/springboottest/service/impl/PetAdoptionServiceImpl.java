package com.fenglin.springboottest.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fenglin.springboottest.entity.Pet;
import com.fenglin.springboottest.entity.PetFriend;
import com.fenglin.springboottest.entity.PetGiftLog;
import com.fenglin.springboottest.entity.PetInteractLog;
import com.fenglin.springboottest.entity.PetSpecies;
import com.fenglin.springboottest.entity.PetTaskProgress;
import com.fenglin.springboottest.entity.PetUserResource;
import com.fenglin.springboottest.mapper.PetFriendMapper;
import com.fenglin.springboottest.mapper.PetGiftLogMapper;
import com.fenglin.springboottest.mapper.PetInteractLogMapper;
import com.fenglin.springboottest.mapper.PetMapper;
import com.fenglin.springboottest.mapper.PetSpeciesMapper;
import com.fenglin.springboottest.mapper.PetTaskProgressMapper;
import com.fenglin.springboottest.mapper.PetUserResourceMapper;
import com.fenglin.springboottest.service.PetAdoptionService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 萌宠领养养成实现。
 *
 * 数据表：pet_species / pet_user_resource / pet / pet_task_progress / pet_interact_log / pet_gift_log
 * （建表见项目根 pet_adoption.sql）。好友关系复用 pet_friend 表做赠送合法性校验。
 */
@Service
public class PetAdoptionServiceImpl implements PetAdoptionService {

    private static final int INTERACT_LIMIT = 5;     // 每日摸摸上限
    private static final int GIFT_LIMIT = 3;         // 每日赠送上限
    private static final int GIFT_NUTRIENT = 10;     // 每次赠送养分
    private static final int GIFT_FOOD = 1;          // 每次赠送食物

    /** 任务模板（与前端 petAdoption.js 对齐） */
    private static final List<TaskTpl> TASK_TEMPLATES = List.of(
            new TaskTpl("sign", "每日签到", "打卡领养分", "nutrient"),
            new TaskTpl("read", "阅读萌宠小知识", "涨知识也涨养分", "nutrient"),
            new TaskTpl("quiz", "完成萌宠小测验", "考考你对毛孩子的了解", "nutrient"),
            new TaskTpl("walk", "带毛孩子散步", "出门溜达一圈", "nutrient"),
            new TaskTpl("post", "发布一条动态", "分享萌宠得饲料", "food"),
            new TaskTpl("like", "给好友动态点赞", "送出爱心得饲料", "food"),
            new TaskTpl("jump", "陪宠物跳障碍", "操控毛孩子跳跃躲障碍", "nutrient")
    );

    private final PetSpeciesMapper speciesMapper;
    private final PetUserResourceMapper resourceMapper;
    private final PetMapper petMapper;
    private final PetTaskProgressMapper taskMapper;
    private final PetInteractLogMapper interactMapper;
    private final PetGiftLogMapper giftMapper;
    private final PetFriendMapper friendMapper;

    public PetAdoptionServiceImpl(PetSpeciesMapper speciesMapper,
                                  PetUserResourceMapper resourceMapper,
                                  PetMapper petMapper,
                                  PetTaskProgressMapper taskMapper,
                                  PetInteractLogMapper interactMapper,
                                  PetGiftLogMapper giftMapper,
                                  PetFriendMapper friendMapper) {
        this.speciesMapper = speciesMapper;
        this.resourceMapper = resourceMapper;
        this.petMapper = petMapper;
        this.taskMapper = taskMapper;
        this.interactMapper = interactMapper;
        this.giftMapper = giftMapper;
        this.friendMapper = friendMapper;
    }

    /* ============================ 工具 ============================ */

    private static String today() {
        return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    private static long now() {
        return System.currentTimeMillis();
    }

    private static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    /** 取资源行，不存在则创建（养分/食物=0） */
    private PetUserResource getOrCreateResource(String owner) {
        PetUserResource r = resourceMapper.selectById(owner);
        if (r == null) {
            r = new PetUserResource();
            r.setUserKey(owner);
            r.setNutrients(0);
            r.setFood(0);
            r.setUpdatedAt(now());
            resourceMapper.insert(r);
        }
        return r;
    }

    /** 取属于 owner 的宠物 */
    private Pet getOwnedPet(String owner, String petId) {
        if (petId == null) return null;
        Pet p = petMapper.selectById(petId);
        if (p == null || !owner.equals(p.getUserKey())) return null;
        return p;
    }

    /** 取今日任务进度（不存在返回未持久化的空对象） */
    private PetTaskProgress getOrCreateProgress(String owner, String taskId, String date) {
        PetTaskProgress p = taskMapper.selectOne(new LambdaQueryWrapper<PetTaskProgress>()
                .eq(PetTaskProgress::getUserKey, owner)
                .eq(PetTaskProgress::getTaskId, taskId)
                .eq(PetTaskProgress::getTaskDate, date));
        if (p == null) {
            p = new PetTaskProgress();
            p.setUserKey(owner);
            p.setTaskId(taskId);
            p.setTaskDate(date);
            p.setCompleted(0);
            p.setClaimed(0);
        }
        return p;
    }

    private void saveProgress(PetTaskProgress p) {
        if (p.getId() == null) taskMapper.insert(p);
        else taskMapper.updateById(p);
    }

    /* ============================ 面板 ============================ */

    @Override
    public Map<String, Object> dashboard(String owner) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", true);

        // 物种
        List<PetSpecies> species = speciesMapper.selectList(
                new LambdaQueryWrapper<PetSpecies>().orderByAsc(PetSpecies::getSortOrder).orderByAsc(PetSpecies::getId));
        List<Map<String, Object>> speciesView = new ArrayList<>(species.size());
        for (PetSpecies s : species) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("id", s.getId());
            v.put("name", s.getName());
            v.put("emoji", s.getEmoji());
            v.put("color", s.getColor());
            v.put("description", s.getDescription());
            v.put("hatchCost", s.getHatchCost());
            speciesView.add(v);
        }
        m.put("species", speciesView);

        // 资源
        PetUserResource r = getOrCreateResource(owner);
        m.put("nutrients", r.getNutrients());
        m.put("food", r.getFood());

        // 宠物
        List<Pet> pets = petMapper.selectList(
                new LambdaQueryWrapper<Pet>().eq(Pet::getUserKey, owner).orderByAsc(Pet::getCreatedAt));
        List<Map<String, Object>> petView = new ArrayList<>(pets.size());
        for (Pet p : pets) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("id", p.getId());
            v.put("typeId", p.getSpeciesId());
            v.put("name", p.getName());
            v.put("status", p.getStatus());
            v.put("intimacy", p.getIntimacy());
            v.put("satiety", p.getSatiety());
            v.put("mood", p.getMood());
            v.put("createdAt", p.getCreatedAt());
            v.put("hatchedAt", p.getHatchedAt());
            v.put("lastFed", p.getLastFed());
            v.put("lastInteract", p.getLastInteract());
            petView.add(v);
        }
        m.put("pets", petView);

        // 任务（今日进度）
        String date = today();
        Map<String, PetTaskProgress> progMap = new LinkedHashMap<>();
        for (PetTaskProgress tp : taskMapper.selectList(new LambdaQueryWrapper<PetTaskProgress>()
                .eq(PetTaskProgress::getUserKey, owner).eq(PetTaskProgress::getTaskDate, date))) {
            progMap.put(tp.getTaskId(), tp);
        }
        List<Map<String, Object>> taskView = new ArrayList<>(TASK_TEMPLATES.size());
        for (TaskTpl t : TASK_TEMPLATES) {
            PetTaskProgress tp = progMap.get(t.id);
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("id", t.id);
            v.put("title", t.title);
            v.put("desc", t.desc);
            v.put("rewardType", t.rewardType);
            v.put("completed", tp != null && tp.getCompleted() != null && tp.getCompleted() == 1);
            v.put("claimed", tp != null && tp.getClaimed() != null && tp.getClaimed() == 1);
            taskView.add(v);
        }
        m.put("tasks", taskView);

        // 互动（每日）
        PetInteractLog il = interactMapper.selectOne(new LambdaQueryWrapper<PetInteractLog>()
                .eq(PetInteractLog::getUserKey, owner).eq(PetInteractLog::getInteractDate, date));
        int interactCount = il == null ? 0 : (il.getInteractCount() == null ? 0 : il.getInteractCount());
        Map<String, Object> interactView = new LinkedHashMap<>();
        interactView.put("count", interactCount);
        interactView.put("limit", INTERACT_LIMIT);
        interactView.put("canInteract", interactCount < INTERACT_LIMIT);
        m.put("interact", interactView);

        // 赠送（每日）
        int giftCount = Math.toIntExact(giftMapper.selectCount(new LambdaQueryWrapper<PetGiftLog>()
                .eq(PetGiftLog::getUserKey, owner).eq(PetGiftLog::getGiftDate, date)));
        List<PetGiftLog> giftRows = giftMapper.selectList(new LambdaQueryWrapper<PetGiftLog>()
                .eq(PetGiftLog::getUserKey, owner).eq(PetGiftLog::getGiftDate, date));
        Map<String, Object> claimed = new LinkedHashMap<>();
        for (PetGiftLog g : giftRows) claimed.put(g.getFriendKey(), true);
        Map<String, Object> giftView = new LinkedHashMap<>();
        giftView.put("count", giftCount);
        giftView.put("limit", GIFT_LIMIT);
        giftView.put("canGift", giftCount < GIFT_LIMIT);
        giftView.put("claimed", claimed);
        m.put("gifts", giftView);

        // 真实好友（用于赠送入口显隐与列表）
        List<PetFriend> friends = friendMapper.selectList(new LambdaQueryWrapper<PetFriend>()
                .eq(PetFriend::getOwnerKey, owner).eq(PetFriend::getStatus, "friend"));
        List<Map<String, Object>> friendView = new ArrayList<>(friends.size());
        for (PetFriend f : friends) {
            Map<String, Object> v = new LinkedHashMap<>();
            v.put("key", f.getFriendKey());
            v.put("name", f.getFriendName());
            friendView.add(v);
        }
        m.put("friends", friendView);

        return m;
    }

    /* ============================ 写操作 ============================ */

    @Override
    public Map<String, Object> adopt(String owner, String speciesId, String name) {
        PetSpecies s = speciesMapper.selectById(speciesId);
        if (s == null) return fail("unknown");
        Pet p = new Pet();
        p.setId("p" + Long.toHexString(now()) + Integer.toHexString(randInt(0, 0xffff)));
        p.setUserKey(owner);
        p.setSpeciesId(speciesId);
        p.setName((name != null && !name.isBlank()) ? name.trim() : s.getName());
        p.setStatus("egg");
        p.setIntimacy(0);
        p.setSatiety(60);
        p.setMood(80);
        p.setCreatedAt(now());
        p.setHatchedAt(null);
        p.setLastFed(0L);
        p.setLastInteract(0L);
        petMapper.insert(p);
        return dashboard(owner);
    }

    @Override
    public Map<String, Object> hatch(String owner, String petId) {
        Pet p = getOwnedPet(owner, petId);
        if (p == null) return fail("notfound");
        if (!"egg".equals(p.getStatus())) return fail("already");
        PetSpecies s = speciesMapper.selectById(p.getSpeciesId());
        int cost = s != null && s.getHatchCost() != null ? s.getHatchCost() : 20;
        PetUserResource r = getOrCreateResource(owner);
        if (r.getNutrients() < cost) return fail("lack");
        r.setNutrients(r.getNutrients() - cost);
        r.setUpdatedAt(now());
        resourceMapper.updateById(r);
        p.setStatus("hatched");
        p.setHatchedAt(now());
        petMapper.updateById(p);
        return dashboard(owner);
    }

    @Override
    public Map<String, Object> feed(String owner, String petId) {
        Pet p = getOwnedPet(owner, petId);
        if (p == null) return fail("notfound");
        if (!"hatched".equals(p.getStatus())) return fail("notegg");
        PetUserResource r = getOrCreateResource(owner);
        if (r.getFood() == null || r.getFood() <= 0) return fail("nofood");
        r.setFood(r.getFood() - 1);
        r.setUpdatedAt(now());
        resourceMapper.updateById(r);
        int gain = randInt(4, 8);
        p.setIntimacy(clamp(p.getIntimacy() + gain, 0, 100));
        p.setSatiety(clamp(p.getSatiety() + 25, 0, 100));
        p.setMood(clamp(p.getMood() + 5, 0, 100));
        p.setLastFed(now());
        petMapper.updateById(p);
        Map<String, Object> m = dashboard(owner);
        m.put("gain", gain);
        return m;
    }

    @Override
    public Map<String, Object> interact(String owner, String petId) {
        Pet p = getOwnedPet(owner, petId);
        if (p == null) return fail("notfound");
        if (!"hatched".equals(p.getStatus())) return fail("notegg");
        String date = today();
        PetInteractLog il = interactMapper.selectOne(new LambdaQueryWrapper<PetInteractLog>()
                .eq(PetInteractLog::getUserKey, owner).eq(PetInteractLog::getInteractDate, date));
        int count = il == null ? 0 : (il.getInteractCount() == null ? 0 : il.getInteractCount());
        if (count >= INTERACT_LIMIT) return fail("limit");
        if (il == null) {
            il = new PetInteractLog();
            il.setUserKey(owner);
            il.setInteractDate(date);
            il.setInteractCount(1);
            interactMapper.insert(il);
        } else {
            il.setInteractCount(count + 1);
            interactMapper.updateById(il);
        }
        int gain = randInt(1, 5);
        p.setIntimacy(clamp(p.getIntimacy() + gain, 0, 100));
        p.setMood(clamp(p.getMood() + 2, 0, 100));
        p.setLastInteract(now());
        petMapper.updateById(p);
        Map<String, Object> m = dashboard(owner);
        m.put("gain", gain);
        return m;
    }

    @Override
    public Map<String, Object> completeTask(String owner, String taskId) {
        if (TASK_TEMPLATES.stream().noneMatch(t -> t.id.equals(taskId))) return fail("unknown");
        String date = today();
        PetTaskProgress tp = getOrCreateProgress(owner, taskId, date);
        if (tp.getClaimed() != null && tp.getClaimed() == 1) return fail("done");
        tp.setCompleted(1);
        saveProgress(tp);
        return dashboard(owner);
    }

    @Override
    public Map<String, Object> claimTask(String owner, String taskId) {
        TaskTpl tpl = TASK_TEMPLATES.stream().filter(t -> t.id.equals(taskId)).findFirst().orElse(null);
        if (tpl == null) return fail("unknown");
        String date = today();
        PetTaskProgress tp = getOrCreateProgress(owner, taskId, date);
        if (tp.getClaimed() != null && tp.getClaimed() == 1) return fail("done");
        if (tp.getCompleted() == null || tp.getCompleted() == 0) return fail("incomplete");
        PetUserResource r = getOrCreateResource(owner);
        int amount;
        String type;
        if ("food".equals(tpl.rewardType)) {
            type = "food";
            amount = 1;
            r.setFood((r.getFood() == null ? 0 : r.getFood()) + amount);
        } else {
            type = "nutrient";
            amount = randInt(5, 10);
            r.setNutrients((r.getNutrients() == null ? 0 : r.getNutrients()) + amount);
        }
        r.setUpdatedAt(now());
        resourceMapper.updateById(r);
        tp.setClaimed(1);
        saveProgress(tp);
        Map<String, Object> m = dashboard(owner);
        m.put("reward", Map.of("type", type, "amount", amount));
        return m;
    }

    @Override
    public Map<String, Object> claimGift(String owner, String friendKey) {
        if (friendKey == null || friendKey.isBlank()) return fail("unknown");
        // 校验好友关系
        Long friendCnt = friendMapper.selectCount(new LambdaQueryWrapper<PetFriend>()
                .eq(PetFriend::getOwnerKey, owner)
                .eq(PetFriend::getFriendKey, friendKey)
                .eq(PetFriend::getStatus, "friend"));
        if (friendCnt == null || friendCnt == 0) return fail("notfriend");
        String date = today();
        int giftCount = Math.toIntExact(giftMapper.selectCount(new LambdaQueryWrapper<PetGiftLog>()
                .eq(PetGiftLog::getUserKey, owner).eq(PetGiftLog::getGiftDate, date)));
        if (giftCount >= GIFT_LIMIT) return fail("limit");
        Long already = giftMapper.selectCount(new LambdaQueryWrapper<PetGiftLog>()
                .eq(PetGiftLog::getUserKey, owner)
                .eq(PetGiftLog::getFriendKey, friendKey)
                .eq(PetGiftLog::getGiftDate, date));
        if (already != null && already > 0) return fail("already");
        PetGiftLog g = new PetGiftLog();
        g.setUserKey(owner);
        g.setFriendKey(friendKey);
        g.setGiftDate(date);
        giftMapper.insert(g);
        PetUserResource r = getOrCreateResource(owner);
        r.setNutrients((r.getNutrients() == null ? 0 : r.getNutrients()) + GIFT_NUTRIENT);
        r.setFood((r.getFood() == null ? 0 : r.getFood()) + GIFT_FOOD);
        r.setUpdatedAt(now());
        resourceMapper.updateById(r);
        Map<String, Object> m = dashboard(owner);
        m.put("reward", Map.of("nutrient", GIFT_NUTRIENT, "food", GIFT_FOOD));
        return m;
    }

    /* ============================ 辅助 ============================ */

    @Override
    public Map<String, Object> rename(String owner, String petId, String name) {
        Pet p = getOwnedPet(owner, petId);
        if (p == null) return fail("notfound");
        if (name != null && !name.isBlank()) p.setName(name.trim());
        petMapper.updateById(p);
        return dashboard(owner);
    }

    private Map<String, Object> fail(String reason) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("ok", false);
        m.put("reason", reason);
        return m;
    }

    /** 任务模板 */
    private static class TaskTpl {
        final String id;
        final String title;
        final String desc;
        final String rewardType;

        TaskTpl(String id, String title, String desc, String rewardType) {
            this.id = id;
            this.title = title;
            this.desc = desc;
            this.rewardType = rewardType;
        }
    }
}
