package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.inventory.InstockForcastDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.InventoryEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.util.List;

/**
 * @Classname: SampleFeign
 * @CreateTime: 2025-09-10
 * @Author: jack
 */
@FeignClient(name = "erp-wms", contextId = "sampleFeign" ,configuration = {FeignErrorDecoder.class})
public interface SampleFeign {

}
