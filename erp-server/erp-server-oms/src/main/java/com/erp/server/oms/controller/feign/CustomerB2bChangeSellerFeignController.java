package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("feign/customerB2bChangeSeller")
@Slf4j
public class CustomerB2bChangeSellerFeignController extends BaseController {

    @Resource
    private CustomerB2bSellerChangeService customerB2bSellerChangeService;

    @Resource
    private CustomerInfoService customerInfoService;

    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<CustomerInfoEntity> entityList = customerInfoService.listByIds(ids);
        List<CustomerB2bSellerChangeEntity> changeEntityList = customerB2bSellerChangeService.listByMainIds(ids);

        for (String id : ids) {
            CustomerInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"客户信息不存在"));
                continue;
            }
            CustomerB2bSellerChangeEntity customerB2bSellerChangeEntity = changeEntityList.stream().filter(v->v.getMainId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(customerB2bSellerChangeEntity)){
                resultDTOS.add(BatchResultDTO.fail(id,entity.getCode(),"客户销售员变更单不存在"));
                continue;
            }
            try {
                BaseApproveParamDTO baseApproveParamDTO = new BaseApproveParamDTO();
                baseApproveParamDTO.setIds(Arrays.asList(id));
                baseApproveParamDTO.setType( dto.getType());
                baseApproveParamDTO.setComment(dto.getComment());
                baseApproveParamDTO.setIsNeedProcess(dto.getIsNeedProcess());
                baseApproveParamDTO.setDeliveryDate(dto.getDeliveryDate());

                resultDTOS.add(customerB2bSellerChangeService.approve(baseApproveParamDTO,customerB2bSellerChangeEntity,entity));
            }catch (Exception e){
                log.error("B2B客户销售员变更审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}
