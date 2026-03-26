package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.ParamData;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.entity.DmpThirdWarehouseInfoEntity;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.server.dmp.inout.dto.request.DmpInputHotfixCreateRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputCreateResponse;
import com.erp.server.dmp.inout.dto.response.DmpInputFinishResponse;
import com.erp.server.dmp.inout.handler.input.task.mongo.DmpInputMongoHandler;
import com.erp.server.dmp.service.DmpThirdWarehouseInfoService;
import com.erp.server.dmp.utils.MapCountUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 订单详情字段映射转换
 */
@Service
@Scope("prototype")
public class TikTokOrderDetailDmpHandler extends TikTokOrderGetDetailDmpHandler {

    @Resource
    private DmpThirdWarehouseInfoService dmpThirdWarehouseInfoService;

    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> dmpDataKeyMaps = dmpInputDataDmpRelationMap.getKey();
            String warehouseId = "";
            for (Map<String, Object> keyMap : dmpDataKeyMaps){
                Object warehouseIdObj = keyMap.get("warehouseId");
                if (ObjectUtil.isNotEmpty(warehouseIdObj)) {
                    warehouseId = warehouseIdObj.toString();
                }
            }
            String warehouseName = "";
            if(StringUtils.isNotBlank(warehouseId)){
                List<DmpThirdWarehouseInfoEntity> dmpThirdWarehouseInfoEntityList = dmpThirdWarehouseInfoService
                        .getByPlatformAndAuthIdAndCode(PlatformDictEnum.TIK_TOK.getCode(), nextLevelId, warehouseId);
                if(CollectionUtil.isEmpty(dmpThirdWarehouseInfoEntityList)){
                    //创建推送任务
                    Map<String, Object> map = new HashMap<>();
                    map.put("nextLevelId",nextLevelId);
                    String dataJson = JSON.toJSONString(map);
                    DmpInputHotfixCreateRequest dmpInputCreateRequest = new DmpInputHotfixCreateRequest();
                    dmpInputCreateRequest.setCfgInputId("1999365641511940097");
                    dmpInputCreateRequest.setDetailExtendJson(dataJson);
                    dmpInputCreateRequest.setNextExecTime(LocalDateTimeUtil.offset(LocalDateTime.now(), 1, ChronoUnit.MINUTES));
                    // 拉取时间
                    dmpInputCreateRequest.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
                    // 创建任务并执行
                    List<DmpInputFinishResponse> response = dmpInputCreateFactory.doHotfixInputTask(dmpInputCreateRequest);
                    dmpThirdWarehouseInfoEntityList = dmpThirdWarehouseInfoService
                            .getByPlatformAndAuthIdAndCode(PlatformDictEnum.TIK_TOK.getCode(), nextLevelId, warehouseId);
                    if(CollectionUtil.isNotEmpty(dmpThirdWarehouseInfoEntityList)){
                        warehouseName = dmpThirdWarehouseInfoEntityList.get(0).getWarehouseName();
                    }
                }else{
                    warehouseName = dmpThirdWarehouseInfoEntityList.get(0).getWarehouseName();
                }
            }

            Map<String, Object> data = new HashMap<>();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object platformDiscountObj = dmpDataMap.get("platformDiscount");
                if (platformDiscountObj != null) {
                    BigDecimal platformDiscount = MathUtil.valueOf(platformDiscountObj);
                    dmpDataMap.put("discountAmount", platformDiscount);

                    Object sellerDiscountObj = dmpDataMap.get("sellerDiscount");
                    if (sellerDiscountObj != null) {
                        dmpDataMap.put("discountAmount", platformDiscount.add(MathUtil.valueOf(sellerDiscountObj)));
                    }
                }
                dmpDataMap.put("warehouseName",warehouseName);
                dmpDataMap.put("warehouseId", warehouseId);
                Object itemTaxObj = dmpDataMap.get("itemTax");
                if (itemTaxObj != null) {
                    List<Map<String, Object>> itemTaxMap = (List<Map<String, Object>>) itemTaxObj;
                    data.put("itemTax", itemTaxMap);
					dmpDataMap.put("extendData", JSON.toJSONString(data));
					if (CollUtil.isNotEmpty(itemTaxMap)) {
					    if (ObjectUtil.isNotEmpty(itemTaxMap.get(0).get("taxType")) && "SALES_TAX".equals(itemTaxMap.get(0).get("taxType").toString())) {
                            dmpDataMap.put("taxRate", itemTaxMap.get(0).get("taxRate"));
                        }
                    }
                }
            }
        }
    }
}
