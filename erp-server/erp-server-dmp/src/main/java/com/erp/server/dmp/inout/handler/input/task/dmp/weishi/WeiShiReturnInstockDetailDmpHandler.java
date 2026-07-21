package com.erp.server.dmp.inout.handler.input.task.dmp.weishi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedList;
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
public class WeiShiReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler {

    private static final String DMP_KEY_DEFECTIVE_PRODUCT_FLAG = "defectiveProductFlag";
    /**
     * blGood 未在 dmp_cfg_input_convert_mapping 中配置，保留原始 key。
     * blGood=1 或 null 表示良品，0 表示不良品。
     */
    private static final String MONGO_KEY_BL_GOOD = "blGood";
    private static final int BL_GOOD_VALUE = 1;

    @Override
    protected List<Map<String, Object>> getDetailList(Map<String, Object> dmpInputMongoEntity) {
        Object detailListObj = dmpInputMongoEntity.get("skuList");
        if (null == detailListObj) {
            return Collections.emptyList();
        }
        JSONArray jsonArray = JSON.parseArray(JSON.toJSONString(detailListObj));
        if (CollectionUtils.isEmpty(jsonArray)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> resultList = new LinkedList<>();
        for (Object detailObj : jsonArray) {
            JSONObject jsonObject = (JSONObject) JSON.toJSON(detailObj);
            resultList.add(jsonObject);
        }
        return resultList;
    }

    /**
     * 基类完成字段映射后，将纬狮 {@code blGood} 标识转换为 {@code defectiveProductFlag}：
     * <ul>
     *   <li>blGood=1 / null → defectiveProductFlag=false（良品）</li>
     *   <li>blGood=0 → defectiveProductFlag=true（不良品）</li>
     * </ul>
     * <p>
     * 字段映射说明（来自 dmp_cfg_input_convert_mapping）：
     * SDK {@code handleQty} → dmpDataMap key {@code realQty} 和 {@code mustQty}；
     * SDK {@code blGood} 未配置映射，保留 key {@code blGood}。
     */
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
                Object blGoodObj = dmpDataMap.get(MONGO_KEY_BL_GOOD);
                boolean isGood = blGoodObj == null || BL_GOOD_VALUE == toIntValue(blGoodObj);
                dmpDataMap.put(DMP_KEY_DEFECTIVE_PRODUCT_FLAG, !isGood);
            }
        }
    }

    private int toIntValue(Object val) {
        if (val == null) {
            return 0;
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
