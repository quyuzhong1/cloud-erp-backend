package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.model.plm.dto.SysProductFieldDTO;
import com.erp.server.plm.service.SysProductFieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Classname 系统设置字段
 * @Description TODO
 * @Date 2022-09-15 11:53
 * @Created by yl
 */
@RestController
@RequestMapping("/plm/sys/field")
public class SysProductFieldController extends BaseController {

    @Autowired
    private SysProductFieldService sysProductFieldService;

    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated SysProductFieldDTO dto) {
        Boolean flag = sysProductFieldService.saveOrUpdateField(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated StateDTO dto) {
        Boolean flag = sysProductFieldService.updateState(dto);
        return flag == true ? success() : failure();
    }

    @PostMapping("/paging")
    public ApiResult paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO pagingVO = sysProductFieldService.paging(dto);
        return success(pagingVO);
    }
}
