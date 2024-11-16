package com.erp.server.plm.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProjectReportFormsDTO;

import java.util.List;

public interface ProjectReportFormsService {
    /**
     * 项目报表-分页查询
     * @Author Luo_WG
     * @Date 2023/6/16 16:18
     * @param dto
     * @return com.common.business.vo.PagingVO<java.util.List<com.erp.model.plm.dto.ProjectReportFormsDTO.PagingView>>
     **/
    PagingVO<ProjectReportFormsDTO.PagingView> projectReportFormsPaging(PagingDTO<ProjectReportFormsDTO.PagingParam> dto);

    /**
     * 任务详情
     * @Author Luo_WG
     * @Date 2023/6/19 10:27
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.ProjectReportFormsDTO.TaskDetailParam>
     **/
    List<ProjectReportFormsDTO.TaskDetail> taskDetailView(ProjectReportFormsDTO.TaskDetailParam dto);

    /**
     * 导出项目报表
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/6/19 10:39
     **/
    Boolean exportExcelProjectReportForms(ProjectReportFormsDTO.PagingParam dto);

    /**
     * 导出项目任务明细
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/6/19 10:39
     **/
    Boolean exportExcelTaskDetail(ProjectReportFormsDTO.TaskDetailParam dto);

    PagingVO<ProjectReportFormsDTO.PagingView> exportProductPurchaseBusiness(PagingDTO<ProjectReportFormsDTO.PagingParam> dto);

    PagingVO<ProjectReportFormsDTO.TaskDetail> exportProductTaskDetail(PagingDTO<ProjectReportFormsDTO.TaskDetailParam> dto);
}
