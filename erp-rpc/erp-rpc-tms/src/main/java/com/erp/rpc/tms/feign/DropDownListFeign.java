package com.erp.rpc.tms.feign;


import com.common.business.dto.base.BaseDropDownDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 中转报关服务商
 */
@FeignClient(name = "erp-tms", contextId = "dropDownList")
public interface DropDownListFeign {

    @GetMapping("/feign/drop/down/dict/list")
    List<BaseDropDownDTO.CommonDTO> list(@RequestParam("key") String key);
}
