package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.gyy.GyyDeliveryDetailServiceImpl;
import com.erp.server.dmp.pull.service.gyy.GyyOrderInfoServiceImpl;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管易详情订单下载任务
 *
 * @Author Cloud
 * @Date 2023/5/30 11:35
 **/

@Component
@Slf4j
public class GyyDetailDownloadJob {

    @Resource
    private GyyOrderInfoServiceImpl gyyOrderInfoService;
    @Resource
    private GyyDeliveryDetailServiceImpl gyyDeliveryDetailService;
    @Resource
    private MongoService mongoService;

    @XxlJob("gyySalesOrderDetailDownload")
    public void gyySalesOrderDetail(){
        Integer size = 1000;
        String jobParamStr = XxlJobHelper.getJobParam();
        if(StrUtil.isNotBlank(jobParamStr)){
            JSONObject jobParam = JSONUtil.parseObj(jobParamStr);
            size = jobParam.getInt("size");
        }
        // 根据状态查询未下载数据
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDownloadStatus(0);
        List<GyyOrderEntity> orderEntityList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
        if(CollectionUtil.isEmpty(orderEntityList)){
            return;
        }
        orderEntityList.forEach(gyyOrderEntity -> {
            try {
                gyyOrderInfoService.addOrderDetail(gyyOrderEntity);
            }catch (Exception e){
                log.error("管易销售数据下载失败 code={}", gyyOrderEntity.getCode(), e);
            }
        });
    }

    @XxlJob("gyyDeliveryDetailDownload")
    public void gyyDeliveryDetail(){
        Integer size = 1000;
        String jobParamStr = XxlJobHelper.getJobParam();
        if(StrUtil.isNotBlank(jobParamStr)){
            JSONObject jobParam = JSONUtil.parseObj(jobParamStr);
            size = jobParam.getInt("size");
        }
        // 根据状态查询未下载数据
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDownloadStatus(0);
        List<GyyDeliveryDetailEntity> orderEntityList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_GYY_DELIVERY_DETAIL, GyyDeliveryDetailEntity.class);
        if(CollectionUtil.isEmpty(orderEntityList)){
            return;
        }
        orderEntityList.forEach(deliveryEntity -> {
            try {
                gyyDeliveryDetailService.addOrderDetail(deliveryEntity);
            }catch (Exception e){
                log.error("管易销售数据下载失败 code={}", deliveryEntity.getCode(), e);
            }
        });
    }

}
