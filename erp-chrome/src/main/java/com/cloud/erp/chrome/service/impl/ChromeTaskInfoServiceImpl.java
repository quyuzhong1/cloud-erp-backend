package com.cloud.erp.chrome.service.impl;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.cloud.erp.chrome.constant.BusinessType;
import com.cloud.erp.chrome.constant.ErpPlatform;
import com.cloud.erp.chrome.constant.TaskState;
import com.cloud.erp.chrome.dto.FindTaskDTO;
import com.cloud.erp.chrome.dto.GyyParamDTO;
import com.cloud.erp.chrome.dto.GyySearchParamDTO;
import com.cloud.erp.chrome.dto.MabangOrderParamDTO;
import com.cloud.erp.chrome.entity.ScheduleTaskEntity;
import com.cloud.erp.chrome.mapper.ChromeTaskInfoMapper;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.exception.ServiceException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-08-25
 */
@Service
public class ChromeTaskInfoServiceImpl extends ServiceImpl<ChromeTaskInfoMapper, ScheduleTaskEntity> implements ChromeTaskInfoService {

    @Override
    public void createOrderTask(String platform) {
        Date nextDate = queryLatestTaskDate(platform, BusinessType.ORDER);

        while(nextDate.compareTo(DateUtil.yesterday())<=0){
            //创建任务
            saveTask(platform,BusinessType.ORDER,nextDate);

            // 日期递增
            nextDate=DateUtil.offsetDay(nextDate,1);
        }

    }

    /**
     * 查询最新任务时间, 按ID倒序
     * @return 返回日期，如果从未生成任务，则返回当年第一天
     */
    private Date queryLatestTaskDate(String platform,String businessType) {
        Date nextDate=DateUtil.beginOfYear(new Date());

        LambdaQueryWrapper<ScheduleTaskEntity> queryWrapper = new LambdaQueryWrapper<ScheduleTaskEntity>();
        queryWrapper.eq(ScheduleTaskEntity::getPlatform,platform);
        queryWrapper.eq(ScheduleTaskEntity::getBusinessType,businessType);
        queryWrapper.orderByDesc(ScheduleTaskEntity::getEndTime);
        queryWrapper.last("LIMIT 1");
        ScheduleTaskEntity result=this.getOne(queryWrapper);

        if (null!=result){
            nextDate=result.getEndTime();
        }

        return nextDate;
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
            DateTime endTime=DateUtil.offsetDay(startTime,1);
            Date nowDate=new Date();

            // 创建任务
            ScheduleTaskEntity entity=new ScheduleTaskEntity();
            entity.setPlatform(platform);
            entity.setBusinessType(businessType);
            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            this.save(entity);
        } catch (Exception e) {
            log.error("addMaBanTask 出错了 e==",e);
            throw new RuntimeException("创建管易云爬虫任务出错:"+ e.getMessage());
        }
    }

    /**
     * 获取任务列表
     * @param dto   查询过滤参数
     * @return  任务列表
     */
    @Override
    public synchronized List<ScheduleTaskEntity> getChromeTaskList(FindTaskDTO  dto) {
        List<ScheduleTaskEntity> resultList=new ArrayList<>();

        List<String> list= Arrays.asList(ErpPlatform.GYY,ErpPlatform.MABANG,ErpPlatform.YXK);
        if(!list.contains(dto.getPlatform())){
            throw new ServiceException(1,"平台类型有误");
        }
        LambdaQueryWrapper<ScheduleTaskEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScheduleTaskEntity::getPlatform,dto.getPlatform());
        queryWrapper.le(ScheduleTaskEntity::getRetryCount,10);
        queryWrapper.and(st->st
            .eq(ScheduleTaskEntity::getTaskStatus,TaskState.NOT_START)
            .or(i->i
                .eq(ScheduleTaskEntity::getTaskStatus,TaskState.TAKEN)
                .lt(ScheduleTaskEntity::getUpdateTime,DateUtil.offsetMinute(new Date(),-10))
            )
        );
        queryWrapper.orderByAsc(ScheduleTaskEntity::getId);
        queryWrapper.last("LIMIT 1");
