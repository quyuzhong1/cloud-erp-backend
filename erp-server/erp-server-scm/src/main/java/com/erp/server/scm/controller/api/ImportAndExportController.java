package com.erp.server.scm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.scm.service.CommonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Lambda
 * @Classname ImportAndExportController
 * @Description TODO
 * @Date 2023-03-21 9:13
 * @Created by yl
 */
@RestController
@RequestMapping("/importAndExport")
public class ImportAndExportController extends BaseController {


    @Resource
    private CommonService commonService;


    /**
     * 下载模板
     *
     * @param
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "type") String type) {
        commonService.downloadTemplate(request,response,type);
        return success();
    }
}
