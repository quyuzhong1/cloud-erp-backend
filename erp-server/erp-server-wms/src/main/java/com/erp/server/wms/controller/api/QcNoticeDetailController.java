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
import com.erp.server.wms.service.QcNoticeDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.QcNoticeDetailDTO;

/**
 * 质检通知单明细
 *
 * @author jack
 * @since 2025-04-21
 */
@Slf4j
@RestController
@LogSystemModule("质检通知单明细")
@RequestMapping("/qcNoticeDetail")
public class QcNoticeDetailController extends BaseController {

    @Resource
    private QcNoticeDetailService qcNoticeDetailService;

    /**
    * 新增
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "质检通知单明细新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated QcNoticeDetailDTO.AddDTO dto) {
        return success(qcNoticeDetailService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-04-21
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "质检通知单明细修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:qcNoticeDetail:update",
        serviceClass = QcNoticeDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated QcNoticeDetailDTO.UpdateDTO dto) {
        qcNoticeDetailService.update(dto);
        return success();
    }



}
