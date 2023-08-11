package com.erp.server.wms.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.StocktakingProfitLossDTO;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.server.wms.service.StocktakingProfitLossService;
import org.springframework.data.annotation.Reference;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 盘点管理-盘盈盘亏单
 *
 * @author Lambda
 * @since 2023-07-31
 */
@RestController
@RequestMapping("/stocktakingProfitLoss")
public class StocktakingProfitLossController extends BaseController {

    @Resource
    private StocktakingProfitLossService stocktakingProfitLossService;

    /**
     * 获取 tab列表
     *
     * @return
     */
    @PostMapping("/tabList")
    public ApiResult<List<StocktakingProfitLossDTO.TabDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<StocktakingProfitLossDTO.TabDTO> tabList = stocktakingProfitLossService.tabList(dto);
        return success(tabList);
    }

    /**
     * 分页列表
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "create_user_id",
//            menuCode = "wms:stocktakingProfitLoss:paging",
//            tableAlias = "ti"
//    )
    public ApiResult<PagingVO<StocktakingProfitLossDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<StocktakingProfitLossDTO.PagingParamDTO> dto) {
        PagingVO<StocktakingProfitLossDTO.PagingViewDTO> pagingVO = stocktakingProfitLossService.paging(dto);
        return success(pagingVO);
    }
}
