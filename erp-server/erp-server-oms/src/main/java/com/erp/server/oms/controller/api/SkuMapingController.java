package com.erp.server.oms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.oms.service.SkuMapingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 *sku对照
 *
 * @author Lambda
 * @since 2023-06-28
 */
@RestController
@RequestMapping("/skuMaping")
public class SkuMapingController extends BaseController {


    @Resource
    private SkuMapingService skuMapingService;




    /**
     * 下载模板
     *
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        skuMapingService.downloadTemplate(response);
        return success();
    }




}
