package com.erp.server.oms.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.scm.dto.SkuCostProfitDTO;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 销售订单工具类
 *
 * @CreateTime: 2023-07-04  17:18
 * @Author: zhangchunlin
 */
@Slf4j
public class SoUtils {

    /**
     * 计算成本毛利
     *
     * @param purchasePrice
     * @param costParam
     * @param skuCostProfitResult
     */
    public static SkuCostProfitDTO.SkuCostProfitResult calCostProfit(BigDecimal purchasePrice,
                                                                     SkuCostProfitDTO.SkuCostProfitParam costParam,
                                                                     SkuCostProfitDTO.SkuCostProfitResult skuCostProfitResult) {
        skuCostProfitResult.setPurchasePrice(purchasePrice);
        // 当没有计算得到采购单价或为0，所有都返回0
        if (Objects.isNull(skuCostProfitResult.getPurchasePrice()) || skuCostProfitResult.getPurchasePrice().compareTo(BigDecimal.ZERO) <= 0) {
            return skuCostProfitResult;
        }
        skuCostProfitResult.setSaleCost(skuCostProfitResult.getPurchasePrice().multiply(new BigDecimal(costParam.getQty())).setScale(4, BigDecimal.ROUND_HALF_UP));
        if (Objects.isNull(costParam.getTaxRate())) {
            costParam.setTaxRate(BigDecimal.ZERO);
        }

        // 销售毛利=销售金额(折后)*汇率-总成本
        BigDecimal saleAmount = costParam.getAmountLocalCurrency();
        //总成本
        BigDecimal saleCost = skuCostProfitResult.getSaleCost();

        BigDecimal saleProfit = saleAmount.subtract(saleCost).setScale(4, BigDecimal.ROUND_HALF_UP);
        skuCostProfitResult.setSaleProfit(saleProfit);
        // 销售毛利率
        if (costParam.getSaleAmount().compareTo(BigDecimal.ZERO) > 0 && costParam.getAmountLocalCurrency().compareTo(BigDecimal.ZERO) > 0) {
            skuCostProfitResult.setSaleProfitRate(skuCostProfitResult.getSaleProfit().divide(costParam.getAmountLocalCurrency(), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100")));
        }
        return skuCostProfitResult;
    }

    /**
     * 销售订单明细成本
     *
     * @param item
     */
    public static void updateSoDetailCost(SoDetailEntity item, BigDecimal purchasePrice) {
        SkuCostProfitDTO.SkuCostProfitResult skuCostProfitResult = new SkuCostProfitDTO.SkuCostProfitResult();
        skuCostProfitResult.setSkuId(item.getSkuId());
        skuCostProfitResult.setPurchasePrice(BigDecimal.ZERO);
        skuCostProfitResult.setSaleCost(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfit(BigDecimal.ZERO);
        skuCostProfitResult.setSaleProfitRate(BigDecimal.ZERO);

        SkuCostProfitDTO.SkuCostProfitParam costParam = new SkuCostProfitDTO.SkuCostProfitParam();
        costParam.setSkuId(item.getSkuId());
        //该值应该为数量*单价*汇率
        BigDecimal amount = MathUtil.multiplyWithTwo(item.getPrice(), item.getQty());
        BigDecimal saleAmount = MathUtil.multiplyWithTwo(amount, item.getExchangeRate());
        //销售毛利=销售金额(折后)*汇率-总成本
        //销售金额(折后)*汇率
        BigDecimal amountLocalCurrency = item.getAmountLocalCurrency();
        costParam.setAmountLocalCurrency(amountLocalCurrency);
        costParam.setSaleAmount(saleAmount);
        costParam.setQty(item.getQty());
        costParam.setTaxRate(item.getTaxRate());


        SoUtils.calCostProfit(purchasePrice, costParam, skuCostProfitResult);

        item.setPurchasePrice(skuCostProfitResult.getPurchasePrice());
        item.setSaleCost(skuCostProfitResult.getSaleCost());
        item.setSaleProfit(skuCostProfitResult.getSaleProfit());
        item.setSaleProfitRate(skuCostProfitResult.getSaleProfitRate());
    }

    /**
     * 计算折扣额信息等
     *
     * @param discountAmount
     * @param saveOrUpdateList
     * @param isTax            是否含税
     */
    public static void handleDetailAmount(Boolean isTax, BigDecimal discountAmount, List<SoDetailEntity> saveOrUpdateList) {
        // 折扣总额
        discountAmount = Objects.nonNull(discountAmount) ? discountAmount : BigDecimal.ZERO;
        // 总的价税合计（折前）
        BigDecimal totalTaxAmountBefore = BigDecimal.ZERO;

        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            SoDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            //税率
            BigDecimal taxRate = item.getTaxRate();
            if (Objects.isNull(taxRate)) {
                taxRate = BigDecimal.ZERO;
            }
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = item.getTaxPrice();
            if (Objects.nonNull(isGift) && isGift){
                taxPrice = BigDecimal.ZERO;
            }
            if (Objects.isNull(taxPrice)){
                taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax,4);
            }
            //价税合计（折前）
            BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, new BigDecimal(qty),4);
            totalTaxAmountBefore = totalTaxAmountBefore.add(taxAmount).setScale(4, RoundingMode.HALF_UP);
        }

        // 此处需要注意，所有的明细折扣额汇总起来需等于总的折扣额
        // 找出最后一条非赠品的明细序号
        int lastNoGiftIndex = -1;
        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            SoDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            if (Objects.nonNull(price) && price.compareTo(BigDecimal.ZERO) != 0) {
                lastNoGiftIndex = i;
            }
        }
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        for (int i = 0; i < saveOrUpdateList.size(); i++) {
            SoDetailEntity item = saveOrUpdateList.get(i);
            //是否赠品
            Boolean isGift = item.getIsGift();
            BigDecimal price = item.getPrice();
            Integer qty = item.getQty();
            //当是赠品的时候  单价为0
            if (Objects.nonNull(isGift) && isGift) {
                price = BigDecimal.ZERO;
            }
            item.setPrice(price);

            //税率
            BigDecimal taxRate = item.getTaxRate();
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);

