package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.util.StrUtil;
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
 * 订单主表字段映射转换
 */
@Service
@Scope("prototype")
public class MabangOrderDmpHandler extends MabangDmpHandler {

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                //销售平台
                Object platformId = dmpDataMap.get("platformId");
                if (platformId != null) {
                    String platform = String.valueOf(platformId);

                    //平台编码转ERP编码
                    MabangSourcePlatformEnum platformEnum = MabangSourcePlatformEnum.getByCode(platform) == null ?
                            MabangSourcePlatformEnum.getByDesc(platform) : MabangSourcePlatformEnum.getByCode(platform);

                    if (platformEnum == null) {
                        platformEnum = MabangSourcePlatformEnum.getByName(platform);
                    }
                    dmpDataMap.put("sourcePlatform", platformEnum.getErpPlatformCode() == null ? platform : platformEnum.getErpPlatformCode());
                }
                //平台单号
                Object thirdCodeObj = dmpDataMap.get("thirdCode");
                if (thirdCodeObj != null) {
                    String thirdCode = String.valueOf(thirdCodeObj);
                    dmpDataMap.put("platformCode", thirdCode);
                }

                //销售原始订单状态
                Object orderStatus = dmpDataMap.get("orderstatus");
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
