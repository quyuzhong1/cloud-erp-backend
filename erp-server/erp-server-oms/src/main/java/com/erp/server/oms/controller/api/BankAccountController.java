package com.erp.server.oms.controller.api;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.server.oms.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 银行账号控制器
 * @CreateTime: 2023-07-04  15:27
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/bankAccount")
public class BankAccountController extends BaseController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/select")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> select( ) {
        List<BankAccountEntity> list = bankAccountService.list( );
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getBankAccountNo(), x.getAccountName()))
                .collect(Collectors.toList());
        return success(result);
    }

}
