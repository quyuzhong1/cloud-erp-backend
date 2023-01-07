package com.erp.server.sys.controller.api;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.sys.dto.SysCodeSkuDTO;
import com.erp.server.sys.service.SysCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统编码
 *
 * @author Will
 * @version 1.0
 * @description:
 * @date 2022/11/21 11:33
 */
@RestController
@RequestMapping("/sys/code")
public class SysCodeController extends BaseController {

    @Autowired
    private SysCodeService sysCodeService;


    /**
     * 系统编码-根据编码信息生成系统编码
     *
     * @author Will
     * @date: 2022/11/21 12:14
     * @param dto
     * @return ApiResult
     */
    @RequestMapping("/getSkuNo")
    public ApiResult getSkuNo(@RequestBody @Validated SysCodeSkuDTO dto) {
        String sysCode = sysCodeService.getSkuNo(dto);
        return success(sysCode);
    }

}
