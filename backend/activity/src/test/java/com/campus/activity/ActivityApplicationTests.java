package com.campus.activity;

import com.campus.activity.common.AuthContext;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.Role;
import com.campus.activity.model.dto.CheckInRequest;
import com.campus.activity.model.dto.FeedbackRequest;
import com.campus.activity.model.vo.CheckInCodeVO;
import com.campus.activity.model.vo.CheckInResultVO;
import com.campus.activity.model.vo.RegistrationActionVO;
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

		AuthContext.set(student(fixture.studentOneId(), "测试学生1"));
		RegistrationActionVO enrolled = registrationService.enroll(fixture.activityId());
		assertThat(enrolled.registrationStatus()).isEqualTo("ENROLLED");

		AuthContext.set(student(fixture.studentTwoId(), "测试学生2"));
		RegistrationActionVO waitlisted = registrationService.enroll(fixture.activityId());
		assertThat(waitlisted.registrationStatus()).isEqualTo("WAITLISTED");
		assertThat(waitlisted.queueNo()).isEqualTo(1);

		AuthContext.set(student(fixture.studentOneId(), "测试学生1"));
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

		AuthContext.set(student(fixture.studentOneId(), "测试学生1"));
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

		AuthContext.set(student(fixture.studentOneId(), "测试学生1"));
		feedbackService.submit(fixture.activityId(), new FeedbackRequest(5, "流程顺畅，活动体验很好"));
		feedbackService.submit(fixture.activityId(), new FeedbackRequest(3, "签到顺畅，但场地指引可以更清楚"));

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

		AuthContext.set(student(fixture.studentOneId(), "测试学生1"));
		assertThatThrownBy(() -> registrationService.enroll(fixture.activityId()))
				.isInstanceOf(BusinessException.class)
				.hasMessage("报名已截止");
	}

	@Test
	void studentCannotMarkAbsences() {
		TestFixture fixture = createFixture(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(2), 5);

		AuthContext.set(student(fixture.studentOneId(), "测试学生1"));
		assertThatThrownBy(() -> registrationService.markAbsences(fixture.activityId()))
				.isInstanceOf(BusinessException.class)
				.hasMessage("当前角色无权限");
	}

	@Test
	void organizerCannotManageOthersActivityRegistrations() {
		TestFixture fixture = createFixture(LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(1).plusHours(2), 5);
		int otherOrganizerId = insertUser("ORGANIZER", "测试其他组织者", null, "155" + Long.toString(System.nanoTime()).substring(0, 8));

		AuthContext.set(organizer(otherOrganizerId));
		assertThatThrownBy(() -> registrationService.activityRegistrations(fixture.activityId(), 1, 10, null))
				.isInstanceOf(BusinessException.class)
				.hasMessage("只能查看自己活动的名单");
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
				""", "测试校区-" + suffix, "测试位置");
		int venueId = insertAndTrack(venueIds, """
				INSERT INTO Venue(venue_name, room_number, capacity, campus_id)
				VALUES (?, ?, ?, ?)
				""", "测试场馆", "T-" + suffix, capacity, campusId);
		int categoryId = insertAndTrack(categoryIds, """
				INSERT INTO Category(category_name)
				VALUES (?)
				""", "测试分类-" + suffix);
		int organizerId = insertUser("ORGANIZER", "测试组织者-" + suffix, null, "188" + suffix.substring(0, 8));
		int studentOneId = insertUser("STUDENT", "测试学生1-" + suffix, "S" + suffix + "1", "177" + suffix.substring(0, 8));
		int studentTwoId = insertUser("STUDENT", "测试学生2-" + suffix, "S" + suffix + "2", "166" + suffix.substring(0, 8));
		int activityId = insertAndTrack(activityIds, """
				INSERT INTO Activity(title, venue_id, category_id, start_time, end_time, enroll_deadline,
				                     capacity_limit, current_enrollment, description, status, organizer_id)
				VALUES (?, ?, ?, ?, ?, ?, ?, 0, ?, 'PUBLISHED', ?)
				""", "测试活动-" + suffix, venueId, categoryId, startTime, endTime,
				startTime.minusHours(1), capacity, "集成测试活动", organizerId);
		return new TestFixture(activityId, organizerId, studentOneId, studentTwoId);
	}

	private int insertUser(String role, String username, String studentNo, String phone) {
		int userId = insertAndTrack(userIds, """
				INSERT INTO User(role, username, student_no, password, phone)
				VALUES (?, ?, ?, '123456', ?)
				""", role, username, studentNo, phone);
		return userId;
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

	private CurrentUser student(int userId, String name) {
		return new CurrentUser(userId, name, Role.STUDENT, "TEST-" + userId, "1" + userId);
	}

	private CurrentUser organizer(int userId) {
		return new CurrentUser(userId, "测试组织者", Role.ORGANIZER, null, "1" + userId);
	}

	private record TestFixture(int activityId, int organizerId, int studentOneId, int studentTwoId) {
	}

}
