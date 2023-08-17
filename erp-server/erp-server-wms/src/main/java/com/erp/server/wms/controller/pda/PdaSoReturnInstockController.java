package com.erp.server.wms.controller.pda;


import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.server.wms.service.SoReturnInstockService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * PDA:销售退货入库单
 * @author LUO_WG
 * @since 2023-05-10
 */
@RestController
@RequestMapping("/pdaSoReturnInstock")
public class PdaSoReturnInstockController extends BaseController {

    @Resource
    private SoReturnInstockService soReturnInstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnInstockDTO.PagingViewDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:pdaSoReturnInstock:paging",
            tableAlias = "sri"
    )
    public ApiResult<PagingVO<SoReturnInstockDTO.PagingView>> paging(@RequestBody @Validated PagingDTO<SoReturnInstockDTO.PagingParam> dto) {
        PagingVO<SoReturnInstockDTO.PagingView> pagingVO = soReturnInstockService.paging(dto);
        return success(pagingVO);
    }
}
