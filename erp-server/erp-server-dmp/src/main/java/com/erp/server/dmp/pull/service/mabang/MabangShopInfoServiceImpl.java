package com.erp.server.dmp.pull.service.mabang;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.message.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.mabang.ShopEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.common.message.service.mq.MQProducerService;
import com.erp.server.dmp.utils.MabangApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 马帮店铺
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.SYS_GET_SHOP_LIST)
public class MabangShopInfoServiceImpl implements IReportSaveService<ShopEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<DmpShopInfoEntity> mqProducerService;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<ShopEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取马帮店铺列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取马帮店铺列表 entityList.size = {} ", entityList.size());
        List<ShopEntity> insertList = new ArrayList<>();
        List<ShopEntity> pushToMqList = new ArrayList<>();
        for (ShopEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<ShopEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            ShopEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_MABANG_SHOP, ShopEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_MABANG_SHOP);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("马帮店铺信息, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<DmpShopInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.MABANG_SHOP_INFO_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlarformShopNo(), msg.getFinanceCode()));
            if (!SendStatus.SEND_OK .equals(result.getSendStatus())){
                throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
    }

    /**
     * 请求马帮店铺信息接口
     */
    private List<ShopEntity> pullDate(RequestDTO dto) {
        return MabangApiUtils.queryShopList(dto.getPlatformApiEnum().getTaskName());
    }

    /**
     * 解析店铺数据
     **/
    private DmpShopInfoEntity initOrderInfoEntity(ShopEntity shopEntity) {
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        //平台店铺编号
        dmpShopInfoEntity.setPlarformShopNo(shopEntity.getId());
        //平台店铺账户
        dmpShopInfoEntity.setAccountUserName(shopEntity.getAccountUsername());
        //平台店铺标识
        dmpShopInfoEntity.setAccountStoreName(shopEntity.getAccountStoreName());
        //店铺名称
        dmpShopInfoEntity.setName(shopEntity.getName());
        // 店铺站点
        if(StrUtil.isNotBlank(shopEntity.getAmazonsite())){
            dmpShopInfoEntity.setSite(shopEntity.getAmazonsite());
        }
        //店铺状态
        dmpShopInfoEntity.setStatus(shopEntity.getStatus());
        //平台名称
        dmpShopInfoEntity.setPlatformName(shopEntity.getPlatformName());
        // 财务编码
        dmpShopInfoEntity.setFinanceCode(shopEntity.getFinanceCode());
        //平台标识
        dmpShopInfoEntity.setPlatformSign(PlatformEnum.MABANG.getDesc());
        dmpShopInfoEntity.setCreateTime(LocalDateTime.now());
        return dmpShopInfoEntity;
    }
}
