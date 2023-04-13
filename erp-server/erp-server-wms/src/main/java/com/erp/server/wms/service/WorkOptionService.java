package com.erp.server.wms.service;

import com.common.business.service.SuperService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WorkOptionDTO;
import com.erp.model.wms.entity.WorkOptionEntity;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

/**
 * <p>
 *  工作台选项表服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-11
 */
public interface WorkOptionService extends SuperService<WorkOptionEntity> {

    /**
     * 审批中心-下拉搜索选项
     * @Author Luo_WG
     * @Date 2023/4/12 11:58
     * @return java.util.List<com.erp.model.wms.dto.WorkOptionDTO.ApproveSearchOptionDTO>
     **/
    List<WorkOptionDTO.ApproveSearchOptionDTO> approveSearchOption();
}
