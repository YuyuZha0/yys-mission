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
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/19.
 */
@RestController
@RequestMapping(value = "/api", method = RequestMethod.POST)
public final class ApiController {

    private static final Logger logger = LoggerFactory.getLogger(ApiController.class);

    private final FuzzyQueryService fuzzyQueryService;
    private final QueryService queryService;
    private final MessageService messageService;

    @Autowired
    public ApiController(FuzzyQueryService fuzzyQueryService,
                         QueryService queryService,
                         MessageService messageService) {
        this.fuzzyQueryService = fuzzyQueryService;
        this.queryService = queryService;
        this.messageService = messageService;
    }

    @RequestMapping("getAutoComplement")
    public JsonBody getAutoComplement(@RequestParam String query,
                                      @RequestParam(defaultValue = "5") int limit) {
        return JsonBody.builder()
                .append("queryList", fuzzyQueryService.guessRealMeaning(query, limit))
                .msg("获取成功")
                .build(JsonBody.OK);
    }

    @RequestMapping("getQueryResult")
    public JsonBody getChapterList(@RequestParam String query, HttpServletRequest request) {
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
        logger.info("execute query for [{}] and resolved as [{}]", query, character.getName());
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

}
