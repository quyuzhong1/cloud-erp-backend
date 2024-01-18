package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.common.business.constant.MongoTableNameContant;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformOrderDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.scm.entity.SubcontractChangeDetailEntity;
import com.erp.oms.aliexpress.dto.PlatformAliExpressOrderDTO;
import com.erp.oms.aliexpress.handler.AliExpressOrderHandler;
import com.erp.oms.aliexpress.service.AliExpressOrderService;
import com.erp.sdk.oms.amz.spapi.dto.PlatformAmazonOrderDTO;
import com.erp.sdk.oms.amz.spapi.enums.AmazonRequestTypeRateLimiterEnum;
import com.erp.sdk.oms.amz.spapi.model.orders.Order;
import com.erp.server.dmp.enums.CleanDataTableEnum;
import com.erp.server.dmp.pull.thread.PlatformDataThread;
import com.erp.server.dmp.service.DmpPushTaskService;
import com.erp.server.dmp.service.PlatformApiTaskService;
import com.erp.server.dmp.service.impl.BusinessServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
@EnableScheduling
public class PullAliExpressJob {

    @Resource
    private PlatformDataThread platformDataThread;

    @Resource(name = "pullErpOpenApi")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private PlatformApiTaskService platformApiTaskService;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private AliExpressOrderHandler aliExpressOrderHandler;

    @Resource
    private DmpPushTaskService dmpPushTaskService;

    @Resource
    private BusinessServiceImpl businessService;



    /**
     * 拉取Shopify任务
     */
    @XxlJob("aliExpressExecute")
    public void execute() {
        // 分组查询
        List<String> groupIds = platformApiTaskService.findGroupIdByPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        if(CollectionUtils.isEmpty(groupIds)){
            XxlJobHelper.log("[拉取速卖通任务] 任务结束:无任务 =====");
            return;
        }
        groupIds.forEach(x-> threadPoolTaskExecutor.execute(() -> {
            platformDataThread.executeTask(x, true);
        }));
    }
    @XxlJob("aliExpressCleanExecute")
    public void aliExpressCleanExecute() {
        List<CleanDataTableEnum> platforms = CleanDataTableEnum.getByPlatform(PlatformDictEnum.ALI_EXPRESS.getCode());
        if (CollectionUtils.isNotEmpty(platforms)){
            platforms.forEach(cleanDataTableEnum -> {
                JobTaskDTO jobTaskDTO = new JobTaskDTO();
                jobTaskDTO.setPlatformCategory(cleanDataTableEnum.getCategory());
                jobTaskDTO.setDictPlatform(cleanDataTableEnum.getPlatform());
                jobTaskDTO.setBillType(cleanDataTableEnum.getBusiness());
                try {
                    XxlJobHelper.log("开始清洗：{}类{}数据", cleanDataTableEnum.getPlatform(),cleanDataTableEnum.getBusiness());
                    platformDataThread.cleanOrder(jobTaskDTO);
                    XxlJobHelper.log("清洗完成：{}类{}数据", cleanDataTableEnum.getPlatform(),cleanDataTableEnum.getBusiness());
                }catch (Exception e){
                    XxlJobHelper.log("清洗异常：{}", e);
                }
            });
        }
    }

    /**
     * 拉取速卖通地址 解密
     */
    @XxlJob("aliExpressAddressExecute")
    public void aliExpressAddressExecute() {
        Integer size;
        String jobParamStr = XxlJobHelper.getJobParam();
        if (StrUtil.isNotBlank(jobParamStr)) {
            JSONObject jobParam = JSON.parseObject(jobParamStr);
            size = jobParam.getInteger("size");
        } else {
            size = 100;
        }
        // 查询所有任务列表
        List<PlatformApiTaskEntity> taskList = platformApiTaskService.listByPlatformAndBillType(PlatformDictEnum.ALI_EXPRESS.getCode(), CleanDataTableEnum.ALI_EXPRESS_ORDER.getBusiness());
        if (CollectionUtils.isEmpty(taskList)) {
            XxlJobHelper.log("[拉取速卖通订单地址任务] aliExpressAddressExecute 任务结束,未找到需执行的任务");
        }
        String category = PlatformCategoryEnum.THIRD_SYSTEM.getCode();
        String platform = PlatformDictEnum.ALI_EXPRESS.getCode();
        String business = BusinessTypeEnum.ORDER.getCode();
        // 根据groupId分组店铺id
        Map<String, List<PlatformApiTaskEntity>> taskGroupMap = taskList.stream().collect(Collectors.groupingBy(PlatformApiTaskEntity::getGroupId));
        for (Map.Entry<String, List<PlatformApiTaskEntity>> entry : taskGroupMap.entrySet()) {
            String key = entry.getKey();
            List<PlatformApiTaskEntity> list=entry.getValue();
            handlerAddressDetail(key, list, platform, category, business,size);
        }
    }

    /**
     * 处理地址下载
     * @param key
     * @param list
     * @param platform
     * @param category
     * @param business
     */
    private void handlerAddressDetail(String key, List<PlatformApiTaskEntity> list, String platform, String category, String business,Integer size) {
        // 店铺IDS
        List<String> shopIds = list.stream().map(PlatformApiTaskEntity::getShopId).distinct().collect(Collectors.toList());
        List<PlatformAliExpressOrderDTO> orderEntityList = this.listPlatformOrder( shopIds, 1,size);
        if(CollectionUtils.isEmpty(orderEntityList)){
            return;
        }
        for(PlatformAliExpressOrderDTO item:orderEntityList){
            try {
              //下载地址处理
                PlatformAliExpressOrderDTO newDto = aliExpressOrderHandler.downloadAddress(item);
                newDto.setDownloadAddressStatus(1);
                newDto.setDownloadTime(LocalDateTime.now(ZoneId.systemDefault()).toString());
                newDto.setIsClean(2);
                List<PlatformOrderDTO> convertDto = aliExpressOrderHandler.convert(Collections.singletonList(newDto));
                // 保存和发送mq
                businessService.pullDetailProcess(newDto, convertDto.get(0), category, platform, business);
            }catch (Exception e){
                // 发送预警
                dmpPushTaskService.sendWarnMsg(item.getDmpSyncTaskId());
                log.error("下载地址处理失败, 订单号:{}，异常信息:{}",item.getUniqueId(),e.getMessage());
            }

        }

    }

    /**
     * 获取mongodb 数据
     * @description
     * @author Lambda
     * @return 
     * @create 2024-01-10 11:18
     */
    private List<PlatformAliExpressOrderDTO> listPlatformOrder( List<String> shopIds, int currentPage,int pageSize) {
        Query query = new Query();
        query.addCriteria(Criteria.where("isClean").is(2)
                .and("shopId").in(shopIds)
                .and("downloadAddressStatus").is(0));

        if (currentPage > 0 && pageSize > 0) {
            query.skip((long) (currentPage - 1) * pageSize).limit(pageSize);
        }
        return mongoTemplate.find(query, PlatformAliExpressOrderDTO.class, MongoTableNameContant.THIRD_SYSTEM_ALI_EXPRESS_ORDER);
    }
}
