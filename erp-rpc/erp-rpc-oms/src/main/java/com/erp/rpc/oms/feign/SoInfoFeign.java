package com.erp.rpc.oms.feign;

import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "erp-oms", contextId = "soInfo")
public interface SoInfoFeign {

    @PostMapping("feign/soInfo/getSoInfoById")
    SoInfoEntity getSoInfoById(@RequestBody String id);

    @PostMapping("feign/soInfo/listSoDetailByIds")
    List<SoDetailEntity> listSoDetailByIds(@RequestBody List<String> ids);

    @PostMapping("feign/soInfo/listSoDetailByMainIds")
    List<SoDetailEntity> listSoDetailByMainIds(@RequestBody List<String> ids);

    @PostMapping("feign/soInfo/getSoBaseById")
    SoInfoDTO.CustomerDTO getSoBaseById(@RequestBody String id);

    @PostMapping("feign/soInfo/listSoCustomerByIds")
    List<SoInfoDTO.CustomerDTO> listSoCustomer(List<String> soIdList);
}
