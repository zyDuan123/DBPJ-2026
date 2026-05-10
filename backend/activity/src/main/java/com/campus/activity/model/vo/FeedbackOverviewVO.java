package com.campus.activity.model.vo;

import java.util.List;

public record FeedbackOverviewVO(FeedbackSummaryVO summary,
                                 List<RatingDistributionVO> ratingDistribution,
                                 List<KeywordVO> keywords,
                                 List<FeedbackTopActivityVO> topActivities) {
}
