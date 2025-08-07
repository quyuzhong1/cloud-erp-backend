package com.erp.server.file.business.oms.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;

/**
 * 销售订单工具类
 *
 * @CreateTime: 2023-07-04  17:18
 * @Author: zhangchunlin
 */
@Slf4j
public final class SoUtils {

    private SoUtils() {
    }

    /**
     * 获取导出销售订单表头字段
     *
     * @return
     */
    public static Map<String, String> getExportHeadList() {
        Map<String, String> headMap = new LinkedHashMap<>();
        headMap.put("id", "id");
        headMap.put("code", "单据编号");
        headMap.put("trackNoStr", "运单号");
        headMap.put("orderTypeName", "单据类型");
        headMap.put("billDate", "单据日期");
        headMap.put("approveStatusName", "单据状态");
        headMap.put("invalidStatusName", "作废状态");
        headMap.put("customerName", "客户");
        headMap.put("partitionName", "军区");
        headMap.put("countryName", "收货国家");
        headMap.put("salesOrgName", "销售组织");
        headMap.put("sellerName", "销售员");
        headMap.put("salesDeptName", "销售部门");
        headMap.put("warehouseName", "仓库");
        headMap.put("virtualWarehouseName", "虚拟仓");
        headMap.put("warehouseOrgName", "库存组织");
        headMap.put("bankServiceFee", "银行手续费");
        headMap.put("shippingFee", "运费金额");
        headMap.put("receiveAccountName", "收款账号");
        headMap.put("receiveMethodName", "收款方式");
        headMap.put("receiveDate", "收款日期");
        headMap.put("tradeTerm", "贸易条款");
        headMap.put("discountAmount", "折扣总额");
        headMap.put("receiverName", "收货人");
        headMap.put("telNumber", "联系电话");
        headMap.put("receiveAddress", "收货地址");
        headMap.put("deliveryModeName", "交货方式");
        headMap.put("addressTypeName", "地址类型");
        headMap.put("receiveConditionName", "收款条件");
        headMap.put("deliveryStatusName", "发货状态");
        headMap.put("skuNo", "sku");
        headMap.put("productName", "产品名称");
        headMap.put("platformSkuNo", "客户SKU");
        headMap.put("qty", "销售数量");
        headMap.put("frozenQty", "锁定数量");
        headMap.put("virtualUsableQty", "虚拟仓可用库存");
        headMap.put("scarceQty", "缺货数量");
        headMap.put("virtualScarceQty", "虚拟仓缺货数量");
        headMap.put("effectiveNoticeQty", "发货通知数量");
        headMap.put("remainingNoticeQty", "剩余发货通知数量");
        headMap.put("availableQty", "可出数量");
        headMap.put("deliveryQty", "已出库数量");
        headMap.put("waitQty", "剩余未出数量");
        headMap.put("unit", "单位");
        headMap.put("amount", "销售金额");
        headMap.put("price", "销售单价");
        headMap.put("priceLc", "销售单价（本位币）");
        headMap.put("taxRate", "税率");
        headMap.put("taxPrice", "含税单价");
        headMap.put("taxPriceLc", "含税单价（本位币）");
        headMap.put("taxAmount", "价税合计");
        headMap.put(SoB2cEntity.EXCHANGE_RATE, "汇率");
        headMap.put("amountLocalCurrency", "销售金额（本位币）");
        headMap.put("allAmountLocalCurrency", "价税合计（本位币）");
        headMap.put("allAmountLc", "总价税合计（本位币）");
        headMap.put("receiveAmount", "收款金额");
        headMap.put("detailDiscountAmount", "折扣额");
        headMap.put("taxAmountBefore", "价税合计(折前)");
        headMap.put("isGift", "是否赠品");
        headMap.put("isReissue", "是否补发");
        headMap.put("isClose", "是否关闭");
        headMap.put("purchasePrice", "采购单价");
        headMap.put("saleCost", "销售总成本");
        headMap.put("saleProfit", "销售毛利");
        headMap.put("saleProfitRate", "销售毛利率（%）");
        headMap.put("customsFee", "报关费");
        headMap.put("currency", "结算币种");
        headMap.put("requireDate", "要货日期");
        headMap.put("customerOrderNo", "客户订单号");
        headMap.put("isDeclare", "是否报关");
        headMap.put("remark", "备注");
        headMap.put("customerPO", "客户PO号");
        headMap.put("toCountry", "目的地");
        headMap.put("detailRemark", "明细备注");
        headMap.put("approveUserName", "最新审核人");
        headMap.put("createUserName", "创建人");
        headMap.put("createTime", "创建时间");
        return headMap;
    }

    @SuppressWarnings("all")
    public static LinkedHashMap<String, Object> fillToMap(SoInfoDTO.PagingViewDTO item, Map<String, String> headMap, List<String> nopermitFields) {
        LinkedHashMap<String, Object> convertData = new LinkedHashMap<>();
        BeanUtil.copyProperties(item, convertData);

        // 创建时间格式化
        convertData.put("createTime", LocalDateTimeUtil.format(item.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        // 处理汇率
        handleExchangeRate(item.getExchangeRate(), convertData);
        List<String> boolList = Lists.newArrayList("isGift", "isReissue", "isClose", "isDeclare");
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        headMap.keySet().forEach(field -> {
            if (!isFieldPermitted(field, nopermitFields)) {
                if (boolList.contains(field)) {
                    dataMap.put(field, formatBooleanField(convertData.get(field)));
                } else {
                    dataMap.put(field, StrUtils.null2EmptyWithTrim(convertData.get(field)));
                }
            }
        });
        return dataMap;
    }

    // 格式化布尔字段
    private static String formatBooleanField(Object boolObj) {
        if (ObjectUtil.isEmpty(boolObj)) {
            return "";
        }
        return(Boolean.TRUE.equals(boolObj) ? "是" : " 否");
    }

    // 检查字段是否允许
    private static boolean isFieldPermitted(String field, List<String> nopermitFields) {
        return CollUtil.isNotEmpty(nopermitFields) && nopermitFields.contains(field);
    }

    // 处理汇率
    private static void handleExchangeRate(BigDecimal exchangeRate, LinkedHashMap<String, Object> convertData) {
        if (exchangeRate != null) {
            if (exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
                convertData.put(SoB2cEntity.EXCHANGE_RATE, "");
            } else {
                convertData.put(SoB2cEntity.EXCHANGE_RATE, " " + StrUtils.null2EmptyWithTrim(exchangeRate));
            }
        }
    }

    /**
     * 导出隐藏主单列
     *
     * @param dataList
     */
    public static void hideForExport(List<LinkedHashMap<String, Object>> dataList) {
        Set<String> mainIds = Sets.newHashSet();
        for (LinkedHashMap<String, Object> data : dataList) {
            String id = StrUtils.null2EmptyWithTrim(data.get("id"));
            if (mainIds.contains(id)) {
                data.put("bankServiceFee", "");
                data.put("shippingFee", "");
                data.put("receiveAmount", "");
                data.put("discountAmount", "");
                data.put("allAmountLc", "");
                continue;
            }
            mainIds.add(id);
        }
    }


}