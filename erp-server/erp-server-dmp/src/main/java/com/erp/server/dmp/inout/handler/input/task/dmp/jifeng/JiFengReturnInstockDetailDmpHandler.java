package com.erp.server.dmp.inout.handler.input.task.dmp.jifeng;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.dmp.inout.handler.input.task.dmp.DmpInputDoNextDmpHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
@Slf4j
@Service
@Scope("prototype")
public class JiFengReturnInstockDetailDmpHandler extends DmpInputDoNextDmpHandler {

    private static final String DMP_KEY_REAL_QTY = "realQty";
    private static final String DMP_KEY_MUST_QTY = "mustQty";
    private static final String DMP_KEY_DEFECTIVE_PRODUCT_FLAG = "defectiveProductFlag";
    /** badCount 未在 dmp_cfg_input_convert_mapping 中配置，保留原始 key */
    private static final String MONGO_KEY_BAD_COUNT = "badCount";

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
     * 基类完成字段映射后，对每条明细做不良品状态转换：
     * <ul>
     *   <li>goodCount=0 &amp;&amp; badCount&gt;0 → defectiveProductFlag=true，realQty 覆盖为 badCount</li>
     *   <li>goodCount&gt;0 &amp;&amp; badCount=0 → defectiveProductFlag=false</li>
     *   <li>goodCount&gt;0 &amp;&amp; badCount&gt;0（混合）→ 拆分为两条：良品一条（flag=false）、不良品一条（flag=true）</li>
     * </ul>
     * <p>
     * 字段映射说明（来自 dmp_cfg_input_convert_mapping）：
     * SDK {@code goodCount} → dmpDataMap key {@code realQty}；
     * SDK {@code badCount} 未配置映射，保留 key {@code badCount}。
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
            List<TreeMap<String, Object>> splitBadMaps = new ArrayList<>();
            for (TreeMap<String, Object> dmpDataMap : dmpDataMaps) {
                // goodCount 在映射配置中映射为 realQty；badCount 无配置，保留原 key
                int goodCount = toIntValue(dmpDataMap.get(DMP_KEY_REAL_QTY));
                int badCount = toIntValue(dmpDataMap.get(MONGO_KEY_BAD_COUNT));

                if (goodCount > 0 && badCount > 0) {
                    // 混合场景：原记录保留为良品，复制出不良品记录
                    log.info("极风退货明细混合场景拆分 productSku={}, goodCount={}, badCount={}",
                            dmpDataMap.get("productSku"), goodCount, badCount);
                    dmpDataMap.put(DMP_KEY_MUST_QTY, goodCount);
                    dmpDataMap.put(DMP_KEY_DEFECTIVE_PRODUCT_FLAG, false);

                    TreeMap<String, Object> badDataMap = new TreeMap<>(dmpDataMap);
                    badDataMap.put(DMP_KEY_REAL_QTY, badCount);
                    badDataMap.put(DMP_KEY_MUST_QTY, badCount);
                    badDataMap.put(DMP_KEY_DEFECTIVE_PRODUCT_FLAG, true);
                    splitBadMaps.add(badDataMap);
                } else if (goodCount == 0 && badCount > 0) {
                    // 纯不良品：realQty 当前为 0，覆盖为 badCount 才能通过 RocketMQ 侧 realQty>0 的过滤
                    dmpDataMap.put(DMP_KEY_REAL_QTY, badCount);
                    dmpDataMap.put(DMP_KEY_MUST_QTY, badCount);
                    dmpDataMap.put(DMP_KEY_DEFECTIVE_PRODUCT_FLAG, true);
                } else {
                    dmpDataMap.put(DMP_KEY_DEFECTIVE_PRODUCT_FLAG, false);
                }
            }
            dmpDataMaps.addAll(splitBadMaps);
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
