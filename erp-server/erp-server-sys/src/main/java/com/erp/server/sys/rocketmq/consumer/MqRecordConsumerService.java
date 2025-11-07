package com.erp.server.sys.rocketmq.consumer;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.common.business.utils.RedisUtil;
import com.common.business.wrapper.FeignQuery;
import com.common.core.utils.JsonFieldDiffUtil;
import com.common.message.constant.RocketMqConsumerGroup;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.sys.dto.MqConsumerRecordDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.*;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionExtendTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.server.sys.service.*;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 *
 */
@Slf4j
@Service
@RocketMQMessageListener(topic = RocketMqTopic.RECEIVE_DDL_TO_MQ_SYS_TOPIC,
        selectorExpression = "sys_receive_ddl_to_mq_tag",
        consumerGroup = RocketMqConsumerGroup.SYS_RECEIVE_DDL_TO_MQ_CONSUMER)
public class MqRecordConsumerService implements RocketMQListener<String> {


//    {
//        "before": {
//        "qc_date": "2024-04-11",
//                "code": "QC24041100003",
//                "create_user_name": "胡双",
//                "qc_user_name": "马乐滋",
//                "purchase_order_id": "1778246528469569538",
//                "qc_user_id": "72",
//                "qc_dept_name": "品质部",
//                "update_time": "2024-04-11T11:54:15.959",
//                "is_deleted": false,
//                "update_user_id": "1778269408016207873",
//                "source_detail_id": "1778246529669140481",
//                "qc_finish_time": "2024-04-11T11:54:15.918",
//                "id": "1778269016553426945",
//                "create_user_id": "1777541206046150658",
//                "create_time": "2024-04-11T11:49:07.067",
//                "source_type": "purchaseOrder",
//                "update_user_name": "七七1",
//                "purchase_order_code": "PO24041100003",
//                "version": 3,
//                "qc_dept_id": "1675798259051794434",
//                "qc_status": "finishQc",
//                "source_id": "1778246528469569538",
//                "supplier_id": "1777949731759067137",
//                "source_code": "PO24041100003",
//                "warehouse_id": "1676949540114010116"
//    },
//        "after": {
//        "qc_date": "2024-04-11",
//                "code": "QC24041100003",
//                "create_user_name": "胡双",
//                "qc_user_name": "马乐滋",
//                "purchase_order_id": "1778246528469569538",
//                "qc_user_id": "72",
//                "qc_dept_name": "品质部",
//                "update_time": "2024-04-11 11:54:15.959000",
//                "is_deleted": false,
//                "update_user_id": "1778269408016207873",
//                "source_detail_id": "1778246529669140481",
//                "qc_finish_time": "2024-04-11 11:54:15.918000",
//                "id": "1778269016553426945",
//                "create_user_id": "1777541206046150658",
//                "create_time": "2024-04-11 11:49:07.067000",
//                "source_type": "purchaseOrder",
//                "update_user_name": "七七1",
//                "purchase_order_code": "PO24041100003",
//                "version": 4,
//                "qc_dept_id": "1675798259051794434",
//                "qc_status": "finishQc",
//                "source_id": "1778246528469569538",
//                "supplier_id": "1777949731759067137",
//                "source_code": "PO24041100003",
//                "warehouse_id": "1676949540114010116",
//                "P_TAG_CTS": "1751880048180",
//                "P_TAG_IUD": "U",
//                "table": "qc_info",
//                "db": "erp-wms",
//                "hashCode": 1142831633,
//                "schema": "",
//                "P_TAG_BATCHNO": "686b9170fc958b4cae4edf8d",
//                "cdc_kafka_key_rc": "1778269016553426945U"
//    }
//    }

    @Resource
    private ThirdNoticePushRecordService thirdNoticePushRecordService;

    public static final String TABLE_BUSINESS_KEY = "TABLE_BUSINESS_KEY";

    @Override
    public void onMessage(String jsonStr) {
        log.info("MqRecordConsumerService 开始");
        if (StringUtils.isBlank(jsonStr)) {
            return;
        }
        // 建议：增加debug日志
        log.info("接收到MQ消息内容: {}", jsonStr);

        Gson gson = new Gson();
        List<Map<String, Map<String, Object>>> list = new ArrayList<>();

        try {
            //jsonStr 有可能是数组的，也有可能是非数组
            if (jsonStr.startsWith("[")) {
                Type mapType = new TypeToken<List<Map<String, Map<String, Object>>>>(){}.getType();
                list = gson.fromJson(jsonStr, mapType);
            } else {
                Type mapType = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
                Map<String, Map<String, Object>> jsonMap = gson.fromJson(jsonStr, mapType);
                list.add(jsonMap);
            }
        } catch (Exception e) {
            log.error("MQ消息JSON解析异常: {}", jsonStr, e);
            return;
        }

        for (Map<String, Map<String, Object>> jsonMap : list) {
            if (Objects.isNull(jsonMap)) {
                log.error("解析后的jsonMap为null");
                continue;
            }
            Map<String, Object> before = jsonMap.getOrDefault("before", null);
            Map<String, Object> after = jsonMap.getOrDefault("after", null);
            if (Objects.isNull(after)) {
                log.error("解析后的after为null");
                continue;
            }
            //获取变动字段
            List<String> diffFields = JsonFieldDiffUtil.compare(before, after);
            if (CollUtil.isEmpty(diffFields)) {
                log.error("解析后的diffFields为null");
                continue;
            }
            //转驼峰
            List<String> convertedDiffFields = diffFields.stream()
                    .map(CharSequenceUtil::toCamelCase)
                    .collect(Collectors.toList());

            try {
                //深拷贝
                Map<String, Object> safeAfter = new HashMap<>(after);
                List<String> safeConvertedDiffFields = new ArrayList<>(convertedDiffFields);

                // 使用线程池直接异步执行
                thirdNoticePushRecordService.sendThirdNoticeByMqAsync(safeAfter, safeConvertedDiffFields);
            } catch (Exception e) {
                log.error("异步调用 sendThirdNoticeByMqAsync 失败", e);
            }
        }
        log.info("MqRecordConsumerService 结束");
    }
}
