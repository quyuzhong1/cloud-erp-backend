package com.erp.rpc.tms.feign;

import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "logistics")
public interface LogisticsFeign {

    /**
     * 根据虾皮订单号查询物流单号
     * @param logisticsQueryVOList
     * @return
     */
    @PostMapping("/feign/logistics/queryOrderList")
    ApiResult<List<LogisticsOrderResponseVO>> queryOrderList(@RequestBody List<LogisticsQueryBaseVO> logisticsQueryVOList);
}
