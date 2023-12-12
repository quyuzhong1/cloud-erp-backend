package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.constant.TaskConstant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.gyy.GyyDeliveryDetailEntity;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.gyy.GyyDeliveryDetailServiceImpl;
import com.erp.server.dmp.pull.service.gyy.GyyOrderInfoServiceImpl;
import com.erp.server.dmp.pull.thread.PullErpDateThread;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
@EnableScheduling
public class PullGyyJob {
    @Resource
    private PullErpDateThread pullErpDateThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private GyyOrderInfoServiceImpl gyyOrderInfoService;
    @Resource
    private GyyDeliveryDetailServiceImpl gyyDeliveryDetailService;
    @Resource
    private MongoService mongoService;

    // 拉取管易云数据任务
    // @Scheduled(cron = "*/5 * * * * ?")
    @XxlJob("gyyExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(()->{
            pullErpDateThread.executeTask(TaskConstant.GYY_PULL_DATA_TASK);
        });
    }

    @XxlJob("gyyCleanExecute")
    public void gyyCleanExecute() {
        String jobParam = XxlJobHelper.getJobParam();
        log.info("管易云清洗任务参数：{}", jobParam);
        List<String> taskList = new ArrayList<>();
        if (StrUtil.isNotBlank(jobParam)) {
            taskList = Arrays.asList(jobParam.split(","));
        }
        pullErpDateThread.executeCleanTask(TaskConstant.GYY_PULL_DATA_TASK, taskList);
    }

    @XxlJob("gyySalesOrderDetailDownload")
    public void gyySalesOrderDetail(){
        Integer size = 1000;
        String jobParamStr = XxlJobHelper.getJobParam();
        if(StrUtil.isNotBlank(jobParamStr)){
            cn.hutool.json.JSONObject jobParam = JSONUtil.parseObj(jobParamStr);
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
