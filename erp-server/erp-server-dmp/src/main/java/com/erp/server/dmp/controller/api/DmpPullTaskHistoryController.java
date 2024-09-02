package com.erp.server.dmp.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.server.dmp.query.DmpTaskQueryHandler;
import com.erp.server.dmp.service.DmpPullTaskHistoryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;


/**
 * 中台同步任务表
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@RestController
@RequestMapping("/dmpPullTask/history")
public class DmpPullTaskHistoryController extends BaseController {

    @Resource
    private DmpPullTaskHistoryService dmpPullTaskHistoryService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/10/13 11:49
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = DmpTaskQueryHandler.class)
    public ApiResult<PagingVO<DmpPullTaskDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpPullTaskDTO.ParamDTO> dto) {
        PagingVO<DmpPullTaskDTO.ListDTO> pagingVO = dmpPullTaskHistoryService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 导出
     * @author Will
     * @date: 2023/10/13 15:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult<String> exportExcel(@RequestBody DmpPullTaskDTO.ParamDTO dto) {
        Boolean flag = dmpPullTaskHistoryService.exportExcel(dto);
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }


    /**
     * 重新同步（批量同步）
     * @author Will
     * @date: 2023/10/13 15:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/batchSync")
    public ApiResult<String> batchSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean flag = dmpPullTaskHistoryService.batchSync(dto.getIds());
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

}
