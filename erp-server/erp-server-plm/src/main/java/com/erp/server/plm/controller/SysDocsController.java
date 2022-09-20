package com.erp.server.plm.controller;


import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.DocsDTO;
import com.erp.model.plm.dto.StateDTO;
import com.erp.server.plm.service.SysDocsService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.erp.common.controller.BaseController;

import java.util.Map;

/**
 * <p>
 * 系统产品文档 前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@RestController
@RequestMapping("/plm/sys/docs")
public class SysDocsController extends BaseController {

    @Autowired
    private SysDocsService sysDocsService;


    @PostMapping("/paging")
    public ApiResult paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto){
        PagingVO pagingVO=sysDocsService.paging(dto);
        return success(pagingVO);
    }

    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody DocsDTO dto){
         sysDocsService.saveOrUpdateDocs(dto);
         return success();
    }


    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated StateDTO dto){
       Boolean flag= sysDocsService.updateState(dto);
       return flag==true?success():failure();
    }

}

