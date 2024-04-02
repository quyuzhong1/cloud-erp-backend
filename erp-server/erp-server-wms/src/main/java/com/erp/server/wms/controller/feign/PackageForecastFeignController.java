package com.erp.server.wms.controller.feign;

import com.common.business.validator.ValidList;
import com.common.core.controller.BaseController;
import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import com.erp.server.wms.service.PackageForecastDetailService;
import com.erp.server.wms.service.PackageForecastService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname PackageForecastFeignController
 * @Description TODO
 * @Date 2024-01-30 9:34
 * @Created by yl
 */
@Slf4j
@RestController
@RequestMapping("/feign/packageForecast")
public class PackageForecastFeignController extends BaseController {

    @Resource
    private PackageForecastService packageForecastService;

    @Resource
    private PackageForecastDetailService packageForecastDetailService;

    /**
     * 添加组包
     *
     * @param list
     * @return
     */
    @PostMapping("/add")
    public Boolean add(@RequestBody @Validated ValidList<PackageForecastDTO.AddDTO> list) {
        for (PackageForecastDTO.AddDTO item : list) {
            try {
                packageForecastService.add(item);
            } catch (Exception e) {
                log.error("添加组包预报异常 {}", e.getMessage());
            }

        }

        return Boolean.TRUE;
    }


    /**
     * @description: 根据销售订单id集合查询
     * @author Will
     * @date: 2024/4/1 14:52
     * @param soIdList
     * @return List<PackageForecastDetailEntity>
     */
    @PostMapping("/listBySoIdList")
    public List<PackageForecastDetailEntity> listBySoIdList(@RequestBody List<String> soIdList){
        return packageForecastDetailService.listBySoIdList(soIdList);
    }
}
