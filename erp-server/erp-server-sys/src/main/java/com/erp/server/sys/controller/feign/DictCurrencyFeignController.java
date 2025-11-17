package com.erp.server.sys.controller.feign;

import com.common.core.controller.BaseController;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.sys.service.DictCurrencyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Classname SysDeptFeignController

 * @Date 2022-12-28 15:18
 * @Created by yl
 */
@RestController
@RequestMapping("feign/currency")
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

    /**
     * 根据金蝶编码查询币别
     */
    @PostMapping("/listCurrencyByKingdeeCodeList")
    public List<CurrencyDTO.ViewDTO> listCurrencyByKingdeeCodeList(@RequestBody List<String> currCodeList) {
        return dictCurrencyService.listCurrencyByKingdeeCodeList(currCodeList);
    }

    /**
     * 获取所有币别
     */
    @GetMapping("/list")
    public List<DictCurrencyEntity> list() {
        return dictCurrencyService.list();
    }
    @GetMapping("/getCurrencyByNum")
    DictCurrencyEntity getCurrencyByNum(@RequestParam(value = "num") String num){
        return dictCurrencyService.getCurrencyByNum(num);
    }
}
