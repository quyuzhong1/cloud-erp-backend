package com.erp.server.wms.controller.pda;

import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.server.wms.service.PoInstockService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * PDA:采购入库单
 * @Author Luo_WG
 * @Date 2023/8/15 10:23
 **/
@RestController
@RequestMapping("/pdaPoInStock")
public class PdaPoInStockController extends BaseController {
    @Resource
    private PoInstockService poInstockService;

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/16 14:50
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.PoInstockDTO.PdaListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<PagingVO<PoInstockDTO.PdaPagingView>> paging(@RequestBody @Validated PagingDTO<PoInstockDTO.PdaSearchParamDTO> dto) {
        PagingVO<PoInstockDTO.PdaPagingView> pagingVO = poInstockService.PdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @author Will
     * @date: 2023/4/11 20:08
     * @param dto
     * @return ApiResult<List<ListStatusCountDTO>>
     */
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<List<PoInstockDTO.ListStatusCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PoInstockDTO.ListStatusCountDTO> list = poInstockService.listCount(dto);
        return success(list);
    }
}
