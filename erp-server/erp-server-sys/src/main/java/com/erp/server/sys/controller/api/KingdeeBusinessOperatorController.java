package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.sys.service.KingdeeBusinessOperatorService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 金蝶业务员 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@RestController
@RequestMapping("/kingdeeBusinessOperator")
public class KingdeeBusinessOperatorController extends BaseController {

    @Resource
    private KingdeeBusinessOperatorService kingdeeBusinessOperatorService;


    /**
     * 导入
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        //Boolean result = kingdeeBusinessOperatorService.importFile(excelFile, response);
        // return result ? success() : failure();
        return success();
    }

}
