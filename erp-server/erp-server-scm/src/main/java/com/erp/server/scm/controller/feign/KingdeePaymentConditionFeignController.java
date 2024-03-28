package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.erp.server.scm.service.KingdeePaymentConditionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname KingdeePaymentConditionFeignController
 * @Description TODO
 * @Date 2024-03-28 15:12
 * @Created by yl
 */
@RestController
@RequestMapping("feign/kingdeePaymentCondition")
public class KingdeePaymentConditionFeignController {

    @Resource
    private KingdeePaymentConditionService kingdeePaymentConditionService;



    @GetMapping("/listPaymentCondition")
    public List<BaseDropDownDTO.DisabledDTO> listPaymentCondition(){
        List<KingdeePaymentConditionEntity> list = kingdeePaymentConditionService.list();
        List<BaseDropDownDTO.DisabledDTO> result = list.stream().sorted(Comparator.comparing(KingdeePaymentConditionEntity::getDisabled))
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getCode(),x.getName(),x.getDisabled()))
                .collect(Collectors.toList());
        return result;
    }
}
