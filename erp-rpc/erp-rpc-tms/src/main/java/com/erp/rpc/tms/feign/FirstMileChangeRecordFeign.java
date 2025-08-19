package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.validator.ValidList;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "firstMileChangeRecord" ,configuration = {FeignErrorDecoder.class})
public interface FirstMileChangeRecordFeign {
    /**
     * 新增
     * @author zdy
     * @date:  2025-05-12
     * @param dtoList
     * @return ApiResult<String>
     */
    @PostMapping("/feign/firstMileChangeRecord/batchAdd")
    List<BaseResultDTO.AddDTO> batchAdd(@RequestBody @Validated List<FirstMileChangeRecordDTO.AddDTO> dtoList);
}
