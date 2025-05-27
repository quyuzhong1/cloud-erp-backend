package com.erp.server.sys.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.vo.PagingVO;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;
import com.erp.server.sys.query.CfgThirdNoticeQueryHandler;
import com.erp.server.sys.query.ThirdNoticePushRecordQueryHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.ThirdNoticePushRecordService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.ThirdNoticePushRecordDTO;

import java.util.List;

/**
 * 三方通知推送记录
 *
 * @author jack
 * @since 2025-05-26
 */
@Slf4j
@RestController
@LogSystemModule("三方通知推送记录")
@RequestMapping("/thirdNoticePushRecord")
public class ThirdNoticePushRecordController extends BaseController {

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    /**
     * 获取状态统计
     * @return
     */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:thirdNoticePushRecord:paging",
            tableAlias = "tnpr"
    )
    public ApiResult<List<ThirdNoticePushRecordDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        return success(thirdNoticePushRecordService.tabList(dto));
    }

    /**
     * 列表查询
     * @author jack
     * @date: 2025-05-26
     * @param dto
     * @return ApiResult<PagingVO<ThirdNoticePushRecordDTO.ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:thirdNoticePushRecord:paging",
            tableAlias = "tnpr"
    )
    @WebAdvanceQuery(handler = ThirdNoticePushRecordQueryHandler.class)
    public ApiResult<PagingVO<ThirdNoticePushRecordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<ThirdNoticePushRecordDTO.PagingParamDTO> dto) {
        return success(thirdNoticePushRecordService.paging(dto));
    }


    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-05-26
     * @param dto
     * @param response
     * @return
     */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "sys:thirdNoticePushRecord:export",
            tableAlias = "tnpr"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出Excel数据")
    @WebAdvanceQuery(handler = ThirdNoticePushRecordQueryHandler.class)
    public ApiResult<Object> exportList(@RequestBody @Validated ThirdNoticePushRecordDTO.PagingParamDTO dto, HttpServletResponse response) {
        thirdNoticePushRecordService.exportList(dto, response);
        return success();
    }

}
