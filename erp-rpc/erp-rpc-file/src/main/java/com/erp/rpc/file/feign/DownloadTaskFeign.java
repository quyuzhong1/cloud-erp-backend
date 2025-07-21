package com.erp.rpc.file.feign;

import com.common.business.dto.base.BaseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("erp-file")
public interface DownloadTaskFeign {

    @PostMapping("/feign/downloadTask/saveExportTask")
    String saveExportTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params);

    @PostMapping("/feign/downloadTask/saveImportTask")
    String saveImportTask(@RequestParam String fileName, @RequestParam String event, @RequestBody Object params);


    @PostMapping("/feign/downloadTask/updateTask")
    void updateTask(@RequestBody BaseDTO.ImportResultDTO importResultDTO);
}
