package com.acc.yys.service.impl;

import com.acc.yys.dao.MessageDao;
import com.acc.yys.pojo.JsonBody;
import com.acc.yys.service.MessageService;
import com.acc.yys.util.HttpRequestUtils;
import com.google.common.base.CharMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by zhaoyy on 2017/5/27.
 */
@Service
public final class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);
    private static final String MSG_COUNT_SESSION_KEY = "USER_MESSAGE_COUNT";

    private final MessageDao messageDao;

    public MessageServiceImpl(@Autowired MessageDao messageDao) {
        this.messageDao = messageDao;
    }

    @Override
    public JsonBody leaveMsg(String name, String email, String msg, HttpServletRequest request) {
        if (msg == null || msg.isEmpty())
            return JsonBody.builder()
                    .msg("请输入留言")
                    .build(JsonBody.ILLEGAL_PARAMETER);
        name = escapeWhitespace(name);
        if (name.isEmpty())
            name = HttpRequestUtils.getRequestIp(request);
        email = escapeWhitespace(email);
        if (name.length() > 255 || email.length() > 255 || msg.length() > 65535)
            return JsonBody.builder()
                    .msg("文本长度超过最大限制")
                    .build(JsonBody.ILLEGAL_PARAMETER);

        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(60 * 60 * 12);
        AtomicInteger msgCount = (AtomicInteger) session.getAttribute(MSG_COUNT_SESSION_KEY);
        if (msgCount == null) {
            msgCount = new AtomicInteger(0);
        }
        if (msgCount.getAndIncrement() > 5) {
            return JsonBody.builder()
                    .msg("超过最大留言次数")
                    .build(JsonBody.FORBIDDEN);
        }

        messageDao.insertMsg(name, email, msg);

        session.setAttribute(MSG_COUNT_SESSION_KEY, msgCount);

        return JsonBody.builder()
                .msg("感谢您的留言！")
                .build(JsonBody.OK);
    }

    private static String escapeWhitespace(String text) {
        if (text == null)
            return "";
        return CharMatcher
                .whitespace()
                .removeFrom(text);
    }
}
