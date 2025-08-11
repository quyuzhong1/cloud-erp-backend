package com.erp.rpc.wms.feign;

import com.common.business.config.FeignErrorDecoder;
import com.erp.model.wms.dto.inventory.InventoryClosedRecordDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 金蝶关账远程调用
 * @author Will
 * @date: 2024/2/28 17:52
 */
@FeignClient(name = "erp-wms", contextId = "inventoryClosedRecord" ,configuration = {FeignErrorDecoder.class})
public interface InventoryCloseRecordFeign {

   /**
    * 根据金蝶组织id查询关账时间
    * @author Will
    * @date: 2024/2/28 17:53
    * @param category
    * @return Map<LocalDate>
    */
    @PostMapping(value = "/feign/inventoryClosedRecord/mapByOrgId")
    Map<String, LocalDate> mapByOrgId(@RequestBody String category);

    /**
     * 根据组织id集合验证是否关账
     * @author Will
     * @date: 2024/2/28 19:27
     * @param closedParamList
     */
    @PostMapping(value = "/feign/inventoryClosedRecord/checkHsClosed")
    void checkHsClosed(@RequestBody List<InventoryClosedRecordDTO.ClosedParamDTO> closedParamList);
}
