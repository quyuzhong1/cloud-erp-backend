package com.erp.server.wms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbaDeliveryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.FbaInventoryService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.FbaInventoryDTO;

import java.util.List;

/**
 * FBA库存
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
@Slf4j
@RestController
@LogSystemModule("FBA库存")
@RequestMapping("/fbaInventory")
public class FbaInventoryController extends BaseController {

    @Autowired
    private FbaInventoryService fbaInventoryService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/10/31 10:32
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.FbaInventoryDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:fbaInventory:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<FbaInventoryDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<FbaInventoryDTO.PagingParamDTO> dto) {
        PagingVO<FbaInventoryDTO.ListDTO> list = fbaInventoryService.paging(dto);
        return success(list);
    }
}
