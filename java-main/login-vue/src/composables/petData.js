// =====================================================================
// petData —— 萌宠分类数据（首页「萌宠分类」与「发现」发帖分类共用）
//
// 数据来源双轨（与 api/post.js 的 withFallback 思路一致）：
//  1) 优先调用后端 GET /api/content/categories（数据存在 MySQL 的
//     pet_category + pet_breed 两张表，可由管理员改库维护）；
//  2) 接口不可用（后端未启动 / 404 / 5xx）时，回退到下面这份内置静态数据，
//     保证纯前端演示、断网、后端挂掉时页面都能正常显示。
//
// 对外暴露的仍是同步的 petCategories 数组（保持组件调用方式不变）：
// 首次渲染先用静态数据兜底，接口返回后原地替换数组内容，Vue 自动更新视图。
// 因此组件无需改成 await，也不用关心当前走的是哪条数据源。
// =====================================================================

import { reactive } from 'vue'

/** 内置静态数据：接口不可用时的兜底，同时作为首次渲染的占位 */
const FALLBACK_CATEGORIES = [
  {
    id: 'cat',
    name: '猫类',
    icon: '喵',
    color: '#ff6fa8',
    image: '/images/pets/cat.png',
    breeds: [
      { name: '布偶猫', image: '/images/pets/cat.png', desc: '温顺黏人，蓝眼睛像小天使' },
      { name: '英国短毛猫', image: '/images/pets/cat_british.png', desc: '圆脸胖墩，慵懒小可爱' },
      { name: '缅因猫', image: '/images/pets/cat_maine.png', desc: '体型巨大，温柔的毛毛怪' },
      { name: '橘猫', image: '/images/pets/cat_orange.png', desc: '十橘九胖，干饭第一名' },
      { name: '暹罗猫', image: '/images/pets/cat_siamese.png', desc: '话痨小煤球，超爱撒娇' },
      { name: '三花猫', image: '/images/pets/cat_calico.png', desc: '三色小仙女，温柔又亲人' }
    ]
  },
  {
    id: 'dog',
    name: '狗类',
    icon: '汪',
    color: '#ff8a5c',
    image: '/images/pets/dog.png',
    breeds: [
      { name: '柯基', image: '/images/pets/dog.png', desc: '蜜桃臀小短腿，快乐源泉' },
      { name: '金毛', image: '/images/pets/dog_golden.png', desc: '暖男代表，温柔大暖炉' },
      { name: '柴犬', image: '/images/pets/dog_shiba.png', desc: '表情包担当，倔强又可爱' },
      { name: '哈士奇', image: '/images/pets/dog_husky.png', desc: '行走的拆迁队，二哈本哈' },
      { name: '边牧', image: '/images/pets/dog_border.png', desc: '狗界智商天花板，运动健将' },
      { name: '泰迪', image: '/images/pets/dog_poodle.png', desc: '卷毛小玩偶，机灵又粘人' }
    ]
  },
  {
    id: 'small',
    name: '小宠',
    icon: '啮',
    color: '#8b7bff',
    image: '/images/pets/hamster.png',
    breeds: [
      { name: '仓鼠', image: '/images/pets/hamster.png', desc: '腮帮子仓库，囤粮小能手' },
      { name: '龙猫', image: '/images/pets/small_chinchilla.png', desc: '软糯棉花糖，怕热小公主' },
      { name: '兔子', image: '/images/pets/small_rabbit.png', desc: '长耳吃货，安静又软萌' },
      { name: '荷兰猪', image: '/images/pets/small_guinea.png', desc: '咕咕叫的小胖墩，超亲人' },
      { name: '蜜袋鼯', image: '/images/pets/small_sugar_glider.png', desc: '会飞的小糖果，夜里撒娇' },
      { name: '沙鼠', image: '/images/pets/small_gerbil.png', desc: '捧着瓜子的小圆球，爱囤粮' }
    ]
  },
  {
    id: 'bird',
    name: '鸟类',
    icon: '啾',
    color: '#3ec4a8',
    image: '/images/pets/bird.png',
    breeds: [
      { name: '鹦鹉', image: '/images/pets/bird.png', desc: '模仿达人，话痨小歌手' },
      { name: '文鸟', image: '/images/pets/bird_bunting.png', desc: '圆滚滚小白团，安静温顺' },
      { name: '玄凤', image: '/images/pets/bird_cockatiel.png', desc: '黄冠腮红，吹口哨高手' },
      { name: '鸽子', image: '/images/pets/bird_pigeon.png', desc: '和平使者，咕咕小邮差' },
      { name: '虎皮鹦鹉', image: '/images/pets/bird_budgie.png', desc: '斑纹小歌手，学舌小天才' },
      { name: '牡丹鹦鹉', image: '/images/pets/bird_lovebird.png', desc: '粉色小情侣，甜到犯规' }
    ]
  },
  {
    id: 'fish',
    name: '水族',
    icon: '鱼',
    color: '#4ab3e8',
    image: '/images/pets/fish.png',
    breeds: [
      { name: '金鱼', image: '/images/pets/fish.png', desc: '圆肚小福星，慢悠悠摆尾' },
      { name: '锦鲤', image: '/images/pets/fish_koi.png', desc: '好运象征，池中花团锦簇' },
      { name: '斗鱼', image: '/images/pets/fish_betta.png', desc: '飘逸长鳍，孤傲小武士' },
      { name: '热带鱼', image: '/images/pets/fish_tropical.png', desc: '色彩斑斓，群游小精灵' },
      { name: '孔雀鱼', image: '/images/pets/fish_guppy.png', desc: '拖着彩虹裙摆，水中舞娘' },
      { name: '小丑鱼', image: '/images/pets/fish_clownfish.png', desc: '橙白小丑服，海葵里捉迷藏' }
    ]
  },
  {
    id: 'reptile',
    name: '爬宠',
    icon: '爬',
    color: '#2eb872',
    image: '/images/pets/reptile.png',
    breeds: [
      { name: '乌龟', image: '/images/pets/reptile_turtle.png', desc: '慢悠悠的小寿星，背壳藏着小宇宙' },
      { name: '蜥蜴', image: '/images/pets/reptile_lizard.png', desc: '攀爬小能手，鳞片闪着微光' },
      { name: '守宫', image: '/images/pets/reptile_gecko.png', desc: '豹纹小可爱，夜里睁着大眼睛' },
      { name: '陆龟', image: '/images/pets/reptile_tortoise.png', desc: '背着小房子的慢悠悠旅行家' },
      { name: '变色龙', image: '/images/pets/reptile_chameleon.png', desc: '伪装大师，情绪写在身上' },
      { name: '玉米蛇', image: '/images/pets/reptile_cornsnake.png', desc: '橙白小玉米，温顺不吓人' }
    ]
  },
  {
    id: 'exotic',
    name: '异宠',
    icon: '奇',
    color: '#b06ad6',
    image: '/images/pets/exotic.png',
    breeds: [
      { name: '刺猬', image: '/images/pets/exotic_hedgehog.png', desc: '软刺小团子，捧着苹果撒娇' },
      { name: '狐狸', image: '/images/pets/exotic_fox.png', desc: '蓬松大尾巴，机灵又黏人' },
      { name: '浣熊', image: '/images/pets/exotic_raccoon.png', desc: '黑眼罩小淘气，洗东西爱好者' },
      { name: '耳廓狐', image: '/images/pets/exotic_fennec.png', desc: '大耳朵小雷达，沙漠萌主' },
      { name: '水獭', image: '/images/pets/exotic_otter.png', desc: '抱着贝壳仰泳，撒娇高手' },
      { name: '雪貂', image: '/images/pets/exotic_ferret.png', desc: '细长小懒虫，蜷成团秒睡' }
    ]
  },
  {
    id: 'farm',
    name: '农场萌宠',
    icon: '牧',
    color: '#e8a33d',
    image: '/images/pets/farm.png',
    breeds: [
      { name: '小羊', image: '/images/pets/farm_sheep.png', desc: '卷卷羊毛云朵，咩咩叫的小甜心' },
      { name: '小猪', image: '/images/pets/farm_pig.png', desc: '圆滚滚小粉团，卷尾巴小懒虫' },
      { name: '小鸭', image: '/images/pets/farm_duck.png', desc: '黄茸茸小水手，摇摇摆摆跟着走' },
      { name: '小牛', image: '/images/pets/farm_calf.png', desc: '黑白花小奶球，叼着三叶草' },
      { name: '小马', image: '/images/pets/farm_pony.png', desc: '蓬松小鬃毛，哒哒哒跑圈圈' },
      { name: '小鹅', image: '/images/pets/farm_gosling.png', desc: '毛茸茸小管家，嘎嘎嘎巡逻' }
    ]
  }
]

