package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollectionUtil;
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
 * 退款单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MabangRefundOrderDmpHandler extends MabangDmpHandler {


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {

                //平台名
                Object platformName = dmpDataMap.get("platformname");
                if (platformName != null) {
                    //平台名
                    String platform = String.valueOf(platformName);

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

                //状态：1、新建退款 2、审核中（原主管审核）3、财务审核 4、成功 5、失败 6、作废 、7：已完结、8：平台申请退款、9：平台退款撤回、10：审核中
                Object flag = dmpDataMap.get("flag");
                if (flag != null) {
                    //退款状态：1、成功 2、失败 3、作废
                    Integer status = Integer.valueOf(flag+"");
                    if (status == 4) {
                        dmpDataMap.put("status", "1");
                    } else if (status == 5) {
                        dmpDataMap.put("status", "2");
                    } else if (status == 6) {
                        dmpDataMap.put("deliveryStatus", "3");
                    } else {
                        dmpDataMap.put("status", "0");
                    }
                }
            }
        }
    }
}
