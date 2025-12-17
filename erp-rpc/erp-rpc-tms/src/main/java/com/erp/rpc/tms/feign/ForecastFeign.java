package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.business.dto.base.BatchResultDTO;
import com.erp.model.tms.dto.ProductRegistrationDTO;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.model.tms.entity.ProductRegistrationEntity;
import com.erp.model.tms.entity.SettingForecastEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author Lambda
 * @Classname ForecastFeign
 * @Date 2024-01-19 17:16
 * @Created by yl
 */
@FeignClient(name = "erp-tms", contextId = "forecast" ,configuration = {FeignErrorDecoder.class})
public interface ForecastFeign {


    /**
     * 根据渠道id 获取到对应的有效时间的预报设置信息
     * @param dto
     * @return
     */
    @PostMapping("/feign/settingForecast/getByLogisticsChannelId")
    SettingForecastDTO.ForecastStatusDTO getByLogisticsChannelId(@RequestBody SettingForecastDTO.FindSettingForecastDTO dto);

    /**
     * 根据平台和sku no 获取到备案产品信息
     * @param dto
     * @return
     */
    @PostMapping("/feign/productRegistration/listNotRegistrationByParam")
    List<String> listNotRegistrationByParam(@RequestBody SettingForecastDTO.CheckRegistrationDTO dto);

    /**
     * 新增备案信息
     * @author Will
     * @date: 2024/3/19 14:40
     * @param dto
     * @return List<BatchResultDTO>
     */
    @PostMapping("/feign/productRegistration/add")
    List<BatchResultDTO> add(@RequestBody @Validated ProductRegistrationDTO.AddDTO dto);

    /**
     * 根据供应商id 获取到对应的有效时间的预报设置信息
     * @param dto
     * @return
     */
    @PostMapping("/feign/settingForecast/getByLogisticsSupplier")
    SettingForecastDTO.ForecastStatusDTO getByLogisticsSupplier(@RequestBody SettingForecastDTO.FindByLogisticsSupplierDTO dto);


    @GetMapping("/feign/settingForecast/getSettingForecastByLogisticsSupplierId")
    SettingForecastEntity getSettingForecastByLogisticsSupplierId(@RequestParam("logisticsSupplierId") String logisticsSupplierId);

    @PostMapping("/feign/settingForecast/getSettingForecastByLogisticsSupplierIdList")
    List<SettingForecastEntity> getSettingForecastByLogisticsSupplierIdList(@RequestBody List<String> logisticsSupplierId);

    /**
     * @description: 根据skuId查询备案
     * @author Will
     * @date: 2024/3/21 16:37
     * @param skuId
     * @return List<ProductRegistrationEntity>
     */
    @GetMapping("/feign/productRegistration/listBySkuId")
    List<ProductRegistrationEntity> listBySkuId(@RequestParam("skuId")String skuId);
}
