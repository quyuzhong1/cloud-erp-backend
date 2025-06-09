package com.erp.server.plm.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.BasicDictDTO;
import com.erp.model.plm.dto.DictControllerDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import com.erp.server.plm.service.BasicDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 公共接口
 * @author yl
 * @since 2022-09-13
 */
@RestController
@LogSystemModule("系统通用设置")
@RequestMapping("dict")
public class BasicDictController extends BaseController {

    @Autowired
    private BasicDictService basicDictService;

    /**
     * 字典管理-保存或者修改plm字典表
     * @Date 2022/10/17 15:34
     * @param dtos dtos
     * @return com.common.core.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "保存或者修改plm字典表")
    @PostMapping("/saveOrUpdate")
    public ApiResult<Object> saveOrUpdateDict(@RequestBody @Validated List<BasicDictDTO> dtos) {
        Boolean flag = basicDictService.saveOrUpdateDict(dtos);
        return flag == true ? success() : failure();
    }

    /**
     * 字典管理-删除plm字典表
     * @Date 2022/10/17 15:34
     * @param id id
     * @return com.common.core.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除plm字典表")
    @PostMapping("/remove")
    public ApiResult<Object> saveOrUpdateDict(String id) {
        Boolean flag = basicDictService.removeById(id);
        return flag == true ? success() : failure();
    }

    /**
     * 新增产品 产品属性，产品等级，品牌 列表
     * @author yl
     * @date 2022-10-11 14:34
     * @param type productProperty 产品属性, productGrade 产品等级, productBrand 产品品牌, declareProperty 报关属性, country 国家, productType 产品部门类型
     * @return com.common.core.vo.ApiResult
     */
    @GetMapping("/list")
    public ApiResult<List<BasicDictEntity>> list(String type) {
        List<BasicDictEntity> list = basicDictService.listByType(type);
        return success(list);
    }

    /**
     * 字典下拉框
     * @mock productBrand
     * @param type productProperty 产品属性, productGrade 产品等级, productBrand 产品品牌, declareProperty 报关属性, country 国家, projectState 项目状态, approvalStatus 立项状态
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictControllerDTO.DictDropDownDTO>> listDictDropDown(@RequestParam(name = "type") String type){
        BasicDictTypeEnum enumByType = BasicDictTypeEnum.getEnumByType(type);
        if (null == enumByType){
            return success(new ArrayList<>());
        }
        List<DictControllerDTO.DictDropDownDTO> result = basicDictService.listDictDropDown(enumByType.getCode());
        return success(result);
    }


}