package com.erp.server.dmp.controller.feign;


import com.erp.model.dmp.entity.DmpThirdCityEntity;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpThirdCityService;
import com.erp.model.dmp.dto.DmpThirdCityDTO;

import java.util.List;

/**
 * 第三方城市字典表
 *
 * @author jack
 * @since 2025-12-17
 */
@Slf4j
@RestController
@LogSystemModule("第三方城市字典表")
@RequestMapping("/feign/dmpThirdCity")
public class DmpThirdCityFeignController extends BaseController {

    @Resource
    private DmpThirdCityService dmpThirdCityService;


    /**
     * 根据erp的地址获取第三方城市信息
     * @author jack
     * @date:  2025-12-17
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/getThirdByAddress")
    public List<DmpThirdCityDTO.ThirdAddressMappingDTO> getThirdByAddress(@RequestBody DmpThirdCityDTO.SysAddressParamsDTO dto) {
        return dmpThirdCityService.getThirdByAddress(dto);
    }



}
