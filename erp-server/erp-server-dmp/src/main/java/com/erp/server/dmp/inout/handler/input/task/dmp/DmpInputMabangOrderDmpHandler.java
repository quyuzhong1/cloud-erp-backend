package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.util.StrUtil;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.enums.DmpOrderReturnStatusEnum;
import com.erp.model.dmp.enums.MabangOriginalOrderStatusEnum;
import com.erp.model.dmp.enums.MabangSourcePlatformEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputMabangOrderDmpHandler extends DmpInputMabangDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                //销售平台
                Object platformId = dmpDataMap.get("platformId");
                if (platformId != null) {
                    String platform = String.valueOf(platformId);
                    if(StrUtil.isNotBlank(platform) && platform.contains("亚马逊")){
                        platform = MabangSourcePlatformEnum.AMAZON_FBA.getCode();
                    }

                    //平台编码转ERP编码
                    MabangSourcePlatformEnum platformEnum = MabangSourcePlatformEnum.getByCode(platform) == null ?
                            MabangSourcePlatformEnum.getByDesc(platform) : MabangSourcePlatformEnum.getByCode(platform);
                    dmpDataMap.put("platformId", platformEnum.getErpPlatformCode());
                }

                //销售原始订单状态
                Object orderStatus = dmpDataMap.get("orderStatus");
                if (orderStatus != null) {
                    MabangOriginalOrderStatusEnum orderStatusEnum = MabangOriginalOrderStatusEnum.getByCode(Integer.valueOf(orderStatus + ""));
                    if (orderStatusEnum.getCode().equals(MabangOriginalOrderStatusEnum.INVALID.getCode())) {
                        dmpDataMap.put("invalidStatus", Boolean.TRUE);
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                    } else if (orderStatusEnum.getCode().equals(MabangOriginalOrderStatusEnum.INDISTRIBUTION.getCode())) {
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
                    } else if (orderStatusEnum.getCode().equals(MabangOriginalOrderStatusEnum.SHIPPED.getCode())) {
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                    } else if (orderStatusEnum.getCode().equals(MabangOriginalOrderStatusEnum.FINISH.getCode())) {
                        dmpDataMap.put("deliveryStatus", SoB2cBillStatusEnum.ENUM_SHIPPED.getCode());
                    }
                }

                //退货状态
                Object isReturned = dmpDataMap.get("isReturned");
                if (isReturned != null) {
                    //1.退货 2.非退货
                    Integer returnedStatus = Integer.valueOf(isReturned + "");
                    if (returnedStatus == 1) {
                        dmpDataMap.put("isReturned", DmpOrderReturnStatusEnum.ORDER_RETURN.getCode());
                    } else {
                        dmpDataMap.put("isReturned", DmpOrderReturnStatusEnum.NOT_RETURN.getCode());
                    }
                }

            }
        }
    }
}
