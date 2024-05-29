package com.erp.server.dmp.controller.feign;

import com.common.business.dto.base.BaseResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.ThirdMappingDTO;
import com.erp.server.dmp.service.ThirdMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 查询第三方映射
 *
 * @Author hyj
 * @Date 2024/05/28
 **/

@Slf4j
@RestController
@RequestMapping("feign/thirdMapping")
public class DmpThirdMappingFeignController {
    @Resource
    private ThirdMappingService thirdMappingService;

    @PostMapping("/getByThirdId")
    public Boolean getByThirdId(@RequestBody ThirdMappingDTO.ViewParamDTO viewParamDTO) {
        return thirdMappingService.getByThirdId(viewParamDTO);
    }
    @PostMapping("/batchAdd")
    public BaseResultDTO.AddDTO batchAdd(@RequestBody ThirdMappingDTO.FeignMappingDTO feignMappingDTO) {
        return thirdMappingService.batchAdd(feignMappingDTO);
    }
}
