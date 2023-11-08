package com.erp.server.wms.controller.api;


import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.FbaDeliveryLogisticsService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.FbaDeliveryLogisticsDTO;

import java.util.List;

/**
 * FBA发货单物流信息表
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("FBA发货单物流信息表")
@RequestMapping("/fbaDeliveryLogistics")
public class FbaDeliveryLogisticsController extends BaseController {

    @Autowired
    private FbaDeliveryLogisticsService fbaDeliveryLogisticsService;

    /**
     * 更新物流信息列表查询
     * @Author Luo_WG
     * @Date 2023/10/30 18:35
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.DeliveryLogisticsView>>
     **/
    @PostMapping("/viewUpdateLogistics")
    public ApiResult<List<FbaDeliveryLogisticsDTO.DeliveryLogisticsView>> viewUpdateLogistics(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<FbaDeliveryLogisticsDTO.DeliveryLogisticsView> result = fbaDeliveryLogisticsService.updateLogisticsView(dto.getIds());
        return success(result);
    }


    /**
     * 更新物流信息列表保存
     * @Author Luo_WG
     * @Date 2023/10/30 18:35
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.DeliveryLogisticsView>>
     **/
    @PostMapping("/saveUpdateLogistics")
    public ApiResult saveUpdateLogistics(@RequestBody @Validated List<FbaDeliveryLogisticsDTO.DeliveryLogisticsSave> dto) {
        Boolean flag = fbaDeliveryLogisticsService.saveUpdateLogistics(dto);
        return flag ? success() : failure();
    }



}
