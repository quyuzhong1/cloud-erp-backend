//package com.erp.server.plm.schedule;
//
//import com.erp.server.plm.service.NoticeMessageService;
//import com.xxl.job.core.handler.annotation.XxlJob;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//
///**
// * 来源xxljob
// * plm 定时任务
// *
// * @Classname PlmJob
// * @Description TODO
// * @Date 2022-11-16 10:25
// * @Created by yl
// */
//@Component
//@Slf4j
//public class PlmJob {
//
//
//    @Autowired
//    private NoticeMessageService noticeMessageService;
//
//    /**
//     * 生成发送任务预警通知 每天17:00
//     */
// //   @XxlJob("sendTaskEarlyWarning")
//    public void sendEarlyWarning() {
//        noticeMessageService.sendEarlyWarning();
//    }
//}
