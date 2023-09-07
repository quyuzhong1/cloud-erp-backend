package com.erp.server.oms.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncStatusEnum;
import com.erp.model.oms.entity.*;
import com.erp.server.oms.kingdee.*;
import com.erp.server.oms.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * 定时推送金蝶定时任务
 * @Author Luo_WG
 * @Date 2023/7/5 11:23
 **/
@Component
@Slf4j
@EnableScheduling
public class KingdeePushJob {
    @Resource
    private CustomerContactService customerContactService;
    @Resource
    private SyncKingdeeCustomerContactService syncKingdeeCustomerContactService;
    @Resource
    private CustomerGroupService customerGroupService;
    @Resource
    private SyncKingdeeCustomerGroupService syncKingdeeCustomerGroupService;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private SyncKingdeeCustomerService syncKingdeeCustomerService;
    @Resource
    private SoChangeService soChangeService;
    @Resource
    private SyncKingdeeSoChangeService syncKingdeeSoChangeService;
    @Resource
    private SoInfoService soInfoService;
    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;

    /**
     * 推送客户联系人信息
     */
    @XxlJob("kingdeePushCustomerContact")
    public void kingdeePushCustomerContact() {
        List<CustomerContactEntity> list = customerContactService.lambdaQuery()
                .in(CustomerContactEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(CustomerContactEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(CustomerContactEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的客户联系人信息");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeCustomerContactService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("客户联系人【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("客户联系人【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送客户分组
     */
    @XxlJob("kingdeePushCustomerGroup")
    public void kingdeePushCustomerGroup() {
        List<CustomerGroupEntity> list = customerGroupService.lambdaQuery()
                .in(CustomerGroupEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(CustomerGroupEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(CustomerGroupEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的客户分组");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeCustomerGroupService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("客户分组【{}】推送金蝶失败,error = {}",obj.getName(),e);
                log.error("客户分组【{}】推送金蝶失败",obj.getName(),e);
            }
        });
    }

    /**
     * 推送客户列表
     */
    @XxlJob("kingdeePushCustomerInfo")
    public void kingdeePushCustomerInfo() {
        List<CustomerInfoEntity> list = customerInfoService.lambdaQuery()
                .in(CustomerInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(CustomerInfoEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(CustomerInfoEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的客户列表");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeCustomerService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("客户【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("客户【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送销售变更
     */
    @XxlJob("kingdeePushSoChange")
    public void kingdeePushSoChange() {
        List<SoChangeEntity> list = soChangeService.lambdaQuery()
                .in(SoChangeEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SoChangeEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SoChangeEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售变更");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSoChangeService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("销售变更【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("销售变更【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }

    /**
     * 推送销售订单
     */
    @XxlJob("kingdeePushSoInfo")
    public void kingdeePushSoInfo() {
        List<SoInfoEntity> list = soInfoService.lambdaQuery()
                .in(SoInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncStatusEnum.TO_BE_SYNC.getCode(), SyncStatusEnum.FAILED_SYNC.getCode()))
                .or(obj -> obj.eq(SoInfoEntity::getSyncKingdeeStatus, SyncStatusEnum.IN_SYNC.getCode()).le(SoInfoEntity::getSyncKingdeeTime, LocalDateTime.now().minusMinutes(10)))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售订单");
            return;
        }
        list.forEach(obj->{
            try {
                syncKingdeeSoService.syncDataToKingdee(obj, obj.getSyncOperate());
            } catch (Exception e) {
                XxlJobHelper.log("销售订单【{}】推送金蝶失败,error = {}",obj.getCode(),e);
                log.error("销售订单【{}】推送金蝶失败",obj.getCode(),e);
            }
        });
    }
}
