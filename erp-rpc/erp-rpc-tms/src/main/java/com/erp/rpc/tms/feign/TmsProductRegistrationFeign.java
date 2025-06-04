package com.erp.rpc.tms.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-tms", contextId = "productRegistration")
public interface TmsProductRegistrationFeign {

    /**
     * 获取已备案的数据
     * @Author jack
     * @Date 2025-06-03
     **/
    @PostMapping("/feign/productRegistration/listByRegistered")
    List<ProductRegistrationEntity> listByRegistered();

}
