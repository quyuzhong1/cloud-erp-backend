package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.ProcessDelegateDTO;
import com.erp.model.workflow.entity.ProcessDelegateEntity;
import com.erp.server.workflow.query.ProcessDelegateQueryHandler;
import com.erp.server.workflow.service.ProcessDelegateService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 委托审批
 *
 * @author will
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("委托审批")
@RequestMapping("/processDelegate")
public class ProcessDelegateController extends BaseController {

    @Resource
    private ProcessDelegateService processDelegateService;


    /**
     * 获取状态统计
     * @author will
     * @date 2025/5/12 16:10
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "start_user_id",
            menuCode = "workflow:processDelegate:paging",
            tableAlias = "pd"
    )
    public ApiResult<List<ProcessDelegateDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(processDelegateService.tabList(dto));
    }

    /**
     * 列表查询
     * @author will
     * @date: 2025-05-12
     * @param dto
     * @return ApiResult<PagingVO<ProcessDelegateDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "start_user_id",
            menuCode = "workflow:processDelegate:paging",
            tableAlias = "pd"
    )
    @WebAdvanceQuery(handler = ProcessDelegateQueryHandler.class)
    public ApiResult<PagingVO<ProcessDelegateDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ProcessDelegateDTO.PagingParamDTO> dto) {
        return success(processDelegateService.paging(dto));
    }

    /**
    * 新增
    * @author will
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "委托审批新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated ProcessDelegateDTO.AddDTO dto) {
        return success(processDelegateService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "委托审批修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "start_user_id",
        menuCode = "workflow:processDelegate:update",
        serviceClass = ProcessDelegateService.class,
        keyIdName = "id")
    public ApiResult<Boolean> update(@RequestBody @Validated ProcessDelegateDTO.UpdateDTO dto) {
        processDelegateService.update(dto);
        return success();
    }

    /**
     * 详情
     * @author will
     * @date:  2025-05-12
     * @param id
     * @return ApiResult<ProcessDelegateDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "start_user_id",
            menuCode = "workflow:processDelegate:view",
            serviceClass = ProcessDelegateService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<ProcessDelegateDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(processDelegateService.view(id));
    }


    /**
     * 终止委托
     * @author will
     * @date 2025/5/12 16:14
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/closeDelegate")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "start_user_id",
            menuCode = "workflow:processDelegate:closeDelegate",
            serviceClass = ProcessDelegateService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.CANCEL, desc = "委托审批单终止委托")
    public ApiResult<List<BatchResultDTO>> closeDelegate(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = processDelegateService.closeDelegate(id);
            }catch (Exception e){
                log.error("委托审批单终止委托失败",e);
                ProcessDelegateEntity entity = processDelegateService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "委托审批单不存在, 终止委托失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 导出Excel
     * @author will
     * @date 2025/5/13 11:09
     * @param dto
     * @return ApiResult<Boolean>
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "委托审批导出Excel数据")
    @WebAdvanceQuery(handler = ProcessDelegateQueryHandler.class)
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "start_user_id",
            menuCode = "workflow:processDelegate:paging",
            tableAlias = "pd"
    )
    public ApiResult<Boolean> exportList(@RequestBody @Validated ProcessDelegateDTO.PagingParamDTO dto) {
        processDelegateService.exportList(dto);
        return success();
    }


    /**
     * 获取委托审批信息
     * @author will
     * @date 2025/5/19 17:48
     * @return ApiResult<ProcessDelegateEntity>
     */
    @GetMapping("/listStartUserId")
    public ApiResult<List<FindUserDTO>> listStartUserId() {
        return success(processDelegateService.listStartUserId());
    }
}
