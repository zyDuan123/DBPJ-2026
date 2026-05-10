package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.BusinessException;
import com.campus.activity.common.CurrentUser;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.Role;
import com.campus.activity.model.dto.FeedbackRequest;
import com.campus.activity.model.mapper.ActivityFeedbackMapper;
import com.campus.activity.model.vo.FeedbackBoardVO;
import com.campus.activity.model.vo.FeedbackMineVO;
import com.campus.activity.model.vo.FeedbackOverviewVO;
import com.campus.activity.model.vo.FeedbackRecordVO;
import com.campus.activity.model.vo.FeedbackSubmitVO;
import com.campus.activity.model.vo.FeedbackSummaryVO;
import com.campus.activity.model.vo.FeedbackTopActivityVO;
import com.campus.activity.model.vo.KeywordVO;
import com.campus.activity.model.vo.RatingDistributionVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Transactional(readOnly = true)
public class FeedbackService {
    private static final int LOW_RATING_THRESHOLD = 3;
    private static final int KEYWORD_LIMIT = 8;
    private static final Pattern KEYWORD_SPLITTER = Pattern.compile("[\\s,，。.!！?？;；:：、（）()【】\\[\\]《》<>\"'“”‘’]+");
    private static final List<String> STOP_WORDS = List.of(
            "活动", "感觉", "比较", "一个", "这个", "可以", "还是", "没有", "不是", "我们", "他们", "非常",
            "有点", "希望", "建议", "整体", "参加", "体验", "不错", "很好"
    );
    private static final List<String> BUSINESS_KEYWORDS = List.of(
            "场地", "时间", "流程", "组织", "签到", "内容", "互动", "通知", "设备", "座位",
            "老师", "志愿者", "排队", "交通", "收获", "讲解", "秩序", "体验"
    );

    private final ActivityFeedbackMapper feedbackMapper;

    public FeedbackService(ActivityFeedbackMapper feedbackMapper) {
        this.feedbackMapper = feedbackMapper;
    }

    @Transactional
    public FeedbackSubmitVO submit(int activityId, FeedbackRequest request) {
        CurrentUser student = Access.require(Role.STUDENT);
        Map<String, Object> registration = findCheckedInRegistration(student.id(), activityId);
        int registrationId = ((Number) registration.get("registration_id")).intValue();

        upsertFeedback(activityId, request, student.id(), registrationId);
        return new FeedbackSubmitVO(activityId, registrationId, request.rating());
    }

    public FeedbackMineVO my(int activityId) {
        CurrentUser student = Access.require(Role.STUDENT);
        var rows = feedbackMapper.findMine(student.id(), activityId);
        return rows.isEmpty() ? null : FeedbackMineVO.from(rows.get(0));
    }

    public FeedbackBoardVO activityFeedback(int activityId, int page, int size, boolean lowRatingOnly) {
        CurrentUser user = Access.require(Role.ORGANIZER, Role.ADMIN);
        validateActivityAccess(activityId, user);

        Map<String, Object> summary = feedbackMapper.activitySummary(activityId);
        int offset = (page - 1) * size;
        long filteredTotal = lowRatingOnly
                ? feedbackMapper.countLowRatingActivityRecords(activityId, LOW_RATING_THRESHOLD)
                : feedbackMapper.countActivityRecords(activityId);
        List<Map<String, Object>> records = lowRatingOnly
                ? feedbackMapper.findLowRatingActivityRecords(activityId, LOW_RATING_THRESHOLD, offset, size)
                : feedbackMapper.findActivityRecords(activityId, offset, size);
        return new FeedbackBoardVO(
                FeedbackSummaryVO.from(normalizeSummary(summary)),
                ratingDistribution(activityId).stream().map(RatingDistributionVO::from).toList(),
                keywords(activityId).stream().map(KeywordVO::from).toList(),
                new PageResult<>(records.stream().map(FeedbackRecordVO::from).toList(), filteredTotal, page, size)
        );
    }

    public FeedbackOverviewVO overview() {
        Access.require(Role.ADMIN);
        return new FeedbackOverviewVO(
                FeedbackSummaryVO.from(normalizeSummary(feedbackMapper.globalSummary())),
                ratingDistribution(null).stream().map(RatingDistributionVO::from).toList(),
                keywords(null).stream().map(KeywordVO::from).toList(),
                feedbackMapper.topActivities().stream().map(FeedbackTopActivityVO::from).toList()
        );
    }

