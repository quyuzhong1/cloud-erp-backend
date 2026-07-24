package com.erp.server.dmp.inout.handler.input.task.dmp.aiya;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * 爱亚退货入库明细 DMP handler，对齐 {@code WegoReturnInstockDetailDmpHandler}。
 * <p>
 * 明细来自 InitHandler 归一化后的 {@code asnLineItems}；
 * 未配置映射的 {@code skuStatus}/{@code thirdDetailId} 会按原 key 保留在 dmpDataMap 中，
 * 本类据此写入 {@code defectiveProductFlag}（DAMAGE=不良品）。
 */
@Slf4j
@Service
@Scope("prototype")
public class AiyaReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler {

    private static final String MONGO_KEY_ASN_LINE_ITEMS = "asnLineItems";

    /** 未映射时原样保留的明细字段 */
    private static final String KEY_SKU_STATUS = "skuStatus";

    private static final String DMP_KEY_DEFECTIVE_PRODUCT_FLAG = "defectiveProductFlag";

    private static final String SKU_STATUS_DAMAGE = "DAMAGE";

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object listObj = dmpInputMongoEntity.get(MONGO_KEY_ASN_LINE_ITEMS);
        if (listObj == null) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(listObj));
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        for (Object item : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(item);
            resultList.add(jsonObject);
        }
        return resultList;
    }

    @Override
    protected void afterConvertData(
            Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> entry
                : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = entry.getValue();
            if (dmpDataMaps == null || dmpDataMaps.isEmpty()) {
                continue;
            }
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                Object skuStatusObj = dmpDataMap.get(KEY_SKU_STATUS);
                String skuStatus = skuStatusObj == null
                        ? "" : skuStatusObj.toString().trim().toUpperCase(Locale.ROOT);
                dmpDataMap.put(DMP_KEY_DEFECTIVE_PRODUCT_FLAG, SKU_STATUS_DAMAGE.equals(skuStatus));
            }
        }
    }
}
