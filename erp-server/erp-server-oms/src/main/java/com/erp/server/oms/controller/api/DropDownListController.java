package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.enums.RefundOrderStatusEnum;
import com.erp.server.oms.service.DictBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
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
     *  customerCompanyCategory  公司客户类别
     *  settleMode 客户结算方式
     *  collectionTerms 收款条件
     *  invoiceType 发票类型
     *  salesPlatform 销售平台
     *  soB2cBillStatus  b2c销售订单状态
     *  soB2cPayStatus  b2c销售订单付款状态
     *  soB2cAbnormalType  b2c销售订单异常信息
     *  soB2cLable  b2c销售订单标签
     *  logisticsMethod b2c销售订单物流方式
     *  shopAuthType 店铺授权类型
     * @return
     */
    @GetMapping("/dict/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(key);
        //list 根据sort排序
        list = list.stream().sorted(Comparator.comparingInt(DictBasicDTO.ViewDTO::getSort)).collect(Collectors.toList());
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getValue(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    @GetMapping("/dict/listByType")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> list(@RequestParam("type") String type,
                                                           @RequestParam(value = "subType", required = false) String subType) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByType(type, subType);
        //list 根据sort排序
        list = list.stream().sorted(Comparator.comparingInt(DictBasicDTO.ViewDTO::getSort)).collect(Collectors.toList());
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getValue(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 根据type和subType返回树状结构
     *
     * @param key
     * @return
     */
    @GetMapping("/dict/tree")
    public ApiResult<List<BaseDropDownDTO.Tree>> tree(@RequestParam("key") String key) {
        return success(dictBasicService.getTreeByKey(key));
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

    /**
     * 公共单据状态下拉
     * type=refundOrder 退货订单状态
     *
     * @return
     */
    @GetMapping("/status/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listStatusDropDown(@RequestParam(value = "type")String type) {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(RefundOrderStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);

    }







}
