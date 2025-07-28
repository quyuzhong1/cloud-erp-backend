package com.erp.rpc.wms.feign;

import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 三方仓发货feign
 */
@FeignClient(name = "erp-wms", contextId = "thirdWarehouseDeliveryFeign")
public interface ThirdWarehouseDeliveryFeign {

    /** 
     * @description 新增
     */
    @PostMapping("feign/thirdWarehouseDelivery/add")
    ThirdWarehouseDeliveryEntity add(@RequestBody ThirdWarehouseDeliveryEntity entity);

    /**
     *  获取最新的三方仓发货单
     */
    @PostMapping("feign/thirdWarehouseDelivery/getLatestBySoId")
    ThirdWarehouseDeliveryEntity getLatestBySoId(@RequestBody String soId);

    @PostMapping("feign/thirdWarehouseDelivery/update")
    boolean update(@RequestBody ThirdWarehouseDeliveryEntity thirdWarehouseDeliveryEntity);

    @PostMapping("feign/thirdWarehouseDelivery/generateDeliveryAndOutStock")
    void generateDeliveryAndOutStock(@RequestBody GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO);
}
