package com.employeehub.leave.service;

import com.employeehub.auth.entity.User;
import com.employeehub.auth.repository.UserRepository;
import com.employeehub.common.PageResponse;
import com.employeehub.common.enums.LeaveStatus;
import com.employeehub.common.enums.LeaveType;
import com.employeehub.employee.entity.Employee;
import com.employeehub.employee.repository.EmployeeRepository;
import com.employeehub.exception.BadRequestException;
import com.employeehub.exception.ConflictException;
import com.employeehub.exception.ResourceNotFoundException;
import com.employeehub.leave.dto.LeaveRequestDto;
import com.employeehub.leave.dto.LeaveResponse;
import com.employeehub.leave.entity.LeaveApprovalHistory;
import com.employeehub.leave.entity.LeaveBalance;
import com.employeehub.leave.entity.LeaveRequest;
import com.employeehub.leave.repository.LeaveApprovalHistoryRepository;
import com.employeehub.leave.repository.LeaveBalanceRepository;
import com.employeehub.leave.repository.LeaveRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final LeaveBalanceRepository leaveBalanceRepository;
    private final LeaveApprovalHistoryRepository historyRepository;
    private final EmployeeRepository employeeRepository;
    private final UserRepository userRepository;

    @Transactional
    public LeaveResponse submit(Long employeeId, LeaveRequestDto dto) {
        if (dto.getEndDate().isBefore(dto.getStartDate())) {
            throw new BadRequestException("End date must be on or after start date");
        }
        Employee employee = employeeRepository.findByIdActive(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        List<LeaveRequest> overlapping = leaveRequestRepository.findOverlapping(
                employeeId, dto.getStartDate(), dto.getEndDate(), null);
        if (!overlapping.isEmpty()) {
            throw new ConflictException("Overlapping leave request exists");
        }

        long days = ChronoUnit.DAYS.between(dto.getStartDate(), dto.getEndDate()) + 1;
        BigDecimal totalDays = BigDecimal.valueOf(days);

        int year = dto.getStartDate().getYear();
        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndYear(employeeId, dto.getLeaveType(), year)
                .orElseGet(() -> leaveBalanceRepository.save(LeaveBalance.builder()
                        .employee(employee)
                        .leaveType(dto.getLeaveType())
                        .year(year)
                        .totalDays(dto.getLeaveType() == LeaveType.UNPAID ? BigDecimal.ZERO : BigDecimal.valueOf(12))
                        .usedDays(BigDecimal.ZERO)
                        .pendingDays(BigDecimal.ZERO)
                        .build()));

        if (dto.getLeaveType() != LeaveType.UNPAID
                && balance.getAvailableDays().compareTo(totalDays) < 0) {
            throw new BadRequestException("Insufficient leave balance. Available: " + balance.getAvailableDays());
        }

        balance.setPendingDays(balance.getPendingDays().add(totalDays));
        leaveBalanceRepository.save(balance);

        LeaveRequest request = LeaveRequest.builder()
                .employee(employee)
                .leaveType(dto.getLeaveType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .totalDays(totalDays)
                .reason(dto.getReason())
                .status(LeaveStatus.PENDING)
                .appliedAt(Instant.now())
                .build();

        return toResponse(leaveRequestRepository.save(request));
    }

    @Transactional
    public LeaveResponse approve(Long requestId, String comments, String reviewerEmail) {
        return review(requestId, LeaveStatus.APPROVED, comments, reviewerEmail);
    }

    @Transactional
    public LeaveResponse reject(Long requestId, String comments, String reviewerEmail) {
        return review(requestId, LeaveStatus.REJECTED, comments, reviewerEmail);
    }

    private LeaveResponse review(Long requestId, LeaveStatus newStatus, String comments, String reviewerEmail) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
        if (request.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException("Only pending requests can be reviewed");
        }

        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Reviewer not found"));

        request.setStatus(newStatus);
        request.setReviewedBy(reviewer);
        request.setReviewedAt(Instant.now());
        request.setReviewComments(comments);

        LeaveBalance balance = leaveBalanceRepository
                .findByEmployeeIdAndLeaveTypeAndYear(
                        request.getEmployee().getId(), request.getLeaveType(), request.getStartDate().getYear())
                .orElse(null);
        if (balance != null) {
            balance.setPendingDays(balance.getPendingDays().subtract(request.getTotalDays()).max(BigDecimal.ZERO));
            if (newStatus == LeaveStatus.APPROVED) {
                balance.setUsedDays(balance.getUsedDays().add(request.getTotalDays()));
            }
            leaveBalanceRepository.save(balance);
        }

        historyRepository.save(LeaveApprovalHistory.builder()
                .leaveRequest(request)
                .action(newStatus.name())
                .performedBy(reviewer)
                .comments(comments)
                .performedAt(Instant.now())
                .build());

        return toResponse(leaveRequestRepository.save(request));
    }

    @Transactional(readOnly = true)
    public PageResponse<LeaveResponse> search(LeaveStatus status, Long employeeId, Pageable pageable) {
        Page<LeaveRequest> page = leaveRequestRepository.search(status, employeeId, pageable);
        return PageResponse.from(page.map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public List<LeaveBalance> getBalances(Long employeeId, int year) {
        return leaveBalanceRepository.findByEmployeeIdAndYear(employeeId, year);
    }

    private LeaveResponse toResponse(LeaveRequest lr) {
        return LeaveResponse.builder()
                .id(lr.getId())
                .employeeId(lr.getEmployee().getId())
                .employeeName(lr.getEmployee().getFullName())
                .employeeCode(lr.getEmployee().getEmployeeCode())
                .leaveType(lr.getLeaveType())
                .startDate(lr.getStartDate())
                .endDate(lr.getEndDate())
                .totalDays(lr.getTotalDays())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .appliedAt(lr.getAppliedAt())
                .reviewedBy(lr.getReviewedBy() != null ? lr.getReviewedBy().getEmail() : null)
                .reviewedAt(lr.getReviewedAt())
                .reviewComments(lr.getReviewComments())
                .build();
    }
}
