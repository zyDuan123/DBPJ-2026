package com.campus.activity.model.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.activity.model.entity.ActivityFeedback;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

@Mapper
public interface ActivityFeedbackMapper extends BaseMapper<ActivityFeedback> {
    @Select("""
            SELECT f.feedback_id AS feedbackId, f.rating, f.content,
                   f.created_at AS createdAt, f.updated_at AS updatedAt
            FROM ActivityFeedback f
            WHERE f.student_id = #{studentId} AND f.activity_id = #{activityId}
            """)
    List<Map<String, Object>> findMine(@Param("studentId") int studentId, @Param("activityId") int activityId);

    @Select("""
            SELECT COUNT(*) AS feedbackCount,
                   COALESCE(ROUND(AVG(rating), 1), 0) AS averageRating,
                   SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END) AS positiveCount,
                   SUM(CASE WHEN rating <= 3 THEN 1 ELSE 0 END) AS lowRatingCount
            FROM ActivityFeedback
            WHERE activity_id = #{activityId}
            """)
    Map<String, Object> activitySummary(@Param("activityId") int activityId);

    @Select("""
            SELECT COUNT(*) AS feedbackCount,
                   COALESCE(ROUND(AVG(rating), 1), 0) AS averageRating,
                   SUM(CASE WHEN rating >= 4 THEN 1 ELSE 0 END) AS positiveCount,
                   SUM(CASE WHEN rating <= 3 THEN 1 ELSE 0 END) AS lowRatingCount
            FROM ActivityFeedback
            """)
    Map<String, Object> globalSummary();

    @Select("""
            SELECT f.feedback_id AS feedbackId, u.username AS studentName, u.student_no AS studentNo,
                   f.rating, f.content, f.created_at AS createdAt, f.updated_at AS updatedAt
            FROM ActivityFeedback f
            JOIN User u ON f.student_id = u.user_id
            WHERE f.activity_id = #{activityId}
            ORDER BY f.updated_at DESC
            LIMIT #{offset}, #{size}
            """)
    List<Map<String, Object>> findActivityRecords(@Param("activityId") int activityId,
                                                  @Param("offset") int offset,
                                                  @Param("size") int size);

    @Select("""
            SELECT COUNT(*)
            FROM ActivityFeedback
            WHERE activity_id = #{activityId}
            """)
    Long countActivityRecords(@Param("activityId") int activityId);

    @Select("""
            SELECT f.feedback_id AS feedbackId, u.username AS studentName, u.student_no AS studentNo,
                   f.rating, f.content, f.created_at AS createdAt, f.updated_at AS updatedAt
            FROM ActivityFeedback f
            JOIN User u ON f.student_id = u.user_id
            WHERE f.activity_id = #{activityId}
              AND f.rating <= #{lowRatingThreshold}
            ORDER BY f.updated_at DESC
            LIMIT #{offset}, #{size}
            """)
    List<Map<String, Object>> findLowRatingActivityRecords(@Param("activityId") int activityId,
                                                           @Param("lowRatingThreshold") int lowRatingThreshold,
                                                           @Param("offset") int offset,
                                                           @Param("size") int size);

    @Select("""
            SELECT COUNT(*)
            FROM ActivityFeedback
            WHERE activity_id = #{activityId}
              AND rating <= #{lowRatingThreshold}
            """)
    Long countLowRatingActivityRecords(@Param("activityId") int activityId,
                                       @Param("lowRatingThreshold") int lowRatingThreshold);

    @Select("""
            SELECT a.activity_id AS activityId, a.title,
                   COUNT(f.feedback_id) AS feedbackCount,
                   COALESCE(ROUND(AVG(f.rating), 1), 0) AS averageRating
            FROM Activity a
            LEFT JOIN ActivityFeedback f ON a.activity_id = f.activity_id
            GROUP BY a.activity_id, a.title
            HAVING feedbackCount > 0
            ORDER BY averageRating DESC, feedbackCount DESC
            LIMIT 5
            """)
    List<Map<String, Object>> topActivities();

    @Select("""
            SELECT rating, COUNT(*) AS count
            FROM ActivityFeedback
            WHERE activity_id = #{activityId}
            GROUP BY rating
            """)
    List<Map<String, Object>> activityRatingDistribution(@Param("activityId") int activityId);

    @Select("""
            SELECT rating, COUNT(*) AS count
            FROM ActivityFeedback
            GROUP BY rating
            """)
    List<Map<String, Object>> globalRatingDistribution();

    @Select("""
            SELECT content
            FROM ActivityFeedback
            WHERE activity_id = #{activityId}
              AND content IS NOT NULL
              AND TRIM(content) <> ''
            """)
    List<String> activityFeedbackContents(@Param("activityId") int activityId);

    @Select("""
            SELECT content
            FROM ActivityFeedback
            WHERE content IS NOT NULL
              AND TRIM(content) <> ''
            """)
    List<String> globalFeedbackContents();

    @Select("""
            SELECT registration_id, status
            FROM Registration
            WHERE student_id = #{studentId} AND activity_id = #{activityId}
            """)
    List<Map<String, Object>> findRegistration(@Param("studentId") int studentId, @Param("activityId") int activityId);

    @Select("""
            SELECT feedback_id
            FROM ActivityFeedback
            WHERE registration_id = #{registrationId}
            """)
    List<Map<String, Object>> findByRegistration(@Param("registrationId") int registrationId);

    @Insert("""
            INSERT INTO ActivityFeedback(registration_id, activity_id, student_id, rating, content)
            VALUES (#{registrationId}, #{activityId}, #{studentId}, #{rating}, #{content})
            """)
    int insertFeedback(@Param("registrationId") int registrationId,
                       @Param("activityId") int activityId,
                       @Param("studentId") int studentId,
                       @Param("rating") int rating,
                       @Param("content") String content);

    @Update("""
            UPDATE ActivityFeedback
            SET rating = #{rating}, content = #{content}, updated_at = CURRENT_TIMESTAMP
            WHERE registration_id = #{registrationId}
            """)
    int updateFeedback(@Param("registrationId") int registrationId,
                       @Param("rating") int rating,
                       @Param("content") String content);

    @Select("SELECT organizer_id FROM Activity WHERE activity_id = #{activityId}")
    Integer findActivityOwner(@Param("activityId") int activityId);
}
