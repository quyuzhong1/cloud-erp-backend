package com.erp.server.oms.kingdee.impl;

import cn.hutool.json.JSONObject;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.CustomerAddressEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
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
import java.math.BigDecimal;
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

    @Resource
    private WmsTaskFeign wmsTaskFeign;

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
                resultMap.put("deptCode", departmentDTO.getCode());
            }
        }
        //获取员工
        if (StringUtils.isNotBlank(sellerId)) {
            //获取员工 岗位信息
            KingdeePostDTO.UserKingdeePostInfoDTO userDTO = sysUserFeign.getUserKingdeePostByUserId(sellerId);
            //销售员
            if (!Objects.isNull(userDTO)) {
                resultMap.put("sellerCode", userDTO.getKingdeePostCode());
                resultMap.put("seller", userDTO.getUserName());
            }
        }
        //销售组织
        String salesOrgId = entity.getSalesOrgId();
        String currency = entity.getCurrency();
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(currency));
        //结算币别
        String currencyCode = currencyList.stream().filter(c -> c.getId().equals(currency)).findFirst().
                map(CurrencyDTO.ViewDTO::getKingdeeCode).orElse("");
        //银行手续费
        BigDecimal bankServiceFee = entity.getBankServiceFee();
        resultMap.put("bankServiceFee", bankServiceFee);

        //运费金额
        BigDecimal shippingFee = entity.getShippingFee();
        resultMap.put("shippingFee", shippingFee);

        //是否含税
        Boolean isTax = entity.getIsTax();
        Map<String, Object> finance = new HashMap<>();
        finance.put("FExchangeRate", 1);
        finance.put("FIsIncludedTax", isTax);
        finance.put("FSettleCurrId.FNumber", currencyCode);
        resultMap.put("finance", finance);

        //库存组织
        String warehouseOrgId = entity.getWarehouseOrgId();
        List<String> orgIdList = new ArrayList<>(2);
        orgIdList.add(warehouseOrgId);
        orgIdList.add(salesOrgId);
        String warehouseOrgCode = "";
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
                resultMap.put("customerCode", customerInfo.getCode());
            }

        }
        String warehouseId = entity.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        String kingdeeWarehouseCode = "";
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            kingdeeWarehouseCode = warehouseList.get(0).getKingdeeWarehouseCode();
        }
        //联系电话
        resultMap.put("telNumber", entity.getTelNumber());
        //收货人
        resultMap.put("receiverName", entity.getReceiverName());
        /**
         * todo
         * 先写死
         * 收款账号
         * 收款方式
         */
        String receiveAddressId = entity.getReceiveAddressId();
        CustomerAddressEntity addressEntity = customerAddressService.getById(receiveAddressId);
        String receiveAddressCode = addressEntity != null ? addressEntity.getCode() : "";
        String receiveAddress = addressEntity != null ? addressEntity.getAddress() : "";

        //收货地址
        resultMap.put("receiveAddress", receiveAddress);
        //交货地点
        resultMap.put("receiveAddressCode", receiveAddressCode);
        List<SoDetailDTO.ViewDTO> details = soDetailService.listByMainId(id, warehouseId);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //要货日期
        LocalDate requireDate = entity.getRequireDate();
        List<JSONObject> list = new ArrayList<>(details.size());
        for (SoDetailDTO.ViewDTO item : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo", item.getSkuNo());
            jsonObject.set("requireDate", requireDate);
            jsonObject.set("qty", item.getQty());
            jsonObject.set("baseQty", item.getQty());
            jsonObject.set("price", item.getPrice());
            jsonObject.set("taxPrice", item.getTaxPrice());
            jsonObject.set("taxRate", item.getTaxRate());
            jsonObject.set("isGift", item.getIsGift());
            jsonObject.set("amount", item.getAmount());
            jsonObject.set("unit", item.getUnit());
            jsonObject.set("warehouseOrgCode", warehouseOrgCode);
            jsonObject.set("curInventoryQty", item.getQty());
            jsonObject.set("stockBaseQty", item.getQty());
            jsonObject.set("kingdeeWarehouseCode", kingdeeWarehouseCode);
            jsonObject.set("remark", item.getRemark());
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
