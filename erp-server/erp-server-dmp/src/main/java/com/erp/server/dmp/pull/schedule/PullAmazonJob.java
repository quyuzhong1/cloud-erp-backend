package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.dto.PlatformProductDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.OrderMongoDTO;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.sdk.oms.amz.spapi.handler.AmazonListingHandler;
import com.erp.sdk.oms.amz.spapi.handler.AmazonOrderHandler;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
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
import java.util.List;

@Component
@Slf4j
@EnableScheduling
public class PullAmazonJob {

    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private MongoService mongoService;

    @Resource
    private BusinessServiceImpl businessService;

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private AmazonOrderHandler amazonOrderHandler;

    @Resource
    private AmazonListingHandler amazonListingHandler;


    /**
     * 拉取亚马逊任务
     */
    @XxlJob("amazonExecute")
    public void execute() {
        threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(PlatformDictEnum.AMAZON.getCode());
        });
    }

    /**
     * 拉取亚马逊报表任务
     */
    @XxlJob("amazonReportExecute")
    public void reportExecute() {

    }

    /**
     * 拉取亚马逊订单详情任务
     */
    @XxlJob("amazonSalesOrderDetailDownload")
    public ReturnT<String> amazonSalesOrderDetail() {
        Integer size = 1000;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务开始,size={}", size);
        // 根据状态查询未下载数据
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDownloadStatus(0);
        List<PlatformOrderDTO> orderEntityList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_ORDER, PlatformOrderDTO.class);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        orderEntityList.forEach(dto -> {
            try {
                // 下载和处理详情
                PlatformOrderDTO newDto = amazonOrderHandler.downloadDetail(dto, null);
                String category = PlatformCategoryEnum.OMS.getCode();
                String platform = PlatformDictEnum.AMAZON.getCode();
                String business = BusinessTypeEnum.ORDER.getCode();
                newDto.setDownloadStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                businessService.pullDetailProcess(newDto, category, platform, business);
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail下载失败，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        e.getMessage());
            }
        });
        XxlJobHelper.log("[拉取亚马逊订单详情任务] amazonSalesOrderDetail 任务结束");
        return ReturnT.SUCCESS;
    }


    /**
     * 拉取亚马逊商品详情任务
     */
    @XxlJob("amazonProductDetailDownload")
    public ReturnT<String> amazonProductDetail() {
        Integer size = 1000;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        }
        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务开始,size={}", size);
        // 根据状态查询未下载数据
        OrderMongoDTO orderMongoDTO = OrderMongoDTO.getByDownloadStatus(0);
        List<PlatformProductDTO> orderEntityList = mongoService.findMongoData(orderMongoDTO, 1, size, MongoTableNameContant.THIRD_SYSTEM_AMAZON_PRODUCT, PlatformProductDTO.class);
        if (CollectionUtil.isEmpty(orderEntityList)) {
            XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务结束,无需要更新的信息");
            return ReturnT.SUCCESS;
        }
        orderEntityList.forEach(dto -> {
            try {
                PlatformApiTaskEntity taskEntity = platformApiTaskService.getById(dto.getDmpSyncTaskId());
                if (null == taskEntity) {
                    throw new ServiceException("未找到对应platform_api_task任务，id=" + dto.getDmpSyncTaskId());
                }
                JSONObject extendObj = new JSONObject();
                extendObj.put("shopId", taskEntity.getShopId());
                // 下载和处理详情
                PlatformProductDTO newDto = amazonListingHandler.downloadDetail(dto, extendObj);
                String category = PlatformCategoryEnum.OMS.getCode();
                String platform = PlatformDictEnum.AMAZON.getCode();
                String business = BusinessTypeEnum.PRODUCT.getCode();
                newDto.setDownloadStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                businessService.pullDetailProcess(newDto, category, platform, business);
            } catch (Exception e) {
                XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail，uniqueId={}, error={}",
                        dto.getUniqueId(),
                        e.getMessage());
            }
        });
        XxlJobHelper.log("[拉取亚马逊商品详情任务] amazonProductDetail 任务结束");
        return ReturnT.SUCCESS;
    }
}
