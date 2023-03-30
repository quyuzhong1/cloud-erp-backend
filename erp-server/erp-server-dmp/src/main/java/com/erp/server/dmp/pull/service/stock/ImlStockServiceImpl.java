package com.erp.server.dmp.pull.service.stock;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.lang.Console;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.http.webservice.SoapClient;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.*;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.GyyShopInfoEntity;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.utils.GyyApiUtils;
import com.erp.server.dmp.utils.ImlApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/3/30 11:59
 **/

@Slf4j
@Service
@SaveData(method = PlatformApiEnum.IML_OMS_ASN_LIST)
public class ImlStockServiceImpl implements IReportSaveService {

    @Resource
    private MongoService mongoService;

    @Autowired
    private MQProducerService<GoodcangDTO.MessageDTO> mqProducerService;

    public static void main(String[] args) throws SOAPException {
        String soapAction = "getAsnList";
        Dict paramsJsonMap = new Dict();
        paramsJsonMap.put("pageSize", "20");
        paramsJsonMap.put("page", "1");
        paramsJsonMap.put("modify_date_from", "2023-02-28 00:00:00");
        paramsJsonMap.put("modify_date_to", "2023-03-28 00:00:00");
        JSONObject entries = ImlApiUtils.queryList(soapAction, JSONUtil.toJsonStr(paramsJsonMap));
        log.debug("SOAP Response: {}", entries);
    }


    @Override
    @Transactional(transactionManager = "mongoTransactionManager", rollbackFor = Exception.class)
    public void pullDataSave(RequestDTO dto) {
        //请求api
        List<OmsImlDTO.MessageDTO> ImlEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(ImlEntityList)) {
            log.info("拉取艾姆勒入库单 ImlEntityList.size = 0 ");
            return;
        }
        //过滤数据
        List<OmsImlDTO.MessageDTO> pushToMqList = new ArrayList<>();
        for (OmsImlDTO.MessageDTO entity : ImlEntityList) {
            OmsMongoDTO orderMongoDTO = new OmsMongoDTO(entity.getReceivingCode());
            List<OmsImlDTO.MessageDTO> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_IML_INBOUND_ORDER, OmsImlDTO.MessageDTO.class);
            if(CollectionUtil.isEmpty(mongoData)){
                mongoService.saveMongoData(entity, MongoTableNameContant.ORIGINAL_IML_INBOUND_ORDER);
                pushToMqList.add(entity);
                continue;
            }
            OmsImlDTO.MessageDTO mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
            mongoDatum.set_id(null);
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            // 修改数据
            MapUtil mapUtil = JSONUtil.toBean(JSONUtil.toJsonStr(entity), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_IML_INBOUND_ORDER, OmsImlDTO.MessageDTO.class);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("艾姆勒入库单, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<GoodcangDTO.MessageDTO> entityToMqlist = pushToMqList.stream()
                .filter(x -> "E".equalsIgnoreCase(x.getReceivingStatus()))
                .map(GoodcangDTO.MessageDTO::new)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());
        // 异步推送到MQ
        entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.IML_STOCK_INBOUND_ORDER_TAG.getName(),
                    msg, msg.getReceivingCode());
            if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
    }

    /**
     * 请求管易云店铺接口
     *
     * @param dto
     * @return
     */
    private List<OmsImlDTO.MessageDTO> pullDate(RequestDTO dto) {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        if(null == lastTime || null == nextTime){
            dto.getJobTaskDTO().setNextTime(LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.MIN));
            dto.getJobTaskDTO().setLastTime(LocalDateTime.of(LocalDate.now(), LocalTime.MIN));
        }
        Integer page = 1;
        Integer pageSize = 20;
        Dict paramsJsonMap = new Dict();
        Boolean nextPage = Boolean.TRUE;
        paramsJsonMap.put("pageSize", pageSize);

        paramsJsonMap.put("modify_date_from", LocalDateTimeUtil.format(lastTime, DateUtil.fmt));
        paramsJsonMap.put("modify_date_to", LocalDateTimeUtil.format(nextTime, DateUtil.fmt));
        List<OmsImlDTO.MessageDTO> infoArrayList = new ArrayList<>();
        while (nextPage){
            paramsJsonMap.put("page", page);
            JSONObject entries = ImlApiUtils.queryList(dto.getPlatformApiEnum().getTaskName(), JSONUtil.toJsonStr(paramsJsonMap));
            JSONArray dataArray = entries.getJSONArray("data");
            if(CollectionUtil.isEmpty(dataArray)){
                return Collections.EMPTY_LIST;
            }
            List<OmsImlDTO.MessageDTO> dataList = JSONUtil.toList(dataArray, OmsImlDTO.MessageDTO.class);
            if(CollectionUtil.isNotEmpty(dataList)){
                infoArrayList.addAll(dataList);
            }
            nextPage =entries.getBool("nextPage", Boolean.FALSE);
            page++;
        }

        return infoArrayList;
    }
}
