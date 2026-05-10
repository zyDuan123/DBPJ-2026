package com.campus.activity.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.activity.model.entity.Category;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
    @Select("""
            SELECT category_id AS id, category_name AS categoryName
            FROM Category
            ORDER BY category_id
            """)
    List<Map<String, Object>> listCategories();

    @Insert("INSERT INTO Category(category_name) VALUES (#{categoryName})")
    int createCategory(@Param("categoryName") String categoryName);
}
