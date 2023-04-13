package com.erp.server.wms.controller;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.WorkOptionDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import java.util.List;

/**
 * 工作台选项
 * @author LUO_WG
 * @since 2023-04-11
 */
@RestController
@RequestMapping("/work-option-entity")
public class WorkOptionController extends BaseController {

    /**
     * 代办列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:48
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>>
     **/
    @PostMapping("/pendingView")
    public ApiResult<List<WorkOptionDTO.PendingViewDTO>> pendingView() {
        List<WorkOptionDTO.PendingViewDTO> pendingViewList = null;
        return success(pendingViewList);
    }

    /**
     * 常用列表
     * @Author Luo_WG
     * @Date 2023/4/11 18:50
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WorkOptionDTO.frequentlyViewDTO>>
     **/
    @PostMapping("/frequentlyView")
    public ApiResult<List<WorkOptionDTO.FrequentlyViewDTO>> frequentlyView() {
        List<WorkOptionDTO.FrequentlyViewDTO> frequentlyViewDTO = null;
        return success(frequentlyViewDTO);
    }

    /**
     * 立项阶段列表
     * @Author Luo_WG
     * @Date 2023/4/12 9:33
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WorkOptionDTO.stageViewDTO>>
     **/
    @PostMapping("/stageView")
    public ApiResult<List<WorkOptionDTO.StageViewDTO>> stageView() {
        List<WorkOptionDTO.StageViewDTO> stageViewDTOList = null;
        return success(stageViewDTOList);
    }

    /**
     * 审批中心
     * @Author Luo_WG
     * @Date 2023/4/12 9:35
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WorkOptionDTO.approveViewDTO>>
     **/
    @PostMapping("/approveView")
    public ApiResult<List<WorkOptionDTO.ApproveViewDTO>> approveView(@RequestBody @Validated PagingDTO<WorkOptionDTO.ApproveViewParamDTO> dto) {
        List<WorkOptionDTO.ApproveViewDTO> approveViewDTO = null;
        return success(approveViewDTO);
    }

    /**
     * 审批中心-下拉搜索选项
     * @Author Luo_WG
     * @Date 2023/4/12 9:35
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WorkOptionDTO.approveViewDTO>>
     **/
    @PostMapping("/approveSearchOption")
    public ApiResult<List<WorkOptionDTO.ApproveSearchOptionDTO>> approveSearchOption() {
        List<WorkOptionDTO.ApproveSearchOptionDTO> approveSearchOptionDTO = null;
        return success(approveSearchOptionDTO);
    }
}
