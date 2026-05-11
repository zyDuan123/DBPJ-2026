package com.campus.activity.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.activity.model.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("""
            SELECT user_id AS userId, username, role, student_no AS studentNo, password, phone
            FROM User
            WHERE (student_no = #{login} OR phone = #{login} OR username = #{login})
            LIMIT 1
            """)
    User findByLogin(@Param("login") String login);

    @Select("""
            SELECT user_id AS userId, username, role, student_no AS studentNo, phone
            FROM User
            WHERE user_id = #{userId}
            """)
    User findProfile(@Param("userId") int userId);
}