/**
 * 全局分类数据（响应式）。
 * 组件按数组使用即可：v-for="c in petCategories"
 */
export const petCategories = reactive(cloneCategories(FALLBACK_CATEGORIES))

const API_BASE = (import.meta.env.VITE_API_BASE || '/api').replace(/\/$/, '')
// 默认对接真实后端（与 api/auth.js、api/home.js 的口径一致），后台管理系统
// 修改分类/品种后，首页通过 loadCategories() 立即拿到数据库最新数据。
// 只有显式设置 VITE_USE_MOCK=true 才走本地静态数据（脱离后端单独演示 UI 时用）。
const USE_MOCK =
  import.meta.env.VITE_USE_MOCK === undefined
    ? false
    : String(import.meta.env.VITE_USE_MOCK) === 'true'

/** 数据源运行态，供 UI 显示角标（'static' | 'remote'） */
export const petDataRuntime = reactive({ source: 'static', reason: '' })

/** 空图片兜底：分类/品种没配图时用爪印占位图，避免破图 */
const DEFAULT_PET_IMAGE = '/images/pets/paw-default.png'

function normalizeImage(url) {
  const s = String(url || '').trim()
  return s || DEFAULT_PET_IMAGE
}

function cloneCategories(src) {
  return src.map((c) => ({
    ...c,
    image: normalizeImage(c.image),
    breeds: (c.breeds || []).map((b) => ({ ...b, image: normalizeImage(b.image) }))
  }))
}

