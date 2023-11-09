package com.erp.rpc.tms.feign;

import com.erp.model.tms.dto.LogisticsBillDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "logisticsBill")
public interface LogisticsBillFeign {

    /**
     * 新增物流单
     * @Author Luo_WG
     * @Date 2023/11/9 17:59
     * @param addDTOList
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/logisticsBill/logisticsBillBatchAdd")
    Boolean logisticsBillBatchAdd(@RequestBody List<LogisticsBillDTO.AddDTO> addDTOList);
}
