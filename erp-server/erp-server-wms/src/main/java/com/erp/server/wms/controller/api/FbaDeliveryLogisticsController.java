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
 * FBI发货单物流信息表
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
     * 新增
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "FBI发货单物流信息表新增")
    public ApiResult<String> add(@RequestBody @Validated FbaDeliveryLogisticsDTO.AddDTO dto) {
        return success(fbaDeliveryLogisticsService.add(dto));
    }

    /**
     * 修改
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated FbaDeliveryLogisticsDTO.UpdateDTO dto) {
        fbaDeliveryLogisticsService.update(dto);
        return success();
    }

    /**
     * 更新物流信息列表查询
     * @Author Luo_WG
     * @Date 2023/10/30 18:35
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.DeliveryLogisticsView>>
     **/
    @PostMapping("/ViewUpdateLogistics")
    public ApiResult<List<FbaDeliveryDTO.DeliveryLogisticsView>> ViewUpdateLogistics(@RequestBody @Validated BaseIdsDTO.IdsDTO ids) {
        List<FbaDeliveryDTO.DeliveryLogisticsView> result = fbaDeliveryLogisticsService.updateLogisticsView(ids);
        return success(result);
    }


    /**
     * 更新物流信息列表查询
     * @Author Luo_WG
     * @Date 2023/10/30 18:35
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaDeliveryDTO.DeliveryLogisticsView>>
     **/
    @PostMapping("/SaveUpdateLogistics")
    public ApiResult<List<FbaDeliveryDTO.DeliveryLogisticsView>> SaveUpdateLogistics(@RequestBody @Validated BaseIdsDTO.IdsDTO ids) {
        List<FbaDeliveryDTO.DeliveryLogisticsView> result = fbaDeliveryLogisticsService.updateLogisticsView(ids);
        return success(result);
    }



}
