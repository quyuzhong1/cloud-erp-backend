package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProjectReportFormsDTO;
import com.erp.model.plm.entity.ProductInfoEntity;
import com.erp.model.plm.enums.ApprovalStatusEnum;
import com.erp.model.plm.enums.ProjectReportStatusEnum;
import com.erp.model.plm.enums.ProjectStateEnum;
import com.erp.model.plm.enums.TaskStateEnum;
import com.erp.server.plm.mapper.ProjectReportFormsMapper;
import com.erp.server.plm.service.ProjectReportFormsService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProjectReportFormsServiceImpl extends SuperServiceImpl<ProjectReportFormsMapper, ProductInfoEntity> implements ProjectReportFormsService {

    @Override
    public PagingVO<List<ProjectReportFormsDTO.PagingView>> projectReportFormsPaging(PagingDTO<ProjectReportFormsDTO.PagingParam> pagingDTO) {
        pagingDTO.getParams().setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        List<Integer> statusList = new ArrayList();
        pagingDTO.getParams().setApprovalStatusList(statusList);
        if (ProjectReportStatusEnum.NOTAPPROVAL.getCode().equals(pagingDTO.getParams().getApprovalStatus())) {
            List<Integer> statusCodeList = Arrays.asList(ApprovalStatusEnum.values()).stream().filter(req -> !ApprovalStatusEnum.APPROVAL.getCode().equals(req.getCode())).map(ApprovalStatusEnum::getCode).collect(Collectors.toList());
            statusList.addAll(statusCodeList);
        }
        if (ProjectReportStatusEnum.APPROVAL.getCode().equals(pagingDTO.getParams().getApprovalStatus())) {
            statusList.add(ApprovalStatusEnum.APPROVAL.getCode());
        }
        if (ProjectReportStatusEnum.FINISHED.getCode().equals(pagingDTO.getParams().getApprovalStatus())) {
            statusList.add(ProjectStateEnum.FINISH.getState());
        }
        pagingDTO.getParams().setApprovalStatusList(statusList);
        pagingDTO.getParams().setProjectStatusList(statusList);
        IPage<ProjectReportFormsDTO.PagingView> pageData = baseMapper.projectReportFormsPaging(query, pagingDTO.getParams());
        List<ProjectReportFormsDTO.PagingView> pagingViewList = pageData.getRecords();

        for (ProjectReportFormsDTO.PagingView pagingView : pagingViewList) {
            pagingView.setApprovalStatusName(ApprovalStatusEnum.getName(pagingView.getApprovalStatus()));
            pagingView.setProjectStatusName(ProjectStateEnum.getName(pagingView.getProjectStatus()));
            double approvalProgress = 0;
            double projectProgress = 0;
            //立项任务完成
            if (pagingView.getApprovalTaskCount() != 0) {
                approvalProgress = ((double) pagingView.getApprovalFinishTaskCount() / pagingView.getApprovalTaskCount()) * 100;
            }
            //项目任务完成
            if (pagingView.getProjectTaskCount() != 0) {
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
    public List<ProjectReportFormsDTO.TaskDetail> taskDetailView(String id) {
        List<ProjectReportFormsDTO.TaskDetail> list = baseMapper.taskDetailView(id);
        list.forEach(req -> req.setTaskStateName(TaskStateEnum.getName(req.getTaskState())));
        return list;
    }
}
