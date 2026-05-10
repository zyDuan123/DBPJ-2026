package com.campus.activity.model.vo;

import com.campus.activity.common.PageResult;

import java.util.List;

public record FeedbackBoardVO(FeedbackSummaryVO summary,
                              List<RatingDistributionVO> ratingDistribution,
                              List<KeywordVO> keywords,
                              PageResult<FeedbackRecordVO> records) {
}
