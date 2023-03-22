package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.BaseController;
import com.erp.server.sys.service.SysAccountingCompanyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname AccountingCompanyFeignController
 * @Description TODO
 * @Date 2023-03-22 15:28
 * @Created by yl
 */
@RestController
@RequestMapping("sys/feign/accountingCompany")
public class AccountingCompanyFeignController extends BaseController {

    @Resource
    private SysAccountingCompanyService sysAccountingCompanyService;

    @PostMapping("/getByIds")
    public List<BaseIdDTO> getByIds(@RequestBody List<String> ids) {
        List<BaseIdDTO> list = sysAccountingCompanyService.getByIds(ids);
        return list;
    }
}
