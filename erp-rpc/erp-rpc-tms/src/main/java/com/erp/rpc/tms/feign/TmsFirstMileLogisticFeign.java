package com.erp.rpc.tms.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
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
     * @param outstockIds
     * @return com.common.business.dto.base.BaseResultDTO.AddDTO
     **/
    @PostMapping("/feign/tmsFirstMileLogistic/listByOutstockIds")
    List<LogisticsBillEntity> listByOutstockIds(@RequestBody List<String> outstockIds);

    /**
     * 自动生成头程物流单
     **/
    @PostMapping("/feign/tmsFirstMileLogistic/autoGenerateFirstMileLogistic")
    BatchResultDTO autoGenerateFirstMileLogistic(@RequestBody AutoGenerateBillDTO autoGenerateBillDTO);
}
