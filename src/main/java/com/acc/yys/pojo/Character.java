package com.acc.yys.pojo;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class Character implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final String imgsrc;


    public Character(int id, String name, String imgsrc) {
        this.id = id;
        this.name = name;
        this.imgsrc = imgsrc;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImgsrc() {
        return imgsrc;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("imgsrc", imgsrc)
                .toString();
    }
}
