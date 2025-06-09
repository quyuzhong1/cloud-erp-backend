package com.erp.rpc.wms.feign;

import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.wms.dto.PackageForecastDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Lambda
 * @Classname 组包预报feign
 * @Description 组包预报feign
 * @Date 2024-01-30 11:21
 * @Created by yl
 */
@FeignClient(name = "erp-wms", contextId = "packageForecastFeign")
public interface PackageForecastFeign {

    /**
     * 添加组包预报
     * @param addList
     * @return
     */
    @PostMapping("feign/packageForecast/add")
    List<BatchResultDTO> add(@RequestBody List<PackageForecastDTO.AddDTO> addList);

    /**
     * @description: 根据销售订单id集合查询
     * @author Will
     * @date: 2024/4/1 14:50
     * @param soIdList
     * @return List<PackageForecastDetailEntity>
     */
    @PostMapping("feign/packageForecast/listPackageForecastBySoIdList")
    List<PackageForecastDTO.ExportViewDTO> listPackageForecastBySoIdList(@RequestBody List<String> soIdList);

}
