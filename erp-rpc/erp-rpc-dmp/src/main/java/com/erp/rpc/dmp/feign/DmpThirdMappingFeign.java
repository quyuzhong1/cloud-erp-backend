package com.erp.rpc.dmp.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.model.dmp.entity.ThirdMappingEntity;
import com.erp.model.dmp.entity.ThirdWarehouseEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * DMP远程调用ThirdMapping接口
 *
 * @author tanmujin
 * @date 2024-05-27
 */
@FeignClient(value = "erp-dmp", path = "/feign/dmp/thirdMapping", contextId = "dmpThirdMappingFeign")
public interface DmpThirdMappingFeign {

    @GetMapping("/getBySysId")
    ThirdWarehouseEntity getBySysId(@RequestParam String sysId);

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

    /**
     * 新增、编辑
     */
    @PostMapping("/add")
    BaseResultDTO.AddDTO add(@RequestBody @Validated ThirdMappingDTO.AddDTO dto);

    @PostMapping("/view")
    ThirdMappingDTO.MappingViewDTO view(@RequestBody @Validated ThirdMappingDTO.ViewParamDTO viewParamDTO);
}
