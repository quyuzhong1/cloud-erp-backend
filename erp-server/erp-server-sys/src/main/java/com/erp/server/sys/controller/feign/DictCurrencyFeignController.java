package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.server.sys.service.DictCurrencyService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname SysDeptFeignController
 * @Description TODO
 * @Date 2022-12-28 15:18
 * @Created by yl
 */
@RestController
@RequestMapping("sys/feign/currency")
public class DictCurrencyFeignController extends BaseController {

    @Resource
    private DictCurrencyService dictCurrencyService;

    /**
     * 根据币种查询
     */
    @PostMapping("/listByCurrency")
    public List<CurrencyDTO.ViewDTO> listByCurrency(@RequestBody List<String> currencyList) {
        return dictCurrencyService.listByCurrency(currencyList);
    }

}
