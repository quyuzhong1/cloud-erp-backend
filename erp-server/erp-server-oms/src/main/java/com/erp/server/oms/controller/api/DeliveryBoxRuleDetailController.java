package com.erp.server.oms.controller.api;


import com.erp.model.oms.dto.DeliveryBoxRuleDetailDTO;
import com.erp.model.oms.entity.DeliveryBoxRuleDetailEntity;
import com.erp.server.oms.service.DeliveryBoxRuleDetailService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 
 *
 * @author wtr
 * @since 2025-11-24
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/deliveryBoxRuleDetail")
public class DeliveryBoxRuleDetailController extends BaseController {

    @Resource
    private DeliveryBoxRuleDetailService deliveryBoxRuleDetailService;

    /**
    * 新增
    * @author wtr
    * @date:  2025-11-24
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliveryBoxRuleDetailDTO.AddDTO dto) {
        return success(deliveryBoxRuleDetailService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2025-11-24
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:deliveryBoxRuleDetail:update",
        serviceClass = DeliveryBoxRuleDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DeliveryBoxRuleDetailDTO.UpdateDTO dto) {
        deliveryBoxRuleDetailService.update(dto);
        return success();
    }


    /**
     * 批量作废箱规明细（明细id）
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "批量作废箱规明细")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "scm:deliveryBoxRuleDetail:cancelProcess",
            serviceClass = DeliveryBoxRuleDetailService.class,
            keyIdName = "ids")
    public ApiResult<?> invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        Map<String, DeliveryBoxRuleDetailEntity> entityMap = deliveryBoxRuleDetailService.mapByIds(dto.getIds());
        for (String id : dto.getIds()) {
            DeliveryBoxRuleDetailEntity entity = entityMap.get(id);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"箱规明细不存在"));
                continue;
            }
            try {
                resultDTOS.add(deliveryBoxRuleDetailService.invalid(entity, dto.getRemark()));
            }catch (Exception e){
                log.error("箱规明细作废失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getDeliverySkuNo(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }
}
