package com.erp.server.dmp.controller.api;

import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.server.dmp.query.DmpTaskQueryHandler;
import com.erp.server.dmp.service.DmpPullTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * 中台同步任务表
 *
 * @author zhangchunlin
 * @since 2023-06-29
 */
@LogSystemModule("系统监控->单据同步拉取查询")
@RestController
@RequestMapping("/dmpPullTask")
public class DmpPullTaskController extends BaseController {

    @Autowired
    private DmpPullTaskService dmpPullTaskService;

    /**
     * 获取 tab列表
     * @author Will
     * @date: 2023/10/13 11:49
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    public ApiResult<List<DmpPullTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<DmpPullTaskDTO.TabListDTO> tabList = dmpPullTaskService.tabList(dto);
        return success(tabList);
    }

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
        PagingVO<DmpPullTaskDTO.ListDTO> pagingVO = dmpPullTaskService.paging(dto);
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
    public ApiResult exportExcel(@RequestBody DmpPullTaskDTO.ParamDTO dto) {
        Boolean flag = dmpPullTaskService.exportExcel(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 重新同步（批量同步）
     * @author Will
     * @date: 2023/10/13 15:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/batchSync")
    public ApiResult batchSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean flag = dmpPullTaskService.batchSync(dto.getIds());
        return flag == true ? success() : failure();
    }
    /**
     * 批量修改无需同步
     *
     * @param dto
     * @return ApiResult
     * @author hyj
     * @date 2024/4/30 10:51
     */
    @LogAction(value = LogActionEnum.UPDATE_STATUS, desc = "批量修改为无需同步")
    @PostMapping(value = "/batchNoNeedSync")
    public ApiResult batchNoNeedSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean flag = dmpPullTaskService.batchNoNeedSync(dto.getIds());
        return flag == true ? success() : failure();
    }

}
