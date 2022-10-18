package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.BusinessProcessDTO;
import com.erp.model.plm.entity.BusinessProcessEntity;
import com.erp.server.plm.service.BusinessProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程接口
 *
 * @Classname
 * @Description TODO
 * @Date 2022-10-18 10:40
 * @Created by yl
 */
@RestController
@RequestMapping("plm/process")
public class BusinessProcessController extends BaseController {


    @Autowired
    private BusinessProcessService businessProcessService;

    /**
     * 获取流程名及相关信息
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<BusinessProcessEntity>> getProcessList(String businessType) {
        List<BusinessProcessEntity> resultList = businessProcessService.getProcessList(businessType);
        return success(resultList);
    }


    /**
     * 获取流程名及相关信息
     *
     * @return
     */
    @PostMapping("/save")
    public ApiResult saveProcess(@RequestBody BusinessProcessDTO dto) {
        Boolean flag = businessProcessService.saveProcess(dto);
        return flag == true ? success() : failure();
    }


}
