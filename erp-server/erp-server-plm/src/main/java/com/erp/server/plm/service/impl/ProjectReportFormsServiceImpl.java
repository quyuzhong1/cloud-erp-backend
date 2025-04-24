package com.erp.server.plm.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.mapper.ProjectReportFormsMapper;
import com.erp.server.plm.service.ProjectReportFormsService;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PROJECT_REPORT_PURCHASE_BUSINESS;
import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_PROJECT_REPORT_TASK_DETAIL;

@Service
public class ProjectReportFormsServiceImpl extends SuperServiceImpl<ProjectReportFormsMapper, ProductInfoEntity> implements ProjectReportFormsService {
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public PagingVO<ProjectReportFormsDTO.PagingView> projectReportFormsPaging(PagingDTO<ProjectReportFormsDTO.PagingParam> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page<ProjectReportFormsDTO.PagingParam> query = new Page<>(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        List<Integer> statusList = new ArrayList<>();
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
            if (!ObjectUtils.isEmpty(findUserDTO)) {
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
            approvalProgress = (double)  Math.round(approvalProgress * 100) / 100;
            projectProgress = (double)  Math.round(projectProgress * 100) / 100;
            pagingView.setApprovalProgress(BigDecimal.valueOf(approvalProgress));
            pagingView.setProjectProgress(BigDecimal.valueOf(projectProgress));
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public List<ProjectReportFormsDTO.TaskDetail> taskDetailView(ProjectReportFormsDTO.TaskDetailParam dto) {
        List<ProjectReportFormsDTO.TaskDetail> list = baseMapper.taskDetailView(dto);
        list.forEach(req -> req.setTaskStateName(TaskStateEnum.getName(req.getTaskState())));
        return list;
    }

    @Override
    public Boolean exportExcelProjectReportForms(ProjectReportFormsDTO.PagingParam dto) {
        downloadTaskFeign.saveDownloadTask("项目报表", EXPORT_PLM_PROJECT_REPORT_PURCHASE_BUSINESS.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public Boolean exportExcelTaskDetail(ProjectReportFormsDTO.PagingParam dto) {
        List<Integer> statusList = new ArrayList<>();
        dto.setApprovalStatusList(statusList);
        if (ProjectReportStatusEnum.NOTAPPROVAL.getCode().equals(dto.getApprovalStatus())) {
            List<Integer> statusCodeList = Arrays.asList(ApprovalStatusEnum.values()).stream().filter(req -> !ApprovalStatusEnum.APPROVAL.getCode().equals(req.getCode())).map(ApprovalStatusEnum::getCode).collect(Collectors.toList());
            statusList.addAll(statusCodeList);
            dto.setApprovalStatusList(statusList);
        }
        if (ProjectReportStatusEnum.APPROVAL.getCode().equals(dto.getApprovalStatus())) {
            statusList.add(ApprovalStatusEnum.APPROVAL.getCode());
            dto.setApprovalStatusList(statusList);
        }
        if (ProjectReportStatusEnum.FINISHED.getCode().equals(dto.getApprovalStatus())) {
            statusList.add(ProjectStateEnum.FINISH.getState());
            dto.setProjectStatusList(statusList);
        }
        List<ProjectReportFormsDTO.PagingView> pagingViews = baseMapper.projectReportFormsExportExcel(dto);
        List<String> ids = pagingViews.stream().map(ProjectReportFormsDTO.PagingView::getId).distinct().collect(Collectors.toList());
        ProjectReportFormsDTO.TaskDetailParam param = new ProjectReportFormsDTO.TaskDetailParam();
        param.setIds(ids);
        downloadTaskFeign.saveDownloadTask("项目任务明细", EXPORT_PLM_PROJECT_REPORT_TASK_DETAIL.getCode(), param);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<ProjectReportFormsDTO.PagingView> exportProductPurchaseBusiness(PagingDTO<ProjectReportFormsDTO.PagingParam> dto) {

        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<ProjectReportFormsDTO.PagingView> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        List<Integer> statusList = new ArrayList<>();
        dto.getParams().setApprovalStatusList(statusList);
        if (ProjectReportStatusEnum.NOTAPPROVAL.getCode().equals(dto.getParams().getApprovalStatus())) {
            List<Integer> statusCodeList = Arrays.asList(ApprovalStatusEnum.values()).stream().filter(req -> !ApprovalStatusEnum.APPROVAL.getCode().equals(req.getCode())).map(ApprovalStatusEnum::getCode).collect(Collectors.toList());
            statusList.addAll(statusCodeList);
            dto.getParams().setApprovalStatusList(statusList);
        }
        if (ProjectReportStatusEnum.APPROVAL.getCode().equals(dto.getParams().getApprovalStatus())) {
            statusList.add(ApprovalStatusEnum.APPROVAL.getCode());
            dto.getParams().setApprovalStatusList(statusList);
        }
        if (ProjectReportStatusEnum.FINISHED.getCode().equals(dto.getParams().getApprovalStatus())) {
            statusList.add(ProjectStateEnum.FINISH.getState());
            dto.getParams().setProjectStatusList(statusList);
        }
        Page<ProjectReportFormsDTO.PagingView> page = baseMapper.projectReportFormsExportExcel(query, dto.getParams());
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        for (ProjectReportFormsDTO.PagingView pagingView : page.getRecords()) {
            FindUserDTO findUserDTO = userList.stream().filter(req -> req.getUserId().equals(pagingView.getProjectChargeId())).findFirst().orElse(null);
            if (!ObjectUtils.isEmpty(findUserDTO)) {
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
            approvalProgress = (double)  Math.round(approvalProgress * 100) / 100;
            projectProgress = (double)  Math.round(projectProgress * 100) / 100;
            pagingView.setApprovalProgress(BigDecimal.valueOf(approvalProgress));
            pagingView.setProjectProgress(BigDecimal.valueOf(projectProgress));
        }
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<ProjectReportFormsDTO.TaskDetail> exportProductTaskDetail(PagingDTO<ProjectReportFormsDTO.TaskDetailParam> dto) {

        Page<ProjectReportFormsDTO.TaskDetail> page = this.baseMapper.taskDetailView(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollUtil.isEmpty(page.getRecords())) {
            return new PagingVO<>();
        }
        // 数据处理
        page.getRecords().forEach(req -> req.setTaskStateName(TaskStateEnum.getName(req.getTaskState())));
        return new PagingVO<>(page);
    }
}
