package com.cloud.erp.chrome.service.jobhandler;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson2.JSONObject;
import com.cloud.erp.chrome.constant.BusinessType;
import com.cloud.erp.chrome.constant.ErpPlatform;
import com.cloud.erp.chrome.constant.TimeType;
import com.cloud.erp.chrome.dto.GyyShipmentsParamDTO;
import com.cloud.erp.chrome.dto.GyyShipmentsSearchParamDTO;
import com.cloud.erp.chrome.dto.MabangOrderParamDTO;
import com.cloud.erp.chrome.entity.ChromeTaskInfoEntity;
import com.cloud.erp.chrome.service.ChromeTaskInfoService;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.ptg.MemAreaPtg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname TaskJob
 * @Description TODO
 * @Date 2022-08-25 16:37
 * @Created by yl
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
     * @author yl
     * @date 2022-08-30 14:37
     * @param
     * @return void
     */
    @XxlJob("addMabangTask")
    public void addMaBanTask() {
        try {
            MabangOrderParamDTO dto = new MabangOrderParamDTO();
            DateTime yesterday = DateUtil.yesterday();
            //获取开始时间
            DateTime startTime=DateUtil.beginOfDay(yesterday);
            DateTime endTime=DateUtil.endOfDay(yesterday);
            String startTimeStr=startTime.toString("yyyy-MM-dd HH:mm:ss");
            String endTimeStr=endTime.toString("yyyy-MM-dd HH:mm:ss");
            dto.setExpresstimeTimeStart(startTimeStr);
            dto.setExpresstimeTimeEnd(endTimeStr);
            ChromeTaskInfoEntity entity=new ChromeTaskInfoEntity();
            entity.setBusinessType(BusinessType.ORDER);
            entity.setParameter(JSONObject.toJSONString(dto));
            entity.setPlatform(ErpPlatform.MABANG);
            Date nowDate=DateUtil.date();
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            chromeTaskInfoService.save(entity);
        } catch (Exception e) {
           log.error("addMaBanTask 出错了 e==",e);
        }

    }

     /**
      * 生成管易云的任务  每天 00:15
      * @author yl
      * @date 2022-08-30 14:37
      * @param
      * @return void
      */
    @XxlJob("addGyyTask")
    public void addGyyTask() {
        try {
            GyyShipmentsParamDTO dto = new GyyShipmentsParamDTO();
            DateTime yesterday = DateUtil.yesterday();
            //获取开始时间
            DateTime startTime=DateUtil.beginOfDay(yesterday);
            DateTime endTime=DateUtil.endOfDay(yesterday);
            String startTimeStr=startTime.toString("yyyy-MM-dd HH:mm:ss");
            String endTimeStr=endTime.toString("yyyy-MM-dd HH:mm:ss");
            dto.setFieldsName(fieldsName);
            dto.setFieldsText(fieldsText);
            //这个是对应需要的参数
            GyyShipmentsSearchParamDTO searchParam=new GyyShipmentsSearchParamDTO();
            searchParam.setBeginTime(startTimeStr);
            searchParam.setEndTime(endTimeStr);
            dto.setSearchParams(searchParam);
            ChromeTaskInfoEntity entity=new ChromeTaskInfoEntity();
            entity.setBusinessType(BusinessType.SHIPMENTS);
            entity.setParameter(JSONObject.toJSONString(dto));
            entity.setPlatform(ErpPlatform.GYY);
            Date nowDate=DateUtil.date();
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            chromeTaskInfoService.save(entity);
        } catch (Exception e) {
            log.error("addMaBanTask 出错了 e==",e);
        }

    }


  /**
   * 生成云星空任务 每天00:05
   * @author yl
   * @date 2022-08-30 14:35
   * @param
   * @return void
   */
    @XxlJob("addYxkTask")
    public void addYxkTask() {
        try {
            ChromeTaskInfoEntity entity=new ChromeTaskInfoEntity();
            entity.setBusinessType(BusinessType.YXKORDER);
            //对应的参数 默认昨天的
            entity.setParameter(TimeType.YESTERDAY);
            entity.setPlatform(ErpPlatform.YXK);
            Date nowDate=DateUtil.date();
            entity.setCreateTime(nowDate);
            entity.setUpdateTime(nowDate);
            chromeTaskInfoService.save(entity);
        } catch (Exception e) {
            log.error("addMaBanTask 出错了 e==",e);
        }

    }
}
