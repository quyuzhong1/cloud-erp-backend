package com.erp.server.mrp.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.mrp.dto.CfgRuleSalesEstimateFileDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.server.mrp.es.entity.CustomerSalesEstimateEsEntity;
import lombok.Getter;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
public class SalesEstimateExcelFileListener extends AnalysisEventListener<CfgRuleSalesEstimateFileDTO.ExcelDTO> {


    /**
     * 导入错误数据
     */
    private final List<CfgRuleSalesEstimateFileDTO.ExcelDTO> errorList = new ArrayList<>();

    /**
     * 导入正确数据
     */
    private final List<CustomerSalesEstimateEsEntity> successList = new ArrayList<>();

    private final Map<String, String> skuMap;

    private final List<ShopInfoEntity> shopInfoList;

    private final Map<String, String> platformMap;

    private final String platform;

    private static final String REGEX = "^([1-9]\\d{0,8}|0)(\\.\\d{1,2})?$";

    public SalesEstimateExcelFileListener(Map<String, String> skuMap, List<ShopInfoEntity> shopInfoList, Map<String, String> platformMap, String platform) {
        this.skuMap = skuMap;
        this.shopInfoList = shopInfoList;
        this.platformMap = platformMap;
        this.platform = platform;
    }


    @Override
    public void invoke(CfgRuleSalesEstimateFileDTO.ExcelDTO data, AnalysisContext context) {
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (!CollectionUtils.isEmpty(msgList)) {
            msgList.add(String.join(",", msgList));
        }
        if (!platformMap.containsKey(data.getPlatformName())) {
            msgList.add("平台不存在");
        }
        Map<String, String> shopMap = shopInfoList.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getName, ShopInfoEntity::getId, (o1, o2) -> o1));
        if (!shopMap.containsKey(data.getShopName())) {
            msgList.add("店铺不存在");
        }
        ShopInfoEntity info = shopInfoList.stream()
                .filter(v -> v.getName().equals(data.getShopName()))
                .filter(v -> v.getDictPlatform().equals(platformMap.get(data.getPlatformName())))
                .findFirst()
                .orElse(null);
        if (ObjectUtils.isEmpty(info)) {
            msgList.add("该平台下无此店铺");
        }
        if (!skuMap.containsKey(data.getSkuNo())) {
            msgList.add("sku未审核或不存在");
        }
        LocalDate date = LocalDateUtil.parseStrToLocalDate(data.getDate());
        if (LocalDate.now().isAfter(date)) {
            msgList.add("日期，仅限导入未来日期的预估销量，必须晚于今日");
        }
        if (data.getSalesQty().matches(REGEX)) {
            msgList.add("预估日销量：0≤X≤999999999，最多保留2位小数");
        }
        if (!platform.equals(platformMap.get(data.getPlatformName()))) {
            msgList.add("只能导入" + platformMap.get(platform) + "平台的数据");
        }
        CustomerSalesEstimateEsEntity dto = successList.stream()
                .filter(v -> v.getPlatformName().equals(data.getPlatformName()))
                .filter(v -> v.getShopName().equals(data.getShopName()))
                .filter(v -> v.getSkuNo().equals(data.getSkuNo()))
                .filter(v -> v.getDate().equals(date))
                .findFirst()
                .orElse(null);
        if (!ObjectUtils.isEmpty(dto)) {
            msgList.add("平台+店铺+SKU+日期，唯一行");
        }
        //存在错误数据则直接返回
        if (!CollectionUtils.isEmpty(msgList)) {
            data.setErrorMsg(String.join(",", msgList));
            errorList.add(data);
            return;
        }
        CustomerSalesEstimateEsEntity excelSuccessDTO = new CustomerSalesEstimateEsEntity();
        excelSuccessDTO.setPlatform(platformMap.get(data.getPlatformName()));
        excelSuccessDTO.setPlatformName(data.getPlatformName());
        excelSuccessDTO.setShopId(shopMap.get(data.getShopName()));
        excelSuccessDTO.setShopName(data.getShopName());
        excelSuccessDTO.setSkuId(skuMap.get(data.getSkuNo()));
        excelSuccessDTO.setSkuNo(data.getSkuNo());
        excelSuccessDTO.setDate(date);
        excelSuccessDTO.setSalesQty(new BigDecimal(data.getSalesQty()));
        excelSuccessDTO.setShopSkuId(excelSuccessDTO.getShopId()+ "-" + excelSuccessDTO.getSkuId());
        successList.add(excelSuccessDTO);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {

    }
}
