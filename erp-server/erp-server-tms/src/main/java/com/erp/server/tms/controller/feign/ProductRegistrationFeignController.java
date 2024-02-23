package com.erp.server.tms.controller.feign;

import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.server.tms.service.ProductRegistrationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
