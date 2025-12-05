package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * @author Lambda
 * @Classname SoB2cDeliveryFeign
 * @Description SoB2cDeliveryFeign
 * @Date 2023-12-18 11:37
 * @Created by yl
 */
@FeignClient(name = "erp-wms", contextId = "soB2cDeliveryFeign" ,configuration = {FeignErrorDecoder.class})
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
     * 手动标发
     * @Author Luo_WG
     * @Date 2023/12/27 15:30
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    @PostMapping("feign/soB2cDelivery/falseDelivery")
    BatchResultDTO falseDelivery(@RequestBody String id);

    /**
     * 手动标发
     * @Author Luo_WG
     * @Date 2023/12/27 16:00
     * @param ids 发货单id
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/soB2cDelivery/falseDeliveryBatch")
    Boolean falseDeliveryBatch(@RequestBody(required = false) List<String> ids);

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
     * 平台标记发货
     * @param platformShipOrderDTO
     * @return
     */
    @PostMapping("/feign/soB2cDelivery/shipOrder")
    Boolean shipOrder(@RequestBody PlatformShipOrderDTO platformShipOrderDTO);

    /**
     * 流水线称重
     * @author will
     * @date 2024/6/28 15:20 
     * @return String
     */
    @PostMapping("/feign/soB2cDelivery/dimensionalWeightPipeline")
    ApiResult<String> dimensionalWeightPipeline(@RequestBody DimensionalWeightDTO dto);

    /**
     * 修改发货单标发类型
     * @param ids 发货单id
     * @param code 类型
     */
    @PostMapping("feign/soB2cDelivery/updateShipmentMark")
    void updateShipmentMark(@RequestParam("ids") List<String> ids,@RequestParam("code") String code);
    /**
     * 发货生成直接调拨单
     * @author will
     * @date 2024/8/1 9:44
     * @param soId
     * @return Boolean
     */
    @PostMapping("feign/soB2cDelivery/afreshPushTransferInfo")
    Boolean afreshPushTransferInfo(@RequestBody String soId);
    /**
     * 重试发货虚拟仓库存扣减
     * @author will
     * @date 2024/8/1 10:29
     * @param soId
     * @return Boolean
     */
    @PostMapping("feign/soB2cDelivery/afreshOutFreezeVirtualInventory")
    Boolean afreshOutFreezeVirtualInventory(@RequestBody String soId);
    /**
     * 根据销售订单手动标发
     * @Author Luo_WG
     * @Date 2023/12/27 16:00
     * @param id 销售订单id
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/soB2cDelivery/falseDeliveryBySoId")
    BatchResultDTO falseDeliveryBySoId(@RequestBody String id);

    @PostMapping("feign/soB2cDelivery/generateDeliveryAndOutStock")
    void generateDeliveryAndOutStock(@RequestBody GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO);

    @PostMapping("/feign/soB2cDelivery/getDeliveryCodeBySourceId")
    Map<String,String> getDeliveryCodeBySourceId(@RequestParam("sourceIds") List<String> sourceIds);
    /**
     * 自动反审核并删除发货单
     * @param id
     */
    @GetMapping("/feign/soB2cDelivery/deleteSoB2cDelivery")
    void deleteSoB2cDelivery(@RequestParam(value = "id") String id);
}
