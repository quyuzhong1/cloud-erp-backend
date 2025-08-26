package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.service.SampleBorrowInfoService;
import com.erp.server.wms.service.SampleRecipientService;
import com.erp.server.wms.service.SampleScrapInfoService;
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

    @PostMapping("/importSampleBorrow")
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
}
