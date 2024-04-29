package com.erp.rpc.wms.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Lambda
 * @Classname SoB2cDeliveryFeign
 * @Description TODO
 * @Date 2023-12-18 11:37
 * @Created by yl
 */
@FeignClient(name = "erp-wms", contextId = "soB2cDeliveryFeign")
public interface SoB2cDeliveryFeign {

    /** 
     * @description 获取发货单详情
     * @param soDetailIdList B2C销售订单详情id
     * @author Lambda
     * @return 
     * @create 2023-12-18 11:39
     */
    @PostMapping("feign/soB2cDelivery/listBySoDetailIds")
    List<SoB2cDeliveryDetailEntity> listBySoDetailIds(@RequestBody List<String> soDetailIdList);

    /**
     * 添加发货单
     * @param dto
     * @return
     */
    @PostMapping("feign/soB2cDelivery/add")
    Boolean addSoB2cDelivery(@RequestBody SoB2cDeliveryDTO.AddDTO dto);

    /**
     * 添加发货单
     * @param dto
     * @return
     */
    @PostMapping("feign/soB2cDelivery/updateB2cDeliveryWeightBySoId")
    Boolean updateB2cDeliveryWeightBySoId(@RequestBody SoB2cDeliveryDTO.UpdateWeightDTO dto);

    /**
     * 虚假发货
     * @Author Luo_WG
     * @Date 2023/12/27 15:30
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    @PostMapping("feign/soB2cDelivery/falseDelivery")
    BatchResultDTO falseDelivery(@RequestBody String id);

    /**
     * 虚假发货
     * @Author Luo_WG
     * @Date 2023/12/27 16:00
     * @param ids 发货单id
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/soB2cDelivery/falseDeliveryBatch")
    Boolean falseDeliveryBatch(@RequestBody List<String> ids);

    /**
     * 根据来源id查询发货单
     * @Author Luo_WG
     * @Date 2023/12/27 15:40
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryEntity>
     **/
    @PostMapping("feign/soB2cDelivery/listBySourceId")
    List<SoB2cDeliveryEntity> listBySourceId(@RequestBody List<String> sourceIds);

    /**
     * 修改发货状态
     * @Author Luo_WG
     * @Date 2023/12/27 16:00
     * @param ids
     * @param status
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/soB2cDelivery/updateStatus")
    Boolean updateStatus(@RequestParam("ids") List<String> ids, @RequestParam("status") String status);


    /**
     * 合并组包发货
     * @Author Luo_WG
     * @Date 2023/12/27 16:00
     * @param soIdList
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/soB2cDelivery/mergePackageDelivery")
    Boolean mergePackageDelivery(@RequestBody List<String> soIdList);
}
