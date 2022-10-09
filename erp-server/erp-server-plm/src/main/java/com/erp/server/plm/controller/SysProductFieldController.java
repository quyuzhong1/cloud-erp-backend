package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.dto.SysProductFieldDTO;
import com.erp.model.plm.dto.SysProductFieldPagingDTO;
import com.erp.server.plm.service.ProductFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品系统通用设置
 *
 * @Classname
 * @Description TODO
 * @Date 2022-09-15 11:53
 * @Created by yl
 */
@RestController
@RequestMapping("/plm/sys/field")
public class SysProductFieldController extends BaseController {

    @Autowired
    private ProductFieldService productFieldService;


    /**
     * 新增或者修改字段
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SysProductFieldDTO dto) {
        Boolean flag = productFieldService.saveOrUpdateSysField(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改字段状态
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated StateDTO dto) {
        Boolean flag = productFieldService.updateState(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 字段配置分页列表
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SysProductFieldPagingDTO>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<SysProductFieldPagingDTO> pagingVO = productFieldService.sysPaging(dto);
        return success(pagingVO);
    }

    /**
     * 修改字段状态
     */
    @GetMapping("/list")
    public ApiResult paging() {
       List<Map<String,Object>> list=productFieldService.sysList();
        return success(list);
    }
}
