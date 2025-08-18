package com.erp.rpc.srm.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "erp-srm" ,contextId = "srmTaskFeign",configuration = {FeignErrorDecoder.class})
public interface SrmTaskFeign {
    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/srmSyncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);

    /**
     * 新中台查询同步
     * @param syncParamDTO
     * @return
     */
    @PostMapping("/feign/srmSyncKingdee/newFindDataSendSyncTask")
    Map<String, Map<String, Object>> newFindDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}