/** 用接口数据原地替换响应式数组内容（保持引用不变，组件无需重新绑定） */
function applyCategories(list) {
  petCategories.splice(0, petCategories.length, ...cloneCategories(list))
}

/** 校验接口返回结构，避免脏数据把页面搞崩 */
function isValidCategory(c) {
  return c && typeof c.id === 'string' && typeof c.name === 'string' && Array.isArray(c.breeds)
}

/**
 * 拉取分类数据。
 * - mock 模式：直接用静态数据（保持原有纯前端演示行为）。
 * - 真实模式：调接口，失败时保留静态兜底数据，仅记录原因。
 * 不抛异常，调用方无需 try/catch。
 */
export async function loadCategories() {
  if (USE_MOCK) {
    petDataRuntime.source = 'static'
    petDataRuntime.reason = '前端 mock 模式'
    return petCategories
  }
  try {
    const res = await fetch(API_BASE + '/content/categories', { credentials: 'include' })
    if (!res.ok) throw new Error('HTTP ' + res.status)
    const data = await res.json()
    const items = (data && data.items) || []
    if (items.length && items.every(isValidCategory)) {
      applyCategories(items)
      petDataRuntime.source = 'remote'
      petDataRuntime.reason = ''
    } else {
      petDataRuntime.source = 'static'
      petDataRuntime.reason = '接口返回数据为空或格式不符，已用内置数据兜底'
    }
  } catch (e) {
    // 后端未启动 / 接口未实现：静默保留静态数据，不影响页面显示
    petDataRuntime.source = 'static'
    petDataRuntime.reason = '分类接口不可用，已用内置数据兜底'
  }
  return petCategories
}

// 模块加载后立即请求一次（不阻塞首次渲染：先显示静态数据，拿到接口数据再刷新）
loadCategories()

/* ---------------- 兼容原有导出（保持组件不改） ---------------- */

/** 按 id 取分类（找不到返回 null） */
export function findCategory(id) {
  return petCategories.find((c) => c.id === id) || null
}

/** 分类名（找不到时给个兜底） */
export function categoryName(id) {
  const c = findCategory(id)
  return c ? c.name : '其他'
}

/** 分类主题色（找不到时用品牌粉） */
export function categoryColor(id) {
  const c = findCategory(id)
  return c ? c.color : '#ff6fa8'
}

/** 分类图标字（找不到时用「萌」） */
export function categoryIcon(id) {
  const c = findCategory(id)
  return c ? c.icon : '萌'
}
