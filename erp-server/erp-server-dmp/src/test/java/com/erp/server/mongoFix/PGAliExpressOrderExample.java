package com.erp.server.mongoFix;

import cn.hutool.core.io.FileUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.enums.SourceTypeEnum;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.oms.aliexpress.constants.AliexpressConstants;
import com.erp.oms.aliexpress.dto.response.AliExpressOrder;
import com.erp.oms.aliexpress.dto.response.OrderItemDetail;
import com.mongodb.client.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class PGAliExpressOrderExample {
    public static void main(String[] args) {
        // 写个脚本，注意不是mongo，查询pgsql的表so_b2c，条件是SELECT platform_code FROM so_b2c WHERE dict_platform = 'AliExpress' and source_type = 'selfAdd' and platform_code in （？）
        String pgUrl = "jdbc:postgresql://172.16.100.10:5432/prod-erp-oms?autoReconnect=true&useSSL=false&serverTimezone=GMT%2B8&stringtype=unspecified";
        String username = "ulanzi_developer";
        String password = "";
        String sql = "SELECT * FROM so_b2c WHERE dict_platform = 'AliExpress' and platform_code in (\n" +
//                "'1106042587457337',\n" +
//                "'1106263395014408',\n" +
//                "'1106290993419208',\n" +
//                "'1112784047655509',\n" +
//                "'3038245720700905',\n" +
//                "'3039675792448747',\n" +
//                "'3040946653368044',\n" +
//                "'3041063012782807',\n" +
//                "'3041110845407124',\n" +
//                "'3041571703613543',\n" +
//                "'3041581980629885',\n" +
//                "'3041645568167401',\n" +
//                "'3042593215459990',\n" +
//                "'3042761638980831',\n" +
//                "'3042854218054817',\n" +
//                "'3042980153959492',\n" +
//                "'3043191257354696',\n" +
//                "'3043366271252235',\n" +
//                "'3043378091686050',\n" +
//                "'3044407214673997',\n" +
//                "'3044627160847969',\n" +
//                "'3044740551974112',\n" +
//                "'3044759266158449',\n" +
//                "'3044845756303741',\n" +
//                "'3044973307395658',\n" +
//                "'3045617385603729',\n" +
//                "'3045649416512136',\n" +
//                "'3045716328622540',\n" +
//                "'3045722610830078',\n" +
//                "'3045852714172193',\n" +
//                "'3046121727503306',\n" +
//                "'3046215786872306',\n" +
//                "'3046734591386469',\n" +
//                "'3047573762891786',\n" +
//                "'3048962170139249',\n" +
//                "'3048980387394661',\n" +
//                "'3049159926144244',\n" +
//                "'3049645196608202',\n" +
//                "'3049650524919376',\n" +
//                "'3049715635672143',\n" +
//                "'3049813484175864',\n" +
//                "'3049949932650702',\n" +
//                "'3050034261005864',\n" +
//                "'3050182482855380',\n" +
//                "'3050494453285619',\n" +
//                "'3050668559510933',\n" +
//                "'3050866237622340',\n" +
//                "'3051346503061528',\n" +
//                "'3051360418982044',\n" +
//                "'3051444830257451',\n" +
//                "'3051550387719754',\n" +
//                "'3051696542514400',\n" +
//                "'3051789271841051',\n" +
//                "'3051871576423573',\n" +
//                "'3052022176129880',\n" +
//                "'3052142422424449',\n" +
//                "'3052279610544946',\n" +
//                "'3052304550055872',\n" +
//                "'3052355293727381',\n" +
//                "'3052397807614946',\n" +
//                "'3052485687863257',\n" +
//                "'3052552907129543',\n" +
//                "'3052575454685347',\n" +
//                "'3052586336581085',\n" +
//                "'3052645655500851',\n" +
//                "'3052679271692112',\n" +
//                "'3052688411763257',\n" +
//                "'3052712998548227',\n" +
//                "'3052715382539390',\n" +
//                "'3052755307224171',\n" +
//                "'3052763722869390',\n" +
//                "'3052832978145112',\n" +
//                "'3052859084043257',\n" +
//                "'3052864454153257',\n" +
//                "'3052958450856288',\n" +
//                "'3052995975284946',\n" +
//                "'3053028564939341',\n" +
//                "'3053075928038009',\n" +
//                "'3053098226183257',\n" +
//                "'3053199789547421',\n" +
//                "'3053333762227315',\n" +
//                "'3053576059522050',\n" +
//                "'3053666804595907',\n" +
//                "'3053695297194529',\n" +
//                "'3053696489757792',\n" +
//                "'3053774534749749',\n" +
//                "'3053774576448401',\n" +
//                "'3053887366340667',\n" +
//                "'3053936113959111',\n" +
//                "'3054056732860053',\n" +
//                "'3054068222004521',\n" +
//                "'3054186621303611',\n" +
//                "'3054267570300870',\n" +
//                "'3054481889403804',\n" +
//                "'3054599942841434',\n" +
//                "'3054703871670127',\n" +
//                "'3054730270053804',\n" +
//                "'3054734283657683',\n" +
//                "'3054747217504859',\n" +
//                "'3054822052250688',\n" +
//                "'3054860633602733',\n" +
//                "'3054878395718903',\n" +
//                "'3054994370182337',\n" +
//                "'3055028153036138',\n" +
//                "'3055118062529432',\n" +
                "'3042593215459990');";
        Set<Map<String, Object>> soB2cSet = new HashSet<>();

        Set<String> updateSql = new HashSet<>();
        try (Connection connection = DriverManager.getConnection(pgUrl, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {
            // 调整为使用 Set 来存储 so_b2c的结果集，而不是platform_code
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), resultSet.getObject(i));
                }
                soB2cSet.add(row);
            }
            if (CollectionUtils.isEmpty(soB2cSet)){
                System.out.println("结果为空");
                return;
            }
            List<SoB2cEntity> list = JSONUtil.toList(JSONUtil.toJsonStr(soB2cSet), SoB2cEntity.class);
            Map<String, List<SoB2cEntity>> groupMap = list.stream().collect(Collectors.groupingBy(SoB2cEntity::getPlatformCode));
            for (Map.Entry<String, List<SoB2cEntity>> entry : groupMap.entrySet()) {
                SoB2cEntity soB2cEntity = entry.getValue().stream().filter(e -> e.getSourceType().equalsIgnoreCase(SourceTypeEnum.SO_B2C.getCode())).findFirst().orElse(null);

                List<SoB2cEntity> selfAddEntityList = entry.getValue().stream()
                        .filter(e -> e.getSourceType().equalsIgnoreCase(SourceTypeEnum.SELF_ADD.getCode())
                                && !e.getIsDeleted()
//                                && !e.getInvalidStatus()
                                && e.getAmount().compareTo(BigDecimal.ZERO) > 0
                        )
                        .sorted(Comparator.comparing(SoB2cEntity::getCreateTime))
                        .collect(Collectors.toList());
                if (CollectionUtils.isEmpty(selfAddEntityList)){
                    System.out.println("无手工单信息" + entry.getKey());
                    continue;
                }
                if (null == soB2cEntity){
                    System.out.println("无so_b2c信息，平台编码：" + entry.getKey());
                    continue;
                }
                if (selfAddEntityList.size() == 1){
                    SoB2cEntity selfSoB2cEntity = selfAddEntityList.get(0);
                    if (selfSoB2cEntity.getAmount().compareTo(soB2cEntity.getAmount()) == 0){
                        updateSql.add("update so_b2c set update_time = now(), after_tax_amount = '" + soB2cEntity.getAfterTaxAmount()  + "‘  where code = '" + selfSoB2cEntity.getCode() + "';");
                    } else {
                        System.out.println("数据异常，拆单和原金额不一致，平台编码：" + entry.getKey());
                        continue;
                    }
                }
                BigDecimal allAmount = selfAddEntityList.stream().map(SoB2cEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (allAmount.compareTo(soB2cEntity.getAmount()) != 0){
                    System.out.println("数据异常，拆单合计金额和原金额不一致，平台编码：" + entry.getKey());
                    continue;
                }

                BigDecimal afterTaxAmount = soB2cEntity.getAfterTaxAmount();
                for (int i = 0; i < selfAddEntityList.size(); i++) {
                    SoB2cEntity curB2cEntity = selfAddEntityList.get(i);
                    if (i  == selfAddEntityList.size() - 1){
                        // 最后一个
                        updateSql.add("update so_b2c set update_time = now(), after_tax_amount = '" + afterTaxAmount  + "'  where code = '" + curB2cEntity.getCode() + "';");
                        continue;
                    }
                    BigDecimal divideAfterTaxAmount = soB2cEntity.getAfterTaxAmount().multiply(curB2cEntity.getAmount()).divide(allAmount, 2, RoundingMode.DOWN);
                    updateSql.add("update so_b2c set update_time = now(), after_tax_amount = '" + divideAfterTaxAmount  + "'  where code = '" + curB2cEntity.getCode() + "';");
                    afterTaxAmount = afterTaxAmount.subtract(divideAfterTaxAmount);
                }
            }
            // 打印updateSql
            if (CollectionUtils.isEmpty(updateSql)){
                System.out.println("无更新SQL");
                return;
            }
            System.out.println("结果");
            for (String s : updateSql) {
                System.out.println(s);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}