package com.erp.server.fms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.fms.dto.DictBasicDTO;
import com.erp.server.fms.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典管理
 *
 * @author yl
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/dict")
public class DictBasicController extends BaseController {


    @Resource
    private DictBasicService dictBasicService;


    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictBasicDTO.ListDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取对应字典数据
     * 财务系统相关字典类型
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictBasicDTO.ListDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(key);
        return success(list);
    }


    /**
     * 字典通用下拉列表
     * @param type 字典类型
     * @param remark 备注
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<DictBasicDTO.DropDownDTO>> dictDropDown(@RequestParam(value = "type")String type, @RequestParam(value = "remark", required = false) String remark) {
        List<DictBasicDTO.DropDownDTO> result =  dictBasicService.listByType(type, remark);
        return success(result);
    }

}
