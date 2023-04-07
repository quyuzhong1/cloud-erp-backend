package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.BaseController;
import com.erp.server.sys.service.DictBankService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname BankFeignController
 * @Description TODO
 * @Date 2023-03-21 18:18
 * @Created by yl
 */
@RestController
@RequestMapping("feign/bank")
public class BankFeignController extends BaseController {


    @Resource
    private DictBankService bankService;

    @PostMapping("/getByIds")
    public List<BaseIdDTO> getByIds(@RequestBody List<String> ids) {
        List<BaseIdDTO> list = bankService.getByIds(ids);
        return list;
    }
}
