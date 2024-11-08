package com.erp.server.wms.controller.feign;

import com.common.business.enums.OverseasInstockStatusEnum;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.erp.server.wms.service.OverseasWarehouseInboundService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 海外仓入库单Feign接口
 * @date 2024-08-31
 * @author tanmujin
 */
@RestController
@RequestMapping("/feign/overseasWarehouseInbound")
public class OverseaWarehouseInboundFeignController {

    @Resource
    private OverseasWarehouseInboundService overseasWarehouseInboundService;

    /**
     * 根据来源ID查询
     */
    @PostMapping("/listBySourceIds")
    List<OverseasWarehouseInboundEntity> listBySourceIds(@RequestBody List<String> sourceIds){
        return overseasWarehouseInboundService.lambdaQuery()
                .in(OverseasWarehouseInboundEntity::getSourceId, sourceIds)
                .notIn(OverseasWarehouseInboundEntity::getInstockStatus, Arrays.asList(OverseasInstockStatusEnum.CANCELED.getCode(), OverseasInstockStatusEnum.ABNORMAL.getCode()))
                .list();
    }
}
