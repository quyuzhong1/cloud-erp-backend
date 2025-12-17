package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseIdsDTO;
import com.erp.model.dmp.dto.DmpSyncKingdeeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * @author Will
 * @description: DMP远程调用接口
 * @date: 2023/1/12 16:54
 */
@FeignClient(value = "erp-dmp", path = "feign/dmp/", contextId = "DmpSyncFeign",configuration = {FeignErrorDecoder.class})
public interface DmpSyncFeign {

    /**
     * 根据多个id查询推送任务
     * @param paramDTO
     * @return
     */
    @PostMapping("sync/listKingdeeData")
    List<Map<String, Object>> listKingdeeData(@RequestBody @Valid DmpSyncKingdeeDTO.ParamDTO paramDTO);
    /**
     * 无需同步
     * @param dto
     * @return
     */
    @PostMapping("/dmpOutputTaskRecord/batchNoNeedSyncBySourceCode")
    Boolean batchNoNeedSyncBySourceCode(@RequestBody BaseIdsDTO.SourceCodeDTO dto);
}