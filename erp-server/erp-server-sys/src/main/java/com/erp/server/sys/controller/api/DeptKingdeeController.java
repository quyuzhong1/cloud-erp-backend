package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.server.sys.service.DeptKingdeeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2023-07-07
 */
@RestController
@RequestMapping("/deptKingdee")
public class DeptKingdeeController extends BaseController {


    @Resource
    private DeptKingdeeService deptKingdeeService;


    /**
     * 导入
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = deptKingdeeService.importFile(excelFile, response);
        return result ? success() : failure();
    }

}
