package com.erp.server.scm.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.enums.ApiError;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.excel.ImportPurchasePriceExcelDTO;
import com.erp.model.scm.entity.PurchasePriceDetailEntity;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.server.scm.service.PurchasePriceDetailService;
import com.erp.server.scm.service.PurchasePriceService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @CreateTime: 2023-08-03  18:07
 * @Author: zhangchunlin
 */
@Slf4j
public class PurchasePriceExcelListener extends AnalysisEventListener<ImportPurchasePriceExcelDTO> {

    private List<FindUserDTO> userList;

    private List<SkuVO> skuList;

    private List<DictCurrencyEntity> currencyList;

    List<Map<String, Object>> supplierList;

    private List<BaseIdDTO> orgList;

    private PurchasePriceDetailService priceDetailService;

    private PurchasePriceService purchasePriceService;

    /**
     * 错误信息
     */
    private List<ImportPurchasePriceExcelDTO> errorList = new ArrayList<>();

    /**
     * 可以添加的数据
     */
    private List<PurchasePriceDTO.ImportAddDTO> handleList = new ArrayList<>();


    /**
     * 已经解析的上传数据
     */
    private List<ImportPurchasePriceExcelDTO> importList = new ArrayList<>();

    private static final String DEFAULT_PURCHASE_ORG_NAME = "东莞市简拍智造科技有限公司";

    private static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static DateTimeFormatter TIME_FORMAT2 = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 用于补充月份和日期中缺少的零的正则表达式
    private static final Pattern pattern = Pattern.compile("(\\d{4})/(\\d{1,2})/(\\d{1,2})");
    private static final Pattern pattern2 = Pattern.compile("(\\d{4})-(\\d{1,2})-(\\d{1,2})");

    //为TIME_FORMAT2 进行补充数据结构
    public static LocalDate parseDate(String dateString, String splitStr,Pattern pattern, DateTimeFormatter TIME_FORMAT) {
        // 使用正则表达式补充零
        Matcher matcher = pattern.matcher(dateString);
        if (matcher.matches()) {
            String year = matcher.group(1);
            String month = matcher.group(2).length() == 1 ? "0" + matcher.group(2) : matcher.group(2);
            String day = matcher.group(3).length() == 1 ? "0" + matcher.group(3) : matcher.group(3);
            dateString = year + splitStr + month + splitStr + day;
        }
        // 使用补充后的字符串进行解析
        return LocalDate.parse(dateString, TIME_FORMAT);
    }
    private static final String DEFAULT_CURRENCY = "CNY";

