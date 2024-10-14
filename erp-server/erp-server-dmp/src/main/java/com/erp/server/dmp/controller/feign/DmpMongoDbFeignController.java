package com.erp.server.dmp.controller.feign;

import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MapUtil;
import com.erp.model.dmp.DmpPullOtherOutStockDTO;
import com.erp.model.dmp.dto.DmpPullSoOutStockDTO;
import com.erp.model.dmp.dto.MongoDBUpdateDTO;
import com.erp.model.dmp.kingdee.KingdeeDeliveryDetailEntity;
import com.erp.model.oms.dto.OmsMongoDTO;
import com.erp.rpc.dmp.feign.DmpMongoDbFeign;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonFulfilledShipmentsDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.server.dmp.convert.DmpTrackConverter;
import com.erp.server.dmp.enums.CleanDataTableEnum;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.service.AmzBusinessHandleService;
import com.erp.server.dmp.service.DmpTrackService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.response.LocalLogisticsInfo;
import com.sdk.tms.track123.model.response.TrackDetail;
import com.sdk.tms.track123.model.response.TrackingDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 中台请求亚马逊Feign控制类
 *
 * @Author Cloud
 * @Date 2023/9/1 12:03
 **/
@Slf4j
@RestController
@RequestMapping("feign/mongodb")
public class DmpMongoDbFeignController{

    @Resource
    private MongoService mongoService;
    @Resource
    private MongoTemplate mongoTemplate;
    @Resource
    private AmzBusinessHandleService amzBusinessHandleService;
    @Resource
    private DmpTrackService dmpTrackService;

    /**
     * 拉取货件
     *
     * @Author Jim
     * @since 2023-10-10
     **/
    @PostMapping("/updateMongoDbData")
    public void updateMongoDbData(@RequestBody MongoDBUpdateDTO dto){
        if (Objects.isNull(dto) || StringUtils.isEmpty(dto.getTableName()) || StringUtils.isEmpty(dto.getUniqueId()) || Objects.isNull(dto.getIsClean())){
            return;
        }
        log.info("获取到配置：{}", JSONObject.toJSONString(dto));
        CleanDataTableEnum cleanDataTableEnum = CleanDataTableEnum.getByName(dto.getTableName());
        if (Objects.isNull(cleanDataTableEnum)) return;
        Class tClass = cleanDataTableEnum.getTClass();
        if (Objects.isNull(tClass)) return;
        OmsMongoDTO updateDto = new OmsMongoDTO();
        updateDto.setUniqueId(dto.getUniqueId());
        MapUtil mapUtil = new MapUtil();
        mapUtil.put("isClean", dto.getIsClean());
        mongoService.updateMongoData(updateDto, mapUtil, dto.getTableName(), tClass);
    }


    /**
     * 查询mongodb是否有销售出库单
     *
     * @Author Jim
     * @since 2024-01-04
     **/
    @GetMapping("/checkHasDeliveryDetail")
    public Boolean updateMongoDbData(@RequestParam(value = "platformCode") String platformCode, @RequestParam(value = "platform") String platform){
        if (StringUtils.isBlank(platformCode)){
            throw new ServiceException("订单id为空");
        }
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(platform)){
            platformCode = platformCode.replaceAll("-","");
        }
        Query query = new Query();
        query.addCriteria(Criteria.where("kingdeeOutStockItemEntityList.fSrcBillNo").is(platformCode)).limit(1);
        List<KingdeeDeliveryDetailEntity> list = mongoTemplate.find(query, KingdeeDeliveryDetailEntity.class, MongoTableNameContant.ORIGINAL_KINGDEE_DELIVERY_DETAIL);
        return CollectionUtils.isNotEmpty(list);

    }

    /**
     * 查询mongodb是否有销售出库单
     *
     * @Author Jim
     * @since 2024-02-14
     **/
    @PostMapping("/checkSoOutStock")
    public Boolean checkSoOutStock(@RequestBody DmpPullSoOutStockDTO resultDTO){
        return amzBusinessHandleService.checkAndSendSoOutStock(resultDTO);
    }



    /**
     * 查询mongodb生成其他出库单
     *
     * @Author Jim
     * @since 2024-02-14
     **/
    @PostMapping("/checkOtherOutStock")
    Boolean checkOtherOutStock(@RequestBody DmpPullOtherOutStockDTO resultDTO){
        return amzBusinessHandleService.checkAndSendOtherOutStock(resultDTO);
    }

    /**
     * 获取mongo DB中track123数据
     * @param trackNoList
     * @return
     */
    @PostMapping("/listMongoTractDataByTrackNoList")
    String listMongoTractDataByTrackNoList(@RequestBody List<String> trackNoList){
        return dmpTrackService.listMongoTractDataByTrackNoList(trackNoList);
    }
}
