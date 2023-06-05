package com.erp.server.oms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoChangeDetailDTO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoChangeEntity;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoChangeService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.SoChangeDetailService;
import com.erp.server.oms.service.SoChangeService;
import com.erp.server.oms.service.SoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoServiceImpl
 * @Description TODO
 * @Date 2023-05-30 11:46
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeSoChangeServiceImpl implements SyncKingdeeSoChangeService {


    @Resource
    private SysUserFeign sysUserFeign;


    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoChangeDetailService soChangeDetailService;

    @Resource
    private SoChangeService soChangeService;

    @Resource
    private CustomerInfoService customerInfoService;

    /**
     * 销售变更单同步金碟
     *
     * @param entity
     * @param operate
     * @return void
     * @author yl
     * @date 2023-05-30 11:50
     */
    @Override
    public void syncDataToKingdee(SoChangeEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        String id = entity.getId();
        //业务id
        resultMap.put("id", id);
        //编码
        resultMap.put("code", entity.getCode());
        String soId = entity.getSoId();
        SoInfoDTO.CustomerDTO soInfo = soInfoService.getSoCustomer(soId);
        if (Objects.isNull(soInfo)) {
            return;
        }
        String salesDeptId = soInfo.getSalesDeptId();
        //获取部门id
        if (StringUtils.isNotBlank(salesDeptId)) {
            SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(salesDeptId);
            //销售部门
            if (!Objects.isNull(departmentDTO)) {
                resultMap.put("deptCode", departmentDTO.getCode());
            }
        }

        //客户id
        String customerId = soInfo.getCustomerId();
        //销售订单号
        resultMap.put("soCode", soInfo.getCode());
        resultMap.put("soId", soInfo.getId());
        resultMap.put("soKingdeeId","181079");
        //单据类型
        resultMap.put("orderType", "XSDDBGD01_SYS");
        //单据日期
        resultMap.put("billDate", entity.getBillDate());
        //客户
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            if (customerInfo != null) {
                resultMap.put("customerCode", customerInfo.getCode());
            }
        }



        //变更原因
        resultMap.put("remark", entity.getRemark());

        //销售员
        String sellerId = soInfo.getSellerId();
        //获取员工
        if (StringUtils.isNotBlank(sellerId)) {
            //获取员工 岗位信息
            KingdeePostDTO.UserKingdeePostInfoDTO userDTO = sysUserFeign.getUserKingdeePostByUserId(sellerId);
            //销售员
            if (!Objects.isNull(userDTO)) {
                resultMap.put("sellerCode",userDTO.getKingdeePostCode());
                resultMap.put("seller", userDTO.getUserName());
            }
        }

        //销售组织
        String salesOrgId = soInfo.getSalesOrgId();
        if (StringUtils.isNotBlank(salesOrgId)) {
            List<BaseIdDTO.CodeDTO> salesOrgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
            if (CollectionUtils.isNotEmpty(salesOrgList)) {
                resultMap.put("salesOrgCode", salesOrgList.get(0).getCode());
            }
        }

        List<SoChangeDetailDTO.ViewDTO> details = soChangeDetailService.listDetailByMainId(id);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }

        List<JSONObject> list = new ArrayList<>(details.size());
        for (SoChangeDetailDTO.ViewDTO item : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo", item.getSkuNo());
            jsonObject.set("soDetailKingdeeId","279939");
            jsonObject.set("changeType", item.getChangeType().getCode());
            jsonObject.set("oldQty", item.getOldQty());
            jsonObject.set("qty", item.getQty());
            jsonObject.set("baseQty", item.getQty());
            jsonObject.set("curInventoryQty", item.getQty());
            jsonObject.set("price", item.getPrice());
            jsonObject.set("oldPrice", item.getOldPrice());
            jsonObject.set("isGift", false);
            jsonObject.set("unit", "Pcs");
            list.add(jsonObject);
        }

        resultMap.put("detailList", list);
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SO_CHANGE_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return soChangeService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "", entity.getSyncOperate());
            }
            return Boolean.TRUE;
        });
    }
}
