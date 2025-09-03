package com.erp.server.oms.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.oms.service.ExhibitionOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportOmsFeignController {
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private ExhibitionOrderService exhibitionOrderService;



    @PostMapping("/exhibitionOrder")
    public void importExhibitionOrder(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            exhibitionOrderService.importExhibitionOrder(dto);
        } catch (Exception e) {
            log.error("导入展会订单失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

}
