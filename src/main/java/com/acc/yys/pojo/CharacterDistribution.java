package com.acc.yys.pojo;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by zhaoyy on 2017/5/23.
 */
public final class CharacterDistribution implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final char split = '/';
    private static final char mul = '×';

    private final String chapterName;
    private final String battleName;
    private final String roundName;
    private final String characterName;
    private final int count;

    public CharacterDistribution(String chapterName,
                                 String battleName,
                                 String roundName,
                                 String characterName,
                                 int count) {
        this.chapterName = chapterName;
        this.battleName = battleName;
        this.roundName = roundName;
        this.characterName = characterName;
        this.count = count;
    }

    public String getChapterName() {
        return chapterName;
    }

    public String getBattleName() {
        return battleName;
    }

    public String getRoundName() {
        return roundName;
    }

    public String getCharacterName() {
        return characterName;
    }

    public int getCount() {
        return count;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("chapterName", chapterName)
                .add("battleName", battleName)
                .add("roundName", roundName)
                .add("characterName", characterName)
                .add("count", count)
                .toString();
    }

    public String getChapterPath(boolean includeChapterName) {
        StringBuilder builder = includeChapterName ? new StringBuilder(chapterName).append(split)
                : new StringBuilder();
        return builder.append(battleName)
                .append(split)
                .append(roundName)
                .append(':')
                .append(chapterName)
                .append(mul)
                .append(count)
                .toString();

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CharacterDistribution that = (CharacterDistribution) o;

        if (count != that.count) return false;
        if (!chapterName.equals(that.chapterName)) return false;
        if (!battleName.equals(that.battleName)) return false;
        if (!roundName.equals(that.roundName)) return false;
        return characterName.equals(that.characterName);

    }

    @Override
    public int hashCode() {
        int result = chapterName.hashCode();
        result = 31 * result + battleName.hashCode();
        result = 31 * result + roundName.hashCode();
        result = 31 * result + characterName.hashCode();
        result = 31 * result + count;
        return result;
    }
}
