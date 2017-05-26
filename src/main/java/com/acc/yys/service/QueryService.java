package com.acc.yys.service;

import com.acc.yys.pojo.Character;

import java.util.List;

/**
 * Created by zhaoyy on 2017/5/26.
 */
public interface QueryService {


    class QueryResult {

    }

    List<QueryResult> queryChapter(Character character);
}
