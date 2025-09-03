package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.oms.dto.ExhibitionOrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.List;

@FeignClient(name = "erp-oms", contextId = "exhibitionOrderFeign",configuration = {FeignErrorDecoder.class})
public interface ExhibitionOrderFeign {

    /**
     * 获取展会订单sku已使用的数据（未审批）
     * @author jack
     * @date: 2025-09-02
     * @param dto
     * @return List<ExhibitionOrderDTO.FreezeQtyBySku>
     */
    @PostMapping("feign/exhibitionOrder/listFreezeQtyBySku")
    List<ExhibitionOrderDTO.FreezeQtyBySku> listFreezeQtyBySku(@RequestBody @Validated ExhibitionOrderDTO.SearchDTO dto);

}
