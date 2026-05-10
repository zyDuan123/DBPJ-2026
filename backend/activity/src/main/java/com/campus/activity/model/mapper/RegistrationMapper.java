package com.campus.activity.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.activity.model.entity.Registration;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Map;

@Mapper
public interface RegistrationMapper extends BaseMapper<Registration> {
    @Select("""
            SELECT r.registration_id AS registrationId, r.student_id AS studentId,
                   r.status, a.end_time AS endTime
            FROM Registration r
            JOIN Activity a ON r.activity_id = a.activity_id
            WHERE r.registration_id = #{registrationId}
            """)
    Map<String, Object> findCheckInCodeTarget(@Param("registrationId") int registrationId);

    @Select("""
            SELECT r.registration_id AS registrationId, r.status, a.organizer_id AS organizerId
            FROM Registration r
            JOIN Activity a ON r.activity_id = a.activity_id
            WHERE r.registration_id = #{registrationId}
            FOR UPDATE
            """)
    Map<String, Object> findCheckInTargetForUpdate(@Param("registrationId") int registrationId);

    @Update("""
            UPDATE Registration
            SET status = 'CHECKED_IN', check_in_time = CURRENT_TIMESTAMP
            WHERE registration_id = #{registrationId}
            """)
    int markCheckedIn(@Param("registrationId") int registrationId);
}
