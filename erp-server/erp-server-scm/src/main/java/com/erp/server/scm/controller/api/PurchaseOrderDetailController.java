package com.erp.server.scm.controller.api;


import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.server.scm.service.PurchaseOrderDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 采购订单明细管理
 * @author will
 * @since 2026-03-23
 */
@Slf4j
@RestController
@LogSystemModule("采购订单明细")
@RequestMapping("/purchaseOrderDetail")
public class PurchaseOrderDetailController extends BaseController {

    @Resource
    private PurchaseOrderDetailService purchaseOrderDetailService;

    /**
     * 采购订单下推质检申请数据列表
     * @author will
     * @date 2026/3/23 17:00
     * @param dto
     * @return ApiResult<PagingVO<PurchaseOrderDetailDTO.ListPushQcApplicationDTO>>
     */
    @PostMapping("/listPushQcApplication")
    public ApiResult<List<PurchaseOrderDetailDTO.ListPushQcApplicationDTO>> listPushQcApplication(@RequestBody @Validated PurchaseOrderDetailDTO.ListPushQcApplicationParamDTO dto) {
        List<PurchaseOrderDetailDTO.ListPushQcApplicationDTO> pagingVO = purchaseOrderDetailService.listPushQcApplication(dto);
        return success(pagingVO);
    }

}
