package com.erp.server.plm.controller;

import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.DocsShowDTO;
import com.erp.model.plm.dto.SysLogSelectDTO;
import com.erp.model.plm.dto.SysLogShowDTO;
import com.erp.server.plm.service.SysDocsService;
import com.erp.server.plm.service.SysLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 操作日志
 * @author Will
 * @version 1.0
 * @date 2022/12/5 20:36
 */
@RestController
@RequestMapping("/plm/sys/log")
public class SysLogController extends BaseController {

    @Autowired
    private SysLogService sysLogService;

    /**
     * 操作日志-列表查询
     * @author Will
     * @date: 2022/12/5 21:29
     * @param dto
     * @return ApiResult<PagingVO<SysLogShowDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<SysLogShowDTO>> paging(@RequestBody @Validated PagingDTO<SysLogSelectDTO> dto){
        dto.getParams().getClassName();
        PagingVO<SysLogShowDTO> pagingVO=sysLogService.paging(dto);
        return success(pagingVO);
    }

}
