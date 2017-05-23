package com.acc.yys.pojo;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class Chapter implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final String name;
    private final int index;
    private final List<Battle> battles;


    public Chapter(int id, String name, int index, List<Battle> battles) {
        this.id = id;
        this.name = name;
        this.index = index;
        this.battles = battles;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public List<Battle> getBattles() {
        return battles;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("name", name)
                .add("index", index)
                .add("battles", battles)
                .toString();
    }
}
