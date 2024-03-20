package com.erp.rpc.tms.feign;

import com.erp.model.tms.entity.LogisticsBillEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "tmsFirstMileLogistic")
public interface TmsFirstMileLogisticFeign {

    /**
     * 根据来源id查询物流单
     * @Author Luo_WG
     * @Date 2024/1/25 18:31
     * @param sourceIds
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/feign/tmsFirstMileLogistic/listBySourceIds")
    List<LogisticsBillEntity> listBySourceIds(@RequestBody List<String> sourceIds);
}
