package com.erp.server.wms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.wms.dto.CartonDTO;
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
import com.erp.server.wms.service.PackingTaskService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.PackingTaskDTO;

import java.util.List;

/**
 * 装箱任务表
 *
 * @author zdy
 * @since 2024-07-02
 */
@Slf4j
@RestController
@LogSystemModule("装箱任务表")
@RequestMapping("/packingTask")
public class PackingTaskController extends BaseController {

    @Resource
    private PackingTaskService packingTaskService;
    /**
     * 获取状态统计
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<PackingTaskDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(packingTaskService.tabList(dto));
    }
    /**
     * 装箱任务-分页列表
     * @author zdy
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<PackingTaskDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<PackingTaskDTO.PagingParamDTO> dto) {
        return success(packingTaskService.paging(dto));
    }

    /**
    * 新增
    * @author zdy
    * @date:  2024-07-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "装箱任务表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated PackingTaskDTO.AddDTO dto) {
        return success(packingTaskService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-07-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "装箱任务表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:packingTask:update",
        serviceClass = PackingTaskService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated PackingTaskDTO.UpdateDTO dto) {
        packingTaskService.update(dto);
        return success();
    }



}
