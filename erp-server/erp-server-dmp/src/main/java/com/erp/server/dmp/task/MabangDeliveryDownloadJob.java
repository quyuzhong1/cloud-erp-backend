package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.mabang.OrderEntity;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.mabang.MabangDeliveryDetailServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
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
public class MabangDeliveryDownloadJob {

    @Resource
    private MongoService mongoService;
    @Resource
    private MabangDeliveryDetailServiceImpl mabangDeliveryDetailService;

    @XxlJob("mabangDeliveryClean")
    public ReturnT mabangDeliveryClean(){
        Integer size = 1000;
        String jobParamStr = XxlJobHelper.getJobParam();
        Integer isClean = null;
        Integer isExists = null;
        if(StrUtil.isNotBlank(jobParamStr)){
            JSONObject jobParam = JSONUtil.parseObj(jobParamStr);
            if (ObjectUtil.isNotEmpty(jobParam.get("size"))) {
                size = jobParam.getInt("size");
            }
            if (ObjectUtil.isNotEmpty(jobParam.get("isClean"))) {
                isClean = jobParam.getInt("isClean");
            }
            if (ObjectUtil.isNotEmpty(jobParam.get("isExists"))) {
                isExists = jobParam.getInt("isExists");
            }
        }
        if(ObjectUtil.isEmpty(isClean) && ObjectUtil.isEmpty(isExists)){
            log.error("马帮销售数据下载任务参数错误 jobParam={}", jobParamStr);
            XxlJobHelper.log("马帮销售数据下载任务参数错误 jobParam={}", jobParamStr);
            return ReturnT.FAIL;
        }
        // 根据状态查询未下载数据
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByCleanToDelivery(isClean, isExists);
        List<OrderEntity> orderEntityList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.ORIGINAL_MABANG_ORDER, OrderEntity.class);
        if(CollectionUtil.isEmpty(orderEntityList)){
            log.info("马帮销售数据下载任务无数据");
            XxlJobHelper.log("马帮销售数据下载任务无数据");
            return ReturnT.SUCCESS;
        }
        orderEntityList.forEach(orderEntity -> {
            try {
                mabangDeliveryDetailService.addDeliveryOrder(orderEntity);
            }catch (Exception e){
                log.error("马帮销售数据下载失败 code={}", orderEntity.getPlatformOrderId(), e);
                XxlJobHelper.log("马帮销售数据下载失败 code={}", orderEntity.getPlatformOrderId());
            }
        });
        return ReturnT.SUCCESS;
    }



}
