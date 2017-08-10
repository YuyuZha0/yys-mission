package com.acc.yys.pojo;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class JsonBody {

    public static final int OK = 0;
    public static final int SERVER_ERROR = 1;
    public static final int ILLEGAL_PARAMETER = 1 << 1;
    public static final int ILLEGAL_ACCESS = 1 << 2;
    public static final int PROCESSING = 1 << 3;
    public static final int NOT_FOUND = 1 << 4;
    public static final int FORBIDDEN = 1 << 5;
    private final int status;
    private final String msg;
    private final Map<String, Object> data;

    private JsonBody(int status, String msg, Map<String, Object> data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public static class Builder {

        private String msg = "";
        private ImmutableMap.Builder<String, Object> data = ImmutableMap.builder();

        public Builder msg(String msg) {
            this.msg = msg;
            return this;
        }

        public Builder append(String key, Object value) {
            if (key != null)
                data.put(key, value);
            return this;
        }

        public Builder appendAll(Map<String, ?> map) {
            data.putAll(map);
            return this;
        }

        public JsonBody build(int status) {
            return new JsonBody(status, msg, data.build());
        }
    }

}
