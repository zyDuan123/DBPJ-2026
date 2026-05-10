package com.campus.activity.service;

import com.campus.activity.common.Access;
import com.campus.activity.common.PageResult;
import com.campus.activity.common.Role;
import com.campus.activity.model.mapper.CreditRecordMapper;
import com.campus.activity.model.vo.CreditMyVO;
import com.campus.activity.model.vo.CreditOverviewVO;
import com.campus.activity.model.vo.CreditRecordVO;
import com.campus.activity.model.vo.CreditRiskStudentVO;
import com.campus.activity.model.vo.CreditSummaryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class CreditService {
    private static final int INITIAL_CREDIT_SCORE = 100;

    private final CreditRecordMapper creditRecordMapper;

    public CreditService(CreditRecordMapper creditRecordMapper) {
        this.creditRecordMapper = creditRecordMapper;
    }

    public CreditMyVO my(int page, int size) {
        int studentId = Access.require(Role.STUDENT).id();
        return studentCredit(studentId, page, size);
    }

    public CreditOverviewVO overview() {
        Access.require(Role.ADMIN);
        return new CreditOverviewVO(
                CreditSummaryVO.from(creditRecordMapper.overviewSummary()),
                creditRecordMapper.riskStudents().stream().map(CreditRiskStudentVO::from).toList()
        );
    }

    private CreditMyVO studentCredit(int studentId, int page, int size) {
        Long totalChange = creditRecordMapper.totalChange(studentId);
        long total = creditRecordMapper.countStudentRecords(studentId);
        List<CreditRecordVO> records = creditRecordMapper.studentRecords(studentId, (page - 1) * size, size)
                .stream()
                .map(CreditRecordVO::from)
                .toList();
        return new CreditMyVO(INITIAL_CREDIT_SCORE + (totalChange == null ? 0 : totalChange),
                new PageResult<>(records, total, page, size));
    }
}
