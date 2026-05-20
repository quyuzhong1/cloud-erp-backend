package com.erp.rpc.fms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * FMS 任务 Feign
 * @author wuht
 * @date 2025-11-06
 */
@FeignClient(name = "erp-fms", contextId = "fmsTaskFeign", configuration = {FeignErrorDecoder.class})
public interface FmsTaskFeign {

    /**
     * 资产卡片审核
     */
    @PostMapping("/feign/assetCard/approve")
    List<BatchResultDTO> assetCardApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 资产验收单审核
     */
    @PostMapping("/feign/assetAccept/approve")
    List<BatchResultDTO> assetAcceptApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 盘盈盘亏单审核
     */
    @PostMapping("/feign/assetProfitLoss/approve")
    List<BatchResultDTO> assetProfitLossApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 资产盘点表审核
     */
    @PostMapping("/feign/assetStocktaking/approve")
    List<BatchResultDTO> assetStocktakingApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 资产盘点方案审核
     */
    @PostMapping("/feign/assetStocktakingPlan/approve")
    List<BatchResultDTO> assetStocktakingPlanApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 资产位置审核
     */
    @PostMapping("/feign/assetLocation/approve")
    List<BatchResultDTO> assetLocationApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 资产处置单审核
     */
    @PostMapping("/feign/assetDisposal/approve")
    List<BatchResultDTO> assetDisposalApprove(@RequestBody BaseApproveParamDTO dto);

    /**
     * 更新业务单据状态
     */
    @PostMapping("feign/fmsSyncKingdee/updateBusinessSyncKingdeeStatus")
    void updateBusinessSyncKingdeeStatus(@RequestBody Map<String, Object> params);

    /**
     * 新中台查询同步
     * @param syncParamDTO
     * @return
     */
    @PostMapping("/feign/fmsSyncKingdee/newFindDataSendSyncTask")
    Map<String, Map<String, Object>> newFindDataSendSyncTask(@RequestBody DmpSyncMqDTO.SyncParamDTO syncParamDTO);
}

