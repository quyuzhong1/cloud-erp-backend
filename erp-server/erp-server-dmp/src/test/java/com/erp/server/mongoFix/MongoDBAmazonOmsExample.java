package com.erp.server.mongoFix;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.erp.server.mongoFix.docment.AmazonOrderEntity;
import com.mongodb.client.*;
import org.bson.Document;

import java.math.BigDecimal;
import java.util.*;

public class MongoDBAmazonOmsExample {
	public static void main(String[] args) {
        // MongoDB 连接字符串，包括用户名和密码
        String connectionString = "mongodb://?:mongoDBulanzi@172.16.100.12:27017";
//        String connectionString = "mongodb://?:ulanzi_developer@172.16.100.10:27017";

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
//            String dbName = "erp-dmp-prod";
            String dbName = "erp-dmp";
            // 连接到数据库
            MongoDatabase database = mongoClient.getDatabase(dbName);

            MongoCollection<Document> orderCollection = database.getCollection("amazon_order_data");
            MongoCollection<Document> detailCollection = database.getCollection("amazon_order_items_data");

//            String amazonOrderId = "WH3314510640832722";
//            // 查询主表
//            FindIterable<Document> orderCollection = orderCollection.find(new Document("amazonOrderId", fulfillmentOrderNo1));

            // **查询主表所有数据**
            FindIterable<Document> orderDocuments = orderCollection.find();

            // **Step 1: 一次性查询所有子表数据，并按 amazonOrderId，platformShopCode 进行分组**
            Map<String, List<Document>> detailMap = new HashMap<>();
            FindIterable<Document> detailDocuments = detailCollection.find();

            for (Document detail : detailDocuments) {
                String amazonOrderId = detail.getString("amazonOrderId");
                String platformShopCode = detail.getString("platformShopCode");
                String key = CharSequenceUtil.format("{}_{}", amazonOrderId, platformShopCode);
                detailMap.computeIfAbsent(key, k -> new ArrayList<>()).add(detail);
            }

            List<AmazonOrderEntity> resultList = new ArrayList<>();

            for (Document orderDoc : orderDocuments) {
                String amazonOrderId = orderDoc.getString("amazonOrderId");
                String platformShopCode = orderDoc.getString("platformShopCode");
                String key = CharSequenceUtil.format("{}_{}", amazonOrderId, platformShopCode);
                String shopId = orderDoc.getString("shopId");
                AmazonOrderEntity entity = new AmazonOrderEntity();
                entity.setShopId(shopId);
                entity.setAmazonOrderId(amazonOrderId);
                entity.setPlatformShopCode(platformShopCode);

                List<Document> details = detailMap.getOrDefault(key, Collections.emptyList());
                BigDecimal allAmount = BigDecimal.ZERO;
                BigDecimal allDiscount = BigDecimal.ZERO;
                for (Document detail : details) {
                    Object itemPriceObj = detail.get("itemPrice");
                    if (null != itemPriceObj){
                        JSONObject parse = (JSONObject) JSON.parse(JSONUtil.toJsonStr(itemPriceObj));
                        String amount = parse.getString("amount");
                        allAmount = allAmount.add(new BigDecimal(amount));
                    }

                    Object promotionDiscountObj = detail.get("promotionDiscount");
                    if (null != promotionDiscountObj){
                        JSONObject parse2 = (JSONObject) JSON.parse(JSONUtil.toJsonStr(promotionDiscountObj));
                        String discount = parse2.getString("amount");
                        allDiscount = allDiscount.add(new BigDecimal(discount));
                    }

                }
                entity.setAll_amount(allAmount.toPlainString());
                entity.setTotal_discount(allDiscount.toPlainString());
                resultList.add(entity);
            }
            // 迭代结果集
            Set<String> set = new HashSet<>();
            for (AmazonOrderEntity entity : resultList) {
                String amazonOrderId = entity.getAmazonOrderId();
                String shopId = entity.getShopId();
                String total_discount = entity.getTotal_discount();

                set.add("update so_b2c set update_time = now(),total_discount = "+  total_discount + " where platform_code = '"+ amazonOrderId + "' and shop_id = '"+ shopId + "'  and dict_platform = 'Amazon' and is_deleted = 'f';");
            }


            String url = "C:\\Users\\Jim\\Desktop\\amazon_order\\amazon_order_oms_"+ dbName +".sql";
            FileUtil.writeUtf8Lines(set, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}