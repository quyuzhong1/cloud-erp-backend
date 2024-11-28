package com.erp.rpc.wms.feign;


import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-wms", contextId = "AliexpressDelivery")
public interface AliexpressDeliveryFeign {

    /**
     * 新增速卖通发货单
     * @Author Luo_WG
     * @Date 2024/1/30 17:09
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping(value = "/feign/aliexpressDelivery/add")
    BaseResultDTO.AddDTO add(@RequestBody AliexpressDeliveryDTO.AddDTO dto);

    /**
     * 更新速卖通发货单 出库状态
     * @param statusDTO
     */
    @PostMapping(value = "/feign/aliexpressDelivery/updateAliexpressOustock")
    void updateAliexpressOustock(@RequestBody AliexpressDeliveryDTO.StatusDTO statusDTO);
}
