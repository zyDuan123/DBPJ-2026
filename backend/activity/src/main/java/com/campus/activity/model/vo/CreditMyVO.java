package com.campus.activity.model.vo;

import com.campus.activity.common.PageResult;

public record CreditMyVO(Long score, PageResult<CreditRecordVO> records) {
}
