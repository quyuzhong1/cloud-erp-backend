package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerCreditApplyDTO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.vo.CustomerInfoVO;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("feign/customerCredit")
@Slf4j
public class CustomerCreditFeignController extends BaseController {

    @Resource
    private CustomerCreditApplyService customerCreditApplyService;

    @PostMapping("/updateCustomerCredit")
    public ApiResult<Object> updateCustomerCredit(@RequestBody CustomerCreditApplyDTO.UpdateStatusDTO dto){
        return success(customerCreditApplyService.updateCustomerCredit(dto));
    }
}
