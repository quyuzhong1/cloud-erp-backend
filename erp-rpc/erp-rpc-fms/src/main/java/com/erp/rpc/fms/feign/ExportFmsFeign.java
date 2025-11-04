package com.erp.rpc.fms.feign;

import com.common.business.config.ExportFeignConfig;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.fms.dto.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * FMS异步导出Feign接口
 * @author wuht
 * @date 2025/10/13
 */
@FeignClient(name = "erp-fms", contextId = "exportFmsFeign", configuration = ExportFeignConfig.class)
public interface ExportFmsFeign {

    /**
     * 导出资产位置
     */
    @PostMapping("/feign/export/getAssetLocationPageData")
    PagingVO<AssetLocationDTO.ListDTO> getAssetLocationPageData(@RequestBody PagingDTO<AssetLocationDTO.ExportDTO> dto);

    /**
     * 导出资产验收表
     */
    @PostMapping("/feign/export/getAssetAcceptPageData")
    PagingVO<AssetAcceptDTO.ListDTO> getAssetAcceptPageData(@RequestBody PagingDTO<AssetAcceptDTO.ExportDTO> dto);

    /**
     * 导出资产卡片
     */
    @PostMapping("/feign/export/getAssetCardPageData")
    PagingVO<AssetCardDTO.ListDTO> getAssetCardPageData(@RequestBody PagingDTO<AssetCardDTO.ExportDTO> dto);

    /**
     * 导出资产盘点方案
     */
    @PostMapping("/feign/export/getAssetStocktakingPlanPageData")
    PagingVO<AssetStocktakingPlanDTO.ListDTO> getAssetStocktakingPlanPageData(@RequestBody PagingDTO<AssetStocktakingPlanDTO.ExportDTO> dto);

    /**
     * 导出资产处置单
     */
    @PostMapping("/feign/export/exportAssetDisposal")
    PagingVO<AssetDisposalDTO.ListDTO> exportAssetDisposal(@RequestBody PagingDTO<AssetDisposalDTO.PagingParamDTO> dto);

    /**
     * 导出资产盘点单
     */
    @PostMapping("/feign/export/getAssetStocktakingPageData")
    PagingVO<AssetStocktakingDTO.ListDTO> getAssetStocktakingPageData(@RequestBody PagingDTO<AssetStocktakingDTO.ExportDTO> dto);

    /**
     * 导出盘盈盘亏单
     */
    @PostMapping("/feign/export/getAssetProfitLossPageData")
    PagingVO<AssetProfitLossDTO.ListDTO> getAssetProfitLossPageData(@RequestBody PagingDTO<AssetProfitLossDTO.ExportDTO> dto);

}

