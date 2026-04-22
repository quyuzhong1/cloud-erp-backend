package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.erp.model.tms.entity.LogisticsOrderEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "logisticsOrder", configuration = {FeignErrorDecoder.class})
public interface LogisticsOrderFeign {

    /**
     * 批量添加物流单
     *
     * @param entityList List<LogisticsOrderEntity>
     * @return List<AfterSaleDTO.LogisticsOrderResultDTO>
     */
    @PostMapping("/feign/logisticsOrder/addBatch")
    List<AfterSaleDTO.LogisticsOrderResultDTO> addBatch(@RequestBody List<LogisticsOrderEntity> entityList);

    /**
     * 根据物流跟踪号批量查询物流单
     *
     * @param trackNoList List<String>
     * @return List<LogisticsOrderDTO.ListDTO>
     */
    @PostMapping("/feign/logisticsOrder/getLogisticsOrderListByTrackNo")
    List<LogisticsOrderDTO.ListDTO> getLogisticsOrderListByTrackNo(@RequestBody List<String> trackNoList);

    /**
     * 批量取消物流单
     *
     * @param codeList List<String>
     * @return List<AfterSaleDTO.LogisticsOrderResultDTO>
     */
    @PostMapping("/feign/logisticsOrder/batchCancel")
    List<AfterSaleDTO.LogisticsOrderResultDTO> batchCancel(List<String> codeList);
}
