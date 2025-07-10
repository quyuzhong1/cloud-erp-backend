package com.erp.server.mongoFix;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.mongodb.client.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MongoDBAliExpressOrderDmpExample {
    public static void main(String[] args) {
        // MongoDB 连接字符串，包括用户名和密码
        String connectionString = "mongodb://?:mongoDBulanzi@172.16.100.60:32550";
//        String connectionString = "mongodb://?:ulanzi_developer@172.16.100.10:27017";

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            String dbName = "erp-dmp";
//            String dbName = "erp-dmp-prod";
            // 连接到数据库
            MongoDatabase database = mongoClient.getDatabase(dbName);

            MongoCollection<Document> detailCollection = database.getCollection("aliexpress_orderDetail_data");


            // **Step 1: 一次性查询所有子表数据，并按 fulfillment_order_no 进行分组**
            FindIterable<Document> detailDocuments = detailCollection.find(new Document("order_status", "FINISH"));


            // 迭代结果集
            Set<String> set = new HashSet<>();
            for (Document orderDetail : detailDocuments) {
                String orderId = orderDetail.getString("order_id");
                Object logisticInfoListObj = orderDetail.get("logistic_info_list");
                AliExpressOrder aliExpressOrder = new AliExpressOrder();
                aliExpressOrder.setOrderStatus(orderDetail.getString("order_status"));
                boolean isCancel = aliExpressOrder.convertNewCancel(logisticInfoListObj);
                if (isCancel) {
                    continue;
                }
                //订单明细
                List<OrderItemDetail> orderItemDetailList = com.alibaba.fastjson.JSON.parseObject(JSON.toJSONString(orderDetail.get("child_order_list")), new TypeReference<List<OrderItemDetail>>() {
                }.getType());
                Boolean isAliexpressPlatformWarehouseOrder = Boolean.FALSE;
                if (CollectionUtils.isNotEmpty(orderItemDetailList)) {
                    long count = orderItemDetailList.stream().
                            filter(o -> AliexpressConstants.CAINIAO_INTERNATIONAL_WAREHOUSE.equals(o.getLogisticsWarehouseType())).count();
                    isAliexpressPlatformWarehouseOrder = count > 0;
                }
                // 发货状态
                String delivery_status = aliExpressOrder.convertBillStatus(isAliexpressPlatformWarehouseOrder, logisticInfoListObj);
                // 审核状态状态
                // （ApproveStatus字典类型）
                String order_status = aliExpressOrder.convertApproveStatus(isAliexpressPlatformWarehouseOrder, logisticInfoListObj);

                boolean invalidStatus = false;
                set.add("update dmp_so_info set update_time = now(), delivery_status = '" + delivery_status + "',order_status = '" + order_status + "',is_cancel = " + isCancel + ",invalid_status = " + invalidStatus + "  where third_code = '" + orderId + "' and source_system = 'AliExpress';");
            }


            String url = "C:\\Users\\Jim\\Desktop\\aliExpress_detail\\aliExpress_so_" + dbName + ".sql";
            FileUtil.writeUtf8Lines(set, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static BigDecimal valueOf(Object object) {
        if (object == null) {
            return BigDecimal.ZERO;
        } else if (StringUtils.isBlank(object.toString())) {
            return BigDecimal.ZERO;
        }
        if (object instanceof JSONObject) {
            object = ((JSONObject) object).get("$numberDecimal");
        }
        BigDecimal result;
        try {
            result = new BigDecimal(String.valueOf(object).replaceAll(",", ""));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("数据类型有误，请设置正确的数值：" + object);
        }
        return result;
    }


    // **金额解析**
    private static String parseAmount(String value) {
        if (value == null || !value.contains("(")) return "0";
        return value.split("\\(")[0];
    }

    // **币别解析**
    private static String parseCurrency(String value) {
        if (value == null || !value.contains("(")) return "";
        return value.split("\\(")[1].replace(")", "");
    }
}