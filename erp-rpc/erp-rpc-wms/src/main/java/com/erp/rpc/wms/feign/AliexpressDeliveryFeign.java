package com.erp.rpc.wms.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.wms.dto.AliexpressDeliveryDTO;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-wms", contextId = "AliexpressDelivery" ,configuration = {FeignErrorDecoder.class})
public interface AliexpressDeliveryFeign {

    /**
     * 新增/更新速卖通发货单
     * @Author Luo_WG
     * @Date 2024/1/30 17:09
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping(value = "/feign/aliexpressDelivery/addOrUpdate")
    BaseResultDTO.AddDTO addOrUpdate(@RequestBody AliexpressDeliveryDTO.AddDTO dto);

    /**
     * 更新速卖通发货单 出库状态
     * @param statusDTO
     */
    @PostMapping(value = "/feign/aliexpressDelivery/updateAliexpressOustock")
    void updateAliexpressOustock(@RequestBody AliexpressDeliveryDTO.StatusDTO statusDTO);


    /**
     * 根据销售订单id查询
     */
    @PostMapping(value = "/feign/aliexpressDelivery/listBySoId")
    List<AliexpressDeliveryEntity> listBySoId(@RequestBody String soId);
}
