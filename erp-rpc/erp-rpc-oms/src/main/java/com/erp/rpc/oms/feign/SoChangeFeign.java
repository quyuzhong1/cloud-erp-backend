package com.erp.rpc.oms.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "erp-oms", contextId = "soChange")
public interface SoChangeFeign {

    /**
     * 销售变更审核
     * @Author Luo_WG
     * @Date 2023/7/4 12:28
     * @param dto
     * @return java.lang.Boolean
     **/
    @PostMapping("feign/soChange/approve")
    Boolean approve(@RequestBody BaseApproveParamDTO dto);
}
