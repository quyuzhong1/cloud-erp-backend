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
import com.common.business.dto.RequestDTO;
import com.common.business.enums.PlatformApiEnum;
import com.common.business.service.IReportSaveService;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.EnumTimePattern;
import com.common.core.utils.date.LocalDateUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.BiSkuInfoEntity;
import com.erp.model.dmp.enums.ApiKingdeeOrganizationEnum;
import com.erp.model.dmp.enums.CleanStatusEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.dmp.kingdee.KingdeeSkuEntity;
import com.erp.sdk.third.kingdee.utils.KingdeeApiUtils;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.CfgSettingService;
import com.erp.server.dmp.utils.DataCompareUtil;
import com.xxl.job.core.context.XxlJobHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 金蝶商品
 */
@Slf4j
@Component
@SaveData(method = PlatformApiEnum.BD_MATERIAL)
public class KingdeeSkuInfoServiceImpl implements IReportSaveService<KingdeeSkuEntity> {

    @Resource
    private MongoService mongoService;

    @Resource
    private MQProducerService<BiSkuInfoEntity> mqProducerService;
    @Resource
    private CfgSettingService cfgSettingService;

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void pullDataSave(RequestDTO dto) {
        List<KingdeeSkuEntity> entityList = pullDate(dto);
        if (CollectionUtil.isEmpty(entityList)) {
            log.info("拉取金蝶SKU信息列表数据为空 entityList.size = 0 ");
            return;
        }
        log.info("拉取金蝶SKU信息列表数据 entityList.size = {} ", entityList.size());
        List<KingdeeSkuEntity> insertList = new ArrayList<>();
        List<KingdeeSkuEntity> pushToMqList = new ArrayList<>();
        // 对于多条数据同时存在进行去重
        entityList = entityList.stream()
                .collect(Collectors.toMap(KingdeeSkuEntity::getFMaterialId,
                        item -> item,
                        (oldItem, newItem) -> {
                    // 保留最近更新的一条
                    if(newItem.getFModifyDate().compareTo(oldItem.getFModifyDate())> 0){
                        return newItem;
                    }else {
                        return oldItem;
                    }
                })).values().stream()
                .sorted(Comparator.comparing(KingdeeSkuEntity::getFModifyDate))
                .collect(Collectors.toList());
        for (KingdeeSkuEntity entity : entityList) {
            OrderMongoDTO orderMongoDTO = OrderMongoDTO.getShopByMaterialId(entity.getFMaterialId());
            List<KingdeeSkuEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
            entity.setIsClean(CleanStatusEnum.UNCLEAN.getCode());
            entity.setDownloadTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            if(CollectionUtil.isEmpty(mongoData)){
                insertList.add(entity);
                pushToMqList.add(entity);
                continue;
            }
            KingdeeSkuEntity mongoDatum = mongoData.get(0);
            // 比较数据是否相同
            if (DataCompareUtil.compareObject(mongoDatum , entity)) {
                continue;
            }
            pushToMqList.add(entity);
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(entity), MapUtil.class);
            String fMaterialId = mongoDatum.getFMaterialId();
            OrderMongoDTO updateDto = OrderMongoDTO.getShopByMaterialId(fMaterialId);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
        }
        if(CollectionUtil.isNotEmpty(insertList)){
            mongoService.saveMongoDataMult(insertList, MongoTableNameContant.ORIGINAL_KINGDEE_SKU);
        }
        if (CollectionUtil.isEmpty(pushToMqList)){
            log.warn("金蝶SKU信息, 无需推送到MQ dto={}", JSONUtil.toJsonStr(dto));
            return;
        }
        // 构造订单结构
        List<BiSkuInfoEntity> entityToMqlist = pushToMqList.stream()
                .map(this::initOrderInfoEntity)
                .filter(ObjectUtil::isNotEmpty)
                .collect(Collectors.toList());

