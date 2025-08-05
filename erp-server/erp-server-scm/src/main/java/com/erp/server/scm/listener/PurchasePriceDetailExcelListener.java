package com.erp.server.scm.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.StrUtils;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.PurchasePriceDetailDTO;
import com.erp.model.scm.dto.excel.PurchasePriceDetailImportExcelDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Lambda
 * @Classname PurchasePriceDetailExcelListener

 * @Date 2023-03-27 17:16
 * @Created by yl
 */
@Slf4j
public class PurchasePriceDetailExcelListener extends AnalysisEventListener<PurchasePriceDetailImportExcelDTO> {

    DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/M/d");

    /**
     * sku数据
     */
    private List<SkuVO> skuList;


    /**
     * 成功的数据
     */
    private List<PurchasePriceDetailDTO.AddDTO> successList = new ArrayList<>();


    /**
     * 导入错误数据
     */
    private List<PurchasePriceDetailImportExcelDTO> errorList = new ArrayList<>();


    public PurchasePriceDetailExcelListener(List<SkuVO> skuList) {
        this.skuList = skuList;
    }
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
    /**
     * 每解析一行数据回调一遍
     *
     * @param purchasePriceDetailImportExcelDTO
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-27 17:17
     */
    @Override
    public void invoke(PurchasePriceDetailImportExcelDTO purchasePriceDetailImportExcelDTO, AnalysisContext analysisContext) {

        PurchasePriceDetailDTO.AddDTO addDTO = new PurchasePriceDetailDTO.AddDTO();
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(purchasePriceDetailImportExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        if (CollectionUtils.isEmpty(skuList)) {
            errorMsgList.add("系统中未发现已审核SKU");
        } else {
            SkuVO skuEntity = skuList.stream().filter(obj -> obj.getSkuNo().equals(purchasePriceDetailImportExcelDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(skuEntity)) {
                errorMsgList.add("sku有误");
            }
            if (skuEntity != null) {
                addDTO.setDeliveryDay(purchasePriceDetailImportExcelDTO.getDeliveryDay());
                String effectiveDateStr = purchasePriceDetailImportExcelDTO.getEffectiveDateStr();
                addDTO.setEffectiveDate(StringUtils.isBlank(effectiveDateStr) ? null : getDate(effectiveDateStr));
                String expireDateStr = purchasePriceDetailImportExcelDTO.getExpireDateStr();
                addDTO.setExpireDate(StringUtils.isBlank(expireDateStr) ? LocalDate.of(9999,12,31) : getDate(expireDateStr));
                addDTO.setMinQty(purchasePriceDetailImportExcelDTO.getMinQty());
                addDTO.setMaxQty(purchasePriceDetailImportExcelDTO.getMaxQty());
                addDTO.setTaxPrice(purchasePriceDetailImportExcelDTO.getTaxPrice());
                addDTO.setTaxRate(purchasePriceDetailImportExcelDTO.getTaxRate());
                addDTO.setSkuId(skuEntity.getSkuId());
                addDTO.setSkuNo(skuEntity.getSkuNo());
                addDTO.setProductName(skuEntity.getSkuName());
                successList.add(addDTO);
            }
        }

        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            purchasePriceDetailImportExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(purchasePriceDetailImportExcelDTO);
            return;
        }

    }


    /**
     * 数据全部解析完成
     *
     * @param analysisContext
     * @return void
     * @author yl
     * @date 2023-03-27 17:17
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }

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
    public List<PurchasePriceDetailImportExcelDTO> getErrorList() {
        return errorList;
    }


    public List<PurchasePriceDetailDTO.AddDTO> getSuccessList() {
        return successList;
    }
}
