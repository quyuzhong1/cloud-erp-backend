package com.erp.server.dmp.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.server.dmp.service.CfgApiAuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * API授权信息
 * @author Will
 * @version 1.0
 * @date 2023/1/11 11:47
 */
@RestController
@RequestMapping("dmp/cfgApiAuth")
public class CfgApiAuthController extends BaseController {

    @Resource
    private CfgApiAuthService cfgApiAuthService;

    /**
     * 新增
     * @author Will
     * @date: 2023/1/11 16:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    public ApiResult add(@ModelAttribute @Validated CfgApiAuthDTO dto) {
        Boolean flag = this.cfgApiAuthService.insert(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑
     * @author Will
     * @date: 2023/1/11 16:18
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    public ApiResult update(@ModelAttribute @Validated CfgApiAuthDTO dto) {
        this.cfgApiAuthService.update(dto);
        return success();
    }

}
