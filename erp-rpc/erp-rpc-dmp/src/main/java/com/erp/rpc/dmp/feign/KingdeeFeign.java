package com.erp.rpc.dmp.feign;


import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;

/**
 * 售后申请小程序端接口
 *
 * @author jack
 * @since 2025-04-07
 */
@FeignClient(value = "erp-dmp", path = "feign/k3cloud", contextId = "k3cloudFeign",configuration = {FeignErrorDecoder.class})
public interface KingdeeFeign {

    /**
     * 汇率批量保存
     * @return
     * @author cloud
     * @date: 2025-09-30
     */
    @PostMapping("/batch/add")
    ApiResult batchAdd(@RequestBody HashMap<String, List<HashMap<String, Object>>> paramMap);

    /**
     * 汇率批量审核
     * @return
     * @author cloud
     * @date: 2025-09-30
     */
    @PostMapping("/batch/approve")
    ApiResult batchApprove(@RequestBody HashMap<String, List<HashMap<String, Object>>> paramMap);

}