package com.cloud.erp.admin.modules.sys.controller;


import com.cloud.erp.admin.modules.sys.entity.SysBaseDicEntity;
import com.cloud.erp.admin.modules.sys.service.SysBaseDicService;
import com.common.core.enums.CurrencyEnum;
import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BaseDicDTO;
import com.erp.common.dto.base.BaseSearchDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author yl
 * @since 2022-08-09
 */
@RestController
@RequestMapping("/sys/dic")
public class SysBaseDicController extends BaseController {

    @Autowired
    private SysBaseDicService sysBaseDicService;

    @RequestMapping("/list")
    public ApiResult list(@RequestBody @Validated BaseSearchDTO dto){
        CurrencyEnum[] currencyEnums=CurrencyEnum.values();
        List<Map<String,String>> list=new ArrayList<>(currencyEnums.length);
        for(CurrencyEnum item:currencyEnums){
            Map<String,String> map=new HashMap<>(3);
            map.put("currencyCode",item.getCurrencyCode());
            map.put("currencyName",item.getCurrencyName());
            map.put("currencySymbol",item.getCurrencySymbol());
            list.add(map);
        }
        if(StringUtils.isNotBlank(dto.getSearchKeyword())){

        }
        return success(list);
    }

    @RequestMapping("/list1")
    public ApiResult list1(@RequestBody @Validated BaseDicDTO dto){
        List<SysBaseDicEntity> list= sysBaseDicService.listByDicType(dto);
        return success(list);
    }
}

