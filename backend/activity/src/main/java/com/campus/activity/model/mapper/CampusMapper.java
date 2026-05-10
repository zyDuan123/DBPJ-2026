package com.campus.activity.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.activity.model.entity.Campus;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CampusMapper extends BaseMapper<Campus> {
    @Select("""
            SELECT campus_id AS id, campus_name AS campusName, location
            FROM Campus
            ORDER BY campus_id
            """)
    List<Map<String, Object>> listCampuses();

    @Insert("INSERT INTO Campus(campus_name, location) VALUES (#{campusName}, #{location})")
    int createCampus(@Param("campusName") String campusName, @Param("location") String location);
}
