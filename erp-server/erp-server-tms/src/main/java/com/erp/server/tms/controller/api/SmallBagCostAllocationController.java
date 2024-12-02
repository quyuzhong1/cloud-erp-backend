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
import com.erp.server.tms.service.SmallBagCostAllocationService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.SmallBagCostAllocationDTO;

/**
 * 小包费用分摊
 *
 * @author shukai
 * @since 2024-12-02
 */
@Slf4j
@RestController
@LogSystemModule("小包费用分摊")
@RequestMapping("/smallBagCostAllocation")
public class SmallBagCostAllocationController extends BaseController {

    @Resource
    private SmallBagCostAllocationService smallBagCostAllocationService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-12-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "小包费用分摊新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SmallBagCostAllocationDTO.AddDTO dto) {
        return success(smallBagCostAllocationService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-12-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "小包费用分摊修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:smallBagCostAllocation:update",
        serviceClass = SmallBagCostAllocationService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SmallBagCostAllocationDTO.UpdateDTO dto) {
        smallBagCostAllocationService.update(dto);
        return success();
    }



}
