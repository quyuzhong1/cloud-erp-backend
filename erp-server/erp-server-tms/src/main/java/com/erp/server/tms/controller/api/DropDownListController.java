package com.erp.server.tms.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.model.tms.dto.DictBasicDTO;
import com.erp.server.tms.service.DictBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下拉
 */
@RestController
@RequestMapping("/drop/down")
public class DropDownListController extends BaseController {

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
     * changeRange 修改范围
     * firstReconciliationType 头程对账单类型
     *
     * @return
     */
    @GetMapping("/dict/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(key);
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 单据状态下拉
     *
     * @return
     */
    @GetMapping("/billStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listBillStatusDropDown() {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(BillApproveStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getStatus(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


}

