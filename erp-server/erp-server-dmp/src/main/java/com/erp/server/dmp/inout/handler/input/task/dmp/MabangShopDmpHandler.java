package com.erp.server.dmp.inout.handler.input.task.dmp;

import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
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
public class MabangShopDmpHandler extends MabangDmpHandler {


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

                //销售平台
                Object platformId = dmpDataMap.get("platformName");
                if (platformId != null) {
                    String platform = String.valueOf(platformId);

                    //平台编码转ERP编码
                    MabangSourcePlatformEnum platformEnum = MabangSourcePlatformEnum.getByCode(platform) == null ?
                            MabangSourcePlatformEnum.getByDesc(platform) : MabangSourcePlatformEnum.getByCode(platform);

                    if (platformEnum == null) {
                        platformEnum = MabangSourcePlatformEnum.getByName(platform);
                    }
                    if (platformEnum != null) {
                        dmpDataMap.put("sourcePlatform", platformEnum.getErpPlatformCode());
                    }
                }

                //状态：3：已发货，4：已作废
                Object statusObj = dmpDataMap.get("status");
                if (statusObj != null) {
                    //平台名
                    Integer status = Integer.valueOf(statusObj+"");
                    if (status == 1) {
                        dmpDataMap.put("disabled", false);
                    } else {
                        dmpDataMap.put("disabled", true);
                    }
                }
            }
        }
    }
}
