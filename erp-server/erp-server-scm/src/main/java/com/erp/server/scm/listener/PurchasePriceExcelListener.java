package com.erp.server.scm.listener;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.excel.ImportPurchasePriceExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @CreateTime: 2023-08-03  18:07
 * @Author: zhangchunlin
 */
public class PurchasePriceExcelListener extends AnalysisEventListener<ImportPurchasePriceExcelDTO> {

    private List<FindUserDTO> userList;

    private List<SkuVO> skuList;

    private List<DictCurrencyEntity> currencyList;

    private List<SupplierEntity> supplierList;

    private List<BaseIdDTO> orgList;

    /**
     * 错误信息
     */
    private List<ImportPurchasePriceExcelDTO> errorList = new ArrayList<>();

    private List<PurchasePriceDTO.ImportAddDTO> addList = new ArrayList<>();

    private static final String DEFAULT_PURCHASE_ORG_NAME = "东莞市简拍智造科技有限公司";

    private static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/M/d");

    private static final String DEFAULT_CURRENCY = "CNY";

    public PurchasePriceExcelListener(List<FindUserDTO> userList, List<SkuVO> skuList, List<DictCurrencyEntity> currencyList, List<SupplierEntity> supplierList,
                                      List<BaseIdDTO> orgList) {
        this.userList = userList;
        this.skuList = skuList;
        this.currencyList = currencyList;
        this.supplierList = supplierList;
        this.orgList = orgList;
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
        String supplierName = excelDTO.getSupplierName();
        SupplierEntity supplierEntity = supplierList.stream().filter(r->Objects.equals(supplierName, r.getName())).findFirst().orElse(null);
        if(Objects.isNull(supplierEntity)) {
            errorMsgList.add("供应商不存在");
        } else {
            addDTO.setSupplierId(supplierEntity.getId());
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

        // 明细
        String skuNo = excelDTO.getSkuNo();
        SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(skuNo)).findFirst().orElse(null);
        if (Objects.isNull(skuEntity)) {
            errorMsgList.add("sku编码错误");
        } else {
            addDTO.setSkuId(skuEntity.getSkuId());
            addDTO.setSkuNo(skuNo);
            addDTO.setProductName(skuEntity.getSkuName());
        }

        // 采购交期
        String deliveryDay = excelDTO.getDeliveryDay();
        if(StrUtils.isNotEmpty(deliveryDay)) {
            addDTO.setDeliveryDay(Integer.parseInt(deliveryDay));
        }

        // 区间从
        String minQtyStr = excelDTO.getMinQty();
        if(StrUtils.isNotEmpty(minQtyStr)) {
            int minQty = Integer.parseInt(minQtyStr);
            if(minQty < 0) {
                errorMsgList.add("区间从最小值错误");
            }
            addDTO.setMinQty(minQty);
        } else {
            addDTO.setMinQty(0);
        }
        // 区间到
        String maxQtyStr = excelDTO.getMaxQty();
        if(StrUtils.isNotEmpty(maxQtyStr)) {
            int maxQty = Integer.parseInt(minQtyStr);
            addDTO.setMaxQty(Integer.parseInt(maxQtyStr));
        } else {
            addDTO.setMaxQty(9999999);
        }
        // 币制代码
        String currency = excelDTO.getCurrency();
        if(StrUtils.isNotEmpty(currency)) {
            DictCurrencyEntity dictCurrencyEntity = currencyList.stream().filter(r->Objects.equals(currency, r.getId())).findFirst().orElse(null);
            if(Objects.isNull(dictCurrencyEntity)) {
                errorMsgList.add("币制编码错误");
            } else {
                addDTO.setCurrency(currency);
            }
        } else {
            addDTO.setCurrency(DEFAULT_CURRENCY);
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

}