package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.SampleBorrowInfoService;
import com.erp.server.wms.service.SampleRecipientService;
import com.erp.server.wms.service.SampleScrapInfoService;
import com.erp.server.wms.service.SampleBackInfoService;
import com.erp.server.wms.service.SampleInitialLedgerService;
import com.erp.server.wms.service.SampleTransferInfoService;
import com.erp.server.wms.service.SampleAdjustmentInfoService;
import com.erp.server.wms.service.WarehouseLocationMappingService;
import com.erp.server.wms.service.CfgQcUserService;
import com.erp.server.wms.service.SoReturnInstockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportWmsFeignController {
    @Resource
    private SampleRecipientService sampleRecipientService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private SampleScrapInfoService sampleScrapInfoService;

    @Resource
    private SampleBorrowInfoService sampleBorrowInfoService;

    @Resource
    private SampleBackInfoService sampleBackInfoService;

    @Resource
    private SampleInitialLedgerService sampleInitialLedgerService;

    @Resource
    private SampleTransferInfoService sampleTransferInfoService;

    @Resource
    private SampleAdjustmentInfoService sampleAdjustmentInfoService;

    @Resource
    private WarehouseLocationMappingService warehouseLocationMappingService;

    @Resource
    private CfgQcUserService cfgQcUserService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @PostMapping("/sampleRecipient")
    public void importSampleRecipient(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            sampleRecipientService.importSampleRecipient(dto);
        } catch (Exception e) {
            log.error("导入样品领用单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importSampleScrap")
    public void importSampleScrap(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            sampleScrapInfoService.importSampleScrap(dto);
        } catch (Exception e) {
            log.error("导入样品报废单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importSampleBorrow")
    public void importSampleBorrow(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            sampleBorrowInfoService.importSampleBorrow(dto);
        } catch (Exception e) {
            log.error("导入样品报废单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importSampleBackInfo")
    public void importSampleBackInfo(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            sampleBackInfoService.importSampleBackInfo(dto);
        } catch (Exception e) {
            log.error("导入样品退回单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importSampleInitialLedger")
    public void importSampleInitialLedger(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            sampleInitialLedgerService.importSampleInitialLedger(dto);
        } catch (Exception e) {
            log.error("导入样品期初台账失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importSampleTransfer")
    public void importSampleTransfer(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            sampleTransferInfoService.importSampleTransfer(dto);
        } catch (Exception e) {
            log.error("导入样品转移单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importSampleAdjustment")
    public void importSampleAdjustment(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            sampleAdjustmentInfoService.importSampleAdjustment(dto);
        } catch (Exception e) {
            log.error("导入样品调整单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importWarehouseLocationMapping")
    public void importWarehouseLocationMapping(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            warehouseLocationMappingService.importWarehouseLocationMapping(dto);
        } catch (Exception e) {
            log.error("导入仓位绑定失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importCfgQcUser")
    public void importCfgQcUser(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            cfgQcUserService.importCfgQcUser(dto);
        } catch (Exception e) {
            log.error("导入质检员配置失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

    @PostMapping("/importSoReturnInstock")
    public void importSoReturnInstock(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            soReturnInstockService.importSoReturnInstock(dto);
        } catch (Exception e) {
            log.error("销售退货入库单导入失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }
}
