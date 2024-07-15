package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 退款单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MabangSoOutstockDmpHandler extends MabangDmpHandler {


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

                //状态：3：已发货，4：已作废
                Object statusObj = dmpDataMap.get("status");
                if (statusObj != null) {
                    //平台名
                    Integer status = Integer.valueOf(statusObj+"");
                    if (status == 3) {
                        dmpDataMap.put("status", "1");
                    } else if (status == 4) {
                        dmpDataMap.put("status", "2");
                    } else {
                        dmpDataMap.put("status", "3");
                    }
                }
            }
        }
    }
}
