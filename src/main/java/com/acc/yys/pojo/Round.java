package com.acc.yys.pojo;

import com.google.common.base.MoreObjects;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class Round implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final int number;
    private final List<Character> characters;

    public Round(int id, int number, List<Character> characters) {
        this.id = id;
        this.number = number;
        this.characters = characters;
    }

    public int getId() {
        return id;
    }

    public int getNumber() {
        return number;
    }

    public List<Character> getCharacters() {
        return characters;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("number", number)
                .add("characters", characters)
                .toString();
    }
}
