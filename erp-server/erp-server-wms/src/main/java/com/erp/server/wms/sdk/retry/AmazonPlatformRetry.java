package com.erp.server.wms.sdk.retry;

import com.common.business.annotation.PlatformRetryAnno;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.entity.DmpAmzSoOutstockDetailEntity;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.workflow.entity.ThirdProcessDefinitionEntity;
import com.erp.model.workflow.enums.ThirdProcessDefinitionStatusEnum;
import com.erp.rpc.dmp.feign.DmpAmazonFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.rocketmq.consumer.PlatformNewSoOutStockConsumerService;
import com.erp.server.wms.service.IPlatformRetryService;
import com.erp.server.wms.service.SoOutstockService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.ws.rs.ServerErrorException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@PlatformRetryAnno(method = PlatformDictEnum.AMAZON)
public class AmazonPlatformRetry implements IPlatformRetryService<T> {
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Resource
    private PlatformNewSoOutStockConsumerService platformNewSoOutStockConsumerService;

    @Override
    public Boolean retrySoOutStock(SoB2cEntity currentEntity, List list) {
        if(!currentEntity.hasPlatformWarehouseOrder()){
            return soOutstockService.defaultHandleRetry(currentEntity, list);
        }else{
            return checkRetrySoOutStock(currentEntity);
        }
    }

    private Boolean checkRetrySoOutStock(SoB2cEntity currentEntity) {
        // 查询来源明细ID
        // 查询异常内容
        SoB2cErrorEntity b2cError = soB2cFeign.getB2cError(currentEntity.getId(), SoB2cErrorTypeEnum.GENERATE_OUTSTOCK.getCode());
        if(null == b2cError){
            return true;
        }
        ShopInfoEntity shopInfo = shopInfoFeign.getShopInfoById(currentEntity.getShopId());
        if (null == shopInfo){
            ServiceException.runError("未找到店铺信息");
        }
        String platformCode = currentEntity.getPlatformCode();
        String platformShopCode = shopInfo.getPlatformShopCode();
        if (StringUtils.isBlank(platformShopCode)){
            ServiceException.runError("未找到店铺平台账号代号");
        }
        if (StringUtils.isBlank(platformCode)){
            ServiceException.runError("未找到平台订单编码");
        }
        List<DmpAmzSoOutstockDetailEntity> dmpAmzSoOutstockDetailEntityList = FeignQuery.create(DmpAmzSoOutstockDetailEntity.class)
                .eq(DmpAmzSoOutstockDetailEntity::getAmazonOrderId , platformCode)
                .eq(DmpAmzSoOutstockDetailEntity::getPlatformShopCode, platformShopCode)
                .eq(DmpAmzSoOutstockDetailEntity::getIsDeleted,Boolean.FALSE)
                .list();
        if (CollectionUtils.isEmpty(dmpAmzSoOutstockDetailEntityList)){
            log.warn("未找到亚马逊出库单明细信息，订单号：{}，店铺账号代号：{}",platformCode,platformShopCode);
            return true;
        }
        List<String> shipmentItemIdList = dmpAmzSoOutstockDetailEntityList.stream().map(DmpAmzSoOutstockDetailEntity::getShipmentItemId).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shipmentItemIdList)){
            log.warn("未找到亚马逊出库单明细的发货项ID信息，订单号：{}，店铺账号代号：{}",platformCode,platformShopCode);
            return true;
        }

        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpTaskFeign.getLastOutputTaskRecordList(shipmentItemIdList, "DmpOutputAmzSoOutStockRocketMQTaskHandler");
        if (CollectionUtils.isEmpty(dmpOutputTaskRecordEntityList)) {
            log.warn("未找到亚马逊出库单任务记录信息，订单号：{}，店铺账号代号：{}",platformCode,platformShopCode);
            return true;
        }
        for (DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity : dmpOutputTaskRecordEntityList) {
            platformNewSoOutStockConsumerService.handle(dmpOutputTaskRecordEntity.getRequestData());
        }
        log.info("亚马逊出库单重试处理完成，订单号：{}，店铺账号代号：{}",platformCode,platformShopCode);
        return true;
    }
}
