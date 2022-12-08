package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.model.plm.dto.BasicDictDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.server.plm.service.BasicDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.erp.common.controller.BaseController;

import java.util.List;

/**
 * 公共接口
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("plm/dict")
public class BasicDictController extends BaseController {

    @Autowired
    private BasicDictService basicDictService;

    /**
     * 字典管理-保存或者修改plm字典表
     * @Date 2022/10/17 15:34
     * @param dtos dtos
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdateDict(@RequestBody @Validated List<BasicDictDTO> dtos) {
        Boolean flag = basicDictService.saveOrUpdateDict(dtos);
        return flag == true ? success() : failure();
    }

    /**
     * 字典管理-删除plm字典表
     * @Date 2022/10/17 15:34
     * @param id id
     * @return com.erp.common.dto.base.ApiResult
     **/
    @PostMapping("/remove")
    public ApiResult saveOrUpdateDict(String id) {
        Boolean flag = basicDictService.removeById(id);
        return flag == true ? success() : failure();
    }

    /**
     * 新增产品 产品属性，产品等级，品牌 列表
     * @author yl
     * @date 2022-10-11 14:34
     * @param type productProperty 产品属性, productGrade 产品等级, productBrand 产品品牌, declareProperty 报关属性, country 国家
     * @return com.erp.common.dto.base.ApiResult
     */
    @GetMapping("/list")
    public ApiResult<List<BasicDictEntity>> list(String type) {
        List<BasicDictEntity> list = basicDictService.listByType(type);
        return success(list);
    }


}