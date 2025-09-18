package com.erp.rpc.wms.feign;

import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryDetailEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 三方仓发货feign
 */
@FeignClient(name = "erp-wms", contextId = "thirdWarehouseDeliveryFeign",configuration = {FeignErrorDecoder.class})
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

    /**
     * 根据三方仓发货单编号和订单编号获取三方仓发货单
     * @param code
     * @param soId
     * @return
     */
    @GetMapping("feign/thirdWarehouseDelivery/getByCodeAndSoId")
    ThirdWarehouseDeliveryEntity getByCodeAndSoId(@RequestParam("code") String code, @RequestParam("soId") String soId);


    /**
     * 根据主表id获取详情列表
     * @param mainIds
     * @return
     */
    @PostMapping("feign/thirdWarehouseDelivery/listByMainIds")
    List<ThirdWarehouseDeliveryDetailEntity> listByMainIds(@RequestBody List<String> mainIds);
}
