package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.utils.CollectionUtils;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.enums.MabangOriginalOrderStatusEnum;
import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.sys.entity.DictCountryEntity;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 退货订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MabangReturnOrderDmpHandler extends MabangDmpHandler {


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

                //国家
                Object countryNameCN = dmpDataMap.get("countryNameCN");
                if (countryNameCN != null) {

                    //转换erp的国家二字码
                    String country = String.valueOf(countryNameCN);
                    List<DictCountryEntity> dictCountry = dmpHandlerCache.getDictCountry(country);
                    if (CollectionUtil.isNotEmpty(dictCountry)) {
                        dmpDataMap.put("country", dictCountry.get(0).getId());
                    }
                }
            }
        }
    }
}