    private Map<String, Object> findCheckedInRegistration(int studentId, int activityId) {
        var registrations = feedbackMapper.findRegistration(studentId, activityId);
        if (registrations.isEmpty()) {
            throw new BusinessException(40301, "只有报名学生可以评价活动");
        }
        Map<String, Object> registration = registrations.get(0);
        if (!"CHECKED_IN".equals(registration.get("status"))) {
            throw new BusinessException(40903, "只有已签到活动可以评价");
        }
        return registration;
    }

    private void upsertFeedback(int activityId, FeedbackRequest request, int studentId, int registrationId) {
        var existing = feedbackMapper.findByRegistration(registrationId);
        if (existing.isEmpty()) {
            feedbackMapper.insertFeedback(registrationId, activityId, studentId, request.rating(), request.content());
            return;
        }
        feedbackMapper.updateFeedback(registrationId, request.rating(), request.content());
    }

    private void validateActivityAccess(int activityId, CurrentUser user) {
        if (user.role() == Role.ADMIN) {
            return;
        }
        Integer ownerId = feedbackMapper.findActivityOwner(activityId);
        if (ownerId == null || ownerId != user.id()) {
            throw new BusinessException(40301, "只能查看自己活动的反馈");
        }
    }

    private Map<String, Object> normalizeSummary(Map<String, Object> summary) {
        long feedbackCount = ((Number) summary.get("feedbackCount")).longValue();
        BigDecimal averageRating = summary.get("averageRating") instanceof BigDecimal value ? value : BigDecimal.ZERO;
        long positiveCount = summary.get("positiveCount") == null ? 0 : ((Number) summary.get("positiveCount")).longValue();
        int positiveRate = feedbackCount == 0 ? 0 : Math.round((positiveCount * 100f) / feedbackCount);
        long lowRatingCount = summary.get("lowRatingCount") == null ? 0 : ((Number) summary.get("lowRatingCount")).longValue();
        return Map.of(
                "feedbackCount", feedbackCount,
                "averageRating", averageRating,
                "positiveRate", positiveRate,
                "lowRatingCount", lowRatingCount
        );
    }

    private List<Map<String, Object>> ratingDistribution(Integer activityId) {
        List<Map<String, Object>> rows = activityId == null
                ? feedbackMapper.globalRatingDistribution()
                : feedbackMapper.activityRatingDistribution(activityId);
        Map<Integer, Long> counts = new HashMap<>();
        long total = 0;
        for (Map<String, Object> row : rows) {
            int rating = ((Number) row.get("rating")).intValue();
            long count = ((Number) row.get("count")).longValue();
            counts.put(rating, count);
            total += count;
        }
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (int rating = 5; rating >= 1; rating--) {
            long count = counts.getOrDefault(rating, 0L);
            int rate = total == 0 ? 0 : Math.round((count * 100f) / total);
            distribution.add(Map.of("rating", rating, "count", count, "rate", rate));
        }
        return distribution;
    }

    private List<Map<String, Object>> keywords(Integer activityId) {
        Map<String, Integer> counts = new HashMap<>();
        List<String> contents = activityId == null
                ? feedbackMapper.globalFeedbackContents()
                : feedbackMapper.activityFeedbackContents(activityId);
        for (String content : contents) {
            collectBusinessKeywords(content, counts);
            collectTokenKeywords(content, counts);
        }
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue(Comparator.reverseOrder())
                        .thenComparing(Map.Entry.comparingByKey()))
                .limit(KEYWORD_LIMIT)
                .map(entry -> Map.<String, Object>of("keyword", entry.getKey(), "count", entry.getValue()))
                .toList();
    }

    private void collectBusinessKeywords(String content, Map<String, Integer> counts) {
        for (String keyword : BUSINESS_KEYWORDS) {
            if (content.contains(keyword)) {
                counts.merge(keyword, 1, Integer::sum);
            }
        }
    }

    private void collectTokenKeywords(String content, Map<String, Integer> counts) {
        for (String token : KEYWORD_SPLITTER.split(content)) {
            String normalized = token.trim();
            if (normalized.length() < 2 || normalized.length() > 12 || STOP_WORDS.contains(normalized)) {
                continue;
            }
            counts.merge(normalized, 1, Integer::sum);
        }
    }
}
