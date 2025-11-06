package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncTaskDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.dto.DmpOutputTaskRecordDTO;
import com.erp.model.dmp.dto.DmpPushTaskDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;

@FeignClient(name = "erp-dmp" , contextId = "DmpInoutTaskFeign",configuration = {FeignErrorDecoder.class})
public interface DmpInoutTaskFeign {
	/**
     * 更新任务状态
     */
    @PostMapping("feign/inout/updateOutputTaskRecord")
    ApiResult<Boolean> updateOutputTaskRecord(@RequestBody DmpOutputTaskRecordDTO.UpdateDTO updateDTO);

    /**
     * 查询同步数据
     */
    @PostMapping("feign/inout/getErrorData")
    DmpPushTaskDTO.SyncInfoDTO getErrorData(@RequestBody @Valid DmpSyncTaskDTO.OneDTO oneDTO);

    /**
     * 公共-创建快速输入任务
     */
    @PostMapping("feign/inout/doHotfixInputTask")
    Boolean doInputTask(@RequestBody List<DmpInoutDTO.CreateInputDTO> createDTOList);

    /**
     * 公共-创建快速输入任务
     */
    @PostMapping("feign/inout/doHotfixReturnInputTask")
    List<String> doHotfixReturnInputTask(@RequestBody List<DmpInoutDTO.CreateInputDTO> createDTOList);

    /**
     * 公共-查询输入任务最新状态
     */
    @PostMapping("feign/inout/newInputTaskList")
    List<DmpInoutDTO.LastOneDTO> newInputTaskList(@RequestBody List<DmpInoutDTO.CommonDTO> commonDTOList);

    /**
     * 公共-查询输入任务明细记录
     */
    @PostMapping("feign/inout/inputDetailList")
    List<DmpInoutDTO.ListDTO> inputDetailList(@RequestBody List<DmpInoutDTO.CommonDTO> commonDTOList);

    /**
     * 公共-查询输入任务明细记录
     */
    @PostMapping("dmpInout/querySyncByIds")
    ApiResult<?> querySyncIds(@RequestBody BaseIdsDTO.IdsDTO dto);


    /**
     * 更新任务
     */
    @PostMapping("feign/inout/updateDmpOutputTaskRecordEntity")
    Boolean updateDmpOutputTaskRecordEntity(@RequestBody List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList);

    /**
     * 获取最后一条拉取记录
     * @author will
     * @date 2025/8/27 18:23
     * @param paramDTO
     * @return LastPullDTO
     */
    @PostMapping("feign/inout/getLastPullRecord")
    DmpPushTaskDTO.LastPullDTO getLastPullRecord(@RequestBody DmpPushTaskDTO.LastPullParamDTO paramDTO);
}