package com.erp.server.plm.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.utils.ObjectUtils;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.*;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProjectReportFormsMapper;
import com.erp.server.plm.service.ProjectReportFormsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectReportFormsServiceImpl extends SuperServiceImpl<ProjectReportFormsMapper, ProductInfoEntity> implements ProjectReportFormsService {
    @Resource
    private SysUserFeign sysUserFeign;
    @Override
    public PagingVO<List<ProjectReportFormsDTO.PagingView>> projectReportFormsPaging(PagingDTO<ProjectReportFormsDTO.PagingParam> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        List<Integer> statusList = new ArrayList();
        pagingDTO.getParams().setApprovalStatusList(statusList);
        if (ProjectReportStatusEnum.NOTAPPROVAL.getCode().equals(pagingDTO.getParams().getApprovalStatus())) {
            List<Integer> statusCodeList = Arrays.asList(ApprovalStatusEnum.values()).stream().filter(req -> !ApprovalStatusEnum.APPROVAL.getCode().equals(req.getCode())).map(ApprovalStatusEnum::getCode).collect(Collectors.toList());
            statusList.addAll(statusCodeList);
            pagingDTO.getParams().setApprovalStatusList(statusList);
        }
        if (ProjectReportStatusEnum.APPROVAL.getCode().equals(pagingDTO.getParams().getApprovalStatus())) {
            statusList.add(ApprovalStatusEnum.APPROVAL.getCode());
            pagingDTO.getParams().setApprovalStatusList(statusList);
        }
        if (ProjectReportStatusEnum.FINISHED.getCode().equals(pagingDTO.getParams().getApprovalStatus())) {
            statusList.add(ProjectStateEnum.FINISH.getState());
            pagingDTO.getParams().setProjectStatusList(statusList);
        }
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        IPage<ProjectReportFormsDTO.PagingView> pageData = baseMapper.projectReportFormsPaging(query, pagingDTO.getParams());
        List<ProjectReportFormsDTO.PagingView> pagingViewList = pageData.getRecords();
        for (ProjectReportFormsDTO.PagingView pagingView : pagingViewList) {
            FindUserDTO findUserDTO = userList.stream().filter(req -> req.getUserId().equals(pagingView.getProjectChargeId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(findUserDTO)) {
                pagingView.setProjectChargeName(findUserDTO.getUserName());
            }
            pagingView.setProgressStatusName(ProductProgressStatusEnum.getName(pagingView.getProgressStatus()));
            pagingView.setApprovalStatusName(ApprovalStatusEnum.getName(pagingView.getApprovalStatus()));
            pagingView.setProjectStatusName(ProjectStateEnum.getName(pagingView.getProjectStatus()));
            double approvalProgress = 0;
            double projectProgress = 0;
            //立项任务完成
            if (pagingView.getApprovalTaskCount() != null && pagingView.getApprovalTaskCount() != 0) {
                approvalProgress = ((double) pagingView.getApprovalFinishTaskCount() / pagingView.getApprovalTaskCount()) * 100;
            }
            //项目任务完成
            if (pagingView.getProjectTaskCount() != null && pagingView.getProjectTaskCount() != 0) {
                projectProgress = ((double) pagingView.getProjectFinishTaskCount() / pagingView.getProjectTaskCount()) * 100;
            }
            approvalProgress = Math.round(approvalProgress * 100) / 100;
            projectProgress = Math.round(projectProgress * 100) / 100;
            pagingView.setApprovalProgress(BigDecimal.valueOf(approvalProgress));
            pagingView.setProjectProgress(BigDecimal.valueOf(projectProgress));
        }
        return new PagingVO(pageData);
    }

    @Override
    public List<ProjectReportFormsDTO.TaskDetail> taskDetailView(ProjectReportFormsDTO.TaskDetailParam dto) {
        List<ProjectReportFormsDTO.TaskDetail> list = baseMapper.taskDetailView(dto);
        list.forEach(req -> req.setTaskStateName(TaskStateEnum.getName(req.getTaskState())));
        return list;
    }

    @Override
    public Boolean exportExcelProjectReportForms(ProjectReportFormsDTO.PagingParam dto, HttpServletResponse response) {
        List<Integer> statusList = new ArrayList();
        dto.setApprovalStatusList(statusList);
        if (ProjectReportStatusEnum.NOTAPPROVAL.getCode().equals(dto.getApprovalStatus())) {
            List<Integer> statusCodeList = Arrays.asList(ApprovalStatusEnum.values()).stream().filter(req -> !ApprovalStatusEnum.APPROVAL.getCode().equals(req.getCode())).map(ApprovalStatusEnum::getCode).collect(Collectors.toList());
            statusList.addAll(statusCodeList);
            dto.setApprovalStatusList(statusList);
        }
        if (ProjectReportStatusEnum.APPROVAL.getCode().equals(dto.getApprovalStatus())) {
            statusList.add(ApprovalStatusEnum.APPROVAL.getCode());
            dto.setProjectStatusList(statusList);
        }
        if (ProjectReportStatusEnum.FINISHED.getCode().equals(dto.getApprovalStatus())) {
            statusList.add(ProjectStateEnum.FINISH.getState());
            dto.setProjectStatusList(statusList);
        }

        List<ProjectReportFormsDTO.PagingView> pagingViewList = baseMapper.projectReportFormsExportExcel(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/exportExcelProjectReportForms.xlsx";
        String name = "项目报表";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(pagingViewList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcelTaskDetail(ProjectReportFormsDTO.TaskDetailParam dto, HttpServletResponse response) {
        List<ProjectReportFormsDTO.TaskDetail> pagingViewList = baseMapper.taskDetailView(dto);
        StringBuffer sb = new StringBuffer();
        String excelPath = "excel/exportExcelTaskDetail.xlsx";
        String name = "项目任务明细";
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        try {
            new ExcelPrintUtils().patchExport(pagingViewList, response, sb.toString(), excelPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Boolean.TRUE;
    }
}
