package com.erp.server.oms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.service.CustomerAddressService;
import com.erp.server.oms.service.CustomerInfoService;
import com.erp.server.oms.service.SoDetailService;
import com.erp.server.oms.service.SoInfoService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
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
public class SyncKingdeeSoServiceImpl implements SyncKingdeeSoService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SoInfoService soInfoService;

    /**
     * 销售订单同步金碟
     *
     * @param entity
     * @param operate
     * @return void
     * @author yl
     * @date 2023-05-30 11:50
     */
    @Override
    public void syncDataToKingdee(SoInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        String id = entity.getId();
        //业务id
        resultMap.put("id", id);
        //编码
        resultMap.put("code", entity.getCode());

        //交货方式
        resultMap.put("deliveryMode", entity.getDeliveryMode());
        //单据类型
        resultMap.put("orderType", entity.getOrderType());
        //创建日期
        resultMap.put("createDate", entity.getCreateTime().toLocalDate());
        //是否收取运费
        resultMap.put("isCollectShippingFee", entity.getIsCollectShippingFee());

        String salesDeptId = entity.getSalesDeptId();
        //销售员
        String sellerId = entity.getSellerId();
        //获取部门id
        if (StringUtils.isNotBlank(salesDeptId)) {
            SysDepartmentDTO departmentDTO = sysUserFeign.getUserDeptById(salesDeptId);
            //销售部门
            if (!Objects.isNull(departmentDTO)) {
                resultMap.put("deptCode", "BM000062");
            }
        }
        //获取员工
        if (StringUtils.isNotBlank(sellerId)) {
            FindUserDTO userDTO = sysUserFeign.getUserByUserId(sellerId);
            //销售员
            if (!Objects.isNull(userDTO)) {
                resultMap.put("sellerCode", "0095_GW000020_1");
                resultMap.put("seller", userDTO.getUserName());
            }
        }
        //销售组织
        String salesOrgId = entity.getSalesOrgId();
        //库存组织
        String warehouseOrgId = entity.getWarehouseOrgId();
        List<String> orgIdList = new ArrayList<>(2);
        orgIdList.add(warehouseOrgId);
        orgIdList.add(salesOrgId);
        String warehouseOrgCode = "100";
        if (CollectionUtils.isNotEmpty(orgIdList)) {
            List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(Arrays.asList(salesOrgId));
            String salesOrgCode = orgList.stream().filter(o -> o.getId().equals(salesOrgId)).
                    map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");
            if (StringUtils.isNotBlank(salesOrgCode)) {
                resultMap.put("salesOrgCode", salesOrgCode);
            }
             warehouseOrgCode = orgList.stream().filter(o -> o.getId().equals(warehouseOrgId)).
                    map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("100");

        }


        //客户
        String customerId = entity.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            if (customerInfo != null) {
                resultMap.put("customerCode", "CUST0051");
            }
        }
        //联系电话
        resultMap.put("telNumber", entity.getTelNumber());
        //收货人
        resultMap.put("receiverName", entity.getReceiverName());
        String receiveAddressId = entity.getReceiveAddressId();
        CustomerAddressEntity addressEntity = customerAddressService.getById(receiveAddressId);
        String receiveAddress = addressEntity != null ? addressEntity.getAddress() : "";
        //收货地址
        resultMap.put("receiveAddress", receiveAddress);
        List<SoDetailDTO.ViewDTO> details = soDetailService.listByMainId(id, entity.getWarehouseId());
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //要货日期
        LocalDate requireDate = entity.getRequireDate();
        List<JSONObject> list = new ArrayList<>(details.size());
        for (SoDetailDTO.ViewDTO item : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo", "0009");
            jsonObject.set("requireDate", requireDate);
            jsonObject.set("qty", item.getQty());
            jsonObject.set("price", item.getPrice());
            jsonObject.set("taxPrice", item.getTaxPrice());
            jsonObject.set("isGift", item.getIsGift());
            jsonObject.set("unit", item.getUnit());
            jsonObject.set("warehouseOrgCode",warehouseOrgCode);
            jsonObject.set("curInventoryQty",55);
            list.add(jsonObject);
        }

        resultMap.put("detailList", list);
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SO_INFO_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return soInfoService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "", entity.getSyncOperate());
            }
            return Boolean.TRUE;
        });
    }
}
