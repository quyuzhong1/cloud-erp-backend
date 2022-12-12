package com.erp.server.bi.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseIdDTO;
import com.erp.model.bi.dto.MyDashboardDTO;
import com.erp.model.bi.dto.UpdateDashboardShareDTO;
import com.erp.server.bi.service.BiSubjectDefaultService;
import com.erp.server.bi.service.BiSubjectService;
import com.erp.server.bi.service.BiSubjectShareService;
import com.erp.server.bi.service.CommonService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 仪表盘
 *
 * @author yl
 * @since 2022-12-08 14:33:46
 */
@RestController
@RequestMapping("bi/dashboard")
public class BiDashboardController extends BaseController {

    @Resource
    private BiSubjectShareService biSubjectShareService;

    @Resource
    private BiSubjectService subjectService;

    @Resource
    private BiSubjectDefaultService subjectDefaultService;


    @Resource
    private CommonService commonService;




    /**
     * 设置仪表盘的分享
     *
     * @return 查询结果
     */
    @PostMapping("/setShare")
    public ApiResult setShare(@RequestBody @Validated UpdateDashboardShareDTO dto) {
        Boolean flag = biSubjectShareService.setShare(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 设置仪表盘默认
     *
     * @return 查询结果
     */
    @PostMapping("/setDefault")
    public ApiResult setShare(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = subjectDefaultService.setDefault(dto.getId());
        return flag == true ? success() : failure();
    }


    /**
     * 我的仪表盘
     *
     * @return 查询结果
     */
    @GetMapping("/my/list")
    public ApiResult<MyDashboardDTO> setShare(String searchKeyword) {
        String userId = commonService.getUserInfo().getUid();
        MyDashboardDTO myDashboard = subjectService.myDashboard(userId,searchKeyword);
        return success(myDashboard);
    }

    /**
     * 删除仪表盘
     */
    @PostMapping("/delete")
    public ApiResult deleteById(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = this.subjectService.deleteById(dto.getId());
        return flag == true ? success() : failure();
    }

}

