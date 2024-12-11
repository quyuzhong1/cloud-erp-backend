package com.erp.server.tms.controller.api;


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
import com.erp.server.tms.service.SmallBagCostAllocationDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.SmallBagCostAllocationDetailDTO;

/**
 * 小包费用分摊明细
 *
 * @author shukai
 * @since 2024-11-29
 */
@Slf4j
@RestController
@LogSystemModule("小包费用分摊明细")
@RequestMapping("/smallBagCostAllocationDetail")
public class SmallBagCostAllocationDetailController extends BaseController {

    @Resource
    private SmallBagCostAllocationDetailService smallBagCostAllocationDetailService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "小包费用分摊明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SmallBagCostAllocationDetailDTO.AddDTO dto) {
        return success(smallBagCostAllocationDetailService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-11-29
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "小包费用分摊明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:smallBagCostAllocationDetail:update",
        serviceClass = SmallBagCostAllocationDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SmallBagCostAllocationDetailDTO.UpdateDTO dto) {
        smallBagCostAllocationDetailService.update(dto);
        return success();
    }



}
