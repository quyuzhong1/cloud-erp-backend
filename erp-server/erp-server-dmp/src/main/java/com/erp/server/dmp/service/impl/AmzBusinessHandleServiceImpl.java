package com.erp.server.dmp.service.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformSoOutStockDTO;
import com.common.business.dto.RequestDTO;
import com.common.business.dto.UniqueDto;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.anno.Panno;
import com.common.core.enums.PannoEnum;
import com.common.core.utils.MapUtil;
import com.common.core.utils.date.DateUtil;
import com.common.message.constant.RocketMqTopic;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.gyy.GyyShopInfoEntity;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFbaShipmentDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.handler.AmazonFulfilledShipmentsHandler;
import com.erp.server.dmp.handler.report.AmzReportFulfilledShipmentsHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.AmzBusinessHandleService;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 亚马逊处理 服务类
 *
 * @author Jim
 * @date 2024/3/12
 */
@Service
public class AmzBusinessHandleServiceImpl implements AmzBusinessHandleService {

    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private BusinessServiceImpl businessService;
    @Resource
    private SoOutstockFeign soOutstockFeign;

    @Override
    @Transactional(rollbackFor = Exception.class, transactionManager = "mongoTransactionManager")
    public Boolean checkAndSendSoOutStock(DmpPullSoOutStockDTO dto) {
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.AMAZON.getCode();
        String business = BusinessTypeEnum.SO_OUT_STOCK.getCode();
        String tableName = StrUtil.format("{}_{}_{}", category, platform, business);
        // 查询是否有为处理的记录来源
        Query query = new Query();
        query.addCriteria(
                Criteria.where("amazonOrderId").is(dto.getPlatformCode())
                        .and("shopId").is(dto.getShopId())
        );
        List<PlatformAmazonFulfilledShipmentsDTO> list = mongoTemplate.find(query, PlatformAmazonFulfilledShipmentsDTO.class, tableName);
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }

        for (PlatformAmazonFulfilledShipmentsDTO currentDTO : list) {
            // 重新消费销售出库单
            JobTaskDTO jobTaskDTO = new JobTaskDTO();
            jobTaskDTO.setShopId(dto.getShopId());
            jobTaskDTO.setShopName(dto.getShopId());
            jobTaskDTO.setDictPlatform(PlatformDictEnum.AMAZON.getCode());
            jobTaskDTO.setApiCode(BusinessTypeEnum.SO_OUT_STOCK.getCode());
            jobTaskDTO.setIntervalTime(86400);
            jobTaskDTO.setStatus(3);
            jobTaskDTO.setRetryTimes(0);
            jobTaskDTO.setApiName("亚马逊物流销售");
            jobTaskDTO.setCreateTime(LocalDateTime.now());
            jobTaskDTO.setUpdateTime(LocalDateTime.now());
            jobTaskDTO.setPlatformCategory(PlatformCategoryEnum.THIRD_SYSTEM.getCode());
            jobTaskDTO.setBillType(BusinessTypeEnum.SO_OUT_STOCK.getCode());
            jobTaskDTO.setOperateType("pull");
            jobTaskDTO.setSourceList(Collections.singletonList(currentDTO));
            RequestDTO requestDTO = new RequestDTO();
            requestDTO.setJobTaskDTO(jobTaskDTO);
            // 事务处理
            businessService.pullProcessBusiness(jobTaskDTO.getPlatformCategory(), jobTaskDTO.getDictPlatform(), jobTaskDTO.getBillType(), jobTaskDTO, null);
        }
        return true;
    }
}
