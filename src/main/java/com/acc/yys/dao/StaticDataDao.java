package com.acc.yys.dao;

import com.acc.yys.pojo.CharacterDistribution;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * Created by zhaoyy on 2017/8/10.
 */
public interface StaticDataDao {


    @Insert("insert into tb_character (character_name,image_name,quality,update_time) values " +
            "(#{name},#{imageName},#{quality},now());")
    void insertCharacter(com.acc.yys.pojo.Character character);

    @Insert("insert into tb_character_distribution (chapter_name,battle_name,round_name,character_name,character_count,update_time) values " +
            "(#{chapterName},#{battleName},#{roundName},#{characterName},#{count},now());")
    void insertCharacterDistribution(CharacterDistribution characterDistribution);

    @Insert("insert into tb_tip (content,ref,update_time) values (#{content},#{ref},now());")
    void insertTip(@Param("content") String content, @Param("ref") String ref);

    @Select("select character_name,image_name,quality from tb_character;")
    List<Map<String, Object>> queryCharacterList();

    @Select("select chapter_name,battle_name,round_name,character_name,character_count from tb_character_distribution;")
    List<Map<String, Object>> queryCharacterDistributionList();

    @Select("select content,ref from tb_tip;")
    List<Map<String, Object>> queryTipList();
}
