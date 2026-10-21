package com.fenglin.springboottest.controller;

import com.fenglin.springboottest.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 全局搜索接口（首页搜索框 → 搜索结果页）。
 *
 *  GET /api/content/search?q=<keyword>&limit=20
 *      -> { ok, query, total, counts:{categories,breeds,posts,users},
 *           categories:[...], breeds:[...], posts:[...], users:[...] }
 *
 * 说明：
 *  - 只读查询，无需登录态；与 /api/content/posts、/api/content/categories 同属 content 域，
 *    复用前端 Vite 的 /api 代理，不额外引入跨域配置。
 *  - 该接口存在时，前端 api/home.js 的 searchContent 会自动从「本地帖子兜底」切换为服务端搜索
 *    （前端按 404 / 网络错误判断是否降级，因此老版本前端也不会因为本接口新增而报错）。
 *  - q 为空白时返回空结果，不返回 4xx：搜索框被清空时前端贴出「换个词试试」即可，不必处理异常。
 */
@RestController
@RequestMapping("/api/content")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public Map<String, Object> search(@RequestParam(name = "q", required = false) String q,
                                      @RequestParam(name = "limit", required = false) Integer limit) {
        int perGroup = limit == null ? 20 : limit;
        return searchService.search(q, perGroup);
    }
}
