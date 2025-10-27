package com.erp.server.fms.controller.feign;

import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.AssetLocationDTO;
import com.erp.model.fms.dto.AssetAcceptDTO;
import com.erp.model.fms.dto.AssetCardDTO;
import com.erp.model.fms.dto.AssetStocktakingPlanDTO;
import com.erp.server.fms.handler.AssetLocationQueryHandler;
import com.erp.server.fms.handler.AssetAcceptQueryHandler;
import com.erp.server.fms.service.AssetLocationService;
import com.erp.server.fms.service.AssetAcceptService;
import com.erp.server.fms.service.AssetCardService;
import com.erp.server.fms.service.AssetStocktakingPlanService;
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
    public PagingVO<AssetStocktakingPlanDTO.ListDTO> getAssetStocktakingPlanPageData(@RequestBody PagingDTO<AssetStocktakingPlanDTO.ExportDTO> dto) {
        return assetStocktakingPlanService.getAssetStocktakingPlanPageData(dto);
    }
}

