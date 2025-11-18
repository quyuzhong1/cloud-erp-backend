package com.erp.rpc.workflow.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.ApproveSyncRecordEntity;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * ApproveSyncRecordFeign Feign
 * @date 2025-05-29
 * @author jack
 */
@FeignClient(name = "erp-workflow", contextId = "approveSyncRecordFeign",configuration = {FeignErrorDecoder.class})
public interface ApproveSyncRecordFeign {

    /**
     */
    @PostMapping("feign/approveSyncRecord/add")
    void add(@RequestBody ApproveSyncRecordEntity approveSyncRecordEntity);
    /**
     */
    @PostMapping("feign/approveSyncRecord/updateById")
    void updateById(@RequestBody ApproveSyncRecordEntity approveSyncRecordEntity);
}
