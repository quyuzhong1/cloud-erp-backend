package com.erp.server.wms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.CartonDTO;
import com.erp.server.wms.service.WmsCartonService;
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
import com.erp.server.wms.service.PackingTaskDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;

/**
 * 装箱任务箱子管理
 *
 * @author zdy
 * @since 2024-07-02
 */
@Slf4j
@RestController
@LogSystemModule("装箱任务明细表")
@RequestMapping("/carton")
public class CartonController extends BaseController {

    @Resource
    private WmsCartonService cartonService;
    /**
     * 已装箱分页列表
     * @author zdy
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CartonDTO.PagingViewDTO>> paging(@RequestBody @Validated PagingDTO<CartonDTO.PagingParamDTO> dto) {
        return success(cartonService.paging(dto));
    }

    /**
    * 新增
    * @author zdy
    * @date:  2024-07-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "装箱任务明细表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CartonDTO.AddDTO dto) {
        return success(cartonService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-07-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "装箱任务明细表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:packingTaskDetail:update",
        serviceClass = PackingTaskDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CartonDTO.UpdateDTO dto) {
        cartonService.update(dto);
        return success();
    }



}
