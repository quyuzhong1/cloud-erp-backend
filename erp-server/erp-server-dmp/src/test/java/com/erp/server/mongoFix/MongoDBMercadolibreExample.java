package com.erp.server.mongoFix;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.io.FileUtil;

public class MongoDBMercadolibreExample {
	public static void main(String[] args) {
        // MongoDB 连接字符串，包括用户名和密码
        String connectionString = "mongodb://?:ulanzi_developer@172.16.100.10:27017";

        try (MongoClient mongoClient = MongoClients.create(connectionString)) {
            // 连接到数据库
            MongoDatabase database = mongoClient.getDatabase("erp-dmp-prod");

            // 选择集合
            MongoCollection<Document> collection = database.getCollection("mercadolibre_orderDetail_data");

            Document query = new Document("fid", "576695452298154020"); // 替换为实际的查询条件
            // 查询所有文档
            FindIterable<Document> documents = collection.find();
            // 迭代结果集
            Set<String> set = new HashSet<>();
            try (MongoCursor<Document> cursor = documents.iterator()) {
                while (cursor.hasNext()) {
                    Document document = cursor.next();
                    String json = document.toJson();
					JSONObject parseObject = JSON.parseObject(json);
					Object paymentsObj = parseObject.get("payments");
                    if (paymentsObj != null) {
                        List<Map<String, Object>> feedbackList = (List<Map<String, Object>>) paymentsObj;
                        if (CollectionUtil.isNotEmpty(feedbackList)) {
                        	BigDecimal totalDiscount = feedbackList.stream().map(req -> valueOf(req.get("couponAmount"))).reduce(BigDecimal.ZERO, BigDecimal::add);
    						set.add("update dmp_so_info set total_discount = " + totalDiscount + " where third_code = '"+ parseObject.getLong("fid") +"' and source_system = 'mercadolibre' and source_platform = 'mercadolibre';");
                        }
                    }
					
                }
            }
            FileUtil.writeUtf8Lines(set, "C:\\Users\\Administrator\\Desktop\\discount\\mercadolibre.sql");
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
}