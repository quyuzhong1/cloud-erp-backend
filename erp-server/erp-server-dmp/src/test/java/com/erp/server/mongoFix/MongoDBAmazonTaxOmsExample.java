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

public class MongoDBAmazonTaxOmsExample {
	public static void main(String[] args) {
        // MongoDB 连接字符串，包括用户名和密码
//        String connectionString = "mongodb://root:mongoDBulanzi@172.16.100.12:27017";
        String connectionString = "mongodb://?:ulanzi_developer@172.16.100.10:27017";

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            String dbName = "erp-dmp-prod";
//            String dbName = "erp-dmp";
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
                BigDecimal itemTax = BigDecimal.ZERO;
                BigDecimal shippingTax = BigDecimal.ZERO;
                BigDecimal giftWrapTax = BigDecimal.ZERO;
                BigDecimal promotionDiscountTax = BigDecimal.ZERO;
                for (Document detail : details) {
                    Object itemTaxObj = detail.get("itemTax");
                    if (null != itemTaxObj){
                        JSONObject parse = (JSONObject) JSON.parse(JSONUtil.toJsonStr(itemTaxObj));
                        String amount = parse.getString("amount");
                        itemTax = itemTax.add(new BigDecimal(amount));
                    }

                    Object shippingTaxObj = detail.get("shippingTax");
                    if (null != shippingTaxObj){
                        JSONObject parse2 = (JSONObject) JSON.parse(JSONUtil.toJsonStr(shippingTaxObj));
                        String discount = parse2.getString("amount");
                        shippingTax = shippingTax.add(new BigDecimal(discount));
                    }

                    Object buyerInfoObj = detail.get("buyerInfo");
                    if (null != buyerInfoObj){
                        JSONObject parse2 = (JSONObject) JSON.parse(JSONUtil.toJsonStr(buyerInfoObj));
                        Object giftWrapTaxObj = parse2.get("giftWrapTax");
                        if (null != giftWrapTaxObj){
                            JSONObject parse3 = (JSONObject) JSON.parse(JSONUtil.toJsonStr(giftWrapTaxObj));
                            String amount = parse3.getString("amount");
                            giftWrapTax = giftWrapTax.add(new BigDecimal(amount));
                        }
                    }

                    Object promotionDiscountTaxObj = detail.get("promotionDiscountTax");
                    if (null != promotionDiscountTaxObj){
                        JSONObject parse2 = (JSONObject) JSON.parse(JSONUtil.toJsonStr(promotionDiscountTaxObj));
                        String discount = parse2.getString("amount");
                        promotionDiscountTax = promotionDiscountTax.add(new BigDecimal(discount));
                    }

                }
                entity.setItemTax(itemTax);
                entity.setShippingTax(shippingTax);
                entity.setGiftWrapTax(giftWrapTax);
                entity.setPromotionDiscountTax(promotionDiscountTax);
                resultList.add(entity);
            }
            // 迭代结果集
            Set<String> set = new HashSet<>();
            for (AmazonOrderEntity entity : resultList) {
                String amazonOrderId = entity.getAmazonOrderId();
                String shopId = entity.getShopId();
                if (entity.getPromotionDiscountTax().compareTo(BigDecimal.ZERO) == 0) {
                    continue;
                }
                BigDecimal total_tax_fee = entity.getItemTax().add(entity.getShippingTax()).add(entity.getGiftWrapTax()).subtract(entity.getPromotionDiscountTax());
                if (total_tax_fee.compareTo(BigDecimal.ZERO) <= 0 ){
                    continue;
                }

                set.add("update so_b2c set update_time = now(), total_tax_fee = '" + total_tax_fee +  "' where platform_code = '"+ amazonOrderId + "' and shop_id = '"+ shopId + "'  and dict_platform = 'Amazon' and source_type = 'soB2c' and is_deleted = 'f';");
            }


            String url = "C:\\Users\\Jim\\Desktop\\amazon_tax_oms\\amazon_tax_"+ dbName +".sql";
            FileUtil.writeUtf8Lines(set, url);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}