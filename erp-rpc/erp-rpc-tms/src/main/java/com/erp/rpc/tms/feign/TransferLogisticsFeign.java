package com.erp.rpc.tms.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.tms.dto.TransferLogisticsChannelDTO;
import com.erp.model.tms.dto.TransferLogisticsSupplierDTO;
import com.erp.model.tms.entity.TransferLogisticsChannelEntity;
import com.erp.model.tms.entity.TransferLogisticsSupplierEntity;
import com.erp.model.tms.enums.TransferLogisticsStatusEnum;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 中转报关服务商
 */
@FeignClient(name = "erp-tms", contextId = "transferLogistics" ,configuration = {FeignErrorDecoder.class})
public interface TransferLogisticsFeign {

    /**
     * 更改中转物流商启用状态
     * @param updateDisabledDTO
     * @return
     */
    @PostMapping("/feign/transferLogistics/updateDisabledBySupplierId")
    Boolean updateDisabledBySupplierId(@RequestBody TransferLogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO);

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    @PostMapping("/feign/transferLogistics/listBySupplierId")
    List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId);

    /**
     * 根据id查询中转服务商
     * @param supplierId
     * @return
     */
    @PostMapping("/feign/transferLogistics/getLogisticsSupplierById")
    TransferLogisticsSupplierEntity getLogisticsSupplierById(@RequestBody String supplierId);

    /**
     * 根据id查询中转服务商
     * @param supplierIds
     * @return
     */
    @PostMapping("/feign/transferLogistics/listLogisticsSupplierByIds")
    List<TransferLogisticsSupplierEntity> listLogisticsSupplierByIds(@RequestBody List<String> supplierIds);

    /**
     * 根据主标id查询渠道信息
     * @param supplierIdList
     * @return
     */
    @PostMapping("/feign/transferLogistics/listLogisticsChannelByMainId")
    List<TransferLogisticsChannelEntity> listLogisticsChannelByMainId(@RequestBody List<String> supplierIdList);

    /**
     * 查询平台单据状态
     * @param shippingOrderNo
     * @param transferLogisticsSupplierId
     * @return
     */
    @GetMapping("/feign/transferLogistics/getPlatformTransferStatus")
    TransferLogisticsStatusEnum getPlatformTransferStatus(@RequestParam("shippingOrderNo") String shippingOrderNo,
                                                          @RequestParam("transferLogisticsSupplierId") String transferLogisticsSupplierId);


    @PostMapping("/feign/transferLogistics/listByTransferChannelIds")
    List<TransferLogisticsChannelDTO.ListSelectDTO> listByTransferChannelIds(@RequestBody List<String> channelIds);
}
