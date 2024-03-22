package com.erp.server.wms.controller.api;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import com.common.core.controller.BaseController;
import com.erp.server.wms.service.WmsDataComparePlanService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.WmsDataComparePlanDTO;

/**
 * 数据对比映射方案
 *
 * @author shukai
 * @since 2024-03-20
 */
@Slf4j
@RestController
@LogSystemModule("数据对比映射方案")
@RequestMapping("/wmsDataComparePlan")
public class WmsDataComparePlanController extends BaseController {

    @Resource
    private WmsDataComparePlanService wmsDataComparePlanService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "数据对比映射方案新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated WmsDataComparePlanDTO.AddDTO dto) {
        return success(wmsDataComparePlanService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-03-20
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "数据对比映射方案修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:wmsDataComparePlan:update",
        serviceClass = WmsDataComparePlanService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated WmsDataComparePlanDTO.UpdateDTO dto) {
        wmsDataComparePlanService.update(dto);
        return success();
    }

    /**
     *查询对比映射方案
     * @author shukai
     * @date:  2024-03-21
     * @param dto
     * @return ApiResult<String>
     */
    @PostMapping("/get")
     public ApiResult<List<WmsDataComparePlanDTO.ViewDTO>> get(@RequestBody @Validated WmsDataComparePlanDTO.CommonDTO dto) {
         return success(wmsDataComparePlanService.get(dto));
     }

}
