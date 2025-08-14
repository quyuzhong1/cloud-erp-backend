package com.erp.server.sys.controller.api;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysLogRecordDTO;
import com.erp.server.sys.service.SysLogRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 盘点计划
 *
 * @author Jim
 * @since 2023-09-04
 */
@Slf4j
@RestController
@RequestMapping("/sysLogRecord")
public class SysLogRecordController extends BaseController {

    @Resource
    private SysLogRecordService sysLogRecordService;


    /**
     * 列表查询
     *
     * @param dto
     * @return ApiResult<PagingVO < SysLogRecordDTO.ListDTO>>
     * @author Jim
     * @since 2023-09-04
     */
    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "wms:sysLogRecord:paging",
//            tableAlias = "sys_log_record"
//    )
    public ApiResult<PagingVO<SysLogRecordDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<SysLogRecordDTO.PagingParamDTO> dto) {
        return success(sysLogRecordService.paging(dto));
    }

}
