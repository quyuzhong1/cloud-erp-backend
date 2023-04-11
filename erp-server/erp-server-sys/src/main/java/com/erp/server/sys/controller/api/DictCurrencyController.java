package com.erp.server.sys.controller.api;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.server.sys.service.DictCurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 公共基础
 *
 * @author Lambda
 * @Classname DictCurrencyController
 * @Description TODO
 * @Date 2023-03-21 17:14
 * @Created by yl
 */

@RestController
@RequestMapping("currency")
public class DictCurrencyController extends BaseController {

    @Resource
    private  DictCurrencyService dictCurrencyService;

    /**
     * 获取到货币列表
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<CurrencyDTO.ViewDTO>> getList() {
        List<CurrencyDTO.ViewDTO>  list=  dictCurrencyService.getList();
        return success(list);
    }
}
