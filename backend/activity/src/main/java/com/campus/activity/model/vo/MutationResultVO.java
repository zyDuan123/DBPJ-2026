package com.campus.activity.model.vo;

public record MutationResultVO(Boolean created, Boolean updated) {
    public static MutationResultVO createdResult() {
        return new MutationResultVO(true, null);
    }

    public static MutationResultVO updatedResult() {
        return new MutationResultVO(null, true);
    }
}
