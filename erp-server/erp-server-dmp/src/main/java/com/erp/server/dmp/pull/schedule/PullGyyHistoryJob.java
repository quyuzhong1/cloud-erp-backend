package com.erp.server.dmp.pull.schedule;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.erp.model.dmp.constant.MongoTableNameContant;
import com.erp.model.dmp.dto.CleanOrderMongoDTO;
import com.erp.model.dmp.dto.JobTaskDTO;
import com.erp.model.dmp.dto.RequestDTO;
import com.erp.model.dmp.entity.DmpOrderItemEntity;
import com.erp.model.dmp.entity.PlatformApiTaskEntity;
import com.erp.model.dmp.enums.PlatformApiEnum;
import com.erp.model.dmp.gyy.GyyOrderEntity;
import com.erp.model.dmp.gyy.bean.DetailsBean;
import com.erp.model.dmp.vo.CleanAmountAfterVO;
import com.erp.server.dmp.pull.mongo.MongoService;
import com.erp.server.dmp.pull.service.IReportHistoryService;
import com.erp.server.dmp.pull.service.IReportSaveService;
import com.erp.server.dmp.pull.service.ModelService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderInfoService;
import com.erp.server.dmp.pull.service.dmp.DmpOrderItemService;
import com.erp.server.dmp.pull.service.dmp.PlatformApiTaskService;
import com.erp.server.dmp.pull.service.gyy.GyyHistoryDeliveryDetailServiceImpl;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@Slf4j
public class PullGyyHistoryJob {

    @Resource
    private PlatformApiTaskService platformApiTaskService;
    @Resource
    @Qualifier("gyyHistoryDeliveryDetailServiceImpl")
    private IReportHistoryService historyDeliveryService;

    @Resource
    @Qualifier("gyyHistoryOrderInfoServiceImpl")
    private IReportHistoryService historyTradeService;

    @Resource
    private MongoService mongoService;
    @Resource
    private DmpOrderInfoService dmpOrderInfoService;
    @Resource
    private DmpOrderItemService dmpOrderItemService;

    @XxlJob("GyyDeliveryHistory")
    public ReturnT<String> gyyDeliveryHistory() throws Exception {
        XxlJobHelper.log("GyyDeliveryHistory 任务开始执行！");
        // 查询对应任务配置
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET.getTaskName());
            return ReturnT.SUCCESS;
        }
        JobTaskDTO jobTaskDTO = new JobTaskDTO(entity);
        // 执行拉取任务
        //通过枚举获取对应service
        RequestDTO requestDTO = new RequestDTO(jobTaskDTO, PlatformApiEnum.GY_ERP_TRADE_DELIVERYS_HISTORY_GET);
        historyDeliveryService.pullHistoryOrderInfo(requestDTO);
        XxlJobHelper.log("GyyDeliveryHistory 任务执行结束！");
        return ReturnT.SUCCESS;
    }

    @XxlJob("GyyOrderHistory")
    public ReturnT<String> gyyOrderHistory() throws Exception {
        XxlJobHelper.log("GyyOrderHistory 任务开始执行！");
        // 查询对应任务配置
        PlatformApiTaskEntity entity = platformApiTaskService.getByApiCode(PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET.getTaskName());
        if(ObjectUtil.isEmpty(entity)){
            XxlJobHelper.log("{}任务task记录为空异常", PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET.getTaskName());
            return ReturnT.SUCCESS;
        }
        JobTaskDTO jobTaskDTO = new JobTaskDTO(entity);
        // 执行拉取任务
        //通过枚举获取对应service
        RequestDTO requestDTO = new RequestDTO(jobTaskDTO, PlatformApiEnum.GY_ERP_TRADE_HISTORY_GET);
        historyTradeService.pullHistoryOrderInfo(requestDTO);
        XxlJobHelper.log("GyyOrderHistory 任务执行结束！");
        return ReturnT.SUCCESS;
    }

    @XxlJob("CleanGyyDataByMongo")
    public ReturnT<String> cleanGyyDataByMongo() throws Exception {
        XxlJobHelper.log("CleanGyyDataByMongo 任务开始执行！");
        // 查询dmp gyy 订单数据
        List<CleanAmountAfterVO> vos = dmpOrderInfoService.getCleanOrderList();
        if (CollectionUtil.isEmpty(vos)){
            XxlJobHelper.log("CleanGyyDataByMongo 需要修复数据为空！");
            return ReturnT.SUCCESS;
        }
        List<String> codeList = vos.stream().map(CleanAmountAfterVO::getSalesRecordNumber).distinct().collect(Collectors.toList());
        CleanOrderMongoDTO cleanOrderMongoDTO = new CleanOrderMongoDTO();
        cleanOrderMongoDTO.setCode(codeList);
        // 查询对应mongo数据
        List<GyyOrderEntity> gyyOrderEntities = mongoService.findMongoData(cleanOrderMongoDTO, 0, 0, MongoTableNameContant.ORIGINAL_GYY_ORDER, GyyOrderEntity.class);
        ConcurrentHashMap<String, ConcurrentHashMap<String, DetailsBean>> orderMap = new ConcurrentHashMap<>();
        gyyOrderEntities.stream().forEach(entity->{
            String code = entity.getCode();
            List<DetailsBean> details = entity.getDetails();
            if (CollectionUtil.isEmpty(details)){
                return;
            }
            ConcurrentHashMap<String, DetailsBean> detailsMap = new ConcurrentHashMap<>();
            for (DetailsBean detail : details) {
                String keyCode = detail.getItemCode();
                if(StrUtil.isBlank(keyCode)){
                    keyCode = StrUtil.format("{}_{}_{}",detail.getItemCode(), detail.getItemName(), detail.getPlatformSkuName());
                }
                detailsMap.put(keyCode, detail);
            }
            orderMap.put(code, detailsMap);
        });

        // 更新 amountAfter
        vos.parallelStream().forEach(vo -> {
            ConcurrentHashMap<String, DetailsBean> detailsMap = orderMap.get(vo.getSalesRecordNumber());
            if (CollectionUtil.isEmpty(detailsMap)){
                XxlJobHelper.log("对应code在mongo中不存在code = {}", vo.getSalesRecordNumber());
                return;
            }
            String keyCode = vo.getItemId();
            if(StrUtil.isBlank(keyCode)){
                keyCode = StrUtil.format("{}_{}_{}",vo.getItemId(), vo.getItemName(), vo.getSpecifics());
            }
            DetailsBean detailsBean = detailsMap.get(keyCode);
            if(ObjectUtil.isEmpty(detailsBean)){
                XxlJobHelper.log("对应itemcode在mongo中不存在 code ={} itemCode= {}",vo.getSalesRecordNumber(), vo.getItemId());
                return;
            }
            try {
                boolean update = dmpOrderItemService.lambdaUpdate()
                        .eq(DmpOrderItemEntity::getId, vo.getId())
                        .set(DmpOrderItemEntity::getAmountAfter, detailsBean.getAmountAfter())
                        .set(DmpOrderItemEntity::getRefreshStatus, Boolean.TRUE)
                        .update();
                if (!update){
                    XxlJobHelper.log("对应金额更新失败 id={} mongo={}", vo.getId(), JSONUtil.toJsonStr(detailsBean));
                }
            }catch (Exception e) {
                XxlJobHelper.log("对应金额更新失败 id={} mongo={} e={}", vo.getId(), JSONUtil.toJsonStr(detailsBean), e);
            }
        });

        XxlJobHelper.log("CleanGyyDataByMongo 任务执行结束！");
        return ReturnT.SUCCESS;
    }

}
