package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.DictBasicEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "omsDropDownFeign",configuration = {FeignErrorDecoder.class})
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

    @GetMapping("feign/drop/down/dict/list")
    ApiResult<List<BaseDropDownDTO.CommonDTO>> list(@RequestParam("key") String key);

    @GetMapping("feign/drop/down/dict/listInternalSalesPlatform")
    ApiResult<List<BaseDropDownDTO.CommonDTO>> listInternalSalesPlatform(@RequestParam("key") String key);

}
