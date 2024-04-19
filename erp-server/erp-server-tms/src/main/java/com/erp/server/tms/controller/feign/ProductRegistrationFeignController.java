package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.server.tms.service.ProductRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname ProductRegistrationFeignController
 * @Description TODO
 * @Date 2024-01-27 16:30
 * @Created by yl
 */

@Slf4j
@RestController
@LogSystemModule("物流feign接口")
@RequestMapping("/feign/productRegistration")
public class ProductRegistrationFeignController {

    @Resource
    private ProductRegistrationService productRegistrationService;


    /**
     * 判断是否备案
     * 返回未备案的sku
     * @param dto
     * @return
     */
    @PostMapping("/listNotRegistrationByParam")
    public List<String>  listNotRegistrationByParam(@RequestBody SettingForecastDTO.CheckRegistrationDTO dto) {
        List<String> list = productRegistrationService.listNotRegistrationByParam(dto);
        return list;

    }
    /**
     * 新增
     * @author Will
     * @date: 2024/3/19 14:38
     * @param dto
     * @return List<BatchResultDTO>
     */
    @PostMapping("/add")
    public List<BatchResultDTO> add(@RequestBody @Validated ProductRegistrationDTO.AddDTO dto) {
        List<BatchResultDTO> add = productRegistrationService.add(dto);
        return add;
    }

    /**
     * 根据skuId查询
     * @author Will
     * @date: 2024/3/21 16:43
     * @param skuId
     * @return List<ProductRegistrationEntity>
     */
    @GetMapping("/listBySkuId")
    public List<ProductRegistrationEntity>  listBySkuId(@RequestParam("skuId")String skuId) {
        List<ProductRegistrationEntity> list = productRegistrationService.listBySkuId(skuId);
        return list;

    }
}
