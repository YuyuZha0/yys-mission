package com.acc.yys.service;

import com.acc.yys.pojo.JsonBody;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by zhaoyy on 2017/5/27.
 */
public interface MessageService {

    JsonBody leaveMsg(String name, String email, String msg, HttpServletRequest request);
}
