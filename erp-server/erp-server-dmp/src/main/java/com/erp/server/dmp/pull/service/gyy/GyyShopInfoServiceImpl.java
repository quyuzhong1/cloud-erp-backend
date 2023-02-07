package com.erp.server.dmp.pull.service.gyy;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.core.constant.RocketMqTopic;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.constant.RocketMqTagEnum;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpErrorLogEntity;
import com.erp.model.dmp.entity.DmpShopInfoEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.model.dmp.gyy.GyyShopInfoEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.SaveData;
import com.erp.server.dmp.pull.service.dmp.DmpErrorLogService;
import com.erp.server.dmp.pull.service.dmp.DmpShopInfoService;
import com.erp.server.dmp.service.mq.MQProducerService;
import com.erp.server.dmp.utils.GyyApiUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管易云店铺
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.GY_ERP_SHOP_GET)
public class GyyShopInfoServiceImpl implements IReportSaveService<GyyShopInfoEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private DmpShopInfoService dmpShopInfoService;

    @Resource
    private MQProducerService<DmpShopInfoEntity> rocketMQTemplate;
    @Resource
    @Qualifier("gyyShopInfoServiceImpl")
    private IReportSaveService reportSaveService;

    public static void main(String[] args) {
        GyyShopInfoServiceImpl gyyShopInfoService = new GyyShopInfoServiceImpl();
        PlatformApiEnum platformApiEnum = PlatformApiEnum.GY_ERP_SHOP_GET;
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(platformApiEnum.getTaskName());
        jobTaskDTO.setApiId(12);
        jobTaskDTO.setApiName("管易云查询店铺列表");
        jobTaskDTO.setId(36L);
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(null);
        jobTaskDTO.setNextTime(null);
        jobTaskDTO.setPlatformId(1);
        jobTaskDTO.setState(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(platformApiEnum);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        List<GyyShopInfoEntity> orderEntities = null;
        try {
            orderEntities = gyyShopInfoService.pullDate(requestDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println(orderEntities);
    }


    @Override
    public void pullDataSave(RequestDTO dto) throws Exception {
        List<GyyShopInfoEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取管易店铺列表数据为空 entityList.size = 0 ");
            return;
        }
        List<GyyShopInfoEntity> insertList = new ArrayList<>();
        List<GyyShopInfoEntity> pushToMqList = new ArrayList<>();
        for (GyyShopInfoEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = new OrderMongoDTO(entity.getId());
            List<GyyShopInfoEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            GyyShopInfoEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (mongoDatum.toString().equals(entity.toString())) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(orderMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_GYY_SHOP, GyyShopInfoEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_GYY_SHOP);
        }
        // 构造订单结构
        List<DmpShopInfoEntity> mabangToMqlist = pushToMqList.parallelStream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        mabangToMqlist.stream().peek(msg ->
                        rocketMQTemplate.asyncClassMsg(RocketMqTopic.DMP_TOPIC, RocketMqTagEnum.GYY_SHOP_INFO_TAG.getName(),
                                msg, StrUtil.format("{}_{}", msg.getPlarformShopNo(), msg.getFinanceCode())))
                .collect(Collectors.toList());
    }

    /**
     * 请求管易云店铺接口
     *
     * @param dto
     * @return
     */
    private List<GyyShopInfoEntity> pullDate(RequestDTO dto) throws Exception {
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        dto.getJobTaskDTO().setLastTime(nextTime);
        return GyyApiUtils.queryShopList(dto.getPlatformApiEnum().getTaskName(), lastTime, nextTime);
    }

    /**
     * 解析店铺数据
     **/
    private DmpShopInfoEntity initOrderInfoEntity(GyyShopInfoEntity shopInfoEntity) {
        DmpShopInfoEntity dmpShopInfoEntity = new DmpShopInfoEntity();
        //平台店铺编号
        dmpShopInfoEntity.setPlarformShopNo(shopInfoEntity.getCode());
        //平台店铺账户
        dmpShopInfoEntity.setAccountUserName("");
        //平台店铺标识
        dmpShopInfoEntity.setAccountStoreName(shopInfoEntity.getNick());
        //店铺名称
        dmpShopInfoEntity.setName(shopInfoEntity.getName());
        //店铺站点
        dmpShopInfoEntity.setSite("CN");
        //店铺状态:1启用 2停用
        dmpShopInfoEntity.setStatus(1);
        //平台名称
        dmpShopInfoEntity.setPlatformName(shopInfoEntity.getTypeName());
        //财务编码
        dmpShopInfoEntity.setFinanceCode("");
        //平台标识
        dmpShopInfoEntity.setPlatformSign(PlatformEnum.GYY.getDesc());
        dmpShopInfoEntity.setCreateTime(LocalDateTime.now());
        return dmpShopInfoEntity;
    }

    /**
     * dmpShopInfoService.checkOrder(dmpShopInfoEntity);
     */
}
