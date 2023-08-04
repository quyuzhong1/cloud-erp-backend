package com.erp.server.scm.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.excel.ImportPurchasePriceExcelDTO;
import com.erp.model.scm.dto.excel.SupplierImportExcelDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.scm.service.PurchasePriceDetailService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @CreateTime: 2023-08-03  18:07
 * @Author: zhangchunlin
 */
public class PurchasePriceExcelListener extends AnalysisEventListener<ImportPurchasePriceExcelDTO> {

    private List<FindUserDTO> userList;

    private List<SkuVO> skuList;

    private List<DictCurrencyEntity> currencyList;

    List<Map<String, Object>> supplierList;

    private List<BaseIdDTO> orgList;

    private PurchasePriceDetailService priceDetailService;

    /**
     * 错误信息
     */
    private List<ImportPurchasePriceExcelDTO> errorList = new ArrayList<>();

    /**
     * 可以添加的数据
     */
    private List<PurchasePriceDTO.ImportAddDTO> addList = new ArrayList<>();

    /**
     * 已经解析的上传数据
     */
    private List<ImportPurchasePriceExcelDTO> importList = new ArrayList<>();

    private static final String DEFAULT_PURCHASE_ORG_NAME = "东莞市简拍智造科技有限公司";

    private static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/M/d");

    private static final String DEFAULT_CURRENCY = "CNY";

    public PurchasePriceExcelListener(List<FindUserDTO> userList, List<SkuVO> skuList, List<DictCurrencyEntity> currencyList, List<Map<String, Object>> supplierList,
                                      List<BaseIdDTO> orgList, PurchasePriceDetailService priceDetailService) {
        this.userList = userList;
        this.skuList = skuList;
        this.currencyList = currencyList;
        this.supplierList = supplierList;
        this.orgList = orgList;
        this.priceDetailService = priceDetailService;
    }