        // 异步推送到MQ
        List<BiSkuInfoEntity> biSkuInfoEntityList = entityToMqlist.stream().peek(msg ->{
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_SKU_INFO_TAG.getName(),
                    msg, StrUtil.format("{}_{}", msg.getSkuNo(), msg.getItemCode()));
            if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
                throw new ServiceException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
            }
        }).collect(Collectors.toList());
        log.debug("金蝶产品发送数据为：{}" , JSON.toJSONString(biSkuInfoEntityList));
    }

    @Override
    public void cleanDataSave(String tableName, int size) {
        // 查询mongo待推送数据
        String value = cfgSettingService.getValue(SettingEnum.CLEAN_JOB_DELAY_MINUTE);
        Integer delayMinute = null != value ? NumberUtil.parseInt(value) : 0;
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByIsCleanDateStr(CleanStatusEnum.UNCLEAN.getCode(), delayMinute);
        List<KingdeeSkuEntity> mongoData = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
        if (CollectionUtil.isEmpty(mongoData)) {
            return;
        }
        for (KingdeeSkuEntity mongoDatum : mongoData) {
            mongoDatum.setIsClean(CleanStatusEnum.CLEANING.getCode());
            mongoDatum.setLastPushTime(LocalDateUtil.formatTime(LocalDateTime.now(), DateUtil.fmt));
            updateAndSaveDb(mongoDatum);
        }
    }

    @Override
    // @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public void updateAndSaveDb(KingdeeSkuEntity mongoDatum) {
        BiSkuInfoEntity skuInfo = initOrderInfoEntity(mongoDatum);
        OrderMongoDTO updateDto = new OrderMongoDTO(mongoDatum.get_id());
        if(null == skuInfo){
            mongoDatum.setIsClean(CleanStatusEnum.CLEANED.getCode());
            MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
            mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
            return;
        }
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(mongoDatum), MapUtil.class);
        mongoService.updateMongoData(updateDto, mapUtil, MongoTableNameContant.ORIGINAL_KINGDEE_SKU, KingdeeSkuEntity.class);
        SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.DMP_ERP_ORDER_TOPIC, RocketMqTagEnum.KINGDEE_SKU_INFO_TAG.getName(),
                skuInfo, StrUtil.format("{}_{}", skuInfo.getSkuNo(), skuInfo.getItemCode()));
        if (!SendStatus.SEND_OK.equals(result.getSendStatus())){
            throw new RuntimeException(StrUtil.format("发送MQ数据异常，{}", JSONUtil.toJsonStr(result)));
        }
    }

    /**
     * 请求金蝶云星空订单接口
     *
     * @param dto
     * @return
     */
    public List<KingdeeSkuEntity> pullDate(RequestDTO dto) {
        List<KingdeeSkuEntity> infoArrayList = new ArrayList<>();
        LocalDateTime lastTime = dto.getJobTaskDTO().getLastTime();
        LocalDateTime nextTime = dto.getJobTaskDTO().getNextTime();
        LinkedList<String> queryFilters = new LinkedList<>();
        DateTimeFormatter sdf = DateTimeFormatter.ofPattern(EnumTimePattern.y_m_dhms.toTimePattern());

        queryFilters.add(StrUtil.format("FDocumentStatus in ({})", "'C'"));
        queryFilters.add(StrUtil.format(" (FApproveDate >= '{}' and FApproveDate < '{}')",sdf.format(lastTime.minusMinutes(8)),sdf.format(nextTime)));
        String filterStr = String.join(" and ", queryFilters);

        String fieldKeys = "FUseOrgId,FUseOrgId.FName,FNumber,FMaterialId,FName,FSpecification,FCreateDate,FModifyDate," +
                "FDocumentStatus,FForbidStatus,FRefStatus,FPurPrice_CMK,F_PRVD_Assistant.FDataValue," +
                "F_PRVD_Assistant1.FDataValue,FSalePrice_CMK,F_SSRQ,FErpClsID";

        Boolean dataSign = true;
        //当前页数
        Integer pageIndex = 0;

        //每次最多获取100条
        Integer pageSize = 10000;
        while (dataSign) {
            KingdeeApiUtils kingdeeApiUtils = new KingdeeApiUtils(dto.getPlatformApiEnum().getTaskName());
            List<Map<String, Object>> result = kingdeeApiUtils.queryList(filterStr, fieldKeys, pageSize, pageIndex, 0);
            XxlJobHelper.log("获取金蝶SKU数据第[{}]页 有{}条记录", pageIndex, pageSize);
            if (result.size() < pageSize){
                dataSign = false;
            }
            if (CollectionUtil.isEmpty(result)) {
                return Collections.emptyList();
            }
            List<KingdeeSkuEntity> entityList = result.stream().map(shopEntity ->
                    BeanUtil.toBean(shopEntity, KingdeeSkuEntity.class)).collect(Collectors.toList());
            infoArrayList.addAll(entityList);
            pageIndex ++;
        }
        return infoArrayList;
    }

    /**
     * 解析商品数据
     *
     * @return void
     * @Author Luo_WG
     * @Date 2022/11/14 18:57
     **/
    public BiSkuInfoEntity initOrderInfoEntity(KingdeeSkuEntity skuInfoEntity) {
        if (StrUtil.isEmpty(skuInfoEntity.getFUseOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_YZS.getCode().equals(skuInfoEntity.getFUseOrgId()) ||
                ApiKingdeeOrganizationEnum.ORGANIZATION_XX.getCode().equals(skuInfoEntity.getFUseOrgId())
        ){
            return null;
        }
        BiSkuInfoEntity biSkuInfoEntity = new BiSkuInfoEntity();
        biSkuInfoEntity.setItemCode(skuInfoEntity.getFMaterialId());
        //sku编号
        biSkuInfoEntity.setSkuNo(skuInfoEntity.getFNumber());
        //中文名
        biSkuInfoEntity.setNameCn(skuInfoEntity.getFName());
        //英文名
        biSkuInfoEntity.setNameEn("");
        //统一成本价
        biSkuInfoEntity.setDefaultCost(new BigDecimal(skuInfoEntity.getFPurPrice_CMK()));
        Integer status = 3;
        if (skuInfoEntity.getFForbidStatus().equals("C")) {
            status = 5;
        }
        //商品状态:1.自动创建;2.待开发;3.正常;4.清仓;5.停止销售
        biSkuInfoEntity.setStatus(status);
        //商品创建时间
        biSkuInfoEntity.setSkuCreateTime(skuInfoEntity.getFCreateDate());
        //商品修改时间
        biSkuInfoEntity.setSkuUpdateTime(skuInfoEntity.getFModifyDate());
        //品牌
        biSkuInfoEntity.setBrandName("");
        //商品目录(一级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant()) && !"null".equals(skuInfoEntity.getF_PRVD_Assistant())) {
            biSkuInfoEntity.setParentCategoryName(skuInfoEntity.getF_PRVD_Assistant());
        } else {
            biSkuInfoEntity.setParentCategoryName("");
        }
        //商品目录(二级)
        if (StringUtils.isNotBlank(skuInfoEntity.getF_PRVD_Assistant1()) && !"null".equals(skuInfoEntity.getF_PRVD_Assistant1())) {
            biSkuInfoEntity.setCategoryName(skuInfoEntity.getF_PRVD_Assistant1());
        } else {
            biSkuInfoEntity.setCategoryName("");
        }
        //售价
        biSkuInfoEntity.setSalePrice(new BigDecimal(skuInfoEntity.getFSalePrice_CMK()));
        //申报价格
        biSkuInfoEntity.setDeclarePrice(BigDecimal.ZERO);
        //开发员id
        biSkuInfoEntity.setDeveloperId("");
        //开发员名称
        biSkuInfoEntity.setDeveloperName("");
        //平台标识
        biSkuInfoEntity.setPlatformSign(PlatformEnum.KINGDEE.getDesc());
        //企业id
        biSkuInfoEntity.setCompanyId(skuInfoEntity.getFUseOrgId());
        //企业名称
        biSkuInfoEntity.setCompanyName(skuInfoEntity.getFUseOrgName());
        //上市时间
        if (!"null".equals(skuInfoEntity.getFSSRQ()) && StrUtil.isNotEmpty(skuInfoEntity.getFSSRQ())){
            biSkuInfoEntity.setListingTime(LocalDateTime.parse(skuInfoEntity.getFSSRQ()));
        }
        String itemProperty = "";
        switch (skuInfoEntity.getFErpClsID()) {
            case "1" :
                itemProperty = "外购";
                break;
            case "2" :
                itemProperty = "自制";
                break;
            case "3" :
                itemProperty = "委外";
                break;
            case "6" :
                itemProperty = "服务";
                break;
            default:
                itemProperty = "";
                break;
        }
        //物料属性
        biSkuInfoEntity.setItemProperty(itemProperty);
        biSkuInfoEntity.setCreateTime(LocalDateTime.now());
        return biSkuInfoEntity;
    }
}
