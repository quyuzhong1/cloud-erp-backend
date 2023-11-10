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
    @PostMapping("feign/logisticsBill/logisticsBillBatchSave")
    Boolean logisticsBillBatchSave(@RequestBody List<LogisticsBillDTO.AddDTO> addDTOList);


    /**
     * 根据来源id查询物流信息及跟踪号
     * @Author Luo_WG
     * @Date 2023/11/10 9:06
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     **/
    @PostMapping("feign/logisticsBill/listLogisticsBillVoBySourceIds")
    List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(@RequestBody List<String> sourceIdList);
}
