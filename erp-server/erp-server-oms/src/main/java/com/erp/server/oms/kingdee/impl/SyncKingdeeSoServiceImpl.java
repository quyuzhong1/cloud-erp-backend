package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.StrUtils;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.erp.model.dmp.entity.BiOrderInfoEntity;
import com.erp.model.dmp.entity.BiOrderItemSplitEntity;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.SoDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeeOperatorRefPostDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.convert.SoInfoConverter;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private BankAccountService bankAccountService;

    @Resource
    private KingdeeReceiptConditionService kingdeeReceiptConditionService;

    @Resource
    private PlmTaskFeign productDetailService;
    
    @Resource
    private OmsPushMsgService omsPushMsgService;

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
    public DmpPushTaskEntity syncDataToKingdee(SoInfoEntity entity, String operate) {
        //生成任务
       return saveTask(entity,operate,this.newSyncDataToKingdee(entity, operate));
    }

    /**
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     * @param entity
     * @param operate
     * @param resultMap
     */
    private DmpPushTaskEntity saveTask (SoInfoEntity entity, String operate, Map<String, Object> resultMap) {
    	SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
        		.eq(CfgSettingEntity::getKey, SourceTypeEnum.SO_INFO.getCode())
        		.eq(CfgSettingEntity::getType, settingEnum.getType())
        		.eq(CfgSettingEntity::getValue, "1")
        		.list();
        if(CollUtil.isEmpty(list)) {
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
            
            return  dmpMqFeign.saveTask(taskFeignDTO);
        }
        
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setSourceId(entity.getId());
        omsPushMsgEntity.setSourceCode(entity.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        omsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgService.save(omsPushMsgEntity);
        
        return null;
    }

    /**
     * 推送订单到mq
     *
     * @param entity
     * @param syncOperate
     */
    @Override
    public void syncOrderToDmp(SoInfoEntity entity, String syncOperate) {

        if (Boolean.FALSE.equals(dmpTaskFeign.needPushMQ(LocalDateTime.now()))) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id", entity.getId());
        resultMap.put("operate", syncOperate);
        BiOrderInfoEntity biOrderInfoEntity = this.orderDataConvert(entity);
        resultMap.put("entity", biOrderInfoEntity);


        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.SO_INFO.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_SO_INFO_ORDER_TO_DMP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.APPROVED_SO_INFO_ORDER_TO_DMP_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP_OMS.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc());
        taskFeignDTO.setSyncOperate(syncOperate);
        DmpPushTaskEntity dmpPushTaskEntity = dmpMqFeign.saveTask(taskFeignDTO);

        //推送DMP
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.sendTask(Collections.singletonList(dmpPushTaskEntity));
            }
        });

        log.info("推送消息开始：{}", taskFeignDTO.toString());
    }


    private BiOrderInfoEntity orderDataConvert(SoInfoEntity soInfoEntity) {
        BiOrderInfoEntity biOrderInfoEntity = SoInfoConverter.INSTANCE.soInfoToDmpOrder(soInfoEntity);
        List<SoDetailEntity> soDetailEntities = soDetailService.listSoDetailByMainId(soInfoEntity.getId());
        SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
        BigDecimal exchangeRate;
        if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getExchangeRate())) {
            exchangeRate = detailEntity.getExchangeRate();
        } else {
            exchangeRate = BigDecimal.ONE;
        }
        //订单业务字段设置
        if (Objects.nonNull(soInfoEntity.getInvalidStatus()) && Boolean.TRUE.equals(soInfoEntity.getInvalidStatus())) {
            biOrderInfoEntity.setOrderStatus(5);
        } else {
            //默认待配货
            biOrderInfoEntity.setOrderStatus(1);
        }
        biOrderInfoEntity.setPaidTime(Objects.nonNull(soInfoEntity.getReceiveDate()) ? soInfoEntity.getReceiveDate().atStartOfDay() : null);
        CustomerInfoEntity customerInfo = null;
        try {
            customerInfo = customerInfoService.getCustomerById(soInfoEntity.getCustomerId());
            if (Objects.nonNull(customerInfo)) {
                biOrderInfoEntity.setBuyerName(customerInfo.getName());
                biOrderInfoEntity.setBuyerUserId(customerInfo.getCode());
                biOrderInfoEntity.setShopName(customerInfo.getName());
                biOrderInfoEntity.setShopNo(customerInfo.getCode());
            }
        } catch (Exception e) {
            log.error("请求erp-oms customerFeign.getCustomerById异常:{}", e.getMessage());
        }


        BigDecimal itemTotal = BigDecimal.ZERO;
        BigDecimal itemTotalOrigin = BigDecimal.ZERO;
        BigDecimal orderCost = BigDecimal.ZERO;
        BigDecimal itemTotalCost = BigDecimal.ZERO;
        soDetailEntities.stream().forEach(
                soDetailEntity -> {
                    orderCost.add(Optional.ofNullable(soDetailEntity.getSaleCost()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
                    itemTotal.add(Optional.ofNullable(soDetailEntity.getPrice()).orElse(BigDecimal.ZERO)
                            .multiply(BigDecimal.valueOf(Optional.ofNullable(soDetailEntity.getQty()).orElse(0))).multiply(exchangeRate));
                    itemTotalOrigin.add(Optional.ofNullable(soDetailEntity.getTaxAmountBefore()).orElse(BigDecimal.ZERO));
                    itemTotalCost.add(Optional.ofNullable(soDetailEntity.getSaleCost()).orElse(BigDecimal.ZERO));
                }
        );
        //订单成本价
        biOrderInfoEntity.setOrderCost(orderCost);
        biOrderInfoEntity.setCurrencyRate(exchangeRate);
        biOrderInfoEntity.setItemTotal(itemTotal);
        //先计算运费收入（原币）
        if (Optional.ofNullable(soInfoEntity.getIsCollectShippingFee()).isPresent()) {
            biOrderInfoEntity.setShippingTotalOrigin(soInfoEntity.getShippingFee());
        } else {
            biOrderInfoEntity.setShippingTotalOrigin(BigDecimal.ZERO);
        }
        //运费收入（本位币）
        biOrderInfoEntity.setShippingFee(biOrderInfoEntity.getShippingTotalOrigin().multiply(exchangeRate));
        //商品销售总金额(原币)
        biOrderInfoEntity.setItemTotalOrigin(itemTotalOrigin);
        //商品总成本(原币)
        biOrderInfoEntity.setItemTotalCost(itemTotalCost);
        //国家字典
        if (Objects.nonNull(customerInfo) && com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(customerInfo.getCountryId())) {
            try {
                DictCountryEntity country = sysUserFeign.getCountryById(customerInfo.getCountryId());
                if (Objects.nonNull(country)) {
                    biOrderInfoEntity.setCountryNameCn(country.getNameCn());
                    biOrderInfoEntity.setCountryNameEn(country.getNameEn());
                    biOrderInfoEntity.setSite(country.getId());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.getCountryById {}异常：{}", customerInfo.getCountryId(), e.getMessage());
            }
        }
        //平台创建时间
        biOrderInfoEntity.setCreateTime(LocalDateTime.now());
        //部门名称
        if (com.alibaba.nacos.common.utils.StringUtils.isNotEmpty(soInfoEntity.getSalesDeptId())) {
            try {
                List<SysDepartmentEntity> dept = sysUserFeign.listDeptByIds(Collections.singletonList(soInfoEntity.getSalesDeptId()));
                if (CollUtil.isNotEmpty(dept)) {
                    biOrderInfoEntity.setDeptName(dept.get(0).getName());
                }
            } catch (Exception e) {
                log.error("erp-sys sysUserFeign.listDeptByIds {}异常：{}", soInfoEntity.getSalesDeptId(), e.getMessage());

            }
        }

        biOrderInfoEntity.setCnySettleRate(exchangeRate);
        biOrderInfoEntity.setSourceId(soInfoEntity.getId());
        //订单明细
        List<BiOrderItemSplitEntity> orderItemEntities = new ArrayList<>(soDetailEntities.size());
        //明细字段转换
        if (CollUtil.isNotEmpty(soDetailEntities)) {
            soDetailEntities.forEach(soDetailEntity -> {
                BiOrderItemSplitEntity biOrderItemSplitEntity = SoInfoConverter.INSTANCE.soDetailToDmpOrderItem(soDetailEntity);
                biOrderItemSplitEntity.setOrderId(biOrderInfoEntity.getId());
                if (StringUtils.isNotEmpty(soDetailEntity.getSkuId())) {
                    List<ProductDetailEntity> detailEntityList = productDetailService.getByIdList(Collections.singletonList(soDetailEntity.getSkuId()));
                    if (CollectionUtils.isNotEmpty(detailEntityList)) {
                        biOrderItemSplitEntity.setItemName(detailEntityList.get(0).getName());
                        biOrderItemSplitEntity.setPictureUrl(detailEntityList.get(0).getImagesUrl());
                        biOrderItemSplitEntity.setSpecifics(detailEntityList.get(0).getVariantProperty());
                    }
                }
                biOrderItemSplitEntity.setCostPrice(Optional.ofNullable(soDetailEntity.getPurchasePrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
                biOrderItemSplitEntity.setSellPrice(Optional.ofNullable(soDetailEntity.getPrice()).orElse(BigDecimal.ZERO).multiply(exchangeRate));
                biOrderItemSplitEntity.setStockWarehouseId(soInfoEntity.getWarehouseId());
                biOrderItemSplitEntity.setAmountAfter(Optional.ofNullable(soDetailEntity.getTaxAmountBefore()).orElse(BigDecimal.ZERO).subtract(Optional.ofNullable(soDetailEntity.getDiscountAmount()).orElse(BigDecimal.ZERO)));
                orderItemEntities.add(biOrderItemSplitEntity);
            });
        }
        biOrderInfoEntity.setItemList(orderItemEntities);
        return biOrderInfoEntity;
    }

	@Override
	public Map<String, Object> newSyncDataToKingdee(SoInfoEntity entity, String operate) {
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
            return resultMap;
        }

        String warehouseId = entity.getWarehouseId();
        List<SoDetailDTO.ViewDTO> details = soDetailService.listByMainId(id, warehouseId);
        if (CollectionUtils.isEmpty(details)) {
            throw new ServiceException("未找到销售订单明细");
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
        resultMap.put("seller", entity.getSellerName());

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
        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(salesOrgId);
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingdeeSeller = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingdeeSeller)) {
                deptCode=kingdeeSeller.getDeptCode();
                resultMap.put("sellerCode", kingdeeSeller.getUserPostCode());
            }
        }
        resultMap.put("deptCode", deptCode);
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

        List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.RECEIVE_METHOD.getType());
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
        KingdeeReceiptConditionEntity receiptCondition = kingdeeReceiptConditionService.getById(entity.getReceiveCondition());
        if (Objects.nonNull(receiptCondition)) {
            resultMap.put("receiveCondition", receiptCondition.getCode());
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
        String receiveAccount = entity.getReceiveAccount();
        if (StrUtils.isNotEmpty(receiveAccount)) {
            BankAccountEntity bankAccount = bankAccountService.getById(receiveAccount);
            if (Objects.nonNull(bankAccount)) {
                resultMap.put("receiveAccount", bankAccount.getBankAccountNo());
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
        return resultMap;
	}

}