    private static final List<String> CHECK_STATUS_LIST = Lists.newArrayList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus(), ApproveStatusEnum.APPROVE.getStatus(),
            ApproveStatusEnum.REJECT.getStatus());

    private static final List<String> NO_CROSS_STATUS_LIST = Lists.newArrayList(ApproveStatusEnum.WAIT_SUBMIT.getStatus(), ApproveStatusEnum.APPROVE_ING.getStatus(), ApproveStatusEnum.APPROVE.getStatus(),
            ApproveStatusEnum.REJECT.getStatus());

    private static final int MAX_QTY = 9999999;


    public PurchasePriceExcelListener(List<FindUserDTO> userList, List<SkuVO> skuList, List<DictCurrencyEntity> currencyList, List<Map<String, Object>> supplierList,
                                      List<BaseIdDTO> orgList, PurchasePriceDetailService priceDetailService, PurchasePriceService purchasePriceService) {
        this.userList = userList;
        this.skuList = skuList;
        this.currencyList = currencyList;
        this.supplierList = supplierList;
        this.orgList = orgList;
        this.priceDetailService = priceDetailService;
        this.purchasePriceService = purchasePriceService;
    }

    @Override
    public void invoke(ImportPurchasePriceExcelDTO excelDTO, AnalysisContext analysisContext) {
        List<String> errorMsgList = new ArrayList<>();
        //基础验证
        List<String> msgList = FieldValidUtil.fieldValid(excelDTO);
        if (CollUtil.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        PurchasePriceDTO.ImportAddDTO addDTO;
        String supplierName = StrUtils.null2EmptyWithTrim(excelDTO.getSupplierName());

        PurchasePriceDTO.ImportAddDTO existSupplierPurchasePrice = handleList.stream().filter(r -> Objects.equals(supplierName, r.getSupplierName())).findFirst().orElse(null);
        if(Objects.isNull(existSupplierPurchasePrice)) {
            addDTO = new PurchasePriceDTO.ImportAddDTO();
        } else {
            addDTO = existSupplierPurchasePrice;
        }

        Map<String, Object> supplierMap  = supplierList.stream().filter(r->Objects.equals(supplierName, StrUtils.null2EmptyWithTrim(r.get("name")))).findFirst().orElse(null);
        if(Objects.isNull(supplierMap)) {
            errorMsgList.add(ApiError.ERROR_SUPPLIER_ABSENCE.msg);
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
                LocalDate quotedDate = getDate(quotedDateStr);
                addDTO.setQuotedDate(quotedDate);
            }
        } else {
            addDTO.setQuotedDate(LocalDate.now());
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
            addDTO.setPricingUserName(pricingUserName);
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

        PurchasePriceDetailDTO.ImportSaveDTO detailDTO = new  PurchasePriceDetailDTO.ImportSaveDTO();
        //定价员id
        detailDTO.setPricingUserId(addDTO.getPricingUserId());

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
        if(StrUtils.isNotEmpty(deliveryDayStr)) {
            if(StrUtils.isInteger(deliveryDayStr)) {
                Integer deliveryDay = Integer.parseInt(deliveryDayStr);
                if(deliveryDay < 0) {
                    errorMsgList.add("采购交期不能小于0");
                } else {
                    detailDTO.setDeliveryDay(deliveryDay);
                }
            }
        } else {
            detailDTO.setDeliveryDay(0);
        }

        // 区间从
        String minQtyStr = excelDTO.getMinQty();
        if(StrUtils.isNotEmpty(minQtyStr)) {
            if(StrUtils.isInteger(minQtyStr)) {
                int minQty = Integer.parseInt(minQtyStr);
                if(minQty < 0) {
                    errorMsgList.add("区间从最小值错误");
                }
                detailDTO.setMinQty(minQty);
            } else {
                detailDTO.setMinQty(null);
            }
        } else {
            detailDTO.setMinQty(0);
        }
        // 区间到
        String maxQtyStr = excelDTO.getMaxQty();
        if(StrUtils.isNotEmpty(maxQtyStr)) {
            if(StrUtils.isInteger(maxQtyStr)) {
                int maxQty = Integer.parseInt(maxQtyStr);
                if(maxQty > MAX_QTY) {
                    errorMsgList.add("区间到最大值错误");
                }
                detailDTO.setMaxQty(maxQty);
            } else {
                detailDTO.setMaxQty(null);
            }
        } else {
            detailDTO.setMaxQty(MAX_QTY);
        }

        // 含税单价
        String taxPriceStr = excelDTO.getTaxPrice();
        if(StrUtils.isNotEmpty(taxPriceStr) && isBigDecimal(taxPriceStr)) {
            BigDecimal taxPrice = new BigDecimal(taxPriceStr);
            if(taxPrice.compareTo(BigDecimal.ZERO) < 1) {
                errorMsgList.add("含税单价错误不能小于等于0");
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
                LocalDate effectiveDate = getDate(effectiveDateStr);
                detailDTO.setEffectiveDate(effectiveDate);
            }
        } else {
            detailDTO.setEffectiveDate(LocalDate.now());
        }

        // 失效时间
        String expireDateStr = excelDTO.getExpireDate();
        if(StrUtils.isNotEmpty(expireDateStr)) {
            if(!isDate(expireDateStr)) {
                errorMsgList.add("生效日期格式错误");
            } else {
                LocalDate expireDate = getDate(expireDateStr);
                detailDTO.setExpireDate(expireDate);
            }
        } else {
            // 默认设置为9999年12月31日
            detailDTO.setExpireDate(LocalDate.of(9999, 12, 31));
        }

        // 启用状态
        String disabledStr = excelDTO.getDisabled();
        detailDTO.setDisabled(Objects.equals(disabledStr, "停用"));

        // 验证 区间从和区间到
        if(Objects.nonNull(detailDTO.getMaxQty()) && Objects.nonNull(detailDTO.getMinQty())
                && detailDTO.getMaxQty().intValue() == detailDTO.getMinQty().intValue()) {
            errorMsgList.add("区间从，区间到两个值不能相同");
        }
        if(Objects.nonNull(detailDTO.getMaxQty()) && Objects.nonNull(detailDTO.getMinQty())
                  && detailDTO.getMaxQty().intValue() < detailDTO.getMinQty().intValue()) {
            errorMsgList.add("区间从值不能大于区间到值");
        }

        //根据供应商 获取到系统已有的区间
        if(StrUtils.isNotEmpty(addDTO.getSupplierId())) {
            if(Objects.nonNull(detailDTO.getMinQty()) && Objects.nonNull(detailDTO.getMaxQty())) {
                List<PurchasePriceDetailEntity> supplierPriceDetailList = priceDetailService.getBySupplierIdAndStatus(addDTO.getSupplierId(),addDTO.getPurchaseOrgId(), CHECK_STATUS_LIST);
                Map<String, List<PurchasePriceDetailEntity>> existPriceMap = supplierPriceDetailList.stream().collect(Collectors.groupingBy(PurchasePriceDetailEntity::getSkuId));
                if(existPriceMap.containsKey(detailDTO.getSkuId())) {
                    int[] addRange = {detailDTO.getMinQty(), detailDTO.getMaxQty()};
                    List<PurchasePriceDetailEntity> existPriceList = existPriceMap.get(detailDTO.getSkuId());
                    List<String> detailIds = Lists.newArrayList();
                    for(PurchasePriceDetailEntity price : existPriceList) {
                        int[] existRange = {price.getMinQty(), price.getMaxQty()};
                        // 相同的SKU区间需要更新，区间一样可以更新，不算做区间交叉
                        if(detailDTO.getMinQty().intValue() != price.getMinQty().intValue()
                                || detailDTO.getMaxQty().intValue() != price.getMaxQty().intValue()) {
                            if(NO_CROSS_STATUS_LIST.contains(price.getApproveStatus())) {
                                boolean isCross = checkCross(addRange, existRange);
                                if(isCross) {
                                    errorMsgList.add(StrUtil.format("区间存在重叠，系统已存在区间[{}, {}]", price.getMinQty(),price.getMaxQty() ));
                                    break;
                                }
                            }
                        } else {
                            // 待提交可以区间相同
                            if(!Objects.equals(price.getApproveStatus(), ApproveStatusEnum.WAIT_SUBMIT.getStatus())
                                    && !Objects.equals(price.getApproveStatus(), ApproveStatusEnum.REJECT.getStatus())) {
                                errorMsgList.add(StrUtil.format("区间存在重叠，系统已存在区间[{}, {}]", price.getMinQty(),price.getMaxQty() ));
                                break;
                            } else {
                                // 此处审核不通过，可以存在同区间的多个，会存在覆盖问题
                                detailIds.add(price.getId());
                            }
                        }
                    }
                    if(CollUtil.isNotEmpty(detailIds)) {
                        detailDTO.setIds(detailIds);
                    }
                }
            }
        }

        // 与该Excel已有的行做关联验证
        if(CollUtil.isNotEmpty(importList)) {
            if(Objects.nonNull(detailDTO.getMinQty()) && Objects.nonNull(detailDTO.getMaxQty())) {
                Map<String, List<ImportPurchasePriceExcelDTO>> importPurchaseMap = importList.stream().collect(Collectors.groupingBy(r->StrUtils.null2EmptyWithTrim(r.getSupplierName()) + "-" + StrUtils.null2EmptyWithTrim(r.getSkuNo()) + "-" + StrUtils.null2EmptyWithTrim(r.getPurchaseOrgName())));
                String checkKey = StrUtils.null2EmptyWithTrim(addDTO.getSupplierName()) + "-" + StrUtils.null2EmptyWithTrim(detailDTO.getSkuNo())+ "-" + StrUtils.null2EmptyWithTrim(excelDTO.getPurchaseOrgName());
                List<ImportPurchasePriceExcelDTO> importPriceList = importPurchaseMap.get(checkKey);
                if(CollUtil.isNotEmpty(importPriceList)) {
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
            }
        }

        importList.add(excelDTO);

        if(CollUtil.isNotEmpty(errorMsgList)) {
            excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(excelDTO);
            return;
        }

        List<PurchasePriceDetailDTO.ImportSaveDTO> detailList = addDTO.getDetailList();
        if(CollUtil.isEmpty(detailList)) {
            detailList = Lists.newArrayList();
        }
        detailList.add(detailDTO);
        addDTO.setDetailList(detailList);

        if (Objects.isNull(existSupplierPurchasePrice)) {
            handleList.add(addDTO);
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if(CollUtil.isNotEmpty(handleList)) {
            purchasePriceService.batchImport(handleList);
        }
    }

    /**
     * 判断是否日期格式
     * @param dateStr
     * @return
     */
    private static boolean isDate(String dateStr) {
        if(StrUtils.isNotEmpty(dateStr)) {
            try {
                parseDate(dateStr,"/",pattern, TIME_FORMAT);
                return true;
            } catch (Exception e) {
                log.error("日期转换异常{},{}",dateStr,"yyyy/MM/dd");
            }
            try {
                parseDate(dateStr,"-",pattern2, TIME_FORMAT2);
                return true;
            }catch (Exception e2){
                log.error("日期转换异常{},{}",dateStr,"yyyy-MM-dd");
            }
            return false;
        }
        return true;
    }
    /**
     * 判断是否日期格式
     * @param dateStr
     * @return
     */
    private static LocalDate getDate(String dateStr) {
        if(StrUtils.isNotEmpty(dateStr)) {
            try {
                return parseDate(dateStr,"/",pattern, TIME_FORMAT);
            } catch (Exception e) {
                log.error("日期转换异常{},{}",dateStr,"yyyy/MM/dd");
            }
            try {
                return parseDate(dateStr,"-",pattern2, TIME_FORMAT2);
            }catch (Exception e2){
                log.error("日期转换异常{},{}",dateStr,"yyyy-MM-dd");
            }
        }
        return null;
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