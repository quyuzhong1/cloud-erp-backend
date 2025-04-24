package com.erp.rpc.tms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.tms.dto.LogisticsMappingDTO;
import com.erp.model.tms.entity.LogisticsMappingEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @description: 物流映射表
 * @author Will
 * @date: 2024/4/23 15:56
 */
@FeignClient(name = "erp-tms", contextId = "logisticsMapping" ,configuration = {FeignErrorDecoder.class})
public interface LogisticsMappingFeign {

    /**
     * @description: 根据物流映射表查询
     * @author Will
     * @date: 2024/4/23 16:07
     * @param paramDTO
     * @return LogisticsMappingEntity
     */
    @PostMapping("feign/logisticsMapping/getByLogisticsMappingParam")
    LogisticsMappingEntity getByLogisticsMappingParam(@RequestBody @Validated LogisticsMappingDTO.SearchParamDTO paramDTO);

    /**
     * @description: 根据物流映射表查询
     * @author jack
     * @date: 2024/10/09
     * @param id
     * @return List<LogisticsMappingEntity>
     */
    @PostMapping("feign/logisticsMapping/listDbByChannelId")
    List<LogisticsMappingEntity> listDbByChannelId(@RequestBody String id);
}
