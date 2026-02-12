package com.erp.server.auth.controller.openapi;

import java.util.List;

import javax.annotation.Resource;

import com.erp.model.dmp.dto.DmpInoutDTO;
import org.springframework.web.bind.annotation.RequestBody;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpCfgEtlDTO;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.server.auth.config.OpenApi;


/**
 * 中台任务openapi
 * @author Administrator
 *
 */
@OpenApi
public class DmpTaskOpenApi {

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;

    /**
     * 生成ETL任务
     * @param dto
     * @return
     */
    @OpenApi("doEtlTask")
    public ApiResult<List<BatchResultDTO>> doEtlTask(@RequestBody DmpCfgEtlDTO.DoTaskDTO dto){
        return dmpInoutTaskFeign.doEtlTask(dto);
    }


    /**
     * 生成dmp_input_task任务
     * @return
     */
    @OpenApi("createInputTaskTask")
    public ApiResult<Boolean> createInputTaskTask(@RequestBody List<DmpInoutDTO.CreateInputDTO> createDTOList){
        return ApiResult.success(dmpInoutTaskFeign.doInputTask(createDTOList));
    }
}