            //含税单价=销售单价*（税率+1）
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            //含税单价
            BigDecimal taxPrice = item.getTaxPrice();
            if (Objects.nonNull(isGift) && isGift) {
                taxPrice = BigDecimal.ZERO;
            }
            if (Objects.isNull(taxPrice)){
                taxPrice = MathUtil.multiplyWithTwo(price, multiplyTax,4);
            }
            item.setTaxPrice(taxPrice);
            //金额
            BigDecimal amount = MathUtil.multiplyWithTwo(price, new BigDecimal(qty),4);

            //含税金额（折扣前）
            BigDecimal taxAmount = MathUtil.multiplyWithTwo(taxPrice, new BigDecimal(qty),4);
            BigDecimal taxAmountBefore = MathUtil.multiplyWithTwo(taxPrice, new BigDecimal(qty),4);
            item.setTaxAmountBefore(taxAmountBefore);
            item.setAmount(amount);

            //折扣额=折扣总额*含税金额（折扣前）/总的价税合计（折前）
            BigDecimal detailDiscountAmount = BigDecimal.ZERO;
            if (Objects.nonNull(discountAmount) && totalTaxAmountBefore.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal discountFlag = MathUtil.multiplyWithTwo(discountAmount, taxAmount,4);
                detailDiscountAmount = MathUtil.divide(discountFlag, totalTaxAmountBefore, 2, BigDecimal.ROUND_DOWN);
                log.warn("销售订单明细第【{}】条数据，价税合计（折扣前）比例【{}】，折扣额【{}】", (i + 1), detailDiscountAmount);
            }
            //折扣总额
            totalDiscountAmount = MathUtil.add(totalDiscountAmount, detailDiscountAmount);
            // 最后一行非赠品，判断是否明细折扣额汇总是否等于总的折扣额
            if (i == lastNoGiftIndex) {
                log.warn("销售订单明细汇总折扣额【{}】，总折扣额【{}】", totalDiscountAmount, discountAmount);
                //折扣总额大于 折扣相加的和
                if (discountAmount.compareTo(totalDiscountAmount) > 0) {
                    BigDecimal diff = MathUtil.subtract(discountAmount, totalDiscountAmount);
                    detailDiscountAmount = MathUtil.add(detailDiscountAmount, diff);
                }
            }
            item.setDiscountAmount(detailDiscountAmount);
            // 价税合计（折扣后） 含税单价*数量-折扣额
            taxAmount = MathUtil.subtract(taxAmount, detailDiscountAmount);
            item.setTaxAmount(taxAmount);
            //税额
            BigDecimal tax = BigDecimal.ZERO;
            //含税 不含税就为0
            if (isTax) {
                tax = getIncludeTax(taxAmountBefore, detailDiscountAmount, taxRate);
            }
            item.setTax(tax);
            //减的值
            BigDecimal subNumber = MathUtil.add(tax, detailDiscountAmount);
            // 销售金额（折扣后）=价税合计-折扣额-税额 ps:不含税的时候 含税金额=价税合计
            amount = MathUtil.subtract(taxAmountBefore, subNumber);
            item.setAmount(amount);
            // 折扣金额不可大于价税合计（折扣前）
            if (Objects.nonNull(detailDiscountAmount) && detailDiscountAmount.compareTo(item.getTaxAmountBefore()) > 0) {
                log.warn("销售订单第【{}】行明细价税合计（折扣前）【{}】，折扣额【{}】", (i + 1), item.getTaxAmountBefore(), detailDiscountAmount);
            }
        }
        // 折扣金额不可大于价税合计（折扣前）
        if (Objects.nonNull(discountAmount) && discountAmount.compareTo(totalTaxAmountBefore) > 0) {
            log.warn("销售订单明细汇总价税合计（折扣前）【{}】，总折扣额【{}】", totalTaxAmountBefore, discountAmount);
            throw new ServiceException("折扣金额不可大于价税合计（折前）");
        }
    }


    /**
     * 获取到含税 的税额 =含税金额[含税单价*数量]-折扣额/100+税率） *税率
     *
     * @param taxAmount            这个就是含税金额 含税单价*数量
     * @param detailDiscountAmount 这个是折扣额
     * @param taxRate              这个是税率 12% 就是12
     * @return java.math.BigDecimal
     * @author yl
     * @date 2023-10-11 14:25
     */
    public static BigDecimal getIncludeTax(BigDecimal taxAmount, BigDecimal detailDiscountAmount, BigDecimal taxRate) {
        BigDecimal diff = MathUtil.subtract(taxAmount, detailDiscountAmount);
        //除数
        BigDecimal divideNumber = MathUtil.add(taxRate, MathUtil.BigDecimal_100);
        //除的结果
        BigDecimal divideResult = MathUtil.divide(diff, divideNumber, 8, BigDecimal.ROUND_HALF_UP);
        return MathUtil.multiplyWithTwo(divideResult, taxRate,4);
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
        headMap.put("orderTypeName", "单据类型");
        headMap.put("billDate", "单据日期");
        headMap.put("approveStatusName", "单据状态");
        headMap.put("invalidStatusName", "作废状态");
        headMap.put("customerName", "客户");
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
        headMap.put("scarceQty", "缺货数量");
        headMap.put("virtualScarceQty", "虚拟仓缺货数量");
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
        headMap.put("exchangeRate", "汇率");
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
        headMap.put("detailRemark", "明细备注");
        headMap.put("approveUserName", "最新审核人");
        headMap.put("createUserName", "创建人");
        headMap.put("createTime", "创建时间");

        return headMap;
    }

    public static LinkedHashMap<String, Object> fillToMap(SoInfoDTO.PagingViewDTO item, Map<String, String> headMap, List<String> nopermitFields) {
        LinkedHashMap<String, Object> convertData = new LinkedHashMap<>();
        BeanUtil.copyProperties(item, convertData);

        // 创建时间格式化
        convertData.put("createTime", LocalDateTimeUtil.format(item.getCreateTime(), "yyyy-MM-dd HH:mm:ss"));
        // 汇率
        if (Objects.nonNull(item.getExchangeRate())) {
            if (item.getExchangeRate().compareTo(BigDecimal.ZERO) == 0) {
                convertData.put("exchangeRate", "");
            } else {
                convertData.put("exchangeRate", " " + StrUtils.null2EmptyWithTrim(item.getExchangeRate()));
            }
        }

        List<String> boolList = Lists.newArrayList("isGift", "isReissue", "isClose", "isDeclare");
        LinkedHashMap<String, Object> dataMap = new LinkedHashMap<>();
        headMap.keySet().stream().forEach(field -> {
            if (CollUtil.isNotEmpty(nopermitFields) && nopermitFields.contains(field)) {

            } else {
                if (boolList.contains(field)) {
                    Object boolObj = convertData.get(field);
                    dataMap.put(field, ObjectUtil.isEmpty(boolObj) ? "" : (Objects.equals(boolObj, Boolean.TRUE) ? "是" : " 否"));
                } else {
                    dataMap.put(field, StrUtils.null2EmptyWithTrim(convertData.get(field)));
                }
            }
        });
        return dataMap;
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