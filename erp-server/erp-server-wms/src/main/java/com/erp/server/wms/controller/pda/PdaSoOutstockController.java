package com.erp.server.wms.controller.pda;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.server.wms.service.SoOutstockService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * PDA:销售出库单
 * @author LUO_WG
 * @since 2023-04-07
 */
@RestController
@RequestMapping("/pdaSoOutstock")
public class PdaSoOutstockController extends BaseController {

    @Resource
    private SoOutstockService soOutstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/22 11:32
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PdaPagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoOutstock:paging",
            tableAlias = "so"
    )
    public ApiResult<PagingVO<SoOutstockDTO.PdaPagingViewDTO>> paging(@RequestBody @Validated PagingDTO<SoOutstockDTO.PdaPagingParamDTO> dto) {
        PagingVO<SoOutstockDTO.PdaPagingViewDTO> pagingVO = soOutstockService.pdaPaging(dto);
        return success(pagingVO);
    }
}
