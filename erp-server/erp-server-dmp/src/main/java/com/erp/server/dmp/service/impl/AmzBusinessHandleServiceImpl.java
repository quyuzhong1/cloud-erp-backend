package com.erp.server.dmp.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.controller.vo.ApiResult;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.entity.DmpPullTaskEntity;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.rpc.wms.feign.WmsAmazonFeign;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFulfilledShipmentsHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.AmzBusinessHandleService;
import com.erp.server.dmp.service.DmpPullTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 亚马逊处理 服务类
 *
 * @author Jim
 * @date 2024/3/12
 */
@Slf4j
@Service
public class AmzBusinessHandleServiceImpl implements AmzBusinessHandleService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private SoOutstockFeign soOutstockFeign;
    @Resource
    private MongoService mongoService;
    @Resource
    private DmpPullTaskService dmpPullTaskService;
    @Resource
    private WmsAmazonFeign wmsAmazonFeign;
    @Resource
    private AmazonFulfilledShipmentsHandler amazonFulfilledShipmentsHandler;
    @Resource
    private SoB2cFeign soB2cFeign;

    @Override
//    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public Boolean checkAndSendSoOutStock(DmpPullSoOutStockDTO dto) {
        // 查询来源明细ID
        List<SoB2cDetailEntity> detailEntityList = soB2cFeign.listDetailByMainIds(Collections.singletonList(dto.getSoB2cId()));
        if (CollectionUtils.isEmpty(detailEntityList)){
            throw new ServiceException("检查生成亚马逊FBA销售出库单失败,为找到明细:id=" + dto.getSoB2cId());
        }
        List<String> sourceDetailIds = detailEntityList.stream()
                .map(SoB2cDetailEntity::getSourceDetailId)
                .collect(Collectors.toList());

        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.SO_OUT_STOCK.getCode();
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        // 查询是否有为处理的记录来源
        Query query = new Query();
        query.addCriteria(
                Criteria.where("amazonOrderId").is(dto.getPlatformCode())
                        .and("amazonOrderItemId").in(sourceDetailIds)
        );
        List<PlatformAmazonFulfilledShipmentsDTO> list = mongoTemplate.find(query, PlatformAmazonFulfilledShipmentsDTO.class, tableName);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }

        String topic = RocketMqTopic.PLATFORM_PULL_DATA_TOPIC;
        String tag = StrUtil.format("{}_{}", category, business) + "_tag";
        BusinessTypeEnum businessType = BusinessTypeEnum.getByCodeAndThrow(business);

        for (PlatformAmazonFulfilledShipmentsDTO currentDTO : list) {
            singleHandlerConsumer(currentDTO, tableName, platform, businessType, topic, tag);
//            // 重新消费销售出库单
//            JobTaskDTO jobTaskDTO = new JobTaskDTO();
//            jobTaskDTO.setShopId(dto.getShopId());
//            jobTaskDTO.setShopName(dto.getShopId());
//            jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
//            jobTaskDTO.setApiCode(BusinessTypeEnum.SO_OUT_STOCK.getCode());
//            jobTaskDTO.setIntervalTime(86400);
//            jobTaskDTO.setStatus(3);
//            jobTaskDTO.setRetryTimes(0);
//            jobTaskDTO.setApiName("亚马逊物流销售");
//            jobTaskDTO.setCreateTime(LocalDateTime.now());
//            jobTaskDTO.setUpdateTime(LocalDateTime.now());
//            jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
//            jobTaskDTO.setBillType(BusinessTypeEnum.SO_OUT_STOCK.getCode());
//            jobTaskDTO.setOperateType("pull");
//            jobTaskDTO.setSourceList(Collections.singletonList(currentDTO));
//            RequestDTO requestDTO = new RequestDTO();
//            requestDTO.setJobTaskDTO(jobTaskDTO);
//            // 事务处理
//            businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, null);
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public void singleHandlerConsumer(PlatformAmazonFulfilledShipmentsDTO currentDTO, String tableName, String platform, BusinessTypeEnum businessType, String topic, String tag) {
        List<PlatformSoOutStockDTO> convertList = amazonFulfilledShipmentsHandler.convert(Collections.singletonList(currentDTO));
        PlatformSoOutStockDTO platformSoOutStockDTO = convertList.get(0);
        log.info("手动重试销售出库单：{}", JSONUtil.toJsonStr(platformSoOutStockDTO));
        ApiResult<?> apiResult = wmsAmazonFeign.consumerSoOutStock(platformSoOutStockDTO);
        if (200 != apiResult.getCode()) {
            throw new ServiceException("手动重试销售出库单失败");
        }

        Class<? > tClass = currentDTO.getClass();
        currentDTO.setIsClean(2);
        MapUtil mapUtil = JSONObject.parseObject(JSONObject.toJSONString(currentDTO), MapUtil.class);
        mongoService.updateMongoData(currentDTO, mapUtil, tableName, tClass);

        String modelTaskId = dmpPullTaskService.saveOrUpdateDmpSyncTask(new DmpPullTaskEntity(platform, businessType.getSourceType().getCode(), platform, topic, tag, platformSoOutStockDTO));
        // 同步处理
        dmpPullTaskService.updateSyncInfo(String.valueOf(modelTaskId), SyncStatusEnum.SUCCESS_SYNC.getCode(), "同步成功");

        log.info("手动重试销售出库单结果：{}", JSONUtil.toJsonStr(apiResult));
    }
}
