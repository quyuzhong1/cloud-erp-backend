package com.erp.server.oms.controller.feign;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.core.controller.BaseController;
import com.erp.server.oms.service.SoChangeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 采购单
 *
 * @Author Luo_WG
 * @Date 2023/5/15 9:12
 **/
@RestController
@RequestMapping("feign/soChange")
public class SoChangeFeignController extends BaseController {
    @Resource
    private SoChangeService soChangeService;

    /**
     * 销售变更审核
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("/approve")
    public Boolean approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        return soChangeService.approve(dto);
    }
}
