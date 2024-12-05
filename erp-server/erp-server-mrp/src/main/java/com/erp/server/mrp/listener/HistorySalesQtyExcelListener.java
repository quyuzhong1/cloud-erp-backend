package com.erp.server.mrp.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.mrp.dto.CalcSalesInfoHisDTO;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@NoArgsConstructor
@Getter
public class HistorySalesQtyExcelListener extends AnalysisEventListener<CfgRuleCalcDTO.HistorySaleImportDTO> {

    private List<SkuVO> skuVOS;
    private List<ShopInfoEntity> shopInfoList;
    private CfgRuleCalcEntity cfgRuleCalc;
    private Map<String, String> platformMap;

    public HistorySalesQtyExcelListener(List<SkuVO> skuVOS, List<ShopInfoEntity> shopInfoList, Map<String, String> platformMap, CfgRuleCalcEntity cfgRuleCalc) {
        this.skuVOS = skuVOS;
        this.shopInfoList = shopInfoList;
        this.cfgRuleCalc = cfgRuleCalc;
        this.platformMap = platformMap;
    }

    private List<CalcSalesInfoHisEsEntity> dataList = new ArrayList<>();
    private List<CalcSalesInfoHisDTO> successList = new ArrayList<>();
    private final List<CfgRuleCalcDTO.HistorySaleImportDTO> errorList = new ArrayList<>();

    @Override
    public void invoke(CfgRuleCalcDTO.HistorySaleImportDTO data, AnalysisContext context) {
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (!CollectionUtils.isEmpty(msgList)) {
            data.setErrorMsg(String.join(",", msgList));
            errorList.add(data);
            return;
        }
        Map<String, String> skuMap = skuVOS.stream()
                .collect(Collectors.toMap(SkuVO::getSkuNo, SkuVO::getSkuId, (o1, o2) -> o1));
        if (!skuMap.containsKey(data.getSkuNo())) {
            data.setErrorMsg("该sku不在已选中的试算sku中");
            errorList.add(data);
            return;
        }
        Map<String, String> shopMap = shopInfoList.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getName, ShopInfoEntity::getId, (o1, o2) -> o1));
        if (!shopMap.containsKey(data.getShopName())) {
            data.setErrorMsg("该店铺不在已选中的试算店铺中");
            errorList.add(data);
            return;
        }
        String dictPlatform = platformMap.get(data.getPlatform());
        ShopInfoEntity info = shopInfoList.stream()
                .filter(v -> v.getName().equals(data.getShopName()))
                .filter(v -> v.getDictPlatform().equals(dictPlatform))
                .findFirst()
                .orElse(null);
        if (ObjectUtils.isEmpty(info)) {
            data.setErrorMsg("该平台下无此店铺");
            errorList.add(data);
            return;
        }

        boolean isExit = successList.stream().anyMatch(v -> v.getSkuNo().equals(data.getSkuNo()) &&
                v.getShopName().equals(data.getShopName()) &&
                v.getPlatform().equals(data.getPlatform())
        );
        //校验重复数据
        if (isExit) {
            data.setErrorMsg("sku+平台+店铺重复");
            errorList.add(data);
            return;
        }
        //存在错误数据则直接返回
        if (!CollectionUtils.isEmpty(errorList)) {
            return;
        }
        CalcSalesInfoHisDTO entity = new CalcSalesInfoHisDTO();
        entity.setSkuId(skuMap.get(data.getSkuNo()));
        entity.setSkuNo(data.getSkuNo());
        entity.setShopId(shopMap.get(data.getShopName()));
        entity.setShopName(data.getShopName());
        entity.setDate(LocalDateUtil.parseStrToLocalDate(data.getBillDate()));
        entity.setQty(Integer.parseInt(data.getQty()));
        entity.setCfgRuleCalcId(cfgRuleCalc.getId());
        entity.setPlatform(data.getPlatform());
        successList.add(entity);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        dataList = new ArrayList<>(successList.stream()
                .map(v -> BeanMapperUtils.map(CalcSalesInfoHisEsEntity.class, v))
                .collect(Collectors.toMap(
                        v -> new CfgRuleCalcDTO.GroupDTO(v.getSkuId(), v.getShopId(), v.getDate()),
                        v -> v,
                        (v1, v2) -> {
                            v1.setQty(v1.getQty() + v2.getQty());
                            return v1;
                        }
                ))
                .values());
    }
}
