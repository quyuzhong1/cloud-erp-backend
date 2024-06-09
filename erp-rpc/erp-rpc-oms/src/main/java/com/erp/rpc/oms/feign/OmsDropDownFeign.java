package com.erp.rpc.oms.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(name = "erp-oms", contextId = "dropDown")
public interface OmsDropDownFeign {

    /**
     * 获取下拉列表
     */
    @GetMapping("feign/drop/down/dict/tree")
    List<BaseDropDownDTO.Tree> tree(@RequestParam("key") String key);


}
