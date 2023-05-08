package com.erp.server.plm.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
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
 *  TODO
 * @Date 2022-09-15 11:53
 * @Created by yl
 */
@RestController
@RequestMapping("sys/field")
public class SysProductFieldController extends BaseController {

    @Autowired
    private ProductFieldService productFieldService;


    /**
     * 新增或者修改字段
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    //  @RequestPermissions("plm:sys:field:saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SysProductFieldDTO dto) {
        Boolean flag = productFieldService.saveOrUpdateSysField(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 修改字段状态
     */
    @PostMapping("/updateState")
    //  @RequestPermissions("plm:sys:field:updateState")
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
    //  @RequestPermissions("plm:sys:field:paging")
    public ApiResult<PagingVO<SysProductFieldPagingDTO>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<SysProductFieldPagingDTO> pagingVO = productFieldService.sysPaging(dto);
        return success(pagingVO);
    }

    /**
     * 获取 产品开发管理-设置-新增字段-系统字段列表
     */
    @GetMapping("/list")
    //  @RequestPermissions("plm:sys:field:list")
    public ApiResult paging() {
       List<Map<String,Object>> list=productFieldService.sysList();
        return success(list);
    }
}
