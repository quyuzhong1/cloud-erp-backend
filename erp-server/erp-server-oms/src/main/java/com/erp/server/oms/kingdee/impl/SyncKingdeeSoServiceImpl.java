package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.*;
import com.common.business.enums.SourceTypeEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.DmpOrderInfoEntity;
import com.erp.model.dmp.enums.PlatformEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * @author Lambda
 * @Classname SyncKingdeeSoServiceImpl
 * @Date 2023-05-30 11:46
 * @Created by yl
 */
@Slf4j
@Service
public class SyncKingdeeSoServiceImpl implements SyncKingdeeSoService {


    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private CustomerAddressService customerAddressService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private WmsTaskFeign wmsTaskFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;
    @Autowired
    private DictBasicService dictBasicService;

    @Autowired
    private BankAccountService bankAccountService;
    @Resource
    private MQProducerService mqProducerService;
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
    @GlobalTransactional
    @Transactional(rollbackFor = Exception.class)
    public void syncDataToKingdee(SoInfoEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());

        String id = entity.getId();
        //业务id
        resultMap.put("id", id);
        //编码
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            sendMqAndSaveTask(entity,operate,resultMap);
            return;
        }

        String warehouseId = entity.getWarehouseId();
        List<SoDetailDTO.ViewDTO> details = soDetailService.listByMainId(id, warehouseId);
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        //交货方式
        resultMap.put("deliveryMode", entity.getDeliveryMode());
        //单据类型
        resultMap.put("orderType", entity.getOrderType());
        LocalDate createDate = entity.getCreateTime().toLocalDate();
        LocalDate billDate = entity.getBillDate();
        if (billDate != null) {
            createDate = billDate;
        }
        //创建日期
        resultMap.put("createDate", LocalDateTimeUtil.format(createDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
        //是否收取运费
        resultMap.put("isCollectShippingFee", entity.getIsCollectShippingFee());

        //销售组织
        String salesOrgId = entity.getSalesOrgId();

        //库存组织
        String warehouseOrgId = entity.getWarehouseOrgId();
        List<String> orgIdList = new ArrayList<>(2);
        orgIdList.add(warehouseOrgId);
        orgIdList.add(salesOrgId);
        List<BaseIdDTO.CodeDTO> orgList = sysUserFeign.getAccountingCompanyList(orgIdList);
        //销售组织的金蝶code
        String salesOrgCode = orgList.stream().filter(o -> o.getId().equals(salesOrgId)).
                map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");

        //销售员
        String sellerId = entity.getSellerId();
        String deptCode = "";

        //当为空的时候 就取岗位表的
        KingdeePostDTO.FindUserKingdeePostInfoDTO findUserPostKingdee = new KingdeePostDTO.FindUserKingdeePostInfoDTO();
        findUserPostKingdee.setUserId(sellerId);
        findUserPostKingdee.setOrgCode(salesOrgCode);
        KingdeePostDTO.UserKingdeePostInfoDTO kingdeePost = kingdeeFeign.getUserKingdeePost(findUserPostKingdee);
        if (kingdeePost != null) {
            deptCode = kingdeePost.getKingdeeDeptCode();
        }
        resultMap.put("deptCode", deptCode);


        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(salesOrgCode);
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeBusinessOperatorEntity kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getKingdeePostCode());
                resultMap.put("seller", kingSellerInfo.getKingdeeUserName());
            }
        }

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
        resultMap.put("isTax", isTax);
        resultMap.put("currencyCode", currencyCode);
        resultMap.put("discountAmount", Objects.nonNull(entity.getDiscountAmount()) ? entity.getDiscountAmount() : BigDecimal.ZERO);
        if (StringUtils.isNotBlank(salesOrgCode)) {
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        String warehouseOrgCode = orgList.stream().filter(o -> o.getId().equals(warehouseOrgId)).
                map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");

        //客户
        String customerId = entity.getCustomerId();
        if (StringUtils.isNotBlank(customerId)) {
            CustomerInfoEntity customerInfo = customerInfoService.getById(customerId);
            if (customerInfo != null) {
                resultMap.put("customerCode", customerInfo.getCode());
            }

        }

        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        String kingdeeWarehouseCode = "";
        if (CollectionUtils.isNotEmpty(warehouseList)) {
            kingdeeWarehouseCode = warehouseList.get(0).getKingdeeWarehouseCode();
        }
        //联系电话
        resultMap.put("telNumber", entity.getTelNumber());
        //收货人
        resultMap.put("receiverName", entity.getReceiverName());

        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType(), DictBasicTypeEnum.COLLECTION_TERMS.getType());
        List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);
        Map<String, List<DictBasicEntity>> dictBasicMap = dictBasicEntityList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));

        // 收款方式
        List<DictBasicEntity> receiveMethodList = dictBasicMap.get(DictBasicTypeEnum.RECEIVE_METHOD.getType());
        if (CollectionUtils.isNotEmpty(receiveMethodList) && StrUtils.isNotEmpty(entity.getReceiveMethod())) {
            DictBasicEntity dictBasicEntity = receiveMethodList.stream().filter(obj -> Objects.equals(obj.getValue(), entity.getReceiveMethod())).findFirst().orElse(null);
            if (Objects.nonNull(dictBasicEntity)) {
                resultMap.put("receiveMethod", dictBasicEntity.getRemark());
            }
        }
        // 收款条件
        List<DictBasicEntity> receiveConditionList = dictBasicMap.get(DictBasicTypeEnum.COLLECTION_TERMS.getType());
        if (CollectionUtils.isNotEmpty(receiveConditionList) && StrUtils.isNotEmpty(entity.getReceiveCondition())) {
            DictBasicEntity dictBasicEntity = receiveConditionList.stream().filter(obj -> Objects.equals(obj.getValue(), entity.getReceiveCondition())).findFirst().orElse(null);
            if (Objects.nonNull(dictBasicEntity)) {
                resultMap.put("receiveCondition", dictBasicEntity.getRemark());
            }
        }

        // 收款日期
        if (Objects.nonNull(entity.getReceiveDate())) {
            resultMap.put("receiveDate", LocalDateTimeUtil.format(entity.getReceiveDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
        }
        // 收款金额
        if (Objects.nonNull(entity.getReceiveAmount())) {
            resultMap.put("receiveAmount", entity.getReceiveAmount());
        }
        // 收款账号
        if (StrUtils.isNotEmpty(entity.getReceiveAccount())) {
            List<BankAccountEntity> bankAccountList = bankAccountService.findByOrgIdAndAccountNo(entity.getSalesOrgId(), entity.getReceiveAccount());
            if (CollUtil.isNotEmpty(bankAccountList)) {
                resultMap.put("receiveAccount", entity.getReceiveAccount());
            }
        }

        //报关费 贸易条件
        BigDecimal customsFee = entity.getCustomsFee();
        resultMap.put("customsFee", customsFee);
        String tradeTerm = entity.getTradeTerm();
        resultMap.put("tradeTerm", tradeTerm);
        String receiveAddressId = entity.getReceiveAddressId();
        CustomerAddressEntity addressEntity = customerAddressService.getById(receiveAddressId);
        String receiveAddressCode = addressEntity != null ? addressEntity.getCode() : "";
        String receiveAddress = addressEntity != null ? addressEntity.getAddress() : "";

        //收货地址
        resultMap.put("receiveAddress", receiveAddress);
        //交货地点
        resultMap.put("receiveAddressCode", receiveAddressCode);
        BigDecimal exchangeRate=Objects.isNull(details.get(0).getExchangeRate())||details.get(0).getExchangeRate().compareTo(BigDecimal.ZERO)==0? MathUtil.BigDecimal_1:details.get(0).getExchangeRate();
        //汇率
        resultMap.put("exchangeRate",exchangeRate);
        //要货日期
        LocalDate requireDate = entity.getRequireDate();
        List<JSONObject> list = new ArrayList<>(details.size());
        for (SoDetailDTO.ViewDTO item : details) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.set("skuNo", item.getSkuNo());
            jsonObject.set("requireDate", LocalDateTimeUtil.format(requireDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")) );
            jsonObject.set("qty", item.getQty());
            jsonObject.set("baseQty", item.getQty());
            //对应金蝶含税单价
            jsonObject.set("taxPrice", item.getTaxPrice());
            jsonObject.set("taxRate", item.getTaxRate());
            jsonObject.set("isGift", item.getIsGift());
            //结算组织
            jsonObject.set("settleOrgCode", salesOrgCode);
            BigDecimal discountAmount = Objects.nonNull(item.getDiscountAmount()) ? item.getDiscountAmount() : BigDecimal.ZERO;
            jsonObject.set("amount", item.getAmount().add(discountAmount).setScale(4,BigDecimal.ROUND_HALF_UP));
            //单位
            String unit = item.getUnit();
            jsonObject.set("unit", StringUtils.isNotBlank(unit) ? unit : "Pcs");
            jsonObject.set("warehouseOrgCode", warehouseOrgCode);
            jsonObject.set("curInventoryQty", item.getQty());
            jsonObject.set("stockBaseQty", item.getQty());
            jsonObject.set("kingdeeWarehouseCode", kingdeeWarehouseCode);
            jsonObject.set("remark", item.getRemark());
            jsonObject.set("detailDiscountAmount", discountAmount);
            list.add(jsonObject);
        }

        resultMap.put("detailList", list);
        //生成任务
        sendMqAndSaveTask(entity,operate,resultMap);
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private void sendMqAndSaveTask (SoInfoEntity entity, String operate, Map<String, Object> resultMap) {
        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.KINGDEE_SO_INFO_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
        taskFeignDTO.setSyncOperate(operate);
        dmpMqFeign.sendMqAndSaveTask(taskFeignDTO);
    }

    /**
     * 推送订单到mq
     *
     * @param soInfoEntity
     * @param syncOperate
     */
    @Override
    public void syncOrderToDmp(SoInfoEntity soInfoEntity, String syncOperate) {
        //判断是否需要推送记录
        if (!dmpTaskFeign.needPushMQ(LocalDateTime.now())){
            return;
        }
        //推送同步中台dmp任务
        DmpPullTaskFeignDTO dto = new DmpPullTaskFeignDTO()
                .setMqData(JSON.toJSONString(soInfoEntity))
                .setMqTopic(RocketMqTopic.SYNC_SO_INFO_ORDER_TO_DMP_TOPIC)
                .setMqTag(RocketMqTagEnum.APPROVED_SO_INFO_ORDER_TO_DMP_TAG.getName())
                .setSourceCode(soInfoEntity.getCode())
                .setSourceId(soInfoEntity.getId())
                .setSourceType(SourceTypeEnum.SO_INFO.getCode())
                .setSourcePlatformName(PlatformEnum.ERP_OMS.getDesc())
                .setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc())
                .setSyncOperate(syncOperate);
        log.info("推送消息开始：{}", dto.toString());
        //推送mq
        String dmpPullTaskId = dmpTaskFeign.savePullTask(dto);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("dmpPullTaskId", dmpPullTaskId);
        //业务id
        resultMap.put("id", soInfoEntity.getId());
        //客户编号
        resultMap.put("code", soInfoEntity.getCode());
        resultMap.put("operate", syncOperate);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mqProducerService.syncClassMsg(RocketMqTopic.SYNC_SO_INFO_ORDER_TO_DMP_TOPIC, RocketMqTagEnum.APPROVED_SO_INFO_ORDER_TO_DMP_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                log.error("soReturn.syncDataToDmp 推送MQ失败 :" + resultMap.get("id"));
            }
            return Boolean.TRUE;
        });
    }
}
