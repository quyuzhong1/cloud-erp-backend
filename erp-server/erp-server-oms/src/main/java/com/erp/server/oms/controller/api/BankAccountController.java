package com.erp.server.oms.controller.api;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.BankAccountEntity;
import com.erp.server.oms.service.BankAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 银行账号控制器
 *
 * @CreateTime: 2023-07-04  15:27
 * @Author: zhangchunlin
 */
@RestController
@RequestMapping(value = "/bankAccount")
public class BankAccountController extends BaseController {

    @Autowired
    private BankAccountService bankAccountService;

    @GetMapping("/select")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> select(@RequestParam(value = "orgId") String orgId) {
        List<BankAccountEntity> list = bankAccountService.findByOrgId(orgId);
        List<BaseDropDownDTO.DisabledDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.DisabledDTO(x.getBankAccountNo(), x.getBankAccountNo()+"      "+x.getAccountName(),x.getDisabled()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 导入金蝶银行账号信息
     *
     * @param
     * @return
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = bankAccountService.importExcel(excelFile, response);
        return result ? success() : failure();
    }

}
