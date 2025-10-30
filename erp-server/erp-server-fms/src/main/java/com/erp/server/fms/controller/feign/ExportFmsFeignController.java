package com.erp.server.fms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.fms.dto.*;
import com.erp.server.fms.handler.AssetLocationQueryHandler;
import com.erp.server.fms.handler.AssetAcceptQueryHandler;
import com.erp.server.fms.handler.AssetStocktakingPlanQueryHandler;
import com.erp.server.fms.query.AssetDisposalQueryHandler;
import com.erp.server.fms.service.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * FMS异步导出Feign控制器
 * @author wuht
 * @date 2025/10/13
 */
@RestController
@RequestMapping("/feign/export")
public class ExportFmsFeignController {

    @Resource
    private AssetLocationService assetLocationService;
    
    @Resource
    private AssetAcceptService assetAcceptService;
    
    @Resource
    private AssetCardService assetCardService;
    
    @Resource
    private AssetStocktakingPlanService assetStocktakingPlanService;

    @Resource
    private AssetDisposalService assetDisposalService;

    @PostMapping("/getAssetLocationPageData")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetLocation:export"
    )
    @WebAdvanceQuery(handler = AssetLocationQueryHandler.class)
    public PagingVO<AssetLocationDTO.ListDTO> getAssetLocationPageData(@RequestBody PagingDTO<AssetLocationDTO.ExportDTO> dto) {
        return assetLocationService.getAssetLocationPageData(dto);
    }

    @PostMapping("/getAssetAcceptPageData")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetAccept:export"
    )
    @WebAdvanceQuery(handler = AssetAcceptQueryHandler.class)
    public PagingVO<AssetAcceptDTO.ListDTO> getAssetAcceptPageData(@RequestBody PagingDTO<AssetAcceptDTO.ExportDTO> dto) {
        return assetAcceptService.getAssetAcceptPageData(dto);
    }

    @PostMapping("/getAssetCardPageData")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetCard:export"
    )
    public PagingVO<AssetCardDTO.ListDTO> getAssetCardPageData(@RequestBody PagingDTO<AssetCardDTO.ExportDTO> dto) {
        return assetCardService.getAssetCardPageData(dto);
    }

    @PostMapping("/getAssetStocktakingPlanPageData")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "fms:assetStocktakingPlan:export"
    )
    @WebAdvanceQuery(handler = AssetStocktakingPlanQueryHandler.class)
    public PagingVO<AssetStocktakingPlanDTO.ListDTO> getAssetStocktakingPlanPageData(@RequestBody PagingDTO<AssetStocktakingPlanDTO.ExportDTO> dto) {
        return assetStocktakingPlanService.getAssetStocktakingPlanPageData(dto);
    }


    @PostMapping("/exportAssetDisposal")
    @WebAdvanceQuery(handler = AssetDisposalQueryHandler.class)
    public PagingVO<AssetDisposalDTO.ListDTO> exportAssetDisposal(@RequestBody  PagingDTO<AssetDisposalDTO.PagingParamDTO> dto) {
        return assetDisposalService.paging(dto);
    }

}

