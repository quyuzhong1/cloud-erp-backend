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
import com.erp.server.wms.service.SubcontractIssueDetailService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.SubcontractIssueDetailDTO;

/**
 * 委外发料明细单
 *
 * @author will
 * @since 2024-01-08
 */
@Slf4j
@RestController
@LogSystemModule("委外发料明细单")
@RequestMapping("/subcontractIssueDetail")
public class SubcontractIssueDetailController extends BaseController {

    @Resource
    private SubcontractIssueDetailService subcontractIssueDetailService;

    /**
    * 新增
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "委外发料明细单新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated SubcontractIssueDetailDTO.AddDTO dto) {
        return success(subcontractIssueDetailService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-01-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "委外发料明细单修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:subcontractIssueDetail:update",
        serviceClass = SubcontractIssueDetailService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated SubcontractIssueDetailDTO.UpdateDTO dto) {
        subcontractIssueDetailService.update(dto);
        return success();
    }



}
