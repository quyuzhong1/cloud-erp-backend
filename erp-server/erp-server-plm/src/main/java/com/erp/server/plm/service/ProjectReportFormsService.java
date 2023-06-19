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
    PagingVO<List<ProjectReportFormsDTO.PagingView>> projectReportFormsPaging(PagingDTO<ProjectReportFormsDTO.PagingParam> dto);
}
