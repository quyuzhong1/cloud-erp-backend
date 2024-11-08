package com.erp.server.dmp.service.impl;

import cn.hutool.json.JSONUtil;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.LogisticsTransportTypeEnum;
import com.erp.server.dmp.convert.DmpTrackConverter;
import com.erp.server.dmp.service.DmpTrackService;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import com.sdk.tms.track123.dto.PlatformTrackDetail;
import com.sdk.tms.track123.model.response.LocalLogisticsInfo;
import com.sdk.tms.track123.model.response.TrackDetail;
import com.sdk.tms.track123.model.response.TrackingDetail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName DmpTrackServiceImpl
 * @description: TODO
 * @date 2024年10月08日
 * @version: 1.0
 */
@Slf4j
@Service
public class DmpTrackServiceImpl implements DmpTrackService {

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public String listMongoTractDataByTrackNoList(List<String> trackNoList) {
        if (CollectionUtils.isEmpty(trackNoList)){
            return null;
        }
        String tableName = LogisticsPlatformEnum.TRACK123.getName() + "_" + LogisticsTransportTypeEnum.EXPRESS_DELIVERY.getCode() + "_data";
        Query query = new Query();
        query.addCriteria(Criteria.where("trackNo").in(trackNoList));
        List<TrackDetail> trackDetails = mongoTemplate.find(query, TrackDetail.class, tableName);
        if (CollectionUtils.isEmpty(trackDetails)){
            return null;
        }else {
            return JSONUtil.toJsonStr(trackDetails);
        }
    }
}
