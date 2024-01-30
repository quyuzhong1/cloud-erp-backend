package com.erp.server.wms.controller.feign;

import com.common.business.validator.ValidList;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.enums.LogActionEnum;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.server.wms.service.PackageForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author Lambda
 * @Classname PackageForecastFeignController
 * @Description TODO
 * @Date 2024-01-30 9:34
 * @Created by yl
 */
@Slf4j
@RestController
@LogSystemModule("组包预报")
@RequestMapping("/feign/packageForecast")
public class PackageForecastFeignController extends BaseController {

    @Resource
    private PackageForecastService packageForecastService;


    /**
     * 添加组包
     *
     * @param list
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加组包预报")
    @PostMapping("/add")
    public Boolean add(@RequestBody @Validated ValidList<PackageForecastDTO.AddDTO> list) {
        for (PackageForecastDTO.AddDTO item : list) {
            try {
                packageForecastService.add(item);
            } catch (Exception e) {
                log.error("添加组包预报异常 {}", e);
            }

        }

        return Boolean.TRUE;
    }

}
