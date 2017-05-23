package com.acc.yys.controller;

import com.acc.yys.util.HttpRequestUtils;
import com.acc.yys.util.SimpleTemplate;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Created by zhaoyy on 2017/5/19.
 */
@Controller
public final class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(IndexController.class);

    private static final String UNIQUE_PUBLISH_ID = UUID.randomUUID()
            .toString().replace("-", "").toLowerCase();

    @Value("${index.cache.enable}")
    private boolean enableCache;

    private volatile SimpleTemplate template = null;

    @ResponseBody
    @RequestMapping("/index")
    public String welcome2Index(HttpServletRequest request) {
        String ip = HttpRequestUtils.getRequestIp(request);
        logger.info("record access from [{}] on [{}]", ip, LocalDateTime.now().toString());
        if (!enableCache) {
            Map<String, String> args = ImmutableMap.of("version", UNIQUE_PUBLISH_ID,
                    "context", request.getContextPath());
            SimpleTemplate template = readFromFileSystem(request);
            return template.render(args);
        }
        if (template == null) {
            synchronized (this) {
                if (template == null) {
                    template = readFromFileSystem(request);
                }
            }
        }
        Map<String, String> args = ImmutableMap.of("version", UNIQUE_PUBLISH_ID,
                "context", request.getContextPath());
        return template.render(args);
    }

    private static SimpleTemplate readFromFileSystem(HttpServletRequest request) {
        String root = request.getServletContext().getRealPath("/");
        StringBuilder builder = new StringBuilder(root);
        if (!root.endsWith(File.separator))
            builder.append(File.separator);
        String indexPath = builder.append("WEB-INF")
                .append(File.separator)
                .append("index.html")
                .toString();

        try (InputStream is = new FileInputStream(indexPath)) {
            return new SimpleTemplate(is);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return new SimpleTemplate("");
    }
}
