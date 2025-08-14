package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soPriceFeign",configuration = {FeignErrorDecoder.class})
public interface SoPriceFeign {
    /**
     * 销售价目表审核
     * @author will
     * @date 2025/3/31 18:10
     * @param dto
     * @return java.util.List<com.common.business.dto.base.BatchResultDTO>
     */
    @PostMapping("feign/soPrice/approve")
    ApiResult<List<BatchResultDTO>> approve(@RequestBody BaseApproveParamDTO dto);
    /**
     * 销售调价表审核
     * @author will
     * @date 2025/3/31 18:10
     * @param dto
     * @return java.util.List<com.common.business.dto.base.BatchResultDTO>
     */
    @PostMapping("feign/soPrice/approveChange")
    ApiResult<List<BatchResultDTO>> approveChange(@RequestBody BaseApproveParamDTO dto);
}
