package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.TmsFirstMileLogisticDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.enums.ProductRegistrationEnum;
import com.erp.server.tms.service.ProductRegistrationService;
import com.erp.server.tms.service.TmsDeclareBillService;
import com.erp.server.tms.service.TmsFirstMileLogisticService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@LogSystemModule("产品备案")
@RequestMapping("/feign/productRegistration")
public class TmsProductRegistrationFeignController {

    @Resource
    private ProductRegistrationService poductRegistrationService;

    /**
     * 获取已备案的数据
     * @Author jack
     * @Date 2025-06-03
     **/
    @PostMapping("/listByRegistered")
    public List<ProductRegistrationEntity> listByRegistered() {
        return poductRegistrationService.lambdaQuery().ne(ProductRegistrationEntity::getStatus, ProductRegistrationEnum.StatusEnum.REGISTERED.getCode()).list();
    }


}
