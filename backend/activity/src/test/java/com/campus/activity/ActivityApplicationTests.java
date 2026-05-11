package com.campus.activity;

import com.campus.activity.common.AuthContext;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.Role;
import com.campus.activity.model.dto.ActivityRequest;
import com.campus.activity.model.dto.CheckInRequest;
import com.campus.activity.model.dto.FeedbackRequest;
import com.campus.activity.model.dto.ReviewRequest;
import com.campus.activity.model.vo.CheckInCodeVO;
import com.campus.activity.model.vo.CheckInResultVO;
import com.campus.activity.model.vo.RegistrationActionVO;
import com.campus.activity.service.ActivityService;
import com.campus.activity.service.CheckInService;
import com.campus.activity.service.FeedbackService;
import com.campus.activity.service.RegistrationService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ActivityApplicationTests {
    private final List<Integer> activityIds = new ArrayList<>();
    private final List<Integer> venueIds = new ArrayList<>();
    private final List<Integer> categoryIds = new ArrayList<>();
    private final List<Integer> campusIds = new ArrayList<>();
    private final List<Integer> userIds = new ArrayList<>();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private FeedbackService feedbackService;

    @Test
    void contextLoads() {
    }

    @Test
    void enrollmentWaitlistAndPromotionFlow() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 1);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        RegistrationActionVO enrolled = registrationService.enroll(fixture.activityId());
        assertThat(enrolled.registrationStatus()).isEqualTo("ENROLLED");

        AuthContext.set(student(fixture.studentTwoId(), "student-two"));
        RegistrationActionVO waitlisted = registrationService.enroll(fixture.activityId());
        assertThat(waitlisted.registrationStatus()).isEqualTo("WAITLISTED");
        assertThat(waitlisted.queueNo()).isEqualTo(1);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        RegistrationActionVO cancelled = registrationService.cancel(enrolled.registrationId());
        assertThat(cancelled.registrationStatus()).isEqualTo("CANCELLED");
        assertThat(cancelled.promotedRegistrationId()).isEqualTo(waitlisted.registrationId());

        Map<String, Object> promoted = jdbcTemplate.queryForMap("""
                SELECT status, queue_no, activity_id
                FROM Registration
                WHERE registration_id = ?
                """, waitlisted.registrationId());
        assertThat(promoted.get("status")).isEqualTo("ENROLLED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT current_enrollment FROM Activity WHERE activity_id = ?",
                Integer.class,
                fixture.activityId()
        )).isEqualTo(1);
    }

    @Test
    void checkInIsIdempotentAndFeedbackCanBeUpdated() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        RegistrationActionVO enrolled = registrationService.enroll(fixture.activityId());
        int registrationId = enrolled.registrationId();
        CheckInCodeVO code = checkInService.code(registrationId);

        AuthContext.set(organizer(fixture.organizerId()));
        CheckInResultVO firstCheckIn = checkInService.checkIn(new CheckInRequest(code.checkInCode()));
        CheckInResultVO secondCheckIn = checkInService.checkIn(new CheckInRequest(code.checkInCode()));
        assertThat(firstCheckIn.registrationStatus()).isEqualTo("CHECKED_IN");
        assertThat(secondCheckIn.registrationStatus()).isEqualTo("CHECKED_IN");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM CreditRecord
                WHERE registration_id = ? AND reason_type = 'CHECK_IN'
                """, Integer.class, registrationId)).isEqualTo(1);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        feedbackService.submit(fixture.activityId(), new FeedbackRequest(5, "smooth activity"));
        feedbackService.submit(fixture.activityId(), new FeedbackRequest(3, "smooth check-in but venue guide can improve"));

        Map<String, Object> feedback = jdbcTemplate.queryForMap("""
                SELECT COUNT(*) AS feedbackCount, MAX(rating) AS rating
                FROM ActivityFeedback
                WHERE registration_id = ?
                """, registrationId);
        assertThat(((Number) feedback.get("feedbackCount")).intValue()).isEqualTo(1);
        assertThat(((Number) feedback.get("rating")).intValue()).isEqualTo(3);
    }

    @Test
    void markAbsencesOnlyWritesCreditOnce() {
        TestFixture fixture = createFixture(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2), 5);

        int registrationId = insertAndTrack(new ArrayList<>(), """
                INSERT INTO Registration(student_id, activity_id, status)
                VALUES (?, ?, 'ENROLLED')
                """, fixture.studentOneId(), fixture.activityId());
        jdbcTemplate.update("""
                UPDATE Activity
                SET current_enrollment = 1
                WHERE activity_id = ?
                """, fixture.activityId());

        AuthContext.set(organizer(fixture.organizerId()));
        RegistrationActionVO firstMark = registrationService.markAbsences(fixture.activityId());
        RegistrationActionVO secondMark = registrationService.markAbsences(fixture.activityId());

        assertThat(firstMark.absentCount()).isEqualTo(1);
        assertThat(secondMark.absentCount()).isEqualTo(0);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM Registration WHERE registration_id = ?",
                String.class,
                registrationId
        )).isEqualTo("ABSENT");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM CreditRecord
                WHERE registration_id = ? AND reason_type = 'ABSENT'
                """, Integer.class, registrationId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COALESCE(SUM(change_value), 0)
                FROM CreditRecord
                WHERE registration_id = ?
                """, Integer.class, registrationId)).isEqualTo(-10);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT current_enrollment FROM Activity WHERE activity_id = ?",
                Integer.class,
                fixture.activityId()
        )).isEqualTo(0);
    }

    @Test
    void enrollmentRejectsClosedActivity() {
        TestFixture fixture = createFixture(LocalDateTime.now().minusHours(5), LocalDateTime.now().minusHours(3), 5);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        assertBusinessCode(() -> registrationService.enroll(fixture.activityId()), 40904);
    }

    @Test
    void studentCannotMarkAbsences() {
        TestFixture fixture = createFixture(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2), 5);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        assertBusinessCode(() -> registrationService.markAbsences(fixture.activityId()), 40301);
    }

    @Test
    void organizerCannotManageOthersActivityRegistrations() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);
        int otherOrganizerId = insertUser("ORGANIZER", "other-organizer", null, uniquePhone("155"));

        AuthContext.set(organizer(otherOrganizerId));
        assertBusinessCode(() -> registrationService.activityRegistrations(fixture.activityId(), 1, 10, null), 40301);
    }

    @Test
    void studentCannotCancelOthersRegistration() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        RegistrationActionVO enrolled = registrationService.enroll(fixture.activityId());

        AuthContext.set(student(fixture.studentTwoId(), "student-two"));
        assertBusinessCode(() -> registrationService.cancel(enrolled.registrationId()), 40301);
    }

    @Test
    void checkedInRegistrationCannotBeCancelled() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        RegistrationActionVO enrolled = registrationService.enroll(fixture.activityId());
        CheckInCodeVO code = checkInService.code(enrolled.registrationId());

        AuthContext.set(organizer(fixture.organizerId()));
        checkInService.checkIn(new CheckInRequest(code.checkInCode()));

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        assertBusinessCode(() -> registrationService.cancel(enrolled.registrationId()), 40903);
    }

    @Test
    void feedbackRejectsActivityWithoutCheckedInRegistration() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        registrationService.enroll(fixture.activityId());

        assertBusinessCode(() -> feedbackService.submit(fixture.activityId(), new FeedbackRequest(4, "not checked in")), 40903);
    }

    @Test
    void studentCannotGenerateOthersCheckInCode() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(student(fixture.studentOneId(), "student-one"));
        RegistrationActionVO enrolled = registrationService.enroll(fixture.activityId());

        AuthContext.set(student(fixture.studentTwoId(), "student-two"));
        assertBusinessCode(() -> checkInService.code(enrolled.registrationId()), 40301);
    }

    @Test
    void checkInRejectsMalformedCode() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(organizer(fixture.organizerId()));
        assertBusinessCode(() -> checkInService.checkIn(new CheckInRequest("malformed-code")), 40001);
    }

    @Test
    void organizerCannotUpdatePublishedActivity() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(organizer(fixture.organizerId()));
        assertBusinessCode(() -> activityService.update(fixture.activityId(), validActivityRequest(fixture)), 40903);
    }

    @Test
    void reviewRejectsInvalidStateAndInvalidResult() {
        TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);

        AuthContext.set(admin());
        assertBusinessCode(() -> activityService.review(fixture.activityId(), new ReviewRequest("APPROVED", null)), 40903);

        jdbcTemplate.update("UPDATE Activity SET status = 'PENDING_REVIEW' WHERE activity_id = ?", fixture.activityId());
        assertBusinessCode(() -> activityService.review(fixture.activityId(), new ReviewRequest("UNKNOWN", null)), 40001);
    }

    @AfterEach
    void cleanUp() {
        AuthContext.clear();
        for (Integer activityId : activityIds) {
            jdbcTemplate.update("DELETE FROM ActivityFeedback WHERE activity_id = ?", activityId);
            jdbcTemplate.update("DELETE FROM CreditRecord WHERE activity_id = ?", activityId);
            jdbcTemplate.update("DELETE FROM Registration WHERE activity_id = ?", activityId);
        }
        for (Integer activityId : activityIds) {
            jdbcTemplate.update("DELETE FROM Activity WHERE activity_id = ?", activityId);
        }
        for (Integer venueId : venueIds) {
            jdbcTemplate.update("DELETE FROM Venue WHERE venue_id = ?", venueId);
        }
        for (Integer categoryId : categoryIds) {
            jdbcTemplate.update("DELETE FROM Category WHERE category_id = ?", categoryId);
        }
        for (Integer campusId : campusIds) {
            jdbcTemplate.update("DELETE FROM Campus WHERE campus_id = ?", campusId);
        }
        for (Integer userId : userIds) {
            jdbcTemplate.update("DELETE FROM User WHERE user_id = ?", userId);
        }
        activityIds.clear();
        venueIds.clear();
        categoryIds.clear();
        campusIds.clear();
        userIds.clear();
    }

    private TestFixture createFixture(LocalDateTime startTime, LocalDateTime endTime, int capacity) {
        String suffix = Long.toString(System.nanoTime());
        int campusId = insertAndTrack(campusIds, """
                INSERT INTO Campus(campus_name, location)
                VALUES (?, ?)
                """, "test-campus-" + suffix, "test-location");
        int venueId = insertAndTrack(venueIds, """
                INSERT INTO Venue(venue_name, room_number, capacity, campus_id)
                VALUES (?, ?, ?, ?)
                """, "test-venue", "T-" + suffix, capacity, campusId);
        int categoryId = insertAndTrack(categoryIds, """
                INSERT INTO Category(category_name)
                VALUES (?)
                """, "test-category-" + suffix);
        int organizerId = insertUser("ORGANIZER", "test-organizer-" + suffix, null, uniquePhone("188"));
        int studentOneId = insertUser("STUDENT", "test-student-one-" + suffix, "S" + suffix + "1", uniquePhone("177"));
        int studentTwoId = insertUser("STUDENT", "test-student-two-" + suffix, "S" + suffix + "2", uniquePhone("166"));
        int activityId = insertAndTrack(activityIds, """
                INSERT INTO Activity(title, venue_id, category_id, start_time, end_time, enroll_deadline,
                                     capacity_limit, current_enrollment, description, status, organizer_id)
                VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, 'PUBLISHED', ?)
                """, "test-activity-" + suffix, venueId, categoryId, startTime, endTime,
                startTime.minusHours(1), capacity, "integration test activity", organizerId);
        return new TestFixture(activityId, organizerId, studentOneId, studentTwoId, venueId, categoryId);
    }

    private int insertUser(String role, String username, String studentNo, String phone) {
        return insertAndTrack(userIds, """
                INSERT INTO User(role, username, student_no, password, phone)
                VALUES (?, ?, ?, '123456', ?)
                """, role, username, studentNo, phone);
    }

    private int insertAndTrack(List<Integer> target, String sql, Object... args) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < args.length; i++) {
                ps.setObject(i + 1, args[i]);
            }
            return ps;
        }, keyHolder);
        int id = keyHolder.getKey().intValue();
        target.add(id);
        return id;
    }

    private String uniquePhone(String prefix) {
        return prefix + Long.toString(System.nanoTime()).substring(0, 8);
    }

    private CurrentUser student(int userId, String name) {
        return new CurrentUser(userId, name, Role.STUDENT, "TEST-" + userId, "1" + userId);
    }

    private CurrentUser organizer(int userId) {
        return new CurrentUser(userId, "test-organizer", Role.ORGANIZER, null, "1" + userId);
    }

    private CurrentUser admin() {
        return new CurrentUser(1, "test-admin", Role.ADMIN, null, "10000000000");
    }

    private ActivityRequest validActivityRequest(TestFixture fixture) {
        LocalDateTime startTime = LocalDateTime.now().plusDays(2);
        return new ActivityRequest(
                "updated activity",
                fixture.venueId(),
                fixture.categoryId(),
                startTime,
                startTime.plusHours(2),
                startTime.minusHours(1),
                5,
                null,
                "updated description"
        );
    }

    private void assertBusinessCode(ThrowingOperation operation, int expectedCode) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getCode()).isEqualTo(expectedCode));
    }

    @FunctionalInterface
    private interface ThrowingOperation {
        void run();
    }

    private record TestFixture(int activityId, int organizerId, int studentOneId, int studentTwoId,
                               int venueId, int categoryId) {
    }
}
