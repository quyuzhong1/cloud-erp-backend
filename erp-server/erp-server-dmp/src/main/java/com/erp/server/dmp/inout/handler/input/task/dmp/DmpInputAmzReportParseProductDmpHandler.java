package com.erp.server.dmp.inout.handler.input.task.dmp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dmp处理金蝶明细子类任务handler，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputAmzReportParseProductDmpHandler extends DmpInputDbConvertDmpHandler {

    public static final String SPU_ID = "spuId";

    @Override
    protected Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertData(List<Map<String, Object>> dmpInputMongoEntityList) {
        Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> convertDataMap = super.convertData(dmpInputMongoEntityList);

        Set<String> existProductIdList = new HashSet<>();
        for (List<TreeMap<String, Object>> convertData : convertDataMap.values()) {
            // 保留第一次出现的 productId，移除后续重复
            convertData.removeIf(map -> !existProductIdList.add(map.getOrDefault(SPU_ID, "").toString()));

            // 检查移除相同的productId元素
//            boolean addResult = true;
//            for (TreeMap<String, Object> map : convertData) {
//                // 保留第一次出现的 productId，移除后续重复
//                String productId = map.getOrDefault(PRODUCT_ID, "").toString();
//                addResult = existProductIdList.add(productId);
//            }
//            boolean finalAddResult = !addResult;
//            convertData.removeIf(map-> finalAddResult);
        }

        return convertDataMap;
    }


    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        log.debug("DmpInputAmzReportParseProductDmpHandler afterConvertData 处理");
    }


    public static void main(String[] args) {
        // 示例数据初始化
        Map<List<TreeMap<String, Object>>, List<TreeMap<String, Object>>> convertDataMap = new HashMap<>();

        List<TreeMap<String, Object>> dataList1 = new ArrayList<>();
        TreeMap<String, Object> map1 = new TreeMap<>();
        map1.put("key", "value1");
        map1.put("other", "value1");
        dataList1.add(map1);
        convertDataMap.put(dataList1, dataList1);


        List<TreeMap<String, Object>> dataList2 = new ArrayList<>();
        TreeMap<String, Object> map2 = new TreeMap<>();
        map2.put("key", "value2");
        map2.put("other", "value1");
        dataList2.add(map2);
        convertDataMap.put(dataList2, dataList2);

        List<TreeMap<String, Object>> dataList3 = new ArrayList<>();
        TreeMap<String, Object> map3 = new TreeMap<>();
        map3.put("key", "value1");
        map3.put("other", "value1");
        dataList3.add(map3);
        convertDataMap.put(dataList3, dataList3);

        List<TreeMap<String, Object>> dataList4 = new ArrayList<>();
        TreeMap<String, Object> map4 = new TreeMap<>();
        map4.put("key", "value3");
        map4.put("other", "value1");
        dataList4.add(map4);
        convertDataMap.put(dataList4, dataList4);


        Set<String> existProductId = new HashSet<>();
        // 遍历 convertDataMap 里的每个 List
        for (List<TreeMap<String, Object>> convertData : convertDataMap.values()) {
            // 检查移除相同的productId元素
            // 保留第一次出现的 productId，移除后续重复
            convertData.removeIf(map -> !existProductId.add(map.getOrDefault("key", "").toString()));
        }


        // 输出结果
        convertDataMap.forEach((key, value) -> value.forEach(System.out::println));
    }


}
