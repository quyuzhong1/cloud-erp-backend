package com.erp.server.tms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.FirstMileChangeRecordDTO;
import com.erp.server.tms.query.FirstMileChangeRecordQueryHandler;
import com.erp.server.tms.query.FirstMileCostAllocationQueryHandler;
import com.erp.server.tms.service.FirstMileChangeRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 头程调整记录
 *
 * @author zdy
 * @since 2025-05-12
 */
@Slf4j
@RestController
@LogSystemModule("头程调整记录")
@RequestMapping("/firstMileChangeRecord")
public class FirstMileChangeRecordController extends BaseController {

    @Resource
    private FirstMileChangeRecordService firstMileChangeRecordService;

    /**
    * 新增
    * @author zdy
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "头程调整记录新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated FirstMileChangeRecordDTO.AddDTO dto) {
        return success(firstMileChangeRecordService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2025-05-12
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "头程调整记录修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:firstMileChangeRecord:update",
        serviceClass = FirstMileChangeRecordService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated FirstMileChangeRecordDTO.UpdateDTO dto) {
        firstMileChangeRecordService.update(dto);
        return success();
    }

    /**
     * 分页
     *
     * @param dto
     * @author zdy
     * @date 2025-5-13 10:54
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:firstMileChangeRecord:paging",
            tableAlias = "fmcr"
    )
    @WebAdvanceQuery(handler = FirstMileChangeRecordQueryHandler.class)
    public ApiResult<PagingVO<FirstMileChangeRecordDTO.PagingVO>> paging(@RequestBody @Valid PagingDTO<FirstMileChangeRecordDTO.PagingParamDTO> dto) {
        PagingVO<FirstMileChangeRecordDTO.PagingVO> pagingVO = firstMileChangeRecordService.paging(dto);
        return success(pagingVO);
    }
    /**
     * 导出Excel
     *
     * @param dto
     * @author zdy
     * @date 2024-8-15 10:54
     */
    @PostMapping("/exportExcel")
    @LogAction(value = LogActionEnum.EXPORT, desc = "头程调整记录导出")
    @WebAdvanceQuery(handler = FirstMileCostAllocationQueryHandler.class)
    public ApiResult<Boolean> exportExcel(@RequestBody @Valid FirstMileChangeRecordDTO.PagingParamDTO dto) {
        firstMileChangeRecordService.exportList(dto);
        return success(Boolean.TRUE);
    }
}
