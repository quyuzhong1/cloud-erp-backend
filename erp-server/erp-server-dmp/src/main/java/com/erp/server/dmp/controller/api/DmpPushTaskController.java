package com.erp.server.dmp.controller.api;


import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.server.dmp.service.DmpPushTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 中台推送任务表
 *
 * @author Cloud
 * @since 2023-09-06
 */
@Slf4j
@RestController
@RequestMapping("/dmpPushTask")
public class DmpPushTaskController extends BaseController {

    @Autowired
    private DmpPushTaskService dmpPushTaskService;

   /**
    * 获取 tab列表
    * @author Will
    * @date: 2023/10/13 11:49
    * @param dto
    * @return ApiResult<List<TabListDTO>>
    */
    @PostMapping("/tabList")
    public ApiResult<List<DmpPushTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<DmpPushTaskDTO.TabListDTO> tabList = dmpPushTaskService.tabList(dto);
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
    public ApiResult<PagingVO<DmpPushTaskDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<DmpPushTaskDTO.ParamDTO> dto) {
        PagingVO<DmpPushTaskDTO.ListDTO> pagingVO = dmpPushTaskService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 导出
     * @author Will
     * @date: 2023/10/13 15:34
     * @param dto
     * @param response
     * @return ApiResult
     */
    @PostMapping(value = "/exportExcel")
    public ApiResult exportExcel(@RequestBody DmpPushTaskDTO.ParamDTO dto, HttpServletResponse response) {
        Boolean flag = dmpPushTaskService.exportExcel(dto, response);
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
        Boolean flag = dmpPushTaskService.batchSync(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 重新查询数据后同步（批量同步）
     * @author Will
     * @date: 2023/10/13 15:34
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/batchFindDataSync")
    public ApiResult batchFindDataSync(@RequestBody BaseIdsDTO.IdsDTO dto) {
        Boolean flag = dmpPushTaskService.batchFindDataSync(dto.getIds());
        return flag == true ? success() : failure();
    }

}
