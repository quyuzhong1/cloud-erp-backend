package com.erp.server.scm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.scm.dto.KingdeePaymentConditionDTO;
import com.erp.model.scm.entity.KingdeePaymentConditionEntity;
import com.erp.server.scm.service.KingdeePaymentConditionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 金蝶付款条件
 *
 * @author Lambda
 * @since 2024-03-08
 */
@Slf4j
@RestController
@RequestMapping("/kingdeePaymentCondition")
public class KingdeePaymentConditionController extends BaseController {

    @Resource
    private KingdeePaymentConditionService kingdeePaymentConditionService;



    /**
     * 付款条件下拉
     * @return
     */
    @GetMapping("/select")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> select() {
        List<KingdeePaymentConditionEntity> list = kingdeePaymentConditionService.listAll();
        List<BaseDropDownDTO.DisabledDTO> result = list.stream().sorted(Comparator.comparing(KingdeePaymentConditionEntity::getDisabled))
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getCode(),x.getName(),x.getDisabled()))
                .collect(Collectors.toList());
        return success(result);
    }


    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "scm:kingdeePaymentCondition:update",
        serviceClass = KingdeePaymentConditionService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated KingdeePaymentConditionDTO.UpdateDTO dto) {
        kingdeePaymentConditionService.update(dto);
        return success();
    }



}
