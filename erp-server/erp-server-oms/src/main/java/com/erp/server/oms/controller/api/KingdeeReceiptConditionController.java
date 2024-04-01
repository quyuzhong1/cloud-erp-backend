package com.erp.server.oms.controller.api;


import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.model.oms.entity.KingdeeReceiptConditionEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.oms.service.KingdeeReceiptConditionService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.KingdeeReceiptConditionDTO;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 金蝶收款条件
 *
 * @author Lambda
 * @since 2024-03-08
 */
@Slf4j
@RestController
@LogSystemModule("金蝶收款条件")
@RequestMapping("/kingdeeReceiptCondition")
public class KingdeeReceiptConditionController extends BaseController {

    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;


    /**
     * 收款条件下拉
     * @return
     */
    @GetMapping("/select")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> select() {
        List<KingdeeReceiptConditionEntity> list = kingdeeReceiptConditionService.list();
        List<BaseDropDownDTO.DisabledDTO> result = list.stream().sorted(Comparator.comparing(KingdeeReceiptConditionEntity::getDisabled))
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getId(),x.getName(),x.getDisabled()))
                .collect(Collectors.toList());
        return success(result);
    }

}
