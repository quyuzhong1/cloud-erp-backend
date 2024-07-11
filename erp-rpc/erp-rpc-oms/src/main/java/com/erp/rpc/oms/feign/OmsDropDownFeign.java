package com.erp.rpc.oms.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.oms.dto.CfgRuleOrderHandleDTO;
import com.erp.model.oms.entity.DictBasicEntity;
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

    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return com.erp.model.oms.entity.DictBasicEntity
     */
    @GetMapping("feign/drop/down/dict/getByTypeAndValue")
    DictBasicEntity getByTypeAndValue(@RequestParam("type") String type, @RequestParam("value") String value);

}
