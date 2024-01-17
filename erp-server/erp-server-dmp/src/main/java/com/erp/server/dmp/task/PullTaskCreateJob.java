package com.erp.server.dmp.task;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.annotation.Idempotent;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.JobTaskDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.enums.AuthStatusEnum;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.server.dmp.service.DmpOrderInfoService;
import com.erp.server.dmp.service.DmpRefundInfoService;
import com.erp.server.dmp.service.DmpReturnOrderInfoService;
import com.erp.server.dmp.service.impl.CreateRequestReportTaskService;
import com.erp.server.dmp.service.impl.TbTaskTypeService;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.util.XxlJobRemotingUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class PullTaskCreateJob {

    @Resource
    private TbTaskTypeService tbTaskTypeService;

    @Resource
    private CreateRequestReportTaskService reportTaskService;

    @Resource
    private DmpOrderInfoService dmpOrderInfoService;

    @Resource
    private DmpReturnOrderInfoService dmpReturnOrderInfoService;

    @Resource
    private DmpRefundInfoService dmpRefundInfoService;

    @Resource
    private ShopInfoFeign shopInfoFeign;

    /**
     * 定时扫描需要创建拉取任务拉取数据的任务
     * @Author Luo_WG
     * @Date 2022/11/9 14:50
     **/
    @XxlJob("createOrderJob")
    public void createOrderJob() {
        // 执行
        createOrderHandler("createOrderJob" + LocalDateTime.now());
    }

    @Idempotent(interval = 15)
    private void createOrderHandler(String key) {
        XxlJobHelper.log("createOrderJob 执行任务列表开始:key={}", key);
        List<JobTaskDTO> list = tbTaskTypeService.getTask();
        if(CollectionUtil.isEmpty(list)){
            XxlJobHelper.log("createOrderJob 需要执行任务列表为空");
            return;
        }
        reportTaskService.addTaskToQueue(list);
        XxlJobHelper.log("createOrderJob 执行任务列表结束:key={}", key);
    }
    /**
     * 定时扫描需要添加到任务表的api接口
     * @Author Luo_WG
     * @Date 2022/11/9 14:50
     * @return void
     **/
    @XxlJob("addShopTask")
    public ReturnT<String> addShopTask() {
        XxlJobHelper.log("addShopTask 任务开始执行");
        // 查询需要添加任务的店铺
        ApiResult<List<ShopInfoEntity>> listApiResult = shopInfoFeign.list();
        // 任务添加到任务表
        List<ShopInfoEntity> data = listApiResult.getData();
        if(CollectionUtil.isEmpty(data)){
            XxlJobHelper.log("addShopTask 店铺列表为空");
            return ReturnT.SUCCESS;
        }
        List<ShopInfoEntity> shopInfoList = data.stream()
                // 非禁用状态/未生成调度任务/已授权
                .filter(info -> !info.getDisabled() && !info.getIsGenTask() && AuthStatusEnum.ALREADY.getCode().equalsIgnoreCase(info.getAuthStatus()))
                .collect(Collectors.toList());
        if(CollectionUtil.isEmpty(shopInfoList)){
            XxlJobHelper.log("addShopTask 需要添加任务的店铺为空");
            return ReturnT.SUCCESS;
        }
        shopInfoList.forEach(shopInfoEntity -> {
            try {
                tbTaskTypeService.addTask(shopInfoEntity);
            } catch (Exception e) {
                log.error("addShopTask 添加任务失败,店铺id:{}",shopInfoEntity.getId(),e);
                XxlJobHelper.log("addShopTask 添加任务失败,店铺id:{}",shopInfoEntity.getId());
            }
        });
        XxlJobHelper.log("addShopTask 任务开始完成");
        return ReturnT.SUCCESS;
    }


    /**
     * 清洗订单数据
     * @Author Luo_WG
     * @Date 2022/12/14 11:28
     * @return void
     **/
    @XxlJob("cleanOrderTask")
    public ReturnT<String>  cleanOrderTask() {
        XxlJobHelper.log("cleanOrderTask 任务开始执行");
        String jobParam = XxlJobHelper.getJobParam();
        Integer pageSize = StrUtil.isNotBlank(jobParam) ? Integer.valueOf(jobParam) : 100;
        dmpOrderInfoService.cleanOrder(pageSize);
        XxlJobHelper.log("cleanOrderTask 任务开始完成");
        return ReturnT.SUCCESS;
    }

    /**
     * 清洗退货数据
     * @Author Luo_WG
     * @Date 2022/12/14 11:28
     * @return void
     **/
    @XxlJob("cleanReturnOrderTask")
    public ReturnT<String> cleanReturnOrderTask() {
        XxlJobHelper.log("cleanReturnOrderTask 任务开始执行");
        dmpReturnOrderInfoService.cleanReturnOrderTask();
        XxlJobHelper.log("cleanReturnOrderTask 任务开始完成");
        return ReturnT.SUCCESS;
    }


    /**
     * 清洗退款数据
     * @Author Luo_WG
     * @Date 2022/12/14 11:28
     * @return void
     **/
    @XxlJob("cleanRefundTask")
    public ReturnT<String> cleanRefundTask() {
        XxlJobHelper.log("cleanRefundTask 任务开始执行");
        dmpRefundInfoService.cleanRefundTask();
        XxlJobHelper.log("cleanRefundTask 任务开始完成");
        return ReturnT.SUCCESS;
    }
}
