package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.model.tms.dto.ImportHistoryRecordDTO;
import com.erp.model.tms.dto.LogisticsReconDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportTmsFeignController {
    @Resource
    private LogisticsBillCostService logisticsBillCostService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private LogisticsLastMileCostService logisticsLastMileCostService;
    @Resource
    private LogisticsTrackService logisticsTrackService;
    @Resource
    private CfgLogisticsCostImportService cfgLogisticsCostImportService;
    @Resource
    private ImportHistoryRecordService importHistoryRecordService;
    @Resource
    private LogisticsThirdChannelRefService logisticsThirdChannelRefService;
    @Resource
    private LogisticsReconService logisticsReconService;


    @PostMapping("/logisticsBillCost")
    public void importLogisticsBillCost(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            logisticsBillCostService.importLogisticsBillCost(dto);
        }catch (Exception e) {
            log.error("导入物流成本失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }
    @PostMapping("/logisticsLastMileCost")
    public void importLogisticsLastMileCost(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            logisticsLastMileCostService.importLogisticsLastMileCost(dto);
        } catch (Exception e) {
            log.error("导入尾程费用失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }
    @PostMapping("/importLogisticsTrackInfo")
    public void importLogisticsTrackInfo(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            logisticsTrackService.importLogisticsTrackInfo(dto);
        } catch (Exception e) {
            log.error("导入物流轨迹失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importCfgLogisticsCost")
    public void importCfgLogisticsCost(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            cfgLogisticsCostImportService.importCfgLogisticsCost(dto);
        } catch (Exception e) {
            log.error("导入费用配置失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/preprocessingImportExcel")
    public void preprocessingImportExcel(@RequestBody ImportHistoryRecordDTO.ImportSyncDTO importSyncDTO) {
        try {
            importHistoryRecordService.preprocessingImportExcel(importSyncDTO);
        } catch (Exception e) {
            log.error("导入费用配置失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(importSyncDTO.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importLogisticsThirdChannelRef")
    public void importLogisticsThirdChannelRef(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            logisticsThirdChannelRefService.importLogisticsThirdChannelRef(dto);
        } catch (Exception e) {
            log.error("导入物流-第三方渠道关系表失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    /**
     * 物流商对账单异步导入回调
     * @author Will
     * @date: 2026/06/02
     * @param dto
     * @return void
     */
    @PostMapping("/importLogisticsRecon")
    public void importLogisticsRecon(@RequestBody LogisticsReconDTO.ImportDTO dto) {
        try {
            logisticsReconService.executeImportTask(dto);
        } catch (Exception e) {
            log.error("导入物流商对账单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }
}
