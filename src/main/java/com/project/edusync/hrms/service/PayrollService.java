package com.project.edusync.hrms.service;

import com.project.edusync.hrms.dto.payroll.PayrollRunCreateDTO;
import com.project.edusync.hrms.dto.payroll.PayrollRunResponseDTO;
import com.project.edusync.hrms.dto.payroll.PayrollRunSummaryDTO;
import com.project.edusync.hrms.dto.payroll.PayslipDetailDTO;
import com.project.edusync.hrms.dto.payroll.PayslipSummaryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayrollService {

    PayrollRunResponseDTO createRun(PayrollRunCreateDTO dto);

    PayrollRunResponseDTO approveRun(Long runId);

    PayrollRunResponseDTO disburseRun(Long runId);

    Page<PayslipSummaryDTO> listPayslipsByRun(Long runId, Pageable pageable);

    PayslipDetailDTO getPayslipById(Long payslipId);

    byte[] getPayslipPdf(Long payslipId);

    Page<PayslipSummaryDTO> listMyPayslips(Pageable pageable);

    PayslipDetailDTO getMyPayslipById(Long payslipId);

    byte[] getMyPayslipPdf(Long payslipId);

    Page<PayslipSummaryDTO> listPayslipsByStaff(Long staffId, Pageable pageable);

    Page<PayrollRunSummaryDTO> listRuns(Pageable pageable);

    PayrollRunResponseDTO getRunById(Long runId);
}





