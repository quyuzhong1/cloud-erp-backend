package com.erp.server.wms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OverseasProviderDTO;
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
import com.erp.server.wms.service.OverseasInventoryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.OverseasInventoryDTO;

/**
 * 海外仓库存
 *
 * @author Jim
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外仓库存")
@RequestMapping("/overseasInventory")
public class OverseasInventoryController extends BaseController {

    @Resource
    private OverseasInventoryService overseasInventoryService;


    /**
     * 列表查询
     * @author Jim
     * @date:  2023-11-16
     * @return ApiResult
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasInventory:paging",
            tableAlias = "op"
    )
    public ApiResult<PagingVO<OverseasInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OverseasInventoryDTO.PagingParamDTO> dto) {
        PagingVO<OverseasInventoryDTO.ListDTO> result = overseasInventoryService.paging(dto);
        return success(result);
    }



}
