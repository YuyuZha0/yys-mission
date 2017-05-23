package com.acc.yys.pojo;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class Battle implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final int index;
    private final List<Round> rounds;

    public Battle(int id, int index, List<Round> rounds) {
        this.id = id;
        this.index = index;
        this.rounds = rounds;
    }

    public int getId() {
        return id;
    }

    public int getIndex() {
        return index;
    }

    public List<Round> getRounds() {
        return rounds;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("index", index)
                .add("rounds", rounds)
                .toString();
    }
}
