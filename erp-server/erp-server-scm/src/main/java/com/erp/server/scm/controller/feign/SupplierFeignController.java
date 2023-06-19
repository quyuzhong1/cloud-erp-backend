package com.erp.server.scm.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.scm.dto.SupplierDTO;
import com.erp.server.scm.service.SupplierService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 供应商feign控制器
 * @CreateTime: 2023-06-19  19:07
 * @Author: zhangchunlin
 */
@AllArgsConstructor
@RestController
@RequestMapping(value = "/feign/supplier")
public class SupplierFeignController extends BaseController {

    private final SupplierService supplierService;

    /**
     * 批量获取供应商信息
     * @param ids
     * @return
     */
    @PostMapping("/getSupplierSimpleInfo")
    public Map<String, SupplierDTO.SupplierSimpleDTO> getSupplierSimpleInfo(@RequestBody List<String> ids) {
        return supplierService.getSupplierSimpleInfo(ids);
    }


}