package com.erp.rpc.dmp.feign;


import com.common.business.dto.base.BaseResultDTO;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author hyj
 * @description: DMP第三方绑定数据
 */
@FeignClient(value = "erp-dmp", path = "feign/thirdMapping", contextId = "DmpThirdMappingFeign")
public interface DmpThirdMappingFeign {

    /**
     * 查询绑定关系
     */
    @PostMapping("/getByThirdId")
    Boolean getByThirdId(@RequestBody ThirdMappingDTO.ViewParamDTO viewParamDTO);

    /**
     * 店铺id查询绑定关系
     */
    @PostMapping("/batchAdd")
    BaseResultDTO.AddDTO batchAdd(@RequestBody @Validated ThirdMappingDTO.FeignMappingDTO feignMappingDTO);
}