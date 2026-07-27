package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.sys.openapi.AiyaChangeAttributeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 仓位移动 Feign
 */
@FeignClient(name = "erp-wms", contextId = "warehouseLocationMove", configuration = {FeignErrorDecoder.class})
public interface WarehouseLocationMoveFeign {

    /**
     * 接收爱亚库存状态转移反馈，幂等生成已审核《仓位移动》。
     *
     * @param dto 爱亚转移单业务数据
     * @return 仓位移动主单 id（已存在时返回原单 id）
     */
    @PostMapping("/feign/warehouseLocationMove/receiveAiyaChangeAttribute")
    String receiveAiyaChangeAttribute(@RequestBody @Validated AiyaChangeAttributeDTO dto);
}
