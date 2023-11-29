package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import com.sdk.oms.shopify.dto.PlatformShopifyOrderDTO;
import com.sdk.oms.shopify.handler.ShopifyOrderHandler;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@EnableScheduling
public class PullShopifyJob {

    @Resource
    private MongoService mongoService;

    @Resource
    private ShopifyOrderHandler shopifyOrderHandler;

    @Resource
    private BusinessServiceImpl businessService;

    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    /**
     * 拉取Shopify任务
     */
    @XxlJob("shopifyExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(PlatformDictEnum.SHOPIFY.getCode());
        });
    }


    /**
     * 拉取Shopify订单详情任务
     */
    @XxlJob("shopifySalesOrderDetailDownload")
    public ReturnT<String> shopifySalesOrderDetail() {
        Integer size = 100;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[拉取Shopify订单详情任务] shopifySalesOrderDetail 任务开始,size={}", size);
        // 根据状态查询未下载数据
        PlatformShopifyOrderDTO orderMongoDTO = PlatformShopifyOrderDTO.getByDownloadStatus(0);
        List<PlatformShopifyOrderDTO> orderEntityList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_SHOPIFY_ORDER, PlatformShopifyOrderDTO.class);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取Shopify订单详情任务] shopifySalesOrderDetail 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        orderEntityList.forEach(dto -> {
            try {
                // 下载和处理详情
                PlatformShopifyOrderDTO newDto = shopifyOrderHandler.downloadDetail(dto, null);
                String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
                String platform = PlatformDictEnum.SHOPIFY.getCode();
                String business = BusinessTypeEnum.ORDER.getCode();
                newDto.setDownloadStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                List<PlatformOrderDTO> convertDto = shopifyOrderHandler.convert(Collections.singletonList(newDto));
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
            } catch (Exception e) {
                XxlJobHelper.log("[拉取Shopify订单详情任务] shopifySalesOrderDetail下载失败，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        e.getMessage());
            }
        });
        XxlJobHelper.log("[拉取Shopify订单详情任务] shopifySalesOrderDetail 任务结束");
        return ReturnT.SUCCESS;
    }

}
