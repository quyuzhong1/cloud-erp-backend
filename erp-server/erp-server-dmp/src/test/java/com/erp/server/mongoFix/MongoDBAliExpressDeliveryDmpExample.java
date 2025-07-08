package com.erp.server.mongoFix;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.mongoFix.docment.AliexpressSoOutstock;
import com.mongodb.client.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.math.BigDecimal;
import java.util.*;

public class MongoDBAliExpressDeliveryDmpExample {
	public static void main(String[] args) {
        // MongoDB 连接字符串，包括用户名和密码
//        String connectionString = "mongodb://?:mongoDBulanzi@172.16.100.12:27017";
        String connectionString = "mongodb://?:ulanzi_developer@172.16.100.10:27017";

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            String dbName = "erp-dmp-prod";
            // 连接到数据库
            MongoDatabase database = mongoClient.getDatabase(dbName);

            MongoCollection<Document> outstockCollection = database.getCollection("aliexpress_soOutstock_data");
            MongoCollection<Document> detailCollection = database.getCollection("aliexpress_soOutstockDetail_data");

//            String fulfillmentOrderNo1 = "WH3314510640832722";
//
//            // 查询主表
//            FindIterable<Document> outstockDocuments = outstockCollection.find(new Document("fulfillment_order_no", fulfillmentOrderNo1));

            // **查询主表所有数据**
            FindIterable<Document> outstockDocuments = outstockCollection.find();

            // **Step 1: 一次性查询所有子表数据，并按 fulfillment_order_no 进行分组**
            Map<String, List<Document>> detailMap = new HashMap<>();
            FindIterable<Document> detailDocuments = detailCollection.find();

            for (Document detail : detailDocuments) {
                String fulfillmentOrderNo = detail.getString("fulfillment_order_no");
                detailMap.computeIfAbsent(fulfillmentOrderNo, k -> new ArrayList<>()).add(detail);
            }

            List<AliexpressSoOutstock.MergedOrderData> resultList = new ArrayList<>();

            for (Document outstock : outstockDocuments) {
                String fulfillmentOrderNo = outstock.getString("fulfillment_order_no");
                String tradeOrderNo = outstock.getString("trade_order_no");

                List<Document> details = detailMap.getOrDefault(fulfillmentOrderNo, Collections.emptyList());
                for (Document detail : details) {
                    AliexpressSoOutstock.MergedOrderData merged = new AliexpressSoOutstock.MergedOrderData();
                    merged.setFulfillmentOrderNo(fulfillmentOrderNo);
                    merged.setTradeOrderNo(tradeOrderNo);
                    merged.setScItemId(detail.getString("sc_item_id"));
                    merged.setItemId(detail.getString("item_id"));
                    merged.setSkuId(detail.getString("sku_id"));


                    merged.setSkuActualPaidAmount(parseAmount(detail.getString("sku_actual_paid_amount")));
                    merged.setSkuActualPaidCurrency(parseCurrency(detail.getString("sku_actual_paid_amount")));

                    merged.setSkuDiscountAmount(parseAmount(detail.getString("sku_discount_amount")));
                    merged.setSkuDiscountCurrency(parseCurrency(detail.getString("sku_discount_amount")));

                    merged.setUnitPrice(parseAmount(detail.getString("unit_price")));
                    merged.setUnitPriceCurrency(parseCurrency(detail.getString("unit_price")));

                    resultList.add(merged);
                }
            }
            // 迭代结果集
            Set<String> set = new HashSet<>();
            for (AliexpressSoOutstock.MergedOrderData mergedOrderData : resultList) {
                String currency = mergedOrderData.getUnitPriceCurrency();
                String pay_amount = mergedOrderData.getSkuActualPaidAmount();
                String pay_currency = mergedOrderData.getSkuActualPaidCurrency();
                String discount_amount= mergedOrderData.getSkuDiscountAmount();
                String discount_currency = mergedOrderData.getSkuDiscountCurrency();
                String skuId = mergedOrderData.getSkuId();
                String itemId = mergedOrderData.getItemId();
                String scItemId = mergedOrderData.getScItemId();
                String tradeOrderNo = mergedOrderData.getTradeOrderNo();
                set.add("update dmp_so_outstock_detail set update_time = now(), currency = '" + currency + "',pay_amount = "+  pay_amount + ",pay_currency = '"+ pay_currency + "',discount_amount = "+  discount_amount +",discount_currency = '" + discount_currency + "'  where sku_id = '"+ skuId + "' and third_detail_id = '"+ itemId + "' and platform_detail_id = '"+ scItemId + "' and main_id in (select id FROM dmp_so_outstock WHERE source_id in (select id from dmp_so_info where source_system = 'AliExpress' and third_code ='"+ tradeOrderNo +"' and is_deleted = false));");
            }


            String url = "C:\\Users\\Jim\\Desktop\\aliExpress_detail\\aliExpress_detail_"+ dbName +".sql";
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
        if(object instanceof JSONObject) {
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