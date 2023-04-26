package com.erp.server.workflow.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.workflow.dto.ProcessDTO;
import com.erp.model.workflow.dto.ProcessDefinitionDTO;
import com.erp.server.workflow.service.ProcessDefinitionService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * 流程定义
 *
 * @author Cloud
 * @since 2023-04-21
 */
@RestController
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
    @PostMapping("/addOrUpdate")
    public ApiResult<Boolean> addOrUpdate(@RequestBody @Valid ProcessDefinitionDTO.AddOrUpdateDTO dto) {
        boolean result = processDefinitionService.saveOrUpdate(dto);
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
    @PostMapping("/deploy")
    public ApiResult<ProcessDTO.DeployResultDTO> deploy(@RequestBody @Validated ProcessDTO.DeployDTO dto) {
        try {
            ProcessDTO.DeployResultDTO resultDTO= processDefinitionService.deploy(dto);
            return success(resultDTO);
        }catch (Exception e){
            return failure(e.getMessage());
        }
    }

    /**
     * 流程定义复制
     */
    @PostMapping("/copy")
    public ApiResult copy(@RequestBody @Validated ProcessDefinitionDTO.CopyDTO dto){
        // 复制流程定义
        ProcessDefinitionDTO.CopyResultDTO result = processDefinitionService.copy(dto);
        return success(result);
    }

    /**
     * 流程定义导出excel
     */
    @PostMapping("/exportExcel")
    public ApiResult<Boolean> exportExcel(@RequestBody @Validated ProcessDefinitionDTO.QueryExportDTO dto, HttpServletResponse response){
        // 导出excel
        Boolean result = processDefinitionService.exportExcel(dto,response);
        return success(result);
    }

}