//        System.out.println(queryWrapper.getTargetSql());

        List<ScheduleTaskEntity> taskList=baseMapper.selectList(queryWrapper);
        for(ScheduleTaskEntity task:taskList){
            if (ErpPlatform.MABANG.equals(task.getPlatform())){
                task.setParameter(getTaskParamForMabang(task));
            } else if(ErpPlatform.GYY.equalsIgnoreCase(task.getPlatform())){
                task.setParameter(getTaskParamForGanYiYun(task));
            } else {
                task.setParameter(getTaskParamForYunXingKong(task));
            }
            //更新状态锁定
            this.updateTaskState(task.getId(),TaskState.TAKEN);
        }

        return taskList;
    }

    /**
     * 获取马帮任务参数信息
     * @param task  任务信息
     * @return  参数信息
     */
    private String getTaskParamForMabang(ScheduleTaskEntity task) {
        String startTimeStr= DateUtil.format(task.getStartTime(),"yyyy-MM-dd HH:mm:ss");
        String endTimeStr=DateUtil.format(task.getEndTime(),"yyyy-MM-dd HH:mm:ss");

        //参数对象
        MabangOrderParamDTO param = new MabangOrderParamDTO();
        param.setExpresstimeTimeStart(startTimeStr);
        param.setExpresstimeTimeEnd(endTimeStr);

        return JSONObject.toJSONString(param);
    }

    /**
     * 获取管易云任务参数信息
     * @param task  任务信息
     * @return  参数信息
     */
    private String getTaskParamForGanYiYun(ScheduleTaskEntity task) {
        final String fieldsText="付款时间,制单时间,发货时间,订单编号,店铺类型,订单类型,店铺名称,平台单号,会员名称,会员代码,商品类别,商品税率,商品代码,商品名称,商品简称,数量,总重量,代理售价,成本单价,折扣,标准单价,标准金额,实际单价,实际金额,让利金额,让利后金额,成本总价,物流费用,分销商物流费用,物流成本,商品标准利润,商品实际利润,订单编号,到账,到账时间,买家备注,卖家备注,二次备注,订单标记,仓库名称,业务员,物流公司,物流单号,地区信息,收货地址,商品税额,商品重量,总体积,是否货到付款,到付金额,买家支付金额,平台支付金额,外仓单据,唯一码总实际进价,平台商品名称,平台规格名称,明细备注,其他服务费";
        final String fieldsName="paytimeStr,orderCreateDateStr,deliveryDateStr,code,shopType,orderTypeName,shopName,platformCode,vipName,vipCode,categoryName,taxRateStr,itemCode,itemName,simpleName,qtyStr,totalWeightStr,agentPriceStr,costPriceStr,discountStr,originPriceStr,originAmountStr,priceStr,detailAmountStr,discountFeeStr,amountAfterStr,costTotalStr,postFeeStr,distributionPostFeeStr,postCostStr,itemOriginProfitStr,itemActualProfitStr,orderCode,arrivalStr,arrivalTimeStr,buyerMemo,sellerMemo,sellerMemoLate,tagName,warehouseName,businessManName,expressName,mailNo,areaName,receiverAddress,taxAmountStr,weightStr,volumeStr,codStr,codFeeStr,buyPayment,platformPayment,wmsStr,uniqueCostPriceStr,platformItemName,platformSkuName,memo,otherServiceFeeStr";

        String startTimeStr=DateUtil.format(task.getStartTime(),"yyyy-MM-dd HH:mm:ss");
        String endTimeStr=DateUtil.format(task.getEndTime(),"yyyy-MM-dd HH:mm:ss");

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

        return JSONObject.toJSONString(dto);
    }

    /**
     * 获取云星空任务参数信息
     * @param task  任务信息
     * @return  参数信息
     */
    private String getTaskParamForYunXingKong(ScheduleTaskEntity task) {
        return DateUtil.format(task.getStartTime(),"yyyy-MM-dd");
    }

    /**
     * 修改任务状态
     * @author yl
     * @date 2022-08-26 9:38
     * @param taskId
     * @param status
     * @return void
     */
    @Override
    public void updateTaskState(Integer taskId, Integer status) {
        this.updateTaskState(taskId,status,null);
    }

    /**
     * 修改任务状态
     * @author yl
     * @date 2022-08-26 9:38
     * @param taskId
     * @param status
     * @param remark 备注
     * @return void
     */
    @Override
    public void updateTaskState(Integer taskId, Integer status,String remark) {
        ScheduleTaskEntity queryEntity=this.getById(taskId);
        Integer retryCount=queryEntity==null?0:queryEntity.getRetryCount()+1;

        UpdateWrapper<ScheduleTaskEntity> updateWrapper=new UpdateWrapper<>();
        updateWrapper.lambda().eq(ScheduleTaskEntity::getId,taskId);
        updateWrapper.lambda().set(ScheduleTaskEntity::getTaskStatus, status);
        updateWrapper.lambda().set(ScheduleTaskEntity::getUpdateTime,new Date());
        updateWrapper.lambda().set(remark!=null,ScheduleTaskEntity::getRemark,remark);
        updateWrapper.lambda().set(status.equals(TaskState.TAKEN),ScheduleTaskEntity::getRetryCount,retryCount);
        this.update(updateWrapper);
    }

}
