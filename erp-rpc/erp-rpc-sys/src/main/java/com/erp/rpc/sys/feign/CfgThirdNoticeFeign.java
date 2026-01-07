package com.erp.rpc.sys.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.AuthUserShopDTO;
import com.erp.model.sys.dto.AuthUserWarehouseDTO;
import com.erp.model.sys.dto.CfgThirdNoticeDTO;
import com.erp.model.sys.dto.SysUserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author jack
 * @ClassName CfgThirdNoticeFeign
 * @description:
 * @date 2025-12-29
 * @version: 1.0
 */
@FeignClient(name = "erp-sys", contextId = "CfgThirdNoticeFeign",configuration = {FeignErrorDecoder.class})
public interface CfgThirdNoticeFeign {

    @GetMapping("/feign/cfgThirdNotice/dropDownByMoldMonitor")
    List<CfgThirdNoticeDTO.DropDownDTO> dropDownByMoldMonitor(@RequestParam(value = "sourceType",required = true) String sourceType);

}
