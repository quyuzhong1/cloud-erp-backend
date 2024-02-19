package com.erp.server.dmp.pull.service.lingxing;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.ShopInfoMappingEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.lingxing.FbaReceiveDetailEntity;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.convert.DmpFbaShipmentReceiveConverter;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.ShopInfoMappingService;
import com.sdk.third.lingxing.dto.FbaShipmentReceiveDTO;
import com.sdk.third.lingxing.utils.LingxingApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 领星FBA货件签收明细
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.LX_ERP_FBA_SHIPMENT_RECEIVE_GET)
public class LxFbaShipmentReceiveServiceImpl implements IReportSaveService<FbaReceiveDetailEntity> {
    @Resource
    private MongoService mongoService;
    @Autowired
    private MQProducerService mqProducerService;
    @Resource
    private ShopInfoFeign shopInfoFeign;
    @Resource
    private ShopInfoMappingService shopInfoMappingService;


    /**
     * 拉取货件签收明细数据
     *
     * @param dto 任务信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(dto.getJobTaskDTO().getShopId());
        if (null == shopInfoEntity){
            throw new ServiceException("店铺不存在,id=" + dto.getJobTaskDTO().getShopId());
        }
        // 查询映射关系
        ShopInfoMappingEntity mappingEntity = shopInfoMappingService.getByShopIdAndType(shopInfoEntity.getId(), PlatformEnum.LINGXING.getName());
        if (null == mappingEntity){
            throw new ServiceException("数据异常:找不到领星映射关系, 店铺id=" + shopInfoEntity.getId());
        }
        String sid = mappingEntity.getThirdPlatformShopId();
        List<FbaShipmentReceiveDTO> dtoList = LingxingApiUtils.getAllReceivedInventory(Integer.parseInt(sid), nextTime.toLocalDate());
        if (CollectionUtil.isEmpty(dtoList)) {
            log.info("拉取领星货件签收明细数据列表数据为空 entityList.size = 0 ");
            return;
        }
        List<FbaReceiveDetailEntity> entityList = DmpFbaShipmentReceiveConverter.INSTANCE.dtoListToEntityList(dtoList);

        log.info("拉取领星货件签收明细数据 entityList.size = {} ", entityList.size());
        List<FbaReceiveDetailEntity> insertList = new ArrayList<>();
        List<FbaReceiveDetailEntity> pushToMqList = new ArrayList<>();
        for (FbaReceiveDetailEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getUniqId(entity.getUniqueId());
            List<FbaReceiveDetailEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_LX_FBA_SHIPMENT_RECEIVE, FbaReceiveDetailEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            FbaReceiveDetailEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getUniqueId());
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_ORDER);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("领星FBA货件明细, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 按fba_shipment_id分组构造结构
        Map<String, List<FbaReceiveDetailEntity>> entityToMqList = pushToMqList.stream()
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.groupingBy(FbaReceiveDetailEntity::getFbaShipmentId));

        // 异步推送到MQ
        entityToMqList.entrySet().stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.LX_FBA_SHIPMENT_RECEIVE_TAG.getName(),
                    msg.getValue(), msg.getKey());
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送领星FBA货件签收MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
    }

    @Override
    public void cleanDataSave(String tableName, int size) {

    }

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(FbaReceiveDetailEntity mongoDatum) {

    }

}
