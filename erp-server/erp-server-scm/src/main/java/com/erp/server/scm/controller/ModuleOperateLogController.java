package com.erp.server.scm.controller;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.ModuleOperateLogDTO;
import com.erp.server.scm.service.ModuleOperateLogService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;

/**
 * 操作日志表
 *
 * @author will
 * @since 2023-03-17
 */
@RestController
@RequestMapping("/moduleOperateLog")
public class ModuleOperateLogController extends BaseController {

    @Resource
    private ModuleOperateLogService moduleOperateLogService;

   /**
    * @description: 操作日志-列表查询
    * @author Will
    * @date: 2023/3/17 10:58
    * @param dto
    * @return ApiResult<PagingVO<listDTO>>
    */
    @PostMapping("/paging")
    public ApiResult<PagingVO<ModuleOperateLogDTO.listDTO>> paging(@RequestBody @Validated PagingDTO<ModuleOperateLogDTO.searchDTO> dto){
        PagingVO<ModuleOperateLogDTO.listDTO> pagingVO = moduleOperateLogService.paging(dto);
        return success(pagingVO);
    }
}
