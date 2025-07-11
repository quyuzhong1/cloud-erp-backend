package com.erp.server.mongoFix;

import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.mongodb.client.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;

public class MongoDBWdtSoInfo {
    public static void main(String[] args) {
        String mongoConn = "mongodb://ulanzi_developer:ulanzi_developer@172.16.100.10:27017";
        String dbName = "erp-dmp-prod";
        String pgUrl = "jdbc:postgresql://172.16.100.17:5432/uat-erp-dmp?autoReconnect=true&useSSL=false&serverTimezone=GMT%2B8&stringtype=unspecified";
        String username = "postgres";
        String password = "";
        int pageSize = 300000;

        try (MongoClient mongoClient = MongoClients.create(mongoConn);
             Connection connection = DriverManager.getConnection(pgUrl, username, password);
             Statement statement = connection.createStatement()) {

            MongoDatabase database = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection("wdt_order_qimen_data");

            // 1. 查询总数
            String countSql = "select count(*) from dmp_so_info where source_system = 'wdt' and is_deleted = 'f'";
            ResultSet countRs = statement.executeQuery(countSql);
            int total = 0;
            if (countRs.next()) {
                total = countRs.getInt(1);
            }
            countRs.close();
            int pageCount = (total + pageSize - 1) / pageSize;

            Set<String> sqlSet = new LinkedHashSet<>();
            for (int page = 0; page < pageCount; page++) {
                String sql = "select id, third_code from dmp_so_info where source_system = 'wdt' and is_deleted = 'f' order by id limit " + pageSize + " offset " + (page * pageSize);
                ResultSet rs = statement.executeQuery(sql);

                // 收集本页 third_code 和 id
                Map<String, String> idMap = new HashMap<>();
                List<String> thirdCodeList = new ArrayList<>();
                while (rs.next()) {
                    String id = rs.getString("id");
                    String thirdCode = rs.getString("third_code");
                    if (StringUtils.isNotBlank(thirdCode)) {
                        thirdCodeList.add(thirdCode);
                        idMap.put(thirdCode, id);
                    }
                }
                rs.close();

                if (CollUtil.isEmpty(thirdCodeList)) continue;

                // 2. 批量查 MongoDB
                Document query = new Document("trade_no", new Document("$in", thirdCodeList)).append("trade_type", new Document("$ne", 1));
                FindIterable<Document> docs = collection.find(query);

                Map<String, Document> mongoMap = new HashMap<>();
                for (Document doc : docs) {
                    String tradeNo = doc.getString("trade_no");
                    mongoMap.put(tradeNo, doc);
                }

                // 再按 idMap 顺序遍历
                for (Map.Entry<String, String> entry : idMap.entrySet()) {
                    String thirdCode = entry.getKey();
                    String id = entry.getValue();
                    Document doc = mongoMap.get(thirdCode);
                    if (doc == null) {
                        continue; // 如果 MongoDB 中没有对应的记录，跳过
                    }
                    String tradeType = doc.get("trade_type").toString();
                    if ("1".equals(tradeType)) {
                        continue;
                    }
                    sqlSet.add("update dmp_so_info set update_time = now(), order_type = '" + tradeType + "'  where id = '" + id + "';");
                }
                String fileUrl = "C:\\Users\\Jim\\Desktop\\wdt_detail\\wdt_so_" + dbName + ".sql";
                FileUtil.writeUtf8Lines(sqlSet, fileUrl);
                System.out.println("处理完成，SQL已写入：" + fileUrl);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}