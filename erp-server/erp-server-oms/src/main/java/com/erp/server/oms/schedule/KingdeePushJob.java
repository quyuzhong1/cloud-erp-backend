package com.erp.server.oms.schedule;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.erp.model.oms.entity.*;
import com.erp.model.wms.entity.MachineInfoEntity;
import com.erp.server.oms.kingdee.*;
import com.erp.server.oms.service.*;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
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
    private SoReturnService soReturnService;
    @Resource
    private SyncKingdeeSoReturnService syncKingdeeSoReturnService;
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
                .in(CustomerContactEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的客户联系人信息");
            return;
        }
        list.forEach(obj->{
            syncKingdeeCustomerContactService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

    /**
     * 推送客户分组
     */
    @XxlJob("kingdeePushCustomerGroup")
    public void kingdeePushCustomerGroup() {
        List<CustomerGroupEntity> list = customerGroupService.lambdaQuery()
                .in(CustomerGroupEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的客户分组");
            return;
        }
        list.forEach(obj->{
            syncKingdeeCustomerGroupService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

    /**
     * 推送客户列表
     */
    @XxlJob("kingdeePushCustomerInfo")
    public void kingdeePushCustomerInfo() {
        List<CustomerInfoEntity> list = customerInfoService.lambdaQuery()
                .in(CustomerInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的客户列表");
            return;
        }
        list.forEach(obj->{
            syncKingdeeCustomerService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }

    /**
     * 推送销售变更
     */
    @XxlJob("kingdeePushSoChange")
    public void kingdeePushSoChange() {
        List<SoChangeEntity> list = soChangeService.lambdaQuery()
                .in(SoChangeEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售变更");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSoChangeService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }


    /**
     * 推送销售退货单
     */
    @XxlJob("kingdeePushSoReturn")
    public void kingdeePushSoReturn() {
        List<SoReturnEntity> list = soReturnService.lambdaQuery()
                .in(SoReturnEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售退货单");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSoReturnService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }



    /**
     * 推送销售订单
     */
    @XxlJob("kingdeePushSoInfo")
    public void kingdeePushSoInfo() {
        List<SoInfoEntity> list = soInfoService.lambdaQuery()
                .in(SoInfoEntity::getSyncKingdeeStatus, Arrays.asList(SyncKingdeeStatusEnum.TO_BE_SYNC.getCode(), SyncKingdeeStatusEnum.FAILED_SYNC.getCode()))
                .list();
        if (ObjectUtils.isEmpty(list)) {
            log.info("无需要同步的销售退货单");
            return;
        }
        list.forEach(obj->{
            syncKingdeeSoService.syncDataToKingdee(obj, obj.getSyncOperate());
        });
    }
}
