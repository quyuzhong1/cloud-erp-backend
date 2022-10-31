package com.cloud.erp.chrome.schedule;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cloud.erp.chrome.constant.BusinessType;
import com.cloud.erp.chrome.constant.ErpPlatform;
import com.cloud.erp.chrome.dto.GyyParamDTO;
import com.cloud.erp.chrome.dto.GyySearchParamDTO;
import com.cloud.erp.chrome.dto.MabangOrderParamDTO;
import com.cloud.erp.chrome.entity.ScheduleTaskEntity;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 自动调度创建任务
 * @Date 2022-08-25 16:37
 * @Dreated by yl
 */
@Component
@Slf4j
public class TaskJob {

    @Autowired
    private ChromeTaskInfoService  chromeTaskInfoService;


    @Value("${gyy.fieldsText}")
    private String fieldsText;

    @Value("${gyy.fieldsName}")
    private String fieldsName;


    /**
     * 生成马帮的任务  每天00:25
     */
    @XxlJob("addMabangTask")
    public void addMabangTask() {
        createScheduleOrderTask(ErpPlatform.MABANG);
    }

    /**
     * 生成管易云的任务  每天 00:15
     */
    @XxlJob("addGyyTask")
    public void addGyyTask() {
        createScheduleOrderTask(ErpPlatform.GYY);

    }

    /**
     * 生成云星空任务 每天00:05
     */
    @XxlJob("addYxkTask")
    public void addYxkTask() {
        createScheduleOrderTask(ErpPlatform.YXK);

    }

    /**
     * 创建管易任务
     * @param nextDate  日期
     */
    private void createGuanYiTaskBak(Date nextDate) {
        try {
            DateTime startTime=DateUtil.beginOfDay(nextDate);
            DateTime endTime=DateUtil.endOfDay(nextDate);
            Date nowDate=new Date();

            String startTimeStr=startTime.toString("yyyy-MM-dd HH:mm:ss");
            String endTimeStr=endTime.toString("yyyy-MM-dd HH:mm:ss");

            // 搜索参数信息
            GyySearchParamDTO searchParam=new GyySearchParamDTO();
            searchParam.setDeliveryBeginDate(startTimeStr);
            searchParam.setDeliveryEndDate(endTimeStr);
            searchParam.setCreateBeginDate("2021-01-01 00:00:00");
            searchParam.setCreateEndDate(endTimeStr);
            searchParam.setDeliveryEndDate(endTimeStr);
            // 参数信息
            GyyParamDTO dto = new GyyParamDTO();
            dto.setFieldsName(fieldsName);
            dto.setFieldsText(fieldsText);
            dto.setSearchParams(searchParam);

            // 创建任务
            ScheduleTaskEntity entity=new ScheduleTaskEntity();
            entity.setPlatform(ErpPlatform.GYY);
            entity.setBusinessType(BusinessType.SHIPMENTS);
            entity.setParameter(JSONObject.toJSONString(dto));
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            chromeTaskInfoService.save(entity);
        } catch (Exception e) {
            log.error("addMaBanTask 出错了 e==",e);
            throw new RuntimeException("创建管易云爬虫任务出错:"+ e.getMessage());
        }
    }

    @Deprecated
    private void createMabangTaskBak() {
        try {
            DateTime yesterday = DateUtil.yesterday();
            //获取开始时间
            DateTime startTime=DateUtil.beginOfDay(yesterday);
            DateTime endTime=DateUtil.endOfDay(yesterday);
            String startTimeStr=startTime.toString("yyyy-MM-dd HH:mm:ss");
            String endTimeStr=endTime.toString("yyyy-MM-dd HH:mm:ss");

            //参数对象
            MabangOrderParamDTO param = new MabangOrderParamDTO();
            param.setExpresstimeTimeStart(startTimeStr);
            param.setExpresstimeTimeEnd(endTimeStr);

            //任务
            Date nowDate=DateUtil.date();
            ScheduleTaskEntity entity=new ScheduleTaskEntity();
            entity.setBusinessType(BusinessType.ORDER);
            entity.setParameter(JSONObject.toJSONString(param));
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setPlatform(ErpPlatform.MABANG);
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            chromeTaskInfoService.save(entity);
        } catch (Exception e) {
           log.error("addMaBanTask 出错了 e==",e);
            throw new RuntimeException("创建马帮爬虫任务出错:"+ e.getMessage());
        }
    }

    private void addYxkTaskBak() {
        try {
            //对应的参数 默认昨天的数据
            DateTime yesterday = DateUtil.yesterday();
            String yesterdayStr=yesterday.toString("yyyy-MM-dd");
            Date nowDate=DateUtil.date();

            ScheduleTaskEntity entity=new ScheduleTaskEntity();
            entity.setBusinessType(BusinessType.YXKORDER);
            entity.setPlatform(ErpPlatform.YXK);
            entity.setParameter(yesterdayStr);
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            chromeTaskInfoService.save(entity);
        } catch (Exception e) {
            log.error("addMaBanTask 出错了 e==",e);
            throw new RuntimeException("创建云星空爬虫任务出错:"+ e.getMessage());
        }

    }

    /**
     * 查询最新任务时间, 按ID倒序
     * @return 返回日期，如果从未生成任务，则返回当年第一天
     */
    private Date queryLatestTaskDate(String platform,String businessType) {
        LambdaQueryWrapper<ScheduleTaskEntity> queryWrapper = new LambdaQueryWrapper<ScheduleTaskEntity>();
        queryWrapper.eq(ScheduleTaskEntity::getPlatform,platform);
        queryWrapper.eq(ScheduleTaskEntity::getBusinessType,businessType);
        queryWrapper.orderByDesc(ScheduleTaskEntity::getId);
        queryWrapper.last("LIMIT 1");
        ScheduleTaskEntity result=chromeTaskInfoService.getOne(queryWrapper);
        Date initDate=DateUtil.beginOfYear(new Date());
        if (null!=result){
            initDate=result.getStartTime();
        }

        return initDate;
    }

    /**
     * 创建订单下载调度任务
     * @param platform  平台
     */
    private void createScheduleOrderTask(String platform){
        Date nextDate = queryLatestTaskDate(platform,BusinessType.ORDER);

        while(nextDate.compareTo(DateUtil.yesterday())<=0){
            //创建任务
            saveTask(platform,BusinessType.ORDER,nextDate);

            // 日期递增
            nextDate=DateUtil.offsetDay(nextDate,1);
        }

    }

    /**
     * 保存调度任务
     * @param platform  平台
     * @param businessType  业务类型
     * @param nextDate  日期
     */
    private void saveTask(String platform, String businessType, Date nextDate) {
        try {
            DateTime startTime=DateUtil.beginOfDay(nextDate);
            DateTime endTime=DateUtil.endOfDay(nextDate);
            Date nowDate=new Date();

            // 创建任务
            ScheduleTaskEntity entity=new ScheduleTaskEntity();
            entity.setPlatform(platform);
            entity.setBusinessType(businessType);
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            chromeTaskInfoService.save(entity);
        } catch (Exception e) {
            log.error("addMaBanTask 出错了 e==",e);
            throw new RuntimeException("创建管易云爬虫任务出错:"+ e.getMessage());
        }
    }

    public static void main(String[] args) {
        DateTime yearStartDay = DateUtil.beginOfYear(new Date());
        DateTime yearStartTime=DateUtil.beginOfDay(yearStartDay);
        String yearStartTimeStr=yearStartTime.toString("yyyy-MM-dd HH:mm:ss");
        System.out.println(yearStartTimeStr);
    }
}
