package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.dmp.dto.AfterSaleProgressDTO;
import com.erp.model.dmp.dto.DmpThirdCityDTO;
import com.erp.model.dmp.dto.ThridUserInfoDTO;
import com.erp.model.dmp.entity.DmpThirdCityEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 *
 *
 * @author jack
 * @since 2025-12-18
 */
@FeignClient(value = "erp-dmp", path = "/feign/dmpThirdCity", contextId = "DmpThirdCityFeign",configuration = {FeignErrorDecoder.class})
public interface DmpThirdCityFeign {

    /**
     * @return
     * @author jack
     * @date: 2025-12-18
     */
    @PostMapping("/feign/dmpThirdCity/getThirdByAddress")
    List<DmpThirdCityDTO.ThirdAddressMappingDTO> getThirdByAddress(@RequestBody @Validated DmpThirdCityDTO.SysAddressParamsDTO dto) ;

}