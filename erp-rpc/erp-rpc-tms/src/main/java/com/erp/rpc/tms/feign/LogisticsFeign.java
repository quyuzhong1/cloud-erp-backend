package com.erp.rpc.tms.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.vo.request.LogisticsQueryBaseVO;
import com.erp.model.tms.vo.response.LogisticsOrderResponseVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
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

    /**
     * 根据物流商id 获取到对应的渠道的启用禁用列表
     * @param supplierId
     * @return
     */
    @GetMapping("/feign/logistics/listBySupplierId")
    List<BaseIdDTO.CodeDTO> listBySupplierId(@RequestBody String supplierId);

    @GetMapping("/feign/logistics/updateDisabledBySupplierId")
    Boolean updateDisabledBySupplierId(@RequestBody LogisticsSupplierDTO.UpdateDisabledDTO updateDisabledDTO);
}
