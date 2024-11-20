package com.erp.server.tms.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.anno.LogSystemModule;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.server.tms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@LogSystemModule("下拉接口")
@RequestMapping("/feign/drop/down")
public class DropDownListFeignController {

    @Resource
    private DictBasicService dictBasicService;


    /**
     * 获取对应字典数据
     * logisticsAddress  物流地址类型
     * discountRate 折扣费率
     * fuelSurchargeRate 燃油附加费率
     * side 边长
     * vote 票
     * shippingBillingMethod  计费方式
     * priceBinary  价格进制
     * weightUnit  重量单位
     * shippingTemplateType  运费模板类型
     * logisticsSupplierType 物流商类型
     * salesOrderType 订单类型
     * reconciliationStatus 对账状态
     * diffOption 差异
     * logisticTrackStatus 物流运输状态
     *
     * @return
     */
    @GetMapping("/dict/list")
    public List<BaseDropDownDTO.CommonDTO> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(key);
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return result;
    }

}
