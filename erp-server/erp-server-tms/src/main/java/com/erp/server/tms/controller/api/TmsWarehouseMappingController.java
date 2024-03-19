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
import com.erp.model.tms.dto.TmsWarehouseMappingDTO;
import com.erp.server.tms.query.TmsCfgSailingQueryHandler;
import com.erp.server.tms.service.TmsWarehouseMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 
 * 仓库匹配
 * @author will
 * @since 2024-03-19
 */
@Slf4j
@RestController
@LogSystemModule("")
@RequestMapping("/tmsWarehouseMapping")
public class TmsWarehouseMappingController extends BaseController {

    @Resource
    private TmsWarehouseMappingService tmsWarehouseMappingService;

    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "tms:tmsWarehouseMapping:paging",
            tableAlias = "twm"
    )
    @WebAdvanceQuery(handler = TmsCfgSailingQueryHandler.class)
    public ApiResult<PagingVO<TmsWarehouseMappingDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<TmsWarehouseMappingDTO.PagingParamDTO> dto) {
        return success(tmsWarehouseMappingService.paging(dto));
    }

    /**
    * 新增
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增仓库匹配")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TmsWarehouseMappingDTO.AddDTO dto) {
        return success(tmsWarehouseMappingService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2024-03-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改仓库匹配")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:tmsWarehouseMapping:update",
        serviceClass = TmsWarehouseMappingService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TmsWarehouseMappingDTO.UpdateDTO dto) {
        tmsWarehouseMappingService.update(dto);
        return success();
    }



}
