package com.erp.server.wms.controller.api;


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
import com.erp.server.wms.service.WmsVirtualDetailMsgService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.WmsVirtualDetailMsgDTO;

/**
 * wms虚拟仓明细同步表
 *
 * @author will
 * @since 2024-12-03
 */
@Slf4j
@RestController
@LogSystemModule("wms虚拟仓明细同步表")
@RequestMapping("/wmsVirtualDetailMsg")
public class WmsVirtualDetailMsgController extends BaseController {

    @Resource
    private WmsVirtualDetailMsgService wmsVirtualDetailMsgService;

    /**
    * 新增
    * @author will
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "wms虚拟仓明细同步表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated WmsVirtualDetailMsgDTO.AddDTO dto) {
        return success(wmsVirtualDetailMsgService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-12-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "wms虚拟仓明细同步表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:wmsVirtualDetailMsg:update",
        serviceClass = WmsVirtualDetailMsgService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated WmsVirtualDetailMsgDTO.UpdateDTO dto) {
        wmsVirtualDetailMsgService.update(dto);
        return success();
    }



}
