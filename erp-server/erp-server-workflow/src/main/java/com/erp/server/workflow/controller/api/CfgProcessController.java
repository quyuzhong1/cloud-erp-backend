package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.CfgProcessDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;
import com.erp.server.workflow.handler.CfgProcessQueryHandler;
import com.erp.server.workflow.service.CfgProcessRuleService;
import com.erp.server.workflow.service.CfgProcessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * 流程配置
 *
 * @author hcg
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("流程配置")
@RequestMapping("/cfgProcess")
public class CfgProcessController extends BaseController {

    @Resource
    private CfgProcessService cfgProcessService;

    @Resource
    private CfgProcessRuleService cfgProcessRuleService;

    /**
     * 分页查询
     *
     * @param dto
     * @return ApiResult<PagingVO < CfgProcessDTO.ListDTO>>
     * @author Jason
     * @date: 2023/3/15 16:47
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "分页查询")
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgProcess:paging",
            tableAlias = "p")
    @WebAdvanceQuery(handler = CfgProcessQueryHandler.class)
    public ApiResult<PagingVO<CfgProcessDTO.ProcessViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
        PagingVO<CfgProcessDTO.ProcessViewDTO> pagingVO = cfgProcessService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 导出Excel
     *
     * @param dto
     **/
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel")
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgProcess:paging",
            tableAlias = "p")
    @WebAdvanceQuery(handler = CfgProcessQueryHandler.class)
    public ApiResult<Boolean> export(@RequestBody @Validated CfgProcessDTO.SearchParamDTO dto) {
        cfgProcessService.exportList(dto);
        return success(true);
    }

    /**
     * 流程配置详情
     *
     * @description:
     * @author: hcg
     * @date: 2025/4/9 14:40
     * @param: BaseIdDTO
     * @return: CfgInvoiceSettingDTO.ViewDTO
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgProcess:view",
            serviceClass = CfgProcessService.class,
            keyIdName = "settingId")
    @LogViewService
    public ApiResult<CfgProcessDTO.ViewDTO> view(@RequestParam(value = "settingId") String settingId) {
        return success(cfgProcessService.view(settingId));
    }

    /**
     * 批量启用/禁用
     * 1禁用、0启用
     *
     * @param stateDTO
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE_STATUS, desc = "批量启用/删除")
    @PostMapping("/updateState")
    public ApiResult<List<BatchResultDTO>> updateState(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO stateDTO) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<String> ids = stateDTO.getIds().stream().distinct().collect(Collectors.toList());
        // 查询所有要更新的审核条件
        List<CfgProcessRuleEntity> entityList = cfgProcessRuleService.listByIds(ids);
        //处理要更新的审核条件
        for (String id : ids) {
            CfgProcessRuleEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"流程规则不存在"));
                continue;
            }
            //更新审核条件
            try {
                BatchResultDTO resultDTO = cfgProcessRuleService.updateState(entity, stateDTO.getDisabled());
                resultDTOS.add(resultDTO);
            } catch (Exception e) {
                log.error("流程规则更改状态失败，ID: {}", id, e);
                CfgProcessEntity cfgProcessEntity = cfgProcessService.getById(entity.getCfgProcessId());
                resultDTOS.add(BatchResultDTO.fail(id, cfgProcessEntity.getCode() + SourceTypeEnum.getName(cfgProcessEntity.getBussinessKey()), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 批量删除
     *
     * @param dto
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除")
    @PostMapping("/delete")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CfgProcessRuleEntity> entityList = cfgProcessRuleService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            CfgProcessRuleEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"流程规则不存在"));
                continue;
            }
            try {
                resultDTOS.add(cfgProcessService.delete(entity));
            }catch (Exception e){
                log.error("流程规则删除失败",e);
                CfgProcessEntity cfgProcessEntity = cfgProcessService.getById(entity.getCfgProcessId());
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), ObjUtil.isEmpty(cfgProcessEntity) ? "" : cfgProcessEntity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 新增
     *
     * @param dto
     * @return ApiResult<String>
     * @author hcg
     * @date: 2025-05-12
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "流程配置新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgProcessDTO.AddOrUpdateDTO dto) {
        return success(cfgProcessService.add(dto));
    }

    /**
     * 更新
     *
     * @param dto
     * @return ApiResult<String>
     * @author hcg
     * @date: 2025-05-12
     */
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "workflow:cfgProcess:update",
            serviceClass = CfgProcessService.class,
            keyIdName = "id")
    @LogAction(value = LogActionEnum.UPDATE, desc = "流程配置更新")
    public ApiResult<BaseResultDTO.AddDTO> update(@RequestBody @Validated CfgProcessDTO.AddOrUpdateDTO dto) {
        return success(cfgProcessService.update(dto));
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "workflow:cfgProcess:paging",
            tableAlias = "cp"
    )
    public ApiResult<List<CfgProcessDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(cfgProcessService.tabList(dto));
    }

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/startThirdProcess")
    public ApiResult<List<CfgProcessDTO.TabListDTO>> startThirdProcess(@RequestBody CfgProcessDTO.StartDTO dto) {
        cfgProcessService.startThirdProcess(dto);
        return success();
    }

    /**
     * erp流程下拉
     * @author will
     * @date 2025/6/27 12:26
     * @return ApiResult<List<ProcessSelectDTO>>
     */
    @GetMapping("/listProcessSelect")
    public ApiResult<List<CfgProcessDTO.ProcessSelectDTO>> listProcessSelect() {
        return success(cfgProcessService.listProcessSelect());
    }
}
