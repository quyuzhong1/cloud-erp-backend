package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseDicDTO;
import com.erp.model.plm.dto.BasicDictDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.server.plm.service.BasicDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;

/**
 * <p>
 * plm 字典表 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/dict")
public class BasicDictController extends BaseController {

    @Autowired
    private BasicDictService basicDictService;

    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdateDict(@RequestBody @Validated List<BasicDictDTO> dtos) {
        Boolean flag = basicDictService.saveOrUpdateDict(dtos);
        return flag == true ? success() : failure();
    }

    @PostMapping("/remove")
    public ApiResult saveOrUpdateDict(String id) {
        Boolean flag = basicDictService.removeById(id);
        return flag == true ? success() : failure();
    }

    @GetMapping("/list")
    public ApiResult list(String type) {
        List<BasicDictEntity> list = basicDictService.listByType(type);
        return success(list);
    }


}