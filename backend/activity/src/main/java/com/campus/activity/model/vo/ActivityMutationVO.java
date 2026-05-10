package com.campus.activity.model.vo;

public record ActivityMutationVO(Integer id, String status, Boolean updated) {
    public static ActivityMutationVO created(Integer id, String status) {
        return new ActivityMutationVO(id, status, null);
    }

    public static ActivityMutationVO status(String status) {
        return new ActivityMutationVO(null, status, null);
    }

    public static ActivityMutationVO updatedResult() {
        return new ActivityMutationVO(null, null, true);
    }
}
