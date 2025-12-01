package com.erp.server.sys.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.OperateLogDTO;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogSystemModule;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.sys.service.OperateLogService;
import com.common.core.controller.vo.ApiResult;

/**
 * 操作日志表
 *
 * @author jack
 * @since 2025-05-23
 */
@Slf4j
@RestController
@LogSystemModule("操作日志表")
@RequestMapping("/operateLog")
public class OperateLogController extends BaseController {

    @Resource
    private OperateLogService operateLogService;

    /**
     * 操作日志-列表查询
     * @author Will
     * @date: 2023/3/17 10:58
     * @param dto
     * @return ApiResult<PagingVO<listDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<OperateLogDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OperateLogDTO.SearchDTO> dto){
        PagingVO<OperateLogDTO.ListDTO> pagingVO = operateLogService.paging(dto);
        return success(pagingVO);
    }



}
