package com.erp.server.workflow.controller.api;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.workflow.dto.CfgApproveSyncDTO;
import com.erp.model.workflow.entity.CfgProcessEntity;
import com.erp.model.workflow.entity.CfgProcessRuleEntity;
import com.erp.server.workflow.handler.CfgProcessQueryHandler;
import com.erp.server.workflow.service.CfgProcessRuleService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.workflow.service.CfgProcessService;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.CfgProcessDTO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    public ApiResult<Boolean> export(@RequestBody @Validated PagingDTO<CfgProcessDTO.SearchParamDTO> dto) {
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
    @LogAction(value = LogActionEnum.DELETE, desc = "批量启用/删除")
    @PostMapping("/updateState")
    public ApiResult<List<BatchResultDTO>> updateState(@RequestBody @Validated UpdateStateDTO.BatchUpdateDTO stateDTO) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>();
        List<String> ids = stateDTO.getIds().stream().distinct().collect(Collectors.toList());
        // 查询所有要更新的审核条件
        List<CfgProcessRuleEntity> entityList = cfgProcessRuleService.listByIds(ids);
        Boolean disabled = stateDTO.getDisabled();
        //获取审核条件对应的流程配置
        Map<String, CfgProcessEntity> processEntityMap = cfgProcessService.list(new LambdaQueryWrapper<CfgProcessEntity>().in(CfgProcessEntity::getId, entityList.stream().map(CfgProcessRuleEntity::getCfgProcessId).collect(Collectors.toSet()))).stream()
                .collect(Collectors.toMap(CfgProcessEntity::getId, e -> e));
        //处理要更新的审核条件
        for (String id : ids) {
            CfgProcessRuleEntity entity = entityList.stream()
                    .filter(e -> id.equals(e.getId()))
                    .findFirst()
                    .orElse(null);
            //判断审核条件是否存在
            if (Objects.isNull(entity)) {
                resultDTOS.add(BatchResultDTO.fail(id, id, "流程规则不存在"));
                continue;
            }
            //判断审核条件状态是否发生变化
            CfgProcessEntity cfgProcess = processEntityMap.get(entity.getCfgProcessId());
            if (disabled.equals(entity.getDisabled())) {
                resultDTOS.add(BatchResultDTO.fail(id, cfgProcess.getCode() + SourceTypeEnum.getName(cfgProcess.getBussinessKey()), "状态未发生变化"));
                continue;
            }
            //更新审核条件
            try {
                // 更新状态
                entity.setDisabled(disabled);
                entity.setUpdateTime(LocalDateTime.now());
                cfgProcessRuleService.updateById(entity);
                resultDTOS.add(BatchResultDTO.success(id, cfgProcess.getCode() + SourceTypeEnum.getName(cfgProcess.getBussinessKey())));
            } catch (Exception e) {
                log.error("流程规则更改状态失败，ID: {}", id, e);
                resultDTOS.add(BatchResultDTO.fail(id, cfgProcess.getCode() + SourceTypeEnum.getName(cfgProcess.getBussinessKey()), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 批量删除
     *
     * @param idsDTO idsDTO
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "批量删除")
    @PostMapping("/delete")
    public ApiResult<String> delete(@RequestBody @Validated BaseIdsDTO.IdsDTO idsDTO) {
        cfgProcessService.delete(idsDTO.getIds());
        return success();
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
            tableAlias = "p"
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
}
