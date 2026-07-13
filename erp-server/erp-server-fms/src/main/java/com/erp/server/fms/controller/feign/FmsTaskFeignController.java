package com.erp.server.fms.controller.feign;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.enums.LogActionEnum;
import com.erp.server.fms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * FMS 任务 Feign Controller
 * @author wuht
 * @date 2025-11-06
 */
@Slf4j
@RestController
@RequestMapping("/feign")
public class FmsTaskFeignController {

    @Resource
    private AssetCardService assetCardService;

    @Resource
    private AssetAcceptService assetAcceptService;

    @Resource
    private AssetProfitLossService assetProfitLossService;

    @Resource
    private AssetStocktakingService assetStocktakingService;

    @Resource
    private AssetStocktakingPlanService assetStocktakingPlanService;

    @Resource
    private AssetLocationService assetLocationService;

    @Resource
    private AssetDisposalService assetDisposalService;

    /**
     * 资产卡片审核
     */
    @PostMapping("/assetCard/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产卡片审核")
    public List<BatchResultDTO> assetCardApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultList.add(assetCardService.approve(new com.common.business.dto.base.ApproveOneDTO(id, dto.getType(), dto.getComment())));
            } catch (Exception e) {
                log.error("资产卡片审核失败", e);
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return resultList;
    }

    /**
     * 资产验收单审核
     */
    @PostMapping("/assetAccept/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产验收单审核")
    public List<BatchResultDTO> assetAcceptApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultList.add(assetAcceptService.approve(new com.common.business.dto.base.ApproveOneDTO(id, dto.getType(), dto.getComment())));
            } catch (Exception e) {
                log.error("资产验收单审核失败", e);
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return resultList;
    }

    /**
     * 盘盈盘亏单审核
     */
    @PostMapping("/assetProfitLoss/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "盘盈盘亏单审核")
    public List<BatchResultDTO> assetProfitLossApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultList.add(assetProfitLossService.approve(new com.common.business.dto.base.ApproveOneDTO(id, dto.getType(), dto.getComment())));
            } catch (Exception e) {
                log.error("盘盈盘亏单审核失败", e);
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return resultList;
    }

    /**
     * 资产盘点表审核
     */
    @PostMapping("/assetStocktaking/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产盘点表审核")
    public List<BatchResultDTO> assetStocktakingApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultList.add(assetStocktakingService.approve(new com.common.business.dto.base.ApproveOneDTO(id, dto.getType(), dto.getComment())));
            } catch (Exception e) {
                log.error("资产盘点表审核失败", e);
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return resultList;
    }

    /**
     * 资产盘点方案审核
     */
    @PostMapping("/assetStocktakingPlan/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产盘点方案审核")
    public List<BatchResultDTO> assetStocktakingPlanApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultList.add(assetStocktakingPlanService.approve(new com.common.business.dto.base.ApproveOneDTO(id, dto.getType(), dto.getComment())));
            } catch (Exception e) {
                log.error("资产盘点方案审核失败", e);
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return resultList;
    }

    /**
     * 资产位置审核
     */
    @PostMapping("/assetLocation/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产位置审核")
    public List<BatchResultDTO> assetLocationApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultList.add(assetLocationService.approve(new com.common.business.dto.base.ApproveOneDTO(id, dto.getType(), dto.getComment())));
            } catch (Exception e) {
                log.error("资产位置审核失败", e);
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return resultList;
    }

    /**
     * 资产处置单审核
     */
    @PostMapping("/assetDisposal/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "资产处置单审核")
    public List<BatchResultDTO> assetDisposalApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultList = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            try {
                resultList.add(assetDisposalService.approve(new com.common.business.dto.base.ApproveOneDTO(id, dto.getType(), dto.getComment())));
            } catch (Exception e) {
                log.error("资产处置单审核失败", e);
                resultList.add(BatchResultDTO.fail(id, id, e.getMessage()));
            }
        }
        return resultList;
    }
}