    @Override
    public void invoke(ImportPurchasePriceExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollUtil.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        PurchasePriceDTO.ImportAddDTO addDTO = new PurchasePriceDTO.ImportAddDTO();
        String supplierName = StrUtils.null2EmptyWithTrim(excelDTO.getSupplierName());
        Map<String, Object> supplierMap  = supplierList.stream().filter(r->Objects.equals(supplierName, StrUtils.null2EmptyWithTrim(r.get("name")))).findFirst().orElse(null);
        if(Objects.isNull(supplierMap)) {
            errorMsgList.add("供应商不存在");
        } else {
            addDTO.setSupplierId(StrUtils.null2EmptyWithTrim(supplierMap.get("id")));
        }
        addDTO.setSupplierName(supplierName);
        // 报价日期
        String quotedDateStr = excelDTO.getQuotedDate();
        if(StrUtils.isNotEmpty(quotedDateStr)) {
            if(!isDate(quotedDateStr)) {
                errorMsgList.add("报价日期格式错误");
            } else {
                LocalDate quotedDate = LocalDate.parse(quotedDateStr, TIME_FORMAT);
                addDTO.setQuotedDate(quotedDate);
            }
        }

        // 定价员
        String pricingUserName = excelDTO.getPricingUserName();
        addDTO.setPricingUserName(pricingUserName);
        if(StrUtils.isNotEmpty(pricingUserName)) {
            String userId = userList.stream().filter(r -> Objects.equals(pricingUserName, r.getUserName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getUserId())).orElse("");
            if (StrUtils.isEmpty(userId)) {
                errorMsgList.add("定价员不存在");
            }
            addDTO.setPricingUserId(userId);
        }
        // 采购组织
        String purchaseOrgName = excelDTO.getPurchaseOrgName();
        addDTO.setPurchaseOrgName(purchaseOrgName);
        if(StrUtils.isNotEmpty(purchaseOrgName)) {
            String orgId = orgList.stream().filter(r -> Objects.equals(purchaseOrgName, r.getName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StrUtils.isEmpty(orgId)) {
                errorMsgList.add("采购组织不存在");
            }
            addDTO.setPurchaseOrgId(orgId);
        } else {
            // 默认【东莞市简拍智造科技有限公司】
            addDTO.setPurchaseOrgName(DEFAULT_PURCHASE_ORG_NAME);
            String orgId = orgList.stream().filter(r -> Objects.equals(DEFAULT_PURCHASE_ORG_NAME, r.getName())).findFirst().
                    flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
            if (StrUtils.isEmpty(orgId)) {
                errorMsgList.add("采购组织【东莞市简拍智造科技有限公司】不存在");
            }
            addDTO.setPurchaseOrgId(orgId);
        }

        PurchasePriceDetailDTO.ImportAddDTO detailDTO = new  PurchasePriceDetailDTO.ImportAddDTO();
        // 明细
        String skuNo = excelDTO.getSkuNo();
        SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(skuEntity)) {
            errorMsgList.add("sku编码错误");
        } else {
            detailDTO.setSkuId(skuEntity.getSkuId());
            detailDTO.setSkuNo(skuNo);
            detailDTO.setProductName(skuEntity.getSkuName());
        }

        // 采购交期
        String deliveryDayStr = excelDTO.getDeliveryDay();
        if(StrUtils.isNotEmpty(deliveryDayStr) && StrUtils.isInteger(deliveryDayStr)) {
            Integer deliveryDay = Integer.parseInt(deliveryDayStr);
            if(deliveryDay < 0) {
                errorMsgList.add("采购交期不能小于0");
            } else {
                detailDTO.setDeliveryDay(deliveryDay);
            }
        }

        // 区间从
        String minQtyStr = excelDTO.getMinQty();
        if(StrUtils.isNotEmpty(minQtyStr) && StrUtils.isInteger(minQtyStr)) {
            int minQty = Integer.parseInt(minQtyStr);
            if(minQty < 0) {
                errorMsgList.add("区间从最小值错误");
            }
            detailDTO.setMinQty(minQty);
        } else {
            detailDTO.setMinQty(0);
        }
        // 区间到
        String maxQtyStr = excelDTO.getMaxQty();
        if(StrUtils.isNotEmpty(maxQtyStr) && StrUtils.isInteger(maxQtyStr)) {
            int maxQty = Integer.parseInt(maxQtyStr);
            detailDTO.setMaxQty(maxQty);
        } else {
            detailDTO.setMaxQty(9999999);
        }
        // 币制代码
        String currency = excelDTO.getCurrency();
        if(StrUtils.isNotEmpty(currency)) {
            DictCurrencyEntity dictCurrencyEntity = currencyList.stream().filter(r->Objects.equals(currency, r.getId())).findFirst().orElse(null);
            if(Objects.isNull(dictCurrencyEntity)) {
                errorMsgList.add("币制编码错误");
            } else {
                detailDTO.setCurrency(currency);
            }
        } else {
            detailDTO.setCurrency(DEFAULT_CURRENCY);
        }

        // 含税单价
        String taxPriceStr = excelDTO.getTaxPrice();
        if(StrUtils.isNotEmpty(taxPriceStr) && isBigDecimal(taxPriceStr)) {
            BigDecimal taxPrice = new BigDecimal(taxPriceStr);
            if(taxPrice.compareTo(BigDecimal.ZERO) < 0) {
                errorMsgList.add("含税单价错误不能小于0");
            } else {
                detailDTO.setTaxPrice(taxPrice);
            }
        }

        // 税率
        String taxRateStr = excelDTO.getTaxRate();
        if(StrUtils.isNotEmpty(taxRateStr) && isBigDecimal(taxRateStr)) {
            BigDecimal taxRate = new BigDecimal(taxRateStr);
            if(taxRate.compareTo(BigDecimal.ZERO) < 0) {
                errorMsgList.add("税率错误不能小于0");
            } else {
                detailDTO.setTaxRate(taxRate);
            }
        }

        // 生效时间
        String effectiveDateStr = excelDTO.getEffectiveDate();
        if(StrUtils.isNotEmpty(effectiveDateStr)) {
            if(!isDate(effectiveDateStr)) {
                errorMsgList.add("生效日期格式错误");
            } else {
                LocalDate effectiveDate = LocalDate.parse(effectiveDateStr, TIME_FORMAT);
                detailDTO.setEffectiveDate(effectiveDate);
            }
        }

        // 启用状态
        String disabledStr = excelDTO.getDisabled();
        detailDTO.setDisabled(Objects.equals(disabledStr, "启用") || StrUtils.isEmpty(disabledStr));

        // 验证 区间从和区间到
        if(detailDTO.getMaxQty().intValue() == detailDTO.getMinQty().intValue()) {
            errorMsgList.add("区间从，区间到两个值不能相同");
        }
        if(detailDTO.getMaxQty().intValue() < detailDTO.getMinQty().intValue()) {
            errorMsgList.add("区间从值不能大于区间到值");
        }

        //根据供应商 获取到系统已有的区间
        if(StrUtils.isNotEmpty(addDTO.getSupplierId())) {
            List<PurchasePriceDetailDTO.AddDTO> supplierPriceDetailList = priceDetailService.getBySupplierId(addDTO.getSupplierId(), new ArrayList<>());
            Map<String, List<PurchasePriceDetailDTO.AddDTO>> existPriceMap = supplierPriceDetailList.stream().collect(Collectors.groupingBy(PurchasePriceDetailDTO.AddDTO::getSkuId));
            if(existPriceMap.containsKey(detailDTO.getSkuId())) {
                int[] addRange = {detailDTO.getMinQty(), detailDTO.getMaxQty()};
                List<PurchasePriceDetailDTO.AddDTO> existPriceList = existPriceMap.get(detailDTO.getSkuId());
                for(PurchasePriceDetailDTO.AddDTO price : existPriceList) {
                    int[] existRange = {price.getMinQty(), price.getMaxQty()};
                    // 相同的SKU区间需要更新，区间一样可以更新，不算做区间交叉
                    if(detailDTO.getMinQty().intValue() != price.getMinQty().intValue()
                       || price.getMinQty().intValue() != price.getMaxQty().intValue()) {
                        boolean isCross = checkCross(addRange, existRange);
                        if(isCross) {
                            errorMsgList.add(StrUtil.format("区间存在重叠，系统已存在区间[{}, {}]", price.getMinQty(),price.getMaxQty() ));
                            break;
                        }
                    }
                }
            }
        }

        // 与该Excel已有的行做关联验证
        if(CollUtil.isNotEmpty(importList)) {
            Map<String, List<ImportPurchasePriceExcelDTO>> importPurchaseMap = importList.stream().collect(Collectors.groupingBy(ImportPurchasePriceExcelDTO::getSupplierName));
            List<ImportPurchasePriceExcelDTO> importPriceList = importPurchaseMap.get(supplierName);
            for(ImportPurchasePriceExcelDTO price : importPriceList) {
                if(StrUtils.isInteger(price.getMinQty()) && StrUtils.isInteger(price.getMaxQty())
                   && StrUtils.isInteger(excelDTO.getMinQty()) && StrUtils.isInteger(excelDTO.getMaxQty()) ) {
                    int[] addRange = {Integer.parseInt(excelDTO.getMinQty()), Integer.parseInt(excelDTO.getMaxQty())};
                    int[] existRange = {Integer.parseInt(price.getMinQty()), Integer.parseInt(price.getMaxQty())};
                    boolean isCross = checkCross(addRange, existRange);
                    if(isCross) {
                        errorMsgList.add(StrUtil.format("区间存在重叠，导入Excel已存在区间[{}, {}]", price.getMinQty(),price.getMaxQty() ));
                        break;
                    }
                }
            }
        }

        importList.add(excelDTO);

        PurchasePriceDTO.ImportAddDTO existSupplierPurchasePrice = addList.stream().filter(r -> Objects.equals(addDTO.getSupplierName(), r.getSupplierName())).findFirst().orElse(null);
        if (Objects.isNull(existSupplierPurchasePrice)) {
            addList.add(addDTO);
        }

        if(CollUtil.isNotEmpty(errorMsgList)) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

    /**
     * 判断是否日期格式
     * @param dateStr
     * @return
     */
    private static boolean isDate(String dateStr) {
        if(StrUtils.isNotEmpty(dateStr)) {
            try {
                LocalDate.parse(dateStr, TIME_FORMAT);
                return true;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkCross(int[] range0, int[] range1) {
        int max = Math.max(range0[0] , range1[0]);
        int min = Math.min(range0[1] , range1[1]);
        boolean isRepeat = max < min;
        return isRepeat;
    }

    private static boolean isBigDecimal(String str) {
        try {
            BigDecimal bd = new BigDecimal(str);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<ImportPurchasePriceExcelDTO> getErrorList() {
        return errorList;
    }

}