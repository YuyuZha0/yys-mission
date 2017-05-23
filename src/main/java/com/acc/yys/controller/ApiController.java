package com.acc.yys.controller;

import com.acc.yys.pojo.Chapter;
import com.acc.yys.pojo.JsonBody;
import com.acc.yys.service.FuzzyQueryService;
import com.acc.yys.service.QueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Created by zhaoyy on 2017/5/19.
 */
@RestController
@RequestMapping(value = "/api", method = RequestMethod.POST)
public final class ApiController {

    @Autowired
    private FuzzyQueryService fuzzyQueryService;
    @Autowired
    private QueryService queryService;

    @RequestMapping("getAutoComplement")
    public JsonBody getAutoComplement(@RequestParam String query) {
        return JsonBody.builder()
                .append("queryList", fuzzyQueryService.guessFuzzy(query, 5))
                .msg("获取成功")
                .build(JsonBody.OK);
    }

    @RequestMapping("getChapterList")
    public JsonBody getChapterList(@RequestParam String query) {
        List<Chapter> chapterList = queryService.queryChapterByCharacterName(query);
        if (chapterList.isEmpty()) {
            List<FuzzyQueryService.QueryRanker> rankerList = fuzzyQueryService.guessFuzzy(query, 1);
            if (!rankerList.isEmpty())
                chapterList = queryService.queryChapterByCharacterName(rankerList.get(0).getQuery());
        }
        if (chapterList.isEmpty())
            return JsonBody.builder()
                    .msg("未查询到相关记录")
                    .build(JsonBody.NOT_FOUND);
        return JsonBody.builder()
                .msg("获取成功")
                .append("chapterList", chapterList)
                .build(JsonBody.OK);
    }
}
