package com.erp.server.oms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.OperateLogDTO;
import com.erp.server.oms.service.OperateLogService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 操作日志表
 *
 * @author will
 * @since 2023-05-08
 */
@RestController
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
