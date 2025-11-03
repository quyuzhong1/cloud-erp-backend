package com.erp.server.fms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.fms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * FMS异步导入Feign控制器
 * @author wuht
 * @date 2025/10/11
 */
@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportFmsFeignController {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

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

    private void updateTask(String taskId, Exception e) {
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(taskId);
        importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
        importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
        downloadTaskFeign.updateTask(importResultDTO);
    }

    /**
     * 导入资产位置
     * @param dto 导入参数
     */
    @PostMapping("/assetLocation")
    public void importAssetLocation(@RequestBody BaseDTO.ImportDTO dto) {
        log.info("开始导入资产位置，任务ID：{}", dto.getTaskId());
        try {
            assetLocationService.importAssetLocation(dto);
        } catch (Exception e) {
            log.error("导入资产位置失败，任务ID：{}，错误：{}", dto.getTaskId(), e.getMessage(), e);
            updateTask(dto.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 导入资产验收表
     * @param dto 导入参数
     */
    @PostMapping("/assetAccept")
    public void importAssetAccept(@RequestBody BaseDTO.ImportDTO dto) {
        log.info("开始导入资产验收表，任务ID：{}", dto.getTaskId());
        try {
            assetAcceptService.importAssetAccept(dto);
        } catch (Exception e) {
            log.error("导入资产验收表失败，任务ID：{}，错误：{}", dto.getTaskId(), e.getMessage(), e);
            updateTask(dto.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 导入资产卡片
     * @param dto 导入参数
     */
    @PostMapping("/assetCard")
    public void importAssetCard(@RequestBody BaseDTO.ImportDTO dto) {
        log.info("开始导入资产卡片，任务ID：{}", dto.getTaskId());
        try {
            assetCardService.importAssetCard(dto);
        } catch (Exception e) {
            log.error("导入资产卡片失败，任务ID：{}，错误：{}", dto.getTaskId(), e.getMessage(), e);
            updateTask(dto.getTaskId(), e);
            throw e;
        }
    }

    /**
     * 导入资产盘点方案
     * @param dto 导入参数
     */
    @PostMapping("/assetStocktakingPlan")
    public void importAssetStocktakingPlan(@RequestBody BaseDTO.ImportDTO dto) {
        log.info("开始导入资产盘点方案，任务ID：{}", dto.getTaskId());
        try {
            assetStocktakingPlanService.importAssetStocktakingPlan(dto);
        } catch (Exception e) {
            log.error("导入资产盘点方案失败，任务ID：{}，错误：{}", dto.getTaskId(), e.getMessage(), e);
            updateTask(dto.getTaskId(), e);
            throw e;
        }
    }


    @PostMapping("/importAssetDisposal")
    public void importAssetDisposal(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            assetDisposalService.importAssetDisposal(dto);
        } catch (Exception e) {
            log.error("导入模具档案失败", e);
            updateTask(dto.getTaskId(), e);
        }
    }


}