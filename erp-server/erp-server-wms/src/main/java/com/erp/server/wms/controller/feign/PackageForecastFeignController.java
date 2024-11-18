package com.erp.server.wms.controller.feign;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
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
import java.util.ArrayList;
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
    public List<BatchResultDTO> add(@RequestBody @Validated ValidList<PackageForecastDTO.AddDTO> list) {
        List<BatchResultDTO> resultDTOList = new ArrayList<>();
        for (PackageForecastDTO.AddDTO item : list) {
            try {
                packageForecastService.add(item);
            } catch (Exception e) {
                log.error("添加组包预报异常 {}", e.getMessage());
                item.getDetailList().forEach(v-> resultDTOList.add(BatchResultDTO.fail(item.getLogisticsSupplierId(),v.getSoCode(), CharSequenceUtil.format("添加组包预报异常 {}", ExceptionUtil.getSimpleMessage(e)))));
            }
        }

        return resultDTOList;
    }


    /**
     * @description: 根据销售订单id集合查询
     * @author Will
     * @date: 2024/4/1 14:52
     * @param soIdList
     * @return List<PackageForecastDetailEntity>
     */
    @PostMapping("/listPackageForecastBySoIdList")
    public List<PackageForecastDTO.ExportViewDTO> listPackageForecastBySoIdList(@RequestBody List<String> soIdList){
        return packageForecastDetailService.listPackageForecastBySoIdList(soIdList);
    }
}
