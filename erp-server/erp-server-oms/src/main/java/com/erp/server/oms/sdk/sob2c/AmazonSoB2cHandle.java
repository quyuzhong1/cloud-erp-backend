package com.erp.server.oms.sdk.sob2c;

import com.common.business.annotation.PlatformSoB2cAnnotate;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.server.oms.service.ISoB2cHandleService;
import com.erp.server.oms.service.PlatformOrderConsumerHandleService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * 亚马逊B2C订单处理
 *
 * @Author Jim
 * @Date 2024/03/21
 **/
@Slf4j
@Component
@PlatformSoB2cAnnotate(method = PlatformDictEnum.AMAZON)
public class AmazonSoB2cHandle implements ISoB2cHandleService<T> {

    @Resource
    private PlatformOrderConsumerHandleService platformOrderConsumerHandleService;
    @Resource
    private DmpMongoDbFeign dmpMongoDbFeign;

    @Override
    public Boolean handleRule(SoB2cEntity mainEntity) {
        //平台仓订单不走任何规则
        if (Boolean.TRUE.equals(mainEntity.hasPlatformWarehouseOrder())) {
            return false;
        }
        try {
            platformOrderConsumerHandleService.handleRule(mainEntity);
        } catch (Exception e) {
            log.error("[亚马逊订单规则处理失败]:order={},msg={}", mainEntity.getPlatformCode(), e.getMessage());
        }
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean handleSoOutStock(PlatformOrderDTO dto, SoB2cDTO.PullOrderResultDTO resultDTO, SoB2cEntity mainEntity) {
        // 新的亚马逊FBA订单检查历史配送记录
        if (resultDTO.isNewInsertOrder() && Boolean.TRUE.equals(mainEntity.hasPlatformWarehouseOrder())) {
            try {
                Boolean result = dmpMongoDbFeign.checkSoOutStock(new DmpPullSoOutStockDTO(mainEntity.getShopId(), mainEntity.getPlatformCode(), mainEntity.getId()));
                if (Boolean.FALSE.equals(result)){
                    log.warn("处理检查历史销售出库记录失败:platformOrderId={}", dto.getPlatformCode());
                }
            } catch (Exception e) {
                log.warn("检查历史销售出库记录失败:platformOrderId={}", dto.getPlatformCode());
            }
        }
        return true;
    }
}
