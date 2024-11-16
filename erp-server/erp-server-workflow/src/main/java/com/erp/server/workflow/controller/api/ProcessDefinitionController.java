package com.erp.server.workflow.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.server.workflow.service.ProcessDefinitionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

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
     * 分页查询流程定义
     *
     * @param dto
     * @return ApiResult<Boolean>
     */
    @PostMapping("/page")
    public ApiResult<PagingVO<ProcessDefinitionDTO.ListDTO>> page(@RequestBody PagingDTO<ProcessDefinitionDTO.QueryDTO> dto) {
        PagingVO<ProcessDefinitionDTO.ListDTO> pagingVO = processDefinitionService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 删除流程定义
     *
     * @param dto
     * @return ApiResult<Boolean>
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除流程定义")
    @PostMapping("/delete")
    public ApiResult<Boolean> delete(@RequestBody @Valid ProcessDefinitionDTO.DeleteDTO dto) {
        boolean result = processDefinitionService.deleteByIds(dto.getIds());
        return result ? success() : failure();
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
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated ProcessDefinitionDTO.QueryExportDTO dto){
        // 导出excel
        Boolean result = processDefinitionService.exportExcel(dto);
        return success(result);
    }

}

