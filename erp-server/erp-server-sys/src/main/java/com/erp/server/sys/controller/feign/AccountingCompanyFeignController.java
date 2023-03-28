package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.BaseController;
import com.erp.server.sys.service.SysAccountingCompanyService;
import org.springframework.web.bind.annotation.*;

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
    /**
     * @description: 查询所有已启用组织
     * @author Will
     * @date: 2023/3/22 16:37
     * @return List<BaseIdDTO>
     */
    @GetMapping("/list")
    public List<BaseIdDTO> listAccountingCompany() {
        List<BaseIdDTO> list = sysAccountingCompanyService.listAccountingCompany();
        return list;
    }

}
