package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.AdsPushTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.AdsPushTaskDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;

/**
 * ads推送任务
 *
 * @author shukai
 * @since 2025-10-28
 */
@Slf4j
@RestController
@LogSystemModule("ads推送任务")
@RequestMapping("/adsPushTask")
public class AdsPushTaskController extends BaseController {

    @Resource
    private AdsPushTaskService adsPushTaskService;

    /**
    * 新增
    * @author shukai
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "ads推送任务新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated AdsPushTaskDTO.AddDTO dto) {
        return success(adsPushTaskService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2025-10-28
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "ads推送任务修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:adsPushTask:update",
        serviceClass = AdsPushTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated AdsPushTaskDTO.UpdateDTO dto) {
        adsPushTaskService.update(dto);
        return success();
    }

    /**
     * 获取 tab列表
     * @Author Luo_WG
     * @Date 2024/9/3 15:15
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.DmpOutputTaskDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    public ApiResult<List<DmpOutputTaskRecordDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
    	List<DmpOutputTaskRecordDTO.TabListDTO> tabList = adsPushTaskService.tabList(dto);
        return success(tabList);
    }


}
