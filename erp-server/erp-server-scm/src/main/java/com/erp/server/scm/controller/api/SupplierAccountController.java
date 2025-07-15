package com.erp.server.scm.controller.api;

import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.entity.SupplierAccountEntity;
import com.erp.server.scm.service.SupplierAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 供应商账户管理
 * @author zdy
 * @ClassName SupplierAccountController
 * @description: 供应商账户管理
 * @date 2025年04月23日
 * @version: 1.0
 */
@Slf4j
@RestController
@LogSystemModule("供应商账户管理")
@RequestMapping("/supplierAccount")
public class SupplierAccountController {
    @Resource
    private SupplierAccountService supplierAccountService;
    /**
     * 获取供应商账户列表
     *
     * @param supplierId 供应商id
     * @return
     */
    @GetMapping("/getSupplierAccountList")
    public ApiResult<List<SupplierAccountEntity>> getSupplierAccountList(@RequestParam(value = "supplierId", required = false) String supplierId) {
        return ApiResult.success(supplierAccountService.getSupplierAccountList(supplierId));
    }
}
