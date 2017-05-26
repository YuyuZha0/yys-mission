package com.acc.yys.pojo;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class Character implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String name;
    private final String imageName;
    private final String quality;

    public Character(String name, String imageName, String quality) {
        this.name = name;
        this.imageName = imageName;
        this.quality = quality;
    }

    public String getName() {
        return name;
    }

    public String getImageName() {
        return imageName;
    }

    public String getQuality() {
        return quality;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("name", name)
                .add("imageName", imageName)
                .add("quality", quality)
                .toString();
    }
}
