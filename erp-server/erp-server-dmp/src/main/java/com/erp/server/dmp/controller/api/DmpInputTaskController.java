package com.erp.server.dmp.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpOutputTaskDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.DmpInputTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpInputTaskDTO;

import java.util.List;

/**
 * 拉取任务
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("拉取任务")
@RequestMapping("/dmpInputTask")
public class DmpInputTaskController extends BaseController {

    @Resource
    private DmpInputTaskService dmpInputTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "拉取任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpInputTaskDTO.AddDTO dto) {
        return success(dmpInputTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "拉取任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpInputTask:update",
        serviceClass = DmpInputTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpInputTaskDTO.UpdateDTO dto) {
        dmpInputTaskService.update(dto);
        return success();
    }

    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:15
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.DmpInputTaskDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    public ApiResult<List<DmpInputTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<DmpInputTaskDTO.TabListDTO> tabList = dmpInputTaskService.tabList(dto);
        return success(tabList);
    }

    /**
     * 推送任务列表分页查询
     * @Author Luo_WG
     * @Date 2024/9/3 14:28
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.dmp.dto.DmpInputTaskDTO.PagingDTO>>
     **/
    public ApiResult<PagingVO<DmpInputTaskDTO.PagingDTO>> paging(@RequestBody @Validated PagingDTO<DmpInputTaskDTO.PagingParamDTO> dto) {
        PagingVO<DmpInputTaskDTO.PagingDTO> pagingVO = dmpInputTaskService.paging(dto);
        return success(pagingVO);
    }

}
