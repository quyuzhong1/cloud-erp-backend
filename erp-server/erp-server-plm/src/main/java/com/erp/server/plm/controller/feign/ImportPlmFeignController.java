package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.ProductDetailImagesService;
import com.erp.server.plm.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/import")
public class ImportPlmFeignController {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private ProductDetailImagesService productDetailImagesService;

    @PostMapping("/productDetailImages")
    public void productDetailImages(@RequestBody BaseDTO.ImportDTO dto) {
        try {
            productDetailImagesService.importProductDetailImages(dto);
        }catch (Exception e) {
            log.error("导入产品详情图片失败", e);
            BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
            importResultDTO.setTaskId(dto.getTaskId());
            importResultDTO.setStatus(FileTaskStatusEnum.FAIL.getCode());
            importResultDTO.setRemark(e.getMessage().length() > 490 ? e.getMessage().substring(0, 490) : e.getMessage());
            downloadTaskFeign.updateTask(importResultDTO);
        }
    }

}
