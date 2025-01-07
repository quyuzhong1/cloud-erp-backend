package com.common.core.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.Map;

/**
 * JsonPath工具类
 */
public class JsonPathUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 从Map中获取指定路径的值
     * @param variables 变量
     * @param path 路径
     * @return String 值
     * @throws IOException
     */
    public static String getValueFromPath(Map<String, Object> variables, String path){
        // Remove the ${} from the path
        if (path.startsWith("${") && path.endsWith("}")) {
            path = path.substring(2, path.length() - 1);
        }
        JsonNode rootNode = OBJECT_MAPPER.valueToTree(variables);
        return extractValue(rootNode, path);
    }

    private static String extractValue(JsonNode node, String path) {
        String[] keys = path.split("\\.");
        for (String key : keys) {
            if (null == node) {
                return null;
            }

            if (key.contains("[")) {
                String fieldName = StringUtils.substringBefore(key, "[");
                int index = Integer.parseInt(StringUtils.substringBetween(key, "[", "]"));
                node = node.path(fieldName);
                if (node.isArray()) {
                    node = node.get(index);
                }
            } else {
                node = node.path(key);
            }
        }
        return node.isMissingNode() ? null : node.asText();
    }

//    public static void main(String[] args) throws IOException {
//        Map<String, Object> innerMap = new HashMap<>();
//        innerMap.put("skuId", "161918430401943809");
//        Map<String, Object> innerMap2 = new HashMap<>();
//        innerMap2.put("spuId", "spu");
//        Map<String, Object> variables = new HashMap<>();
//        variables.put("detailList", new Object[]{innerMap});
//        variables.put("spu", innerMap2);
//        String value = JsonPathUtil.getValueFromPath(variables, "detailList[0].skuId");
//        System.out.println(  value);  // 输出：161918430401943809
//        String value2 = JsonPathUtil.getValueFromPath(variables, "spu.spuId");
//        System.out.println(value2);  // 输出：spu
//    }

}
