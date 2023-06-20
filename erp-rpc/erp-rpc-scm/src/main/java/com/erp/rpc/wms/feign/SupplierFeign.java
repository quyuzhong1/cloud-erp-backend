package com.erp.rpc.wms.feign;

import com.erp.model.scm.dto.SupplierDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * @Classname: SupplierFeign
 * @Description: TODO
 * @CreateTime: 2023-06-19  19:22
 * @Author: zhangchunlin
 */
@FeignClient(name = "erp-scm", contextId = "supplier")
public interface SupplierFeign {


    /**
     * 批量获取供应商信息
     * 返回的map不会为空，无需判断
     * @param ids
     * @return
     */
    @PostMapping("/feign/supplier/getSupplierSimpleInfo")
    Map<String, SupplierDTO.SupplierSimpleDTO> getSupplierSimpleInfo(@RequestBody List<String> ids);


}
