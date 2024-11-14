package com.erp.server.mrp.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.mrp.dto.CfgRuleCalcDTO;
import com.erp.model.mrp.entity.CfgRuleCalcEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.server.mrp.es.entity.CalcSalesInfoHisEsEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;

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


    public HistorySalesQtyExcelListener(List<SkuVO> skuVOS, List<ShopInfoEntity> shopInfoList, CfgRuleCalcEntity cfgRuleCalc) {
        this.skuVOS = skuVOS;
        this.shopInfoList = shopInfoList;
        this.cfgRuleCalc = cfgRuleCalc;
    }

    private List<CalcSalesInfoHisEsEntity> dataList = new ArrayList<>();
    private List<CfgRuleCalcDTO.HistorySaleImportDTO> errorList = new ArrayList<>();

    @Override
    public void invoke(CfgRuleCalcDTO.HistorySaleImportDTO data, AnalysisContext context) {
        //注解验证信息
        List<String> msgList = FieldValidUtil.fieldValid(data);
        if (CollectionUtils.isNotEmpty(msgList)) {
            data.setErrorMsg(String.join(",", msgList));
            errorList.add(data);
        }
        Map<String, String> skuMap = skuVOS.stream()
                .collect(Collectors.toMap(SkuVO::getSkuNo, SkuVO::getSkuId, (o1, o2) -> o1));
        Map<String, String> shopMap = shopInfoList.stream()
                .collect(Collectors.toMap(ShopInfoEntity::getName, ShopInfoEntity::getId, (o1, o2) -> o1));
        if (!shopMap.containsKey(data.getShopName())) {
            data.setErrorMsg("该店铺不在已选中的试算店铺中");
            errorList.add(data);
        }
        if (!skuMap.containsKey(data.getSkuNo())) {
            data.setErrorMsg("该sku不在已选中的试算sku中");
            errorList.add(data);
        }
        CalcSalesInfoHisEsEntity entity = new CalcSalesInfoHisEsEntity();
        entity.setSkuId(skuMap.get(data.getSkuNo()));
        entity.setSkuNo(data.getSkuNo());
        entity.setShopId(shopMap.get(data.getShopName()));
        entity.setShopName(data.getShopName());
        entity.setDate(LocalDateUtil.parseStrToLocalDate(data.getBillDate()));
        entity.setQty(Integer.parseInt(data.getQty()));
        entity.setCfgRuleCalcId(cfgRuleCalc.getId());
        dataList.add(entity);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        dataList = new ArrayList<>(dataList.stream()
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
