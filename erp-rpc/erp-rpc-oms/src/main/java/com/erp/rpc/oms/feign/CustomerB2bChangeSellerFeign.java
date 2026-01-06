package com.erp.rpc.oms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.vo.CustomerInfoVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "customerB2bChangeSellerFeign",configuration = {FeignErrorDecoder.class})
public interface CustomerB2bChangeSellerFeign {

    @PostMapping("feign/customerB2bChangeSeller/approve")
    ApiResult<List<BatchResultDTO>> approve(@RequestBody BaseApproveParamDTO dto);

}