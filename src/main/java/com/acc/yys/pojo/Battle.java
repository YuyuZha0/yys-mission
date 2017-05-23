package com.acc.yys.pojo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class Battle implements Serializable {

    private static final long serialVersionUID = 1L;

    private final int id;
    private final List<Round> rounds;

    public Battle(int id,List<Round> rounds){
        this.id = id;
        this.rounds = rounds;
    }
}
