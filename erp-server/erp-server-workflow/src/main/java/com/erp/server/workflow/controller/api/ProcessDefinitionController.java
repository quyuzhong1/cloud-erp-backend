package com.erp.server.workflow.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.model.workflow.entity.ProcessDefinitionEntity;
import com.erp.server.workflow.service.ProcessDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

/**
 * 流程定义
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Slf4j
@RestController
@LogSystemModule("流程设计")
@RequestMapping("/process/definition")
public class ProcessDefinitionController extends BaseController {

    @Resource
    private ProcessDefinitionService processDefinitionService;

    /**
     * 新增或修改流程定义
     *
     * @param dto
     * @return ApiResult<Boolean>
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增或修改流程定义")
    @PostMapping("/addOrUpdate")
    public ApiResult<Boolean> addOrUpdate(@RequestBody @Valid ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        boolean result = processDefinitionService.addOrUpdate(dto);
        return result ? success() : failure();
    }

    /**
     * 获取状态统计
     * @author will
     * @date 2025/5/15 16:10
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    public ApiResult<List<ProcessDefinitionDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(processDefinitionService.tabList(dto));
    }

    /**
     * 分页查询流程定义
     *
     * @param dto
     * @return ApiResult<Boolean>
     */
    @PostMapping("/page")
    @WebAdvanceQuery
    public ApiResult<PagingVO<ProcessDefinitionDTO.ListDTO>> page(@RequestBody PagingDTO<ProcessDefinitionDTO.QueryDTO> dto) {
        PagingVO<ProcessDefinitionDTO.ListDTO> pagingVO = processDefinitionService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 删除流程定义
     *
     * @param list
     * @return ApiResult<Boolean>
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除流程定义")
    @PostMapping("/delete")
    public ApiResult<List<BatchResultDTO>> delete(@RequestBody @Valid List<ProcessDefinitionDTO.DeleteDTO> list) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());
        for (ProcessDefinitionDTO.DeleteDTO deleteDTO : list) {
            BatchResultDTO submit;
            try {
                submit = processDefinitionService.deleteByIds(deleteDTO.getId(),deleteDTO.getProcessVersion(),Boolean.TRUE);
            }catch (Exception e){
                log.error("流程设计删除失败",e);
                ProcessDefinitionEntity entity = processDefinitionService.getProcessVersionEntity(deleteDTO.getId(),deleteDTO.getProcessVersion());
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(deleteDTO.getId(), deleteDTO.getId(), "流程设计单不存在, 删除失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getProcessName(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 流程定义发布
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "流程定义发布:部署流程id={processDefinitionId}")
    @PostMapping("/deploy")
    public ApiResult<ProcessDTO.DeployResultDTO> deploy(@RequestBody @Validated ProcessDTO.DeployDTO dto) {
        try {
            ProcessDTO.DeployResultDTO resultDTO= processDefinitionService.deploy(dto);
            return success(resultDTO);
        }catch (Exception e){
            log.error("流程定义发布失败",e);
            return failure(e.getMessage());
        }
    }

    /**
     * 流程定义复制
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "流程定义复制:流程定义ID={id}")
    @PostMapping("/copy")
    public ApiResult<Object> copy(@RequestBody @Validated ProcessDefinitionDTO.CopyDTO dto){
        // 复制流程定义
        ProcessDefinitionDTO.CopyResultDTO result = processDefinitionService.copy(dto);
        return success(result);
    }

    /**
     * 流程定义导出excel
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "流程定义导出excel")
    @PostMapping("/exportExcel")
    @WebAdvanceQuery
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated ProcessDefinitionDTO.QueryExportDTO dto){
        // 导出excel
        Boolean result = processDefinitionService.exportExcel(dto);
        return success(result);
    }

    /**
     * 获取指定单据类型的ERP审批定义
     * @param bussinessKey
     * @return
     */
    @GetMapping("/getProcessDefinition")
    public ApiResult<List<ProcessDefinitionDTO.DropDTO>> getProcessDefinition(@RequestParam(value = "bussinessKey") String bussinessKey) {
        List<ProcessDefinitionDTO.DropDTO> result = processDefinitionService.getProcessDefinition(bussinessKey);
        return success(result);
    }


    /**
     * 更新启禁用状态
     * @author will
     * @date 2025/5/15 15:55
     * @param list
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/updateDisabled")
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_UPDATE, desc = "更新启禁用状态")
    public ApiResult<List<BatchResultDTO>> updateDisabled(@RequestBody @Validated List<ProcessDefinitionDTO.DisableDTO> list) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());
        for (ProcessDefinitionDTO.DisableDTO disableDTO : list) {
            BatchResultDTO submit;
            try {
                submit = processDefinitionService.updateDisabled(disableDTO);
            }catch (Exception e){
                log.error("流程设计启禁用失败",e);
                ProcessDefinitionEntity entity = processDefinitionService.getProcessVersionEntity(disableDTO.getId(),disableDTO.getProcessVersion());
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(disableDTO.getId(), disableDTO.getId(), "流程设计单不存在, 启禁用失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getProcessName(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 变更流程
     * @author will
     * @date 2025/5/15 15:55
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/changeProcess")
    public ApiResult<Boolean> changeProcess(@RequestBody @Validated ProcessDefinitionDTO.ProcessChangeDTO dto) {
       return success(processDefinitionService.changeProcess(dto));
    }

    /**
     * 高级查询下拉流程名称
     * type = push
     * @return
     */
    @GetMapping("/drop/down")
    public ApiResult<List<ProcessDefinitionDTO.DropDownDTO>> dropDown(@RequestParam("type") @Validated String type) {
        return success(processDefinitionService.dropDown(type));
    }

    /**
     * 下拉ERP审批定义
     * @return
     */
    @GetMapping("/processDefinition/drop/down")
    public ApiResult<List<ProcessDefinitionDTO.DropDownDTO>> proDropDown() {
        return success(processDefinitionService.proDropDown());
    }
}

