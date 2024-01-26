package com.erp.rpc.tms.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-tms", contextId = "transferDeclare")
public interface TransferDeclareFeign {

    /**
     * 新增中转报关单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param dto
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/feign/transferDeclare/add")
    BaseResultDTO.AddDTO add(@RequestBody TransferDeclareDTO.AddDTO dto);
}
