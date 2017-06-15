package com.acc.yys.controller;

import com.acc.yys.pojo.Character;
import com.acc.yys.pojo.JsonBody;
import com.acc.yys.service.FuzzyQueryService;
import com.acc.yys.service.MessageService;
import com.acc.yys.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/19.
 */
@RestController
@RequestMapping(value = "/api", method = RequestMethod.POST)
public final class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private FuzzyQueryService fuzzyQueryService;
    @Autowired
    private QueryService queryService;
    @Autowired
    private MessageService messageService;

    @RequestMapping("getAutoComplement")
    public JsonBody getAutoComplement(@RequestParam String query,
                                      @RequestParam(defaultValue = "5") int limit) {
        return JsonBody.builder()
                .append("queryList", fuzzyQueryService.guessRealMeaning(query, limit))
                .msg("获取成功")
                .build(JsonBody.OK);
    }

    @RequestMapping("getQueryResult")
    public JsonBody getChapterList(@RequestParam String query) {
        Character character = fuzzyQueryService.queryCharacter(query);
        if (character == null)
            return JsonBody.builder()
                    .msg("未查找到对应式神")
                    .build(JsonBody.NOT_FOUND);
        List<QueryService.QueryResult> queryResultList = queryService.queryCharacterLocation(character);
        if (queryResultList == null || queryResultList.isEmpty())
            return JsonBody.builder()
                    .msg("未查找到对应章节")
                    .build(JsonBody.NOT_FOUND);
        logger.info("execute query for [{}] and resolved as [{}]", encodingToISO_8851_1(query),
                encodingToISO_8851_1(character.getName()));
        return JsonBody.builder()
                .append("character", character)
                .append("queryResultList", queryResultList)
                .msg("查询成功")
                .build(JsonBody.OK);
    }

    @RequestMapping("leaveMsg")
    public JsonBody leaveMsg(@RequestParam(required = false) String name,
                             @RequestParam(required = false) String email,
                             @RequestParam String msg, HttpServletRequest request) {
        return messageService.leaveMsg(name, email, msg, request);
    }

    private static String encodingToISO_8851_1(String s) {
        if (s == null || s.isEmpty())
            return s;
        return new String(s.getBytes(), StandardCharsets.ISO_8859_1);
    }
}
