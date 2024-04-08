package com.erp.rpc.wms.feign;

import com.erp.model.wms.dto.PackageForecastDTO;
import com.erp.model.wms.entity.PackageForecastDetailEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;


/**
 * @author Lambda
 * @Classname 组包预报feign
 * @Description TODO
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
    Boolean add(@RequestBody List<PackageForecastDTO.AddDTO> addList);

    /**
     * @description: 根据销售订单id集合查询
     * @author Will
     * @date: 2024/4/1 14:50
     * @param soIdList
     * @return List<PackageForecastDetailEntity>
     */
    @PostMapping("feign/packageForecast/listBySoIdList")
    List<PackageForecastDetailEntity> listBySoIdList(@RequestBody List<String> soIdList);

}
