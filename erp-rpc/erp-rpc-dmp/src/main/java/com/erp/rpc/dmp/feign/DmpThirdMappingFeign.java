package com.erp.rpc.dmp.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * DMP远程调用ThirdMapping接口
 * @date 2024-05-27
 * @author tanmujin
 */
@FeignClient(value = "erp-dmp", path = "/feign/dmp/thirdMapping", contextId = "dmpThirdMappingFeign")
public interface DmpThirdMappingFeign {

    @GetMapping("/getBySysId")
    ThirdMappingEntity getBySysId(@RequestParam String sysId);

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
