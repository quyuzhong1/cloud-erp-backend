package com.erp.server.plm.controller.feign;

import com.common.business.dto.base.BaseDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.ProductDetailDTO;
import com.erp.model.plm.dto.SkuStdCostDTO;
import com.erp.model.plm.enums.SkuStdCostImportTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.service.ProductDetailImagesService;
import com.erp.server.plm.service.SkuStdCostDetailService;
import com.erp.server.plm.service.SkuStdCostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/feign/skuStdCost")
public class SkuStdCostFeignController {

    @Resource
    private SkuStdCostService skuStdCostService;


    @PostMapping("/updateSkuStdCost")
    public void updateSkuStdCost(@RequestBody SkuStdCostDTO.UpdateDTO dto) {
        skuStdCostService.update(dto);
    }


}
