package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.Role;
import com.campus.activity.model.mapper.CreditRecordMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class CreditService {
    private static final int INITIAL_CREDIT_SCORE = 100;

    private final CreditRecordMapper creditRecordMapper;

    public CreditService(CreditRecordMapper creditRecordMapper) {
        this.creditRecordMapper = creditRecordMapper;
    }

    public Map<String, Object> my(int page, int size) {
        int studentId = Access.require(Role.STUDENT).id();
        return studentCredit(studentId, page, size);
    }

    public Map<String, Object> overview() {
        Access.require(Role.ADMIN);
        return Map.of(
                "summary", creditRecordMapper.overviewSummary(),
                "riskStudents", creditRecordMapper.riskStudents()
        );
    }

    private Map<String, Object> studentCredit(int studentId, int page, int size) {
        Long totalChange = creditRecordMapper.totalChange(studentId);
        long total = creditRecordMapper.countStudentRecords(studentId);
        List<Map<String, Object>> records = creditRecordMapper.studentRecords(studentId, (page - 1) * size, size);
        return Map.of(
                "score", INITIAL_CREDIT_SCORE + (totalChange == null ? 0 : totalChange),
                "records", new PageResult<>(records, total, page, size)
        );
    }
}
