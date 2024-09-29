package com.erp.server.mrp.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.CfgDataArchivingDTO;
import com.erp.server.mrp.service.CfgDataArchivingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * <p>
 * 归档配置 前端控制器
 * </p>
 *
 * @author liaohui
 * @since 2024-09-26
 */
@RestController
@RequestMapping("/cfg-data-archiving")
public class CfgDataArchivingController extends BaseController {

    @Resource
    private CfgDataArchivingService cfgDataArchivingService;

    @PostMapping
    public ApiResult<String> save(@RequestBody CfgDataArchivingDTO dto){
        cfgDataArchivingService.saveData(dto);
        return success();
    }
}
