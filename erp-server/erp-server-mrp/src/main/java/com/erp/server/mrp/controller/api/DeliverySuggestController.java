package com.erp.server.mrp.controller.api;


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
import com.erp.server.mrp.service.DeliverySuggestService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.mrp.dto.DeliverySuggestDTO;

/**
 * 发货计划
 *
 * @author will
 * @since 2024-08-27
 */
@Slf4j
@RestController
@LogSystemModule("发货计划")
@RequestMapping("/deliverySuggest")
public class DeliverySuggestController extends BaseController {

    @Resource
    private DeliverySuggestService deliverySuggestService;

    /**
    * 新增
    * @author will
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "发货计划新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DeliverySuggestDTO.AddDTO dto) {
        return success(deliverySuggestService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-08-27
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "发货计划修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "mrp:deliverySuggest:update",
        serviceClass = DeliverySuggestService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DeliverySuggestDTO.UpdateDTO dto) {
        deliverySuggestService.update(dto);
        return success();
    }



}
