package com.erp.server.dmp.pull.service.kingdee;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.annotation.SaveData;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiShopInfoEntity;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.kingdee.KingdeeEccShopEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 金蝶商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.ECC_SHOP)
public class KingdeeEccShopServiceImpl implements IReportSaveService<KingdeeEccShopEntity> {
    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<BiShopInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    public static void main(String[] args) {
        KingdeeEccShopServiceImpl shopService = new KingdeeEccShopServiceImpl();
        JobTaskDTO jobTaskDTO = new JobTaskDTO();
        jobTaskDTO.setApiCode(PlatformApiEnum.ECC_SHOP.getTaskName());
        jobTaskDTO.setPlatformApiId("5");
        jobTaskDTO.setApiName("获取订单列表");
        jobTaskDTO.setId("30");
        jobTaskDTO.setIntervalTime(1800);
        jobTaskDTO.setLastTime(LocalDateTime.parse("2021-01-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setNextTime(LocalDateTime.parse("2023-05-01 00:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        jobTaskDTO.setDictPlatform("1");
        jobTaskDTO.setStatus(1);
        RequestDTO requestDTO = new RequestDTO();
        requestDTO.setPlatformApiEnum(PlatformApiEnum.ECC_SHOP);
        requestDTO.setJobTaskDTO(jobTaskDTO);
        try {
            List<KingdeeEccShopEntity> kingdeeSkuEntities = shopService.pullDate(requestDTO);
            System.out.println(kingdeeSkuEntities);
        }catch (Exception e) {
        	log.error("" , e);
        }

    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<KingdeeEccShopEntity> skuEntityList = pullDate(dto);
        if (CollectionUtil.isEmpty(skuEntityList)){
            log.info("拉取金蝶网店管理列表数据为空 entityList.size = 0 ");
            XxlJobHelper.log("拉取金蝶网店管理列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取金蝶网店管理列表数据 entityList.size = {}} ", skuEntityList.size());
        XxlJobHelper.log("拉取金蝶网店管理列表数据 entityList.size = {}} ", skuEntityList.size());
        List<KingdeeEccShopEntity> insertList = new ArrayList<>();
        List<KingdeeEccShopEntity> pushToMqList = new ArrayList<>();
        for (KingdeeEccShopEntity entity : skuEntityList) {
            OrderMongoDTO queryMongoDTO = new OrderMongoDTO(entity.getId());
            List<KingdeeEccShopEntity> mongoData = mongoService.findMongoData(queryMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP, KingdeeEccShopEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeEccShopEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            mongoService.updateMongoData(queryMongoDTO, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP, KingdeeEccShopEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶网店管理数据为空, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiShopInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiShopInfoEntity> biShopInfoEntityList = entityToMqlist.stream().peek(msg -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_ECC_SHOP_INFO_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getPlatformShopNo(), msg.getId()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("kingdeeEccShop发送数据为：{}" , JSON.toJSONString(biShopInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<KingdeeEccShopEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP, KingdeeEccShopEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (KingdeeEccShopEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }


    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(KingdeeEccShopEntity mongoDatum) {
        BiShopInfoEntity shopInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.getId());
        if(null == shopInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil,  MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP, KingdeeEccShopEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil,  MongoTableNameContant.ORIGINAL_KINGDEE_ECC_SHOP, KingdeeEccShopEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_ECC_SHOP_INFO_TAG.getName(),
                shopInfo, StrUtil.format("{}_{}", shopInfo.getPlatformShopNo(), shopInfo.getId()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    private BiShopInfoEntity initOrderInfoEntity(KingdeeEccShopEntity shopEntity) {
        BiShopInfoEntity biShopInfoEntity = new BiShopInfoEntity();
        //平台店铺编号
        biShopInfoEntity.setPlatformShopNo(shopEntity.getFNumber());
        //平台店铺账户
        biShopInfoEntity.setAccountUserName(shopEntity.getFShopName());
        //平台店铺标识
        biShopInfoEntity.setAccountStoreName(shopEntity.getFName());
        //店铺名称
        biShopInfoEntity.setName(shopEntity.getFCustomerIdName());
        //平台名称
        biShopInfoEntity.setPlatformName(shopEntity.getFGYShopType());
        String orgName = shopEntity.getFSaleOrgIdName();
        if(StrUtil.isNotBlank(orgName)){
            biShopInfoEntity.setIsVijim(Boolean.TRUE);
            if (orgName.contains("优至胜") || orgName.contains("小隼")) {
                biShopInfoEntity.setIsVijim(Boolean.FALSE);
            }
        }
        biShopInfoEntity.setUseOrgId(Integer.parseInt(shopEntity.getFSaleOrgId()));
        biShopInfoEntity.setUseOrgName(orgName);
        biShopInfoEntity.setFinanceCode("");
        //平台标识
        biShopInfoEntity.setPlatformSign(PlatformEnum.KINGDEE_ECC.getDesc());
        biShopInfoEntity.setCustomerId(shopEntity.getFCustomerId());
        biShopInfoEntity.setCreateUserId(shopEntity.getFCreateOrgId());
        return biShopInfoEntity;
    }
    /**
     * 请求金蝶云星空客户列表接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeEccShopEntity> pullDate(RequestDTO dto) {
        // 当前页数
        Integer pageIndex = 0;
        // 每次最多获取100条
        Integer pageSize = 10000;
        List<KingdeeEccShopEntity> shopEntityList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        if(null == lastTime || null == nextTime){
            lastTime = LocalDateTime.parse("2021-01-01T00:00:00");
            nextTime = LocalDateTime.now();
            dto.getJobTaskDTO().setLastTime(lastTime);
            dto.getJobTaskDTO().setNextTime(nextTime);
        }
        //读取配置，初始化SDK
        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());
        queryFilters.add(StrUtil.format(" (FApproveDate >= '{}' and FApproveDate < '{}')",sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime)));
        String filterStr = String.join(" and ", queryFilters);
        String fieldKeys = "FId,FNumber,FName,FSaleOrgId,FSaleOrgId.FNumber,FSaleOrgId.FName," +
                "FCreateOrgId,FCreateOrgId.FNumber,FCreateOrgId.FName,FShopType,FShopName,FStartDate,FSettlementCurrency," +
                "FSettlementCurrency.FName,FCustomerId,FCustomerId.FNumber,FCustomerId.FName," +
                "FSettlementOrgId,FSettlementOrgId.FNumber,FSettlementOrgId.FName,FDownloadByGY,FNick,FGYShopType," +
                "FGYModifyDate,FStockOrgId,FStockOrgId.FName,FModifierId,FModifierId.FName,FCreatorId,FCreatorId.FName," +
                "FCreateDate,FModifyDate";

        Boolean dataSign = true;
        while (dataSign) {
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName(),1);
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            XxlJobHelper.log("获取金蝶店铺数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeEccShopEntity> entityList = result.stream().map(shopEntity ->
                    BeanUtil.toBean(shopEntity, KingdeeEccShopEntity.class)).collect(Collectors.toList());
            shopEntityList.addAll(entityList);
            pageIndex ++;
        }
        return shopEntityList;
    }
}
