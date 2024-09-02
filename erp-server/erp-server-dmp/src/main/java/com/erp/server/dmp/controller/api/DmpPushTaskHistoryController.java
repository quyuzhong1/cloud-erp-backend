package com.erp.server.dmp.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.server.dmp.query.DmpTaskQueryHandler;
import com.erp.server.dmp.service.DmpPushTaskHistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 中台推送任务历史表
 *
 */
@Slf4j
@RestController
@RequestMapping("/dmpPushTask/history")
public class DmpPushTaskHistoryController extends BaseController {

    @Resource
    private DmpPushTaskHistoryService dmpPushTaskHistoryService;

    /**
     * 分页查询
     * @author Will
     * @date: 2023/10/13 11:49
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = DmpTaskQueryHandler.class)
    public ApiResult<PagingVO<DmpPushTaskDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        PagingVO<DmpPushTaskDTO.ListDTO> pagingVO = dmpPushTaskHistoryService.paging(dto);
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
    public ApiResult<String> exportExcel(@RequestBody DmpPushTaskDTO.ParamDTO dto) {
        Boolean flag = dmpPushTaskHistoryService.exportExcel(dto);
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
        Boolean flag = dmpPushTaskHistoryService.batchSync(dto.getIds());
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }

    /**
     * 重新查询数据后同步（批量同步）
     * @author Will
     * @date: 2023/10/13 15:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/batchFindDataSync")
    public ApiResult<String> batchFindDataSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean flag = dmpPushTaskHistoryService.batchFindDataSync(dto.getIds());
        return Boolean.TRUE.equals(flag) ? success() : failure();
    }


}
