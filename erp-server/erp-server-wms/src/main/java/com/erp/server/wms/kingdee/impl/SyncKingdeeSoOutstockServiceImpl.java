package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.dto.CfgSettingDTO;
import com.erp.model.dmp.dto.DmpSoOutstockDTO;
import com.erp.model.dmp.dto.DmpSoOutstockDetailDTO;
import com.erp.model.dmp.entity.BiDeliveryDetailInfoEntity;
import com.erp.model.dmp.entity.BiDeliveryDetailItemEntity;
import com.erp.model.dmp.entity.CfgSettingEntity;
import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.SettingEnum;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.*;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.scm.feign.ScmTaskFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.convert.SoOutstockConverter;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 同步金蝶销售出库单
 *
 * @Author Luo_WG
 * @Date 2023/4/24 11:22
 **/
@Slf4j
@Service
public class SyncKingdeeSoOutstockServiceImpl implements SyncKingdeeSoOutstockService {

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private ScmTaskFeign scmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private KingdeeFeign kingdeeFeign;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private DmpMqFeign dmpMqFeign;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private WmsPushMsgService wmsPushMsgService;

    /**
     * 发送消息同步金蝶
     *
     * @param entity
     * @param operate
     * @return void
     * @Author Luo_WG
     * @Date 2023/4/24 11:27
     **/
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncDataToKingdee(SoOutstockEntity entity, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncB2cDataToKingdee(entity, operate));
    	}
    }


    /**
     * 同步金蝶到
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-27 16:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncB2cDataToKingdee(SoOutstockEntity entity, String operate) {
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncB2cDataToKingdee(entity, operate));
    	}
    }


    /**
     * 同步金蝶到
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-27 16:51
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public DmpPushTaskEntity syncWdtDataToKingdee(SoOutstockEntity entity, String operate) {
        //生成任务
    	if(!SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		return saveTask(entity, operate, DmpOutputConstant.getQuerySyncMap());
    	}else {
    		return saveTask(entity, operate, this.newSyncB2cDataToKingdee(entity, operate));
    	}
    }

    /**
     * @param entity
     * @param operate
     * @param resultMap
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     */
    private DmpPushTaskEntity saveTask(SoOutstockEntity entity, String operate, Map<String, Object> resultMap) {
        SettingEnum settingEnum = SettingEnum.NEW_DMP_PUSH_SWTICH_LIST;
        List<CfgSettingEntity> list = FeignQuery.create(CfgSettingEntity.class)
                .eq(CfgSettingEntity::getKey, SourceTypeEnum.SO_OUTSTOCK.getCode())
                .eq(CfgSettingEntity::getType, settingEnum.getType())
                .eq(CfgSettingEntity::getValue, "1")
                .list();
        if (CollUtil.isEmpty(list)) {
            //添加推送任务
            DmpPushTaskFeignDTO dmpSyncTaskDTO = new DmpPushTaskFeignDTO();
            dmpSyncTaskDTO.setSourceId(entity.getId());
            dmpSyncTaskDTO.setSourceCode(entity.getCode());
            dmpSyncTaskDTO.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
            dmpSyncTaskDTO.setMqTopic(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC);
            dmpSyncTaskDTO.setMqTag(RocketMqTagEnum.KINGDEE_SO_OUTSTOCK_TAG.getName());
            dmpSyncTaskDTO.setMqData(JSONUtil.toJsonStr(resultMap));
            dmpSyncTaskDTO.setSourcePlatformName(PlatformEnum.ERP.getDesc());
            dmpSyncTaskDTO.setTargetPlatformName(PlatformEnum.KINGDEE.getDesc());
            dmpSyncTaskDTO.setSyncOperate(operate);
            // B2C订单不推送到金蝶
            if (OrderTypeEnum.B2C.getCode().equalsIgnoreCase(entity.getOrderType())) {
                dmpSyncTaskDTO.setParentId("");
            } else {
                dmpSyncTaskDTO.setParentId(entity.getSoId());
            }
            log.info("2.销售出库单增加旺店通的物流渠道名称：" + JSONUtil.toJsonStr(dmpSyncTaskDTO));
            return dmpMqFeign.saveTask(dmpSyncTaskDTO);
        }

        WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
        wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.KINGDEE.getCode());
        wmsPushMsgEntity.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        wmsPushMsgEntity.setSourceId(entity.getId());
        wmsPushMsgEntity.setSourceCode(entity.getCode());
        wmsPushMsgEntity.setSyncOperate(operate);
        wmsPushMsgEntity.setPushData(JSON.toJSONString(resultMap));
        if (!OrderTypeEnum.B2C.getCode().equalsIgnoreCase(entity.getOrderType())) {
            wmsPushMsgEntity.setParentId(entity.getSoId());
        }
        log.info("2.销售出库单增加旺店通的物流渠道名称：" + JSONUtil.toJsonStr(wmsPushMsgEntity));
        wmsPushMsgService.save(wmsPushMsgEntity);

        return null;
    }

    /**
     * 推送订单到mq
     *
     * @param entity
     * @param syncOperate
     */
    @Async("wmsErpExecutor")
    @Override
    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    public void syncOrderToDmp(SoOutstockEntity entity, String syncOperate) {
        //判断是否需要推送记录
        if (!dmpTaskFeign.needPushMQ(LocalDateTime.now())) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id", entity.getId());
        resultMap.put("operate", syncOperate);
        BiDeliveryDetailInfoEntity biDeliveryDetailInfoEntity = this.outStockDataConvert(entity);
        resultMap.put("entity", biDeliveryDetailInfoEntity);

        //无需推送
        if (ObjectUtils.isEmpty(biDeliveryDetailInfoEntity)) {
            return;
        }

        //添加推送任务
        DmpPushTaskFeignDTO taskFeignDTO = new DmpPushTaskFeignDTO();
        taskFeignDTO.setSourceId(entity.getId());
        taskFeignDTO.setSourceCode(entity.getCode());
        taskFeignDTO.setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode());
        taskFeignDTO.setMqTopic(RocketMqTopic.SYNC_SO_OUTSTOCK_ORDER_TO_DMP_TOPIC);
        taskFeignDTO.setMqTag(RocketMqTagEnum.APPROVED_SO_OUTSTOCK_ORDER_TO_DMP_TAG.getName());
        taskFeignDTO.setMqData(JSONUtil.toJsonStr(resultMap));
        taskFeignDTO.setSourcePlatformName(PlatformEnum.ERP_WMS.getDesc());
        taskFeignDTO.setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc());
        taskFeignDTO.setSyncOperate(syncOperate);

        DmpPushTaskEntity dmpPushTaskEntity = dmpMqFeign.saveTask(taskFeignDTO);

        //推送DMP
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter() {
            @Override
            public void afterCommit() {
                dmpMqFeign.delayLevel3SendTask(Collections.singletonList(dmpPushTaskEntity));
            }
        });

        log.info("推送消息开始：{}", taskFeignDTO.toString());
    }

    /**
     * 销售出货单字段转换
     *
     * @param soOutstockEntity
     * @return
     */
    private BiDeliveryDetailInfoEntity outStockDataConvert(SoOutstockEntity soOutstockEntity) {
        String soId = "";
        String soCode = "";
        String receiveAddress = "";
        String currency = "";
        String remark = "";
        BigDecimal shippingFee = BigDecimal.ZERO;
        BiDeliveryDetailInfoEntity entity = SoOutstockConverter.INSTANCE.soOutstockToDmpDelivery(soOutstockEntity);
        BigDecimal exchangeRate;
        entity.setDeliveryDate(Objects.nonNull(soOutstockEntity.getActualDeliveryDate()) ? soOutstockEntity.getActualDeliveryDate() : null);

        if (OrderTypeEnum.B2B.getCode().equalsIgnoreCase(soOutstockEntity.getOrderType())) {
            SoInfoEntity soInfoEntity = soInfoFeign.getSoInfoById(soOutstockEntity.getSoId());
            if (Objects.isNull(soInfoEntity) || SourceTypeEnum.SAL_OUTSTOCK.getCode().equals(soOutstockEntity.getSourceType())) {
                log.warn("销售出库单：" + soOutstockEntity.getCode() + "未找到上游订单获取原始B2B订单异常:[" + soOutstockEntity.getSourceId() + "]");
                return null;
            }
            CustomerInfoEntity customerInfo = customerFeign.getCustomerById(soOutstockEntity.getCustomerId());
            if (Objects.nonNull(customerInfo)) {
                entity.setShopName("B2B");
                entity.setShopNo("B2B");
                entity.setCustomerName(customerInfo.getName());
            }

            if (Objects.nonNull(soInfoEntity)) {
                soId = soInfoEntity.getId();
                soCode = soInfoEntity.getCode();
                receiveAddress = soInfoEntity.getReceiveAddress();
                currency = soInfoEntity.getCurrency();
                remark = soInfoEntity.getRemark();
                shippingFee = soInfoEntity.getShippingFee();


                //详情
                List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainId(soInfoEntity.getId());
                if (CollUtil.isNotEmpty(soDetailEntities)) {
                    SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
                    if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getExchangeRate())) {
                        exchangeRate = detailEntity.getExchangeRate();
                    } else {
                        exchangeRate = BigDecimal.ONE;
                    }
                    entity.setCurrencyRate(exchangeRate);
                    //运费收入（本位币）
                    entity.setShippingFee(BigDecimal.ZERO);
                    AtomicReference<BigDecimal> itemTotalCost = new AtomicReference<>(BigDecimal.ZERO);
                    AtomicReference<BigDecimal> orderTotalCost = new AtomicReference<>(BigDecimal.ZERO);
                    soDetailEntities.stream().forEach(
                            soDetailEntity -> {
                                BigDecimal saleCost = Optional.ofNullable(soDetailEntity.getSaleCost()).orElse(BigDecimal.ZERO);
                                itemTotalCost.set(MathUtil.add(saleCost, itemTotalCost.get()));
                                BigDecimal amount = Optional.ofNullable(soDetailEntity.getAmount()).orElse(BigDecimal.ZERO);
                                orderTotalCost.set(MathUtil.add(amount, orderTotalCost.get()));
                            }
                    );
                    entity.setItemTotalCost(itemTotalCost.get());
                    entity.setOrderTotalCost(orderTotalCost.get());
                }
            }
        } else if (OrderTypeEnum.B2C.getCode().equalsIgnoreCase(soOutstockEntity.getOrderType())) {
            if (ObjectUtil.isNotEmpty(soOutstockEntity.getSoId())) {

                SoB2cDTO.ViewDTO view = soB2cFeign.view(soOutstockEntity.getSoId());
                if (Objects.isNull(view) || SourceTypeEnum.SAL_OUTSTOCK.getCode().equals(soOutstockEntity.getSourceType())) {
                    log.warn("销售出库单：" + soOutstockEntity.getCode() + "未找到上游订单获取原始B2C订单异常:[" + soOutstockEntity.getSourceId() + "]");
                    return null;
                }

                if (Objects.nonNull(view)) {
                    soId = view.getId();
                    soCode = view.getCode();
                    receiveAddress = view.getReceiverDTO().getFirstAddress();
                    currency = view.getCurrency();
                    remark = view.getRemark();

                    List<SoB2cDetailDTO.ViewDTO> detailList = view.getDetailList();
                    BigDecimal itemTotalCost = detailList.stream().map(req -> req.getAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                    entity.setItemTotalCost(itemTotalCost);
                    entity.setOrderTotalCost(view.getAmount());
                    entity.setShopName(view.getShopName());
                    entity.setShopNo(view.getShopId());
                    entity.setCustomerName(view.getReceiverDTO().getReceiverName());

                }
            }
        }


        entity.setOrderNo(soCode);
        entity.setManStreet(receiveAddress);
        entity.setCurrencyCode(currency);
        entity.setRemark(remark);
        entity.setShippingFee(shippingFee);
        entity.setCompanyId(soOutstockEntity.getTrackNo());
        entity.setCompanyId(soOutstockEntity.getSalesDeptId());
        entity.setCompanyName(soOutstockEntity.getSalesOrgName());
        entity.setCountryNameCn(soOutstockEntity.getCountry());
        entity.setCountryNameEn(soOutstockEntity.getCountry());

        entity.setOrderTotalCost(Optional.ofNullable(soOutstockEntity.getTotalDiscountAmount()).orElse(BigDecimal.ZERO).add(Optional.ofNullable(entity.getItemTotalCost()).orElse(BigDecimal.ZERO)));

        //销售部门
        if (StringUtils.isNotEmpty(soOutstockEntity.getSalesDeptId())) {

            List<SysDepartmentEntity> dept = sysUserFeign.listDeptByIds(Collections.singletonList(soOutstockEntity.getSalesDeptId()));
            if (CollUtil.isNotEmpty(dept)) {
                entity.setSaleDeptName(dept.get(0).getName());
            }
        }

        //获取销售出库单详情
        List<SoOutstockDetailEntity> details = soOutstockDetailService.listByMainIds(Collections.singletonList(soOutstockEntity.getId()));

        //明细字段转换
        if (CollUtil.isEmpty(details)) {
            throw new ServiceException(ApiError.ERROR_92029);
        }
        //订单明细
        List<BiDeliveryDetailItemEntity> orderItemEntities = new ArrayList<>(details.size());
        String finalSoCode = soCode;
        String finalSoId = soId;

        List<String> soDetailIds = details.stream().map(req -> req.getSoDetailId()).distinct().collect(Collectors.toList());
        //B2C订单信息
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cFeign.listDetailByMainIds(soDetailIds);

        //B2B订单信息
        List<SoDetailEntity> soDetailEntities = soInfoFeign.listSoDetailByMainIds(soDetailIds);

        List<String> skuIdList = details.stream().map(SoOutstockDetailEntity::getSkuId).collect(Collectors.toList());
        Map<String, ProductDetailEntity> idProductDetailMap = FeignQuery.getByIds(ProductDetailEntity.class, skuIdList)
                .stream().collect(Collectors.toMap(ProductDetailEntity::getId, c -> c));
        details.forEach(soOutstockDetailEntity -> {
            BiDeliveryDetailItemEntity dmpOrderItemEntity = SoOutstockConverter.INSTANCE.soOutstockToDmpDeliveryItem(soOutstockDetailEntity);
            dmpOrderItemEntity.setDeliveryDetailId(entity.getId());
            dmpOrderItemEntity.setSaleOrderNo(finalSoId);
            dmpOrderItemEntity.setPlatformOrderId(finalSoCode);
            dmpOrderItemEntity.setStockName(soOutstockEntity.getWarehouseName());
            ProductDetailEntity productDetailEntity = idProductDetailMap.get(soOutstockDetailEntity.getSkuId());
            if (productDetailEntity != null) {
                dmpOrderItemEntity.setItemName(productDetailEntity.getName());
                dmpOrderItemEntity.setItemId(productDetailEntity.getProductId());
                dmpOrderItemEntity.setProductUnit(productDetailEntity.getUnitName());
                dmpOrderItemEntity.setSpecifics(productDetailEntity.getVariantProperty());

                //B2C订单
                SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getSoDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soB2cDetailEntity)) {
                    dmpOrderItemEntity.setCostPrice(soB2cDetailEntity.getAmount());
                    dmpOrderItemEntity.setSellPrice(soB2cDetailEntity.getPrice());
//                    dmpOrderItemEntity.setIsGift(soB2cDetailEntity.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 1 : 2);
                    dmpOrderItemEntity.setAmount(soB2cDetailEntity.getAmount());
                }

                //B2B订单
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getSoDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soDetailEntity)) {
                    dmpOrderItemEntity.setCostPrice(soDetailEntity.getSaleCost());
                    dmpOrderItemEntity.setSellPrice(soDetailEntity.getPrice());
//                    dmpOrderItemEntity.setIsGift(soDetailEntity.getIsGift() ? 1 : 2);
                    dmpOrderItemEntity.setAmount(soDetailEntity.getAmount());
                }
            }

            orderItemEntities.add(dmpOrderItemEntity);
        });
        entity.setDetails(orderItemEntities);
        return entity;
    }


    @Override
    public Map<String, Object> newSyncDataToKingdee(SoOutstockEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //退货单号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }

        //获取销售出库单详情
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        //销售单信息
        SoInfoEntity soInfoById = soInfoFeign.getSoInfoById(entity.getSoId());
        //销售单明细
        List<SoDetailEntity> soDetailEntitieList = new ArrayList<>();
        if (CharSequenceUtil.isNotBlank(soInfoById.getId())) {
            soDetailEntitieList = soInfoFeign.listSoDetailByMainIds(Collections.singletonList(soInfoById.getId()));
        }

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(soInfoById.getSalesOrgId(), entity.getWarehouseOrgId()));
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Collections.singletonList(entity.getCustomerId()));

        //查询供应商信息
        SupplierEntity supplierEntity = null;
        if (CharSequenceUtil.isNotBlank(entity.getCarrierId())) {
            supplierEntity = scmTaskFeign.getSupplierById(entity.getCarrierId());
        }
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Collections.singletonList(soInfoById.getCurrency()));
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Collections.singletonList(entity.getWarehouseId()));

        //单据类型
        resultMap.put("orderType", entity.getOrderType());
        //单据日期
        resultMap.put("billDate", Objects.nonNull(entity.getBillDate()) ? LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDateTimeUtil.format(entity.getCreateTime().toLocalDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //销售组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(soInfoById.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        //客户
        if (CollectionUtils.isNotEmpty(customerInfoEntitieList)) {
            CustomerInfoEntity customerInfoEntity = customerInfoEntitieList.stream().filter(obj -> obj.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            resultMap.put("customerCode", customerInfoEntity.getCode());
            resultMap.put("customerName", customerInfoEntity.getName());
            String platformType = customerInfoEntity.getPlatformType();
            PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(customerInfoEntity.getPlatformType());
            String salesPlatformCode = salesPlatformEnum != null ? salesPlatformEnum.getKingdeeCode() : "";
            //平台类型
            resultMap.put("platformType", salesPlatformCode);
            if(StringUtils.isNotBlank(platformType)) {
            	List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                        .eq(DictBasicEntity::getType, DictBasicTypeEnum.SDY_SUB_PLATFORM.getType())
                        .eq(DictBasicEntity::getName, platformType)
                        .list();
            	if(CollUtil.isNotEmpty(dictBasicEntityList)) {
            		resultMap.put("sdyPlatformType", dictBasicEntityList.get(0).getRemark());
            	}
            }
        }

//        //部门
//        if (CharSequenceUtil.isNotBlank(soInfoById.getSalesDeptId())) {
//            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
//            dto.setDeptId(entity.getSalesDeptId());
//            dto.setOrgId(entity.getSalesOrgId());
//            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
//            if (ObjectUtil.isNotEmpty(deptKingdee)) {
//                resultMap.put("salesDeptCode", deptKingdee.getKingdeeDeptCode());
//            }
//        }

        //销售员
        String sellerId = entity.getSellerId();
        String salesDeptId = entity.getSalesDeptId();
        //获取业务员信息
        if (CharSequenceUtil.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(soInfoById.getSalesOrgId());
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setSalesDeptId(salesDeptId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getUserPostCode());
                resultMap.put("seller", kingSellerInfo.getUserName());
                resultMap.put("salesDeptCode", kingSellerInfo.getDeptCode());
            }
        }
        //销售员
        String warehouseKeeperId = entity.getWarehouseKeeperId();
        if (CharSequenceUtil.isNotBlank(warehouseKeeperId)) {
            String warehouseOrgId = entity.getWarehouseOrgId();
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(warehouseOrgId);
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.WHY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingInfo)) {
                resultMap.put("warehouseKeeperCode", kingInfo.getUserPostCode());
            }
        }

        String billDate = soInfoById.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String currency = CharSequenceUtil.isNotBlank(soInfoById.getCurrency()) ? soInfoById.getCurrency() : "CNY";
        //汇率
        BigDecimal exchangeRate = dmpTaskFeign.getRate(billDate, currency);
        if (Objects.isNull(exchangeRate)) {
            exchangeRate = MathUtil.BigDecimal_1;
        }
        //汇率
        resultMap.put("exchangeRate", exchangeRate);
        //运输单号
        resultMap.put("trackNo", entity.getTrackNo());
        //销售单号
        resultMap.put("soCode", entity.getSoCode());
        //销售单金蝶id
        resultMap.put("soSyncKingdeeId", soInfoById.getSyncKingdeeId());
        //发货组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String warehouseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("warehouseOrgCode", warehouseOrgCode);
        }
        //承运商
        if (ObjectUtil.isNotEmpty(supplierEntity)) {
            resultMap.put("carrierCode", supplierEntity.getCode());
        }
        //2024.09.11 jack 同步物流渠道名称到金蝶销售出库单的物流渠道
        resultMap.put("logisticsChannelName", entity.getLogisticsChannelName());
        //订单标签
        resultMap.put("tradeLabel", entity.getTradeLabel());
        //来源单号
        resultMap.put("F_Ulz_ConsignNum", entity.getSourceCode());
        //————————————————————财务信息SubHeadEntity——————————————————————
        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soInfoById.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());
        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(soInfoById.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        if (soInfoById.getDiscountAmount() != null) {
            resultMap.put("FAllDisCount", entity.getTotalDiscountAmount());
        }

        List<String> soDetailIds = soDetailEntitieList.stream().map(SoDetailEntity::getId).collect(Collectors.toList());
        List<SoDeliveryNoticeDetailEntity> noticeDetailEntities = soDeliveryNoticeDetailService.listDetailBySourceDetailIds(soDetailIds);
        //发货通知详情id
        List<String> noticeDetailIds = noticeDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
        soDetailIds.addAll(noticeDetailIds);
        List<SoOutstockDetailDTO.DeliveryQtyDTO> deliveryQtyDTOS = soOutstockDetailService.listDetailBySoDetailIds(soDetailIds);
        //————————————————————物料信息——————————————————————

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Collections.singletonList(entity.getWarehouseId()));

        List<Map<String, Object>> fEntityList = new ArrayList<>();
        List<String> soKingdeeDetailIdList = soDetailEntitieList.stream().map(req -> req.getKingdeeDetailId()).collect(Collectors.toList());
        resultMap.put("soKingdeeDetailIds", String.join(",", soKingdeeDetailIdList));
        for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
            Map<String, Object> map = new HashMap<>();
            map.put("customerPO", detailEntity.getCustomerPO());
            map.put("skuNo", detailEntity.getSkuNo());
            map.put("actualQty", detailEntity.getActualQty());
            SoOutstockDetailDTO.DeliveryQtyDTO deliveryQtyDTO = deliveryQtyDTOS.stream().filter(req -> req.getId().equals(detailEntity.getId())).findFirst().orElse(new SoOutstockDetailDTO.DeliveryQtyDTO());
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(deliveryQtyDTO.getSoDetailId())).findFirst().orElse(new SoDetailEntity());
            map.put("salesQty", soDetailEntity.getQty());
            map.put("planQty", detailEntity.getPlanQty());
            map.put("price", soDetailEntity.getPrice());

            //含税单价
            BigDecimal flagTaxRate = MathUtil.divide(soDetailEntity.getTaxRate(), MathUtil.BigDecimal_100);
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(soDetailEntity.getPrice(), multiplyTax);
            //含税单价
            map.put("taxPrice", taxPrice);
            map.put("amount", soDetailEntity.getAmount());
            map.put("isGift", soDetailEntity.getIsGift());
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                String warehouseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(soInfoById.getWarehouseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
                map.put("warehouseOrgCode", warehouseOrgCode);
            }
            map.put("taxRate", soDetailEntity.getTaxRate());
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
                map.put("warehouseCode", warehouseCode);
            }
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            }
            map.put("remark", detailEntity.getRemark());
            //销售订单金蝶id
            map.put("soSyncKingdeeId", soDetailEntity.getKingdeeDetailId());
            map.put("FSrcType", "SAL_SaleOrder");
            map.put("FSrcBillNo", soInfoById.getCode());
            map.put("FSoorDerno", soInfoById.getCode());

            List<Map<String, Object>> mapList = new ArrayList<>();
            Map<String, Object> mapPush = new HashMap<>();
            mapPush.put("soKingdeeDetailId", soDetailEntity.getKingdeeDetailId());
            mapPush.put("soSyncKingdeeId", soInfoById.getSyncKingdeeId());
            mapList.add(mapPush);
            //销售单金蝶明细id
            map.put("FEntity_Link", mapList);
            fEntityList.add(map);
        }
        resultMap.put("FEntity", fEntityList);
        return resultMap;
    }


    @Override
    public Map<String, Object> newSyncWdtDataToKingdee(SoOutstockEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //退货单号
        resultMap.put("code", entity.getCode());
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //获取销售出库单详情
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        String sellerId = entity.getSellerId();
        String deptId = entity.getSalesDeptId();
//        SysDepartmentUserNumberDTO deptUser = null;
//        String deptId = "";
//        if (CharSequenceUtil.isNotBlank(sellerId)) {
//            deptUser = sysUserFeign.getDeptByUserId(sellerId);
//        }
//        if (Objects.nonNull(deptUser)) {
//            deptId = deptUser.getDepartmentId();
//        }

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getSalesOrgId(), entity.getWarehouseOrgId()));
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Collections.singletonList(entity.getCustomerId()));

        //部门信息
//        SysDepartmentEntity dept = CharSequenceUtil.isNotBlank(deptId) ? sysUserFeign.getUserDeptById(deptId) : null;
        //查询供应商信息
        SupplierEntity supplierEntity = null;
        if (CharSequenceUtil.isNotBlank(entity.getCarrierId())) {
            supplierEntity = scmTaskFeign.getSupplierById(entity.getCarrierId());
        }
        //币别
        String currency = "CNY";
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Collections.singletonList(currency));
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Collections.singletonList(entity.getWarehouseId()));
        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Collections.singletonList(entity.getWarehouseKeeperId()));
        //单据类型
        resultMap.put("orderType", entity.getOrderType());
        //单据日期
        resultMap.put("billDate", Objects.nonNull(entity.getBillDate()) ? LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDateTimeUtil.format(entity.getCreateTime().toLocalDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //销售组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        //客户
        if (CollectionUtils.isNotEmpty(customerInfoEntitieList)) {
            CustomerInfoEntity customerInfoEntity = customerInfoEntitieList.stream().filter(obj -> obj.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            resultMap.put("customerCode", customerInfoEntity.getCode());
            resultMap.put("customerName", customerInfoEntity.getName());
            String platformType = customerInfoEntity.getPlatformType();
            PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(platformType);
            String salesPlatformCode = salesPlatformEnum != null ? salesPlatformEnum.getKingdeeCode() : "";
            //平台类型
            resultMap.put("platformType", salesPlatformCode);
            if(StringUtils.isNotBlank(platformType)) {
            	List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                        .eq(DictBasicEntity::getType, DictBasicTypeEnum.SDY_SUB_PLATFORM.getType())
                        .eq(DictBasicEntity::getName, platformType)
                        .list();
            	if(CollUtil.isNotEmpty(dictBasicEntityList)) {
            		resultMap.put("sdyPlatformType", dictBasicEntityList.get(0).getRemark());
            	}
            }
        }
//        //部门
//        if (CharSequenceUtil.isNotBlank(deptId)) {
//            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
//            dto.setDeptId(entity.getSalesDeptId());
//            dto.setOrgId(entity.getSalesOrgId());
//            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
//            if (ObjectUtil.isNotEmpty(deptKingdee)) {
//                resultMap.put("salesDeptCode", deptKingdee.getKingdeeDeptCode());
//            }
//        }

        //销售组织
        String salesOrgId = entity.getSalesOrgId();

        //获取业务员信息
        if (CharSequenceUtil.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(entity.getSalesOrgId());
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setSalesDeptId(deptId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getUserPostCode());
                resultMap.put("seller", kingSellerInfo.getUserName());
                resultMap.put("salesDeptCode", kingSellerInfo.getDeptCode());
            }
        }

        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            //仓管员
            resultMap.put("warehouseKeeperCode", userKingdeePostInfoList.get(0).getKingdeeUserCode());
        }


        String billDate = entity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //汇率
        BigDecimal exchangeRate = dmpTaskFeign.getRate(billDate, currency);
        if (Objects.isNull(exchangeRate)) {
            exchangeRate = MathUtil.BigDecimal_1;
        }
        //汇率
        resultMap.put("exchangeRate", exchangeRate);
        //运输单号
        resultMap.put("trackNo", entity.getTrackNo());
        //销售单号
        resultMap.put("soCode", entity.getSoCode());
        //销售单金蝶id
        resultMap.put("soSyncKingdeeId", entity.getSyncKingdeeId());
        //发货组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String warehouseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("warehouseOrgCode", warehouseOrgCode);
        }
        //承运商
        if (ObjectUtil.isNotEmpty(supplierEntity)) {
            resultMap.put("carrierCode", supplierEntity.getCode());
        }
        //2024.09.11 jack 同步物流渠道名称到金蝶销售出库单的物流渠道
        resultMap.put("logisticsChannelName", entity.getLogisticsChannelName());
        //订单标签
        resultMap.put("tradeLabel", entity.getTradeLabel());
        //来源单号
        resultMap.put("F_Ulz_ConsignNum", entity.getSourceCode());
        //————————————————————财务信息SubHeadEntity——————————————————————
        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(currency)).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());
        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(salesOrgId)).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Collections.singletonList(entity.getWarehouseId()));

        //————————————————————物料信息——————————————————————
        List<Map<String, Object>> fEntityList = new ArrayList<>();
        for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
            Map<String, Object> map = new HashMap<>();
            map.put("skuNo", detailEntity.getSkuNo());
            map.put("actualQty", detailEntity.getActualQty());
            map.put("salesQty", detailEntity.getActualQty());
            map.put("planQty", detailEntity.getPlanQty());
            map.put("price", detailEntity.getPrice());
            BigDecimal taxRate = detailEntity.getTaxRate();

            //含税单价
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(detailEntity.getPrice(), multiplyTax);
            //含税单价
            map.put("taxPrice", taxPrice);
            map.put("amount", detailEntity.getAmount());
            map.put("isGift", Boolean.FALSE);
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                String warehouseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
                map.put("warehouseOrgCode", warehouseOrgCode);
            }
            map.put("taxRate", detailEntity.getTaxRate());
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
                map.put("warehouseCode", warehouseCode);
            }
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            }
            map.put("remark", detailEntity.getRemark());
            //销售订单金蝶id
            map.put("soSyncKingdeeId", entity.getSyncKingdeeId());
            map.put("FSrcType", "SAL_SaleOrder");
            // 源单编号
            map.put("FSrcBillNo", entity.getSourceCode());
            // 订单单号
            map.put("FSoorDerno", entity.getSoCode());
            fEntityList.add(map);
        }
        resultMap.put("FEntity", fEntityList);
        return resultMap;
    }


    @Override
    public Map<String, Object> newSyncB2cDataToKingdee(SoOutstockEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //退货单号
        resultMap.put("code", entity.getCode());
        String soId = entity.getSoId();
        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);
        //删除操作
        if (SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
            return resultMap;
        }
        //销售单信息
        SoB2cEntity soB2cEntity = soB2cFeign.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            throw new ServiceException(ApiError.ERROR_SO_B2C_DETAIL_NOT_EXIST);
        }
        List<SoB2cDetailEntity> soB2cDetailList = soB2cFeign.listDetailByMainIds(Collections.singletonList(soId));
        //获取销售出库单详情
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Collections.singletonList(entity.getId()));
        String sellerId = entity.getSellerId();
        String deptId = entity.getSalesDeptId();
//        SysDepartmentUserNumberDTO deptUser = null;
//        String deptId = "";
//        if (CharSequenceUtil.isNotBlank(sellerId)) {
//            deptUser = sysUserFeign.getDeptByUserId(sellerId);
//        }
//        if (Objects.nonNull(deptUser)) {
//            deptId = deptUser.getDepartmentId();
//        }

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(soB2cEntity.getOrgId(), entity.getWarehouseOrgId()));
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Collections.singletonList(entity.getCustomerId()));

        //查询供应商信息
        SupplierEntity supplierEntity = null;
        if (CharSequenceUtil.isNotBlank(entity.getCarrierId())) {
            supplierEntity = scmTaskFeign.getSupplierById(entity.getCarrierId());
        }
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Collections.singletonList(soB2cEntity.getCurrency()));
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Collections.singletonList(entity.getWarehouseId()));
        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Collections.singletonList(entity.getWarehouseKeeperId()));
        //单据类型
        resultMap.put("orderType", entity.getOrderType());
        //单据日期
        resultMap.put("billDate", Objects.nonNull(entity.getBillDate()) ? LocalDateTimeUtil.format(entity.getBillDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDateTimeUtil.format(entity.getCreateTime().toLocalDate(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        //销售组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(soB2cEntity.getOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        //客户
        if (CollectionUtils.isNotEmpty(customerInfoEntitieList)) {
            CustomerInfoEntity customerInfoEntity = customerInfoEntitieList.stream().filter(obj -> obj.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            resultMap.put("customerCode", customerInfoEntity.getCode());
            resultMap.put("customerName", customerInfoEntity.getName());
            String platformType = customerInfoEntity.getPlatformType();
			PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(platformType);
            String salesPlatformCode = salesPlatformEnum != null ? salesPlatformEnum.getKingdeeCode() : "";
            //平台类型
            resultMap.put("platformType", salesPlatformCode);
            if(StringUtils.isNotBlank(platformType)) {
            	List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                        .eq(DictBasicEntity::getType, DictBasicTypeEnum.SDY_SUB_PLATFORM.getType())
                        .eq(DictBasicEntity::getName, platformType)
                        .list();
            	if(CollUtil.isNotEmpty(dictBasicEntityList)) {
            		resultMap.put("sdyPlatformType", dictBasicEntityList.get(0).getRemark());
            	}
            }
        }

        //部门
//        if (CharSequenceUtil.isNotBlank(deptId)) {
//            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
//            dto.setDeptId(entity.getSalesDeptId());
//            dto.setOrgId(entity.getSalesOrgId());
//            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
//            if (ObjectUtil.isNotEmpty(deptKingdee)) {
//                resultMap.put("salesDeptCode", deptKingdee.getKingdeeDeptCode());
//            }
//        }
        //销售组织
        String salesOrgId = soB2cEntity.getOrgId();
        //币别
        String currency = CharSequenceUtil.isNotBlank(soB2cEntity.getCurrency()) ? soB2cEntity.getCurrency() : "CNY";

        //获取业务员信息
        if (CharSequenceUtil.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(soB2cEntity.getOrgId());
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setSalesDeptId(deptId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getUserPostCode());
                resultMap.put("seller", kingSellerInfo.getUserName());
                resultMap.put("salesDeptCode", kingSellerInfo.getDeptCode());
            }
        }

        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            //仓管员
            resultMap.put("warehouseKeeperCode", userKingdeePostInfoList.get(0).getKingdeeUserCode());
        }


        String billDate = soB2cEntity.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        //汇率
        BigDecimal exchangeRate = dmpTaskFeign.getRate(billDate, currency);
        if (Objects.isNull(exchangeRate)) {
            exchangeRate = MathUtil.BigDecimal_1;
        }
        //汇率
        resultMap.put("exchangeRate", exchangeRate);
        //运输单号
        resultMap.put("trackNo", entity.getTrackNo());
        //销售单号
        resultMap.put("soCode", entity.getSoCode());
        //销售单金蝶id
        resultMap.put("soSyncKingdeeId", soB2cEntity.getSyncKingdeeId());
        //发货组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String warehouseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("warehouseOrgCode", warehouseOrgCode);
        }
        //承运商
        if (ObjectUtil.isNotEmpty(supplierEntity)) {
            resultMap.put("carrierCode", supplierEntity.getCode());
        }
        //2024.09.11 jack 同步物流渠道名称到金蝶销售出库单的物流渠道
        resultMap.put("logisticsChannelName", entity.getLogisticsChannelName());
        //订单标签
        resultMap.put("tradeLabel", entity.getTradeLabel());
        //来源单号
        resultMap.put("F_Ulz_ConsignNum", entity.getSourceCode());
        //————————————————————财务信息SubHeadEntity——————————————————————
        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(currency)).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());
        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList) && Objects.isNull(resultMap.get("salesOrgCode"))) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(salesOrgId)).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }


        //————————————————————物料信息——————————————————————

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Collections.singletonList(entity.getWarehouseId()));

        List<Map<String, Object>> fEntityList = new ArrayList<>();
        for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
            SoB2cDetailEntity soB2cDetailEntity = soB2cDetailList.stream().filter(s -> s.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            Map<String, Object> map = new HashMap<>();
            map.put("customerPO", detailEntity.getCustomerPO());
            map.put("skuNo", detailEntity.getSkuNo());
            map.put("actualQty", detailEntity.getActualQty());
            map.put("salesQty", Objects.nonNull(soB2cDetailEntity) ? soB2cDetailEntity.getQty() : detailEntity.getActualQty());
            map.put("planQty", detailEntity.getPlanQty());
            map.put("price", detailEntity.getPrice());
            BigDecimal taxRate = detailEntity.getTaxRate();

            //含税单价
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiplyWithTwo(detailEntity.getPrice(), multiplyTax);
            //含税单价
            map.put("taxPrice", taxPrice);
            map.put("amount", detailEntity.getAmount());
            map.put("isGift", Boolean.FALSE);
            if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
                String warehouseOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
                map.put("warehouseOrgCode", warehouseOrgCode);
            }
            map.put("taxRate", detailEntity.getTaxRate());
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId()))
                        .findFirst().flatMap(obj -> Optional.ofNullable(obj.getKingdeeWarehouseCode())).orElse(null);
                map.put("warehouseCode", warehouseCode);
            }
            //是否下推仓位
            Boolean isPush = pushKingdeeList.stream().filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), entity.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            }
            map.put("remark", detailEntity.getRemark());
            //销售订单金蝶id
            String soSyncKingdeeId = Objects.nonNull(soB2cDetailEntity) ? soB2cDetailEntity.getKingdeeDetailId() : "";
            map.put("soSyncKingdeeId", soSyncKingdeeId);
            map.put("FSrcType", "SAL_SaleOrder");
            // 源单编号
            map.put("FSrcBillNo", soB2cEntity.getPlatformCode());
            // 订单单号
            map.put("FSoorDerno", soB2cEntity.getCode());
            fEntityList.add(map);
        }
        resultMap.put("FEntity", fEntityList);
        return resultMap;
    }


    @Override
    public Map<String, Object> syncDataToSdyFieldHandler(SoOutstockEntity entity,
                                                         SoOutstockDetailEntity soOutstockDetailEntity,
                                                         String operate,
                                                         List<CurrencyDTO.ViewDTO> currencyList,
                                                         List<ShopInfoEntity> shopInfoList,
                                                         List<CustomerInfoEntity> customerInfoList,
                                                         List<BaseIdDTO.CodeDTO> companyEntities,
                                                         List<SkuVO> skuVOList,
                                                         List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                         List<ProductDetailEntity> parentSkuList,
                                                         List<SoB2cEntity> soB2cEntities,
                                                         List<SoInfoEntity> soInfoEntities,
                                                         List<SoB2cReceiverEntity> soB2cReceiverEntityList,
                                                         List<DictBasicEntity> omsAllDictList,
                                                         List<DictPartitionEntity> partitionEntityList,
                                                         List<DictCountryEntity> countryEntityList,
                                                         List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                         List<SysDepartmentEntity> deptList) {
        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 字典分组
        Map<String, List<DictBasicEntity>> dictGroupMap = omsAllDictList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
        // 销售平台
        List<DictBasicEntity> dictBasicEntityList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SALES_PLATFORM.getType(), Collections.emptyList());
        // 数帝云子平台映射
        List<DictBasicEntity> dictList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(), Collections.emptyList());
        // 数帝云军区一级部门映射
        List<DictBasicEntity> sdyPartitionDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(), Collections.emptyList());
        // 数帝云平台二级部门映射
        List<DictBasicEntity> sdyPlatformDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType(), Collections.emptyList());
        // 对应客户信息
        CustomerInfoEntity customerInfo = customerInfoList.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(null);

        // 国家
        String country = entity.getCountry();
        // 军区
        String partitionId = "";
        // 当前数帝云平台二级部门映射
        List<DictBasicEntity> sdyPlatformDeptEntityList = new LinkedList<>();

        String transactionSubType = "";
        String orderPlatformCode = entity.getSoCode();
        if (OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
            SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(entity.getSoId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cEntity)) {
                transactionSubType = OrderSubTypeEnum.ONLINE_ORDER.getCode();
            } else {
                transactionSubType = soB2cEntity.getTransactionSubType();
                if (CharSequenceUtil.isBlank(soB2cEntity.getPlatformCode())) {
                    orderPlatformCode = entity.getSoCode();
                } else {
                    orderPlatformCode = soB2cEntity.getPlatformCode();
                }
                sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(soB2cEntity.getDictPlatform())).collect(Collectors.toList());
            }
            SoB2cReceiverEntity receiverEntity = soB2cReceiverEntityList.stream().filter(req -> req.getMainId().equals(entity.getSoId())).findFirst().orElse(null);
            if (null != receiverEntity){
                partitionId = receiverEntity.getPartitionId();
                if (StringUtils.isNotBlank(receiverEntity.getCountry())){
                    country = receiverEntity.getCountry();
                }
            }
        } else if (OrderTypeEnum.B2B.getCode().equals(entity.getOrderType())) {
            SoInfoEntity soInfoEntity = soInfoEntities.stream().filter(req -> req.getId().equals(entity.getSoId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soInfoEntity)) {
                transactionSubType = OrderSubTypeEnum.OFFLINE_ORDER.getCode();
            } else {
                transactionSubType = soInfoEntity.getTransactionSubType();
                orderPlatformCode = soInfoEntity.getCode();
                partitionId = soInfoEntity.getPartitionId();
            }
            if(null != customerInfo){
                sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(customerInfo.getPlatformType())).collect(Collectors.toList());
            }
        }


        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        // 国家编码
        shudiyunB2cOrderDTO.setCountry_code(country);
        String finalCountry = country;
        DictCountryEntity dictCountryEntity = countryEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalCountry)).findFirst().orElse(null);
        if (null != dictCountryEntity){
            // 国家名称
            shudiyunB2cOrderDTO.setCountry(dictCountryEntity.getShortNameCn());
            // 区域编码
            shudiyunB2cOrderDTO.setRegion_code(dictCountryEntity.getRegionCode());
            // 区域名称
            DictGlobalAreaEntity dictGlobalAreaEntity = dictGlobalEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(dictCountryEntity.getSubregionCode())).findFirst().orElse(null);
            if (null != dictGlobalAreaEntity){
                shudiyunB2cOrderDTO.setRegion_name(dictGlobalAreaEntity.getRegionName());
            }
        }

        String finalPartitionId = partitionId;
        DictPartitionEntity dictPartitionEntity = null;
        if ("qimen".equals(entity.getCreateUserName()) || "wangdiantong".equals(entity.getCreateUserName())){
            dictPartitionEntity = partitionEntityList.stream().filter(e -> e.getCode().equalsIgnoreCase("china")).findFirst().orElse(null);
            sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(customerInfo.getPlatformType())).collect(Collectors.toList());
        } else {
            dictPartitionEntity = partitionEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalPartitionId)).findFirst().orElse(null);
        }

        if (null != dictPartitionEntity){
            // 军区编码
            shudiyunB2cOrderDTO.setMilitary_region_code(dictPartitionEntity.getCode());
            // 军区名称
            shudiyunB2cOrderDTO.setMilitary_region_name(dictPartitionEntity.getName());
            // 军区一级部门映射
            DictPartitionEntity finalDictPartitionEntity = dictPartitionEntity;
            DictBasicEntity sdyPartitionDeptEntity = sdyPartitionDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(finalDictPartitionEntity.getCode())).findFirst().orElse(null);
            if (null != sdyPartitionDeptEntity && !CollectionUtils.isEmpty(sdyPlatformDeptEntityList)){
                List<String> deptLevel2Ids = sdyPlatformDeptEntityList.stream().map(DictBasicEntity::getValue).distinct().collect(Collectors.toList());
                SysDepartmentEntity departmentDTO = deptList.stream().filter(e ->
                                e.getPath().contains(sdyPartitionDeptEntity.getValue())
                                        && deptLevel2Ids.contains(e.getId())
                        )
                        .findFirst()
                        .orElse(null);
                if (null != departmentDTO){
                    // 部门编码
                    shudiyunB2cOrderDTO.setDepartment_code(departmentDTO.getCode());
                    // 部门名称
                    shudiyunB2cOrderDTO.setDepartment_name(departmentDTO.getName());
                }
            }
        }

        shudiyunB2cOrderDTO.setBiz_uni_key(entity.getId() + soOutstockDetailEntity.getId());
        shudiyunB2cOrderDTO.setBiz_no(entity.getCode());
        shudiyunB2cOrderDTO.setBiz_time(localDate.format(entity.getBillDate()));
        //默认出库单
        shudiyunB2cOrderDTO.setTransaction_type("销售出库单");
        shudiyunB2cOrderDTO.setTransaction_sub_type(convertOutstockTransactionSubType(transactionSubType));
        shudiyunB2cOrderDTO.setBiz_status(entity.getApproveStatus().getName());
        shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, entity.getVersion(), soOutstockDetailEntity.getVersion()));

        //客户信息
        if (ObjectUtil.isNotEmpty(customerInfo)) {
            String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(entity.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
            if (CharSequenceUtil.isNotBlank(salesOrgCode)) {
                shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
            } else {
                salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).map(req -> req.getCode()).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
            }

            BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
            }

            if (customerInfo.getCurrency() != null) {
                CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(customerInfo.getCurrency())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(viewDTO)) {
                    shudiyunB2cOrderDTO.setSettlement_currency_code(viewDTO.getId());
                    shudiyunB2cOrderDTO.setSettlement_currency(viewDTO.getName());
                }
            }
            if (customerInfo.getTradeCurrency() != null) {
                CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(customerInfo.getTradeCurrency())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(viewDTO)) {
                    shudiyunB2cOrderDTO.setTransaction_currency_code(viewDTO.getId());
                    shudiyunB2cOrderDTO.setTransaction_currency(viewDTO.getName());
                }
            }
            shudiyunB2cOrderDTO.setShop_no(customerInfo.getCode());
            shudiyunB2cOrderDTO.setShop_name(customerInfo.getName());

            String subPlatformType = customerInfo.getPlatformType();
            if (StringUtils.isNotBlank(subPlatformType)) {
                DictBasicEntity dictBasicEntity = dictList.stream().filter(req -> req.getName().equals(subPlatformType)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(dictBasicEntity)) {
                	shudiyunB2cOrderDTO.setPlatform_id(dictBasicEntity.getRemark());
                	shudiyunB2cOrderDTO.setPlatform_name(dictBasicEntity.getRemark());
                    shudiyunB2cOrderDTO.setSubplatform_no(dictBasicEntity.getValue());
                    shudiyunB2cOrderDTO.setSubplatform_name(dictBasicEntity.getValue());
                }
            }
        }
        shudiyunB2cOrderDTO.setRoot_node_no(orderPlatformCode);

        //产品信息
        shudiyunB2cOrderDTO.setGoods_no(soOutstockDetailEntity.getSkuNo());
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
        shudiyunB2cOrderDTO.setGoods_name(skuVO.getSkuName());
        if (skuVO.getSpuNo() == null) {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSkuName());
        } else {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSpuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSpuName());
        }
        shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
        shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());
        if (soOutstockDetailEntity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            shudiyunB2cOrderDTO.setIs_gift(1);
        } else {
            shudiyunB2cOrderDTO.setIs_gift(0);
        }
        BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
            shudiyunB2cOrderDTO.setIs_comb(1);
            shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
            BomChildrenSkuDTO finalBomChildrenSkuDTO1 = bomChildrenSkuDTO;
            String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO1.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            shudiyunB2cOrderDTO.setSuite_name(skuName);
        } else {
            bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO)) {
                shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
                BomChildrenSkuDTO finalBomChildrenSkuDTO = bomChildrenSkuDTO;
                String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSuite_name(skuName);
            } else {
                shudiyunB2cOrderDTO.setSuite_no(skuVO.getSkuNo());
                shudiyunB2cOrderDTO.setSuite_name(skuVO.getSkuName());
            }
        }

        shudiyunB2cOrderDTO.setRemark(soOutstockDetailEntity.getRemark());
        shudiyunB2cOrderDTO.setWarehouse_no(entity.getWarehouseId());
        shudiyunB2cOrderDTO.setWarehouse_name(entity.getWarehouseName());

        // 商品状态
        shudiyunB2cOrderDTO.setGoods_status("已发货");
        if (entity.getActualDeliveryDate() != null) {
            shudiyunB2cOrderDTO.setDelivery_time(localDateTime.format(entity.getActualDeliveryDate()));
        }

        shudiyunB2cOrderDTO.setGoods_transaction_quantity(soOutstockDetailEntity.getActualQty());
        shudiyunB2cOrderDTO.setUnit(skuVO.getUnitName());
        if (skuVO.getRetailPrice() != null) {
            shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(skuVO.getRetailPrice());
        } else {
            shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(BigDecimal.ZERO);
        }

        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getCurrency())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(viewDTO)) {
            shudiyunB2cOrderDTO.setTransaction_currency(viewDTO.getName());
            shudiyunB2cOrderDTO.setTransaction_currency_code(viewDTO.getId());
        }

        shudiyunB2cOrderDTO.setSource_system("SDC");
        shudiyunB2cOrderDTO.setRoot_node_no_initial(orderPlatformCode);

        return BeanUtil.beanToMap(shudiyunB2cOrderDTO);
    }
    
    @Override
    public Map<String, Object> syncNewDataToSdyFieldHandler(SoOutstockEntity entity,
    		SoOutstockDetailEntity soOutstockDetailEntity,
    		String operate,
    		List<CurrencyDTO.ViewDTO> currencyList,
    		List<ShopInfoEntity> shopInfoList,
    		List<CustomerInfoEntity> customerInfoList,
    		List<BaseIdDTO.CodeDTO> companyEntities,
    		List<SkuVO> skuVOList,
    		List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
    		List<ProductDetailEntity> parentSkuList,
    		List<SoB2cEntity> soB2cEntities,
    		List<SoInfoEntity> soInfoEntities,
    		List<SoB2cReceiverEntity> soB2cReceiverEntityList,
    		List<DictBasicEntity> omsAllDictList,
    		List<DictPartitionEntity> partitionEntityList,
    		List<DictCountryEntity> countryEntityList,
    		List<DictGlobalAreaEntity> dictGlobalEntityList,
    		List<SysDepartmentEntity> deptList) {
    	
    	// 字典分组
    	Map<String, List<DictBasicEntity>> dictGroupMap = omsAllDictList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
    	// 销售平台
    	List<DictBasicEntity> dictBasicEntityList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SALES_PLATFORM.getType(), Collections.emptyList());
    	// 数帝云军区一级部门映射
    	List<DictBasicEntity> sdyPartitionDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(), Collections.emptyList());
    	// 数帝云平台二级部门映射
    	List<DictBasicEntity> sdyPlatformDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType(), Collections.emptyList());
    	// 对应客户信息
    	CustomerInfoEntity customerInfo = customerInfoList.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(null);
    	
    	// 国家
    	String country = entity.getCountry();
    	// 军区
    	String partitionId = "";
    	// 当前数帝云平台二级部门映射
    	List<DictBasicEntity> sdyPlatformDeptEntityList = new LinkedList<>();
    	
    	String transactionSubType = "";
    	String orderPlatformCode = entity.getSoCode();
    	if (OrderTypeEnum.B2C.getCode().equals(entity.getOrderType())) {
    		SoB2cEntity soB2cEntity = soB2cEntities.stream().filter(req -> req.getId().equals(entity.getSoId())).findFirst().orElse(null);
    		if (ObjectUtil.isEmpty(soB2cEntity)) {
    			transactionSubType = OrderSubTypeEnum.ONLINE_ORDER.getCode();
    		} else {
    			transactionSubType = soB2cEntity.getTransactionSubType();
    			if (CharSequenceUtil.isBlank(soB2cEntity.getPlatformCode())) {
    				orderPlatformCode = entity.getSoCode();
    			} else {
    				orderPlatformCode = soB2cEntity.getPlatformCode();
    			}
    			sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(soB2cEntity.getDictPlatform())).collect(Collectors.toList());
    		}
    		SoB2cReceiverEntity receiverEntity = soB2cReceiverEntityList.stream().filter(req -> req.getMainId().equals(entity.getSoId())).findFirst().orElse(null);
    		if (null != receiverEntity){
    			partitionId = receiverEntity.getPartitionId();
    			if (StringUtils.isNotBlank(receiverEntity.getCountry())){
    				country = receiverEntity.getCountry();
    			}
    		}
    	} else if (OrderTypeEnum.B2B.getCode().equals(entity.getOrderType())) {
    		SoInfoEntity soInfoEntity = soInfoEntities.stream().filter(req -> req.getId().equals(entity.getSoId())).findFirst().orElse(null);
    		if (ObjectUtil.isEmpty(soInfoEntity)) {
    			transactionSubType = OrderSubTypeEnum.OFFLINE_ORDER.getCode();
    		} else {
    			transactionSubType = soInfoEntity.getTransactionSubType();
    			orderPlatformCode = soInfoEntity.getCode();
    			partitionId = soInfoEntity.getPartitionId();
    		}
    		if(null != customerInfo){
    			sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(customerInfo.getPlatformType())).collect(Collectors.toList());
    		}
    	}
    	
    	
    	DmpSoOutstockDTO.ViewDTO viewDto = new DmpSoOutstockDTO.ViewDTO();
    	DmpSoOutstockDetailDTO.ViewDTO detailView = new DmpSoOutstockDetailDTO.ViewDTO();
    	
    	// 国家编码
    	viewDto.setCountry(country);
    	String finalCountry = country;
    	DictCountryEntity dictCountryEntity = countryEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalCountry)).findFirst().orElse(null);
    	if (null != dictCountryEntity){
    		// 区域编码
    		viewDto.setProvince(dictCountryEntity.getRegionCode());
    	}
    	
    	String finalPartitionId = partitionId;
    	DictPartitionEntity dictPartitionEntity = null;
    	if ("qimen".equals(entity.getCreateUserName()) || "wangdiantong".equals(entity.getCreateUserName())){
    		dictPartitionEntity = partitionEntityList.stream().filter(e -> e.getCode().equalsIgnoreCase("china")).findFirst().orElse(null);
    		if(CollUtil.isNotEmpty(sdyPlatformDeptList) && customerInfo != null) {
    			sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(customerInfo.getPlatformType())).collect(Collectors.toList());
    		}
    	} else {
    		dictPartitionEntity = partitionEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalPartitionId)).findFirst().orElse(null);
    	}
    	
    	if (null != dictPartitionEntity){
    		// 军区编码
    		viewDto.setDistrict(dictPartitionEntity.getCode());
    		// 军区一级部门映射
    		DictPartitionEntity finalDictPartitionEntity = dictPartitionEntity;
    		DictBasicEntity sdyPartitionDeptEntity = sdyPartitionDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(finalDictPartitionEntity.getCode())).findFirst().orElse(null);
    		if (null != sdyPartitionDeptEntity && !CollectionUtils.isEmpty(sdyPlatformDeptEntityList)){
    			List<String> deptLevel2Ids = sdyPlatformDeptEntityList.stream().map(DictBasicEntity::getValue).distinct().collect(Collectors.toList());
    			SysDepartmentEntity departmentDTO = deptList.stream().filter(e ->
    			e.getPath().contains(sdyPartitionDeptEntity.getValue())
    			&& deptLevel2Ids.contains(e.getId())
    					)
    					.findFirst()
    					.orElse(null);
    			if (null != departmentDTO){
    				// 部门编码
    				detailView.setSaleDeptName(departmentDTO.getCode());
    			}
    		}
    	}
    	
    	String thirdCode = entity.getId();
    	viewDto.setThirdCode(thirdCode);
    	detailView.setThirdCode(thirdCode);
    	detailView.setThirdDetailId(soOutstockDetailEntity.getId());
    	String code = entity.getCode();
    	viewDto.setThirdBillNo(code);
    	String platformCode = "";
    	if(code.startsWith("CK")) {
    		platformCode = entity.getThirdCode();
    		if(!platformCode.startsWith("JY")) {
    			throw new ServiceException("旺店通出库单号CK开头，对应销售订单号不是JY开头，请检查");
    		}
    	}else {
    		platformCode = entity.getSoCode();
    	}
    	viewDto.setPlatformCode(platformCode);
    	viewDto.setBillDate(entity.getBillDate().atStartOfDay());
    	//默认出库单
    	viewDto.setTradeLabel(convertOutstockTransactionSubType(transactionSubType));
    	viewDto.setPlatformStatus(entity.getApproveStatus().getName());
        viewDto.setSourceType(entity.getSourceType());
    	detailView.setDataStatus(new ShudiyunB2cOrderDTO().sdyStatusHandle(operate, entity.getVersion(), soOutstockDetailEntity.getVersion()));
    	
    	//客户信息
    	if (ObjectUtil.isNotEmpty(customerInfo)) {
    		String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(entity.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
    		if (CharSequenceUtil.isNotBlank(salesOrgCode)) {
    			viewDto.setSaleOrgId(salesOrgCode);
    		} else {
    			salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).map(req -> req.getCode()).findFirst().orElse("");
    			viewDto.setSaleOrgId(salesOrgCode);
    		}
    		
    		BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
    		if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
    			viewDto.setFinancialCompanyId(sysAccountingCompanyEntity.getId());
    		}
    		
    		if (customerInfo.getCurrency() != null) {
    			CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(customerInfo.getCurrency())).findFirst().orElse(null);
    			if (ObjectUtil.isNotEmpty(viewDTO)) {
    				detailView.setPayCurrency(viewDTO.getId());
    			}
    		}
    		if (customerInfo.getTradeCurrency() != null) {
    			CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(customerInfo.getTradeCurrency())).findFirst().orElse(null);
    			if (ObjectUtil.isNotEmpty(viewDTO)) {
    				detailView.setCurrency(viewDTO.getId());
    			}
    		}
    		detailView.setPlatformType(customerInfo.getPlatformType());
    		String platformName = dictBasicEntityList.stream().filter(req -> req.getValue().equals(customerInfo.getPlatformType())).map(DictBasicEntity::getName).findFirst().orElse("");
    		detailView.setPlatformName(platformName);
    		viewDto.setShopId(customerInfo.getCode());
    		viewDto.setShopName(customerInfo.getName());
    	}
    	detailView.setThirdOrderCode(orderPlatformCode);
    	
    	//产品信息
    	SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(new SkuVO());
    	
    	detailView.setSkuNo(skuVO.getSkuNo());
    	detailView.setSkuName(skuVO.getSkuName());
    	detailView.setPlatformSku(skuVO.getSpuNo());
    	detailView.setSpecifics(skuVO.getSpuName());
    	
    	if (soOutstockDetailEntity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
    		detailView.setIsGift(true);
    	} else {
    		detailView.setIsGift(false);
    	}
    	BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(null);
    	if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
    		detailView.setIsComb(1);
    		detailView.setSuiteNo(bomChildrenSkuDTO.getParentSkuNo());
    		BomChildrenSkuDTO finalBomChildrenSkuDTO1 = bomChildrenSkuDTO;
    		String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO1.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
    		detailView.setSuiteName(skuName);
    	} else {
    		bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(soOutstockDetailEntity.getSkuId())).findFirst().orElse(null);
    		if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO)) {
    			detailView.setSuiteNo(bomChildrenSkuDTO.getParentSkuNo());
    			BomChildrenSkuDTO finalBomChildrenSkuDTO = bomChildrenSkuDTO;
    			String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
    			detailView.setSuiteName(skuName);
    		} else {
    			detailView.setSuiteNo(skuVO.getSkuNo());
    			detailView.setSuiteName(skuVO.getSkuName());
    		}
    	}
    	
    	detailView.setRemark(soOutstockDetailEntity.getRemark());
    	detailView.setWarehouseId(entity.getWarehouseId());
    	detailView.setWarehouseName(entity.getWarehouseName());
        detailView.setPlatformDetailId(soOutstockDetailEntity.getPlatformDetailId());
    	
    	viewDto.setDeliveryTime(entity.getActualDeliveryDate());
    	
    	detailView.setQty(soOutstockDetailEntity.getActualQty());
    	detailView.setProductUnit(skuVO.getUnitName());
    	if (skuVO.getRetailPrice() != null) {
    		detailView.setSellPrice(skuVO.getRetailPrice());
    	} else {
    		detailView.setSellPrice(BigDecimal.ZERO);
    	}
    	
    	CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getCurrency())).findFirst().orElse(null);
    	if (ObjectUtil.isNotEmpty(viewDTO)) {
    		detailView.setCurrency(viewDTO.getId());
    	}
    	
    	viewDto.setDetailList(Arrays.asList(detailView));
    	
    	return BeanUtil.beanToMap(viewDto);
    }


    @Override
    public void syncDataToSdy(SoOutstockEntity entity, List<SoOutstockDetailEntity> soOutstockDetailEntityList, String operate) {
    	if(SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		this.syncBatchDataToSdy(Arrays.asList(entity), soOutstockDetailEntityList, operate, true, true);
    	}else {
    		for (SoOutstockDetailEntity soOutstockDetailEntity : soOutstockDetailEntityList) {
                WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
                wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                wmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SO_OUTSTOCK.getCode());
                wmsPushMsgEntity.setSourceId(soOutstockDetailEntity.getId());
                wmsPushMsgEntity.setSourceCode(entity.getCode() + "_" + soOutstockDetailEntity.getSkuNo());
                wmsPushMsgEntity.setSyncOperate(operate);
                wmsPushMsgEntity.setPushData(JSON.toJSONString(DmpOutputConstant.getQuerySyncMap()));
                wmsPushMsgService.save(wmsPushMsgEntity);
            }
    	}
    }

    /**
     * 出库单子状态转换
     *
     * @param transactionSubType
     * @return
     */
    private String convertOutstockTransactionSubType(String transactionSubType) {
        if (OrderSubTypeEnum.OFFLINE_ORDER.getCode().equals(transactionSubType) || OrderSubTypeEnum.ONLINE_ORDER.getCode().equals(transactionSubType)) {
            //普通出库
            transactionSubType = "普通出库";
        } else if (OrderSubTypeEnum.GIFT_ORDER.getCode().equals(transactionSubType) || OrderSubTypeEnum.GIFT_REPLENISHMENT.getCode().equals(transactionSubType)) {
            //赠品出库
            transactionSubType = "赠品出库";
        } else if (OrderSubTypeEnum.EXCHANGE_REPLACEMENT.getCode().equals(transactionSubType)) {
            //换货补发
            transactionSubType = "换货补发";
        } else if (OrderSubTypeEnum.REPLENISHMENT.getCode().equals(transactionSubType)) {
            //补发出库
            transactionSubType = "补发出库";
        } else {
            //其他
            transactionSubType = "其他";
        }
        return transactionSubType;
    }


	@Override
	public Map<String, Map<String, Object>> syncBatchDataToSdy(List<SoOutstockEntity> soOutstockEntities,
			List<SoOutstockDetailEntity> soOutstockDetailEntityList, String operate, boolean isSavePush, boolean isNewQuerySync) {
		Map<String , Map<String, Object>> resultList = new HashMap<>();
		//B2C订单
        List<SoOutstockEntity> b2cEntity = soOutstockEntities.stream().filter(req -> OrderTypeEnum.B2C.getCode().equals(req.getOrderType())).collect(Collectors.toList());
        List<String> b2cSoIds = b2cEntity.stream().map(req -> req.getSoId()).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());

        List<SoB2cEntity> soB2cEntities = new ArrayList<>();
        List<SoB2cReceiverEntity> soB2cReceiverEntityList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(b2cSoIds)){
            soB2cEntities = soB2cFeign.listByIds(b2cSoIds);
            // B2C收货信息
            soB2cReceiverEntityList = FeignQuery.create(SoB2cReceiverEntity.class)
                    .in(SoB2cReceiverEntity::getMainId, b2cSoIds)
                    .list();
        }


        //B2B订单
        List<SoOutstockEntity> b2bEntity = soOutstockEntities.stream().filter(req -> OrderTypeEnum.B2B.getCode().equals(req.getOrderType())).collect(Collectors.toList());
        List<String> b2bSoIds = b2bEntity.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
        List<SoInfoEntity> soInfoEntities = soInfoFeign.listSoInfoByIds(b2bSoIds);

        //客户
        List<String> customerIds = soOutstockEntities.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
        List<ShopInfoEntity> shopInfoList = new ArrayList<>();
        List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIds)) {
            //店铺
            shopInfoList = FeignQuery.create(ShopInfoEntity.class)
                    .in(ShopInfoEntity::getCustomerId, customerIds)
                    .list();
            //组织
            customerInfoList = FeignQuery.create(CustomerInfoEntity.class)
                    .in(CustomerInfoEntity::getId, customerIds)
                    .list();
        }
        //币别
        List<String> currencyCodeList = soOutstockDetailEntityList.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<String> currency = customerInfoList.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        currencyCodeList.addAll(currency);
        List<String> tradeCurrency = customerInfoList.stream().map(req -> req.getTradeCurrency()).distinct().collect(Collectors.toList());
        currencyCodeList.addAll(tradeCurrency);
        List<String> settlementCurrency = shopInfoList.stream().map(req -> req.getSettlementCurrency()).distinct().collect(Collectors.toList());
        currencyCodeList.addAll(settlementCurrency);
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyCodeList);

        //组织
        List<String> orgList = new ArrayList<>();
        List<String> salesOrgId = shopInfoList.stream().map(req -> req.getSalesOrgId()).distinct().collect(Collectors.toList());
        orgList.addAll(salesOrgId);
        List<String> financialOrganization = customerInfoList.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
        orgList.addAll(financialOrganization);
        List<String> salesOrgIds = soOutstockEntities.stream().map(req -> req.getSalesOrgId()).distinct().collect(Collectors.toList());
        orgList.addAll(salesOrgIds);
        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(orgList);

        //产品信息
        List<String> skuNos = soOutstockDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listAllStatusSkuBySkuNos(skuNos);
        List<String> skuIds = soOutstockDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }
        // OMS字典信息
        List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class)
                .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                        DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                ))
                .list();

        // 军区信息
        List<DictPartitionEntity> partitionEntityList = FeignQuery.create(DictPartitionEntity.class).list();

        // 国家信息
        List<DictCountryEntity> countryEntityList = FeignQuery.create(DictCountryEntity.class).list();

        // 子区域信息
        List<DictGlobalAreaEntity> dictGlobalEntityList = FeignQuery.create(DictGlobalAreaEntity.class).list();

        // 部门信息
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptEntityList();

        for (SoOutstockEntity soOutstockEntity : soOutstockEntities) {
            List<SoOutstockDetailEntity> detailEntityList = soOutstockDetailEntityList.stream().filter(req -> req.getMainId().equals(soOutstockEntity.getId())).collect(Collectors.toList());
            for (SoOutstockDetailEntity detailEntity : detailEntityList) {
            	Map<String, Object> syncDataToSdyFieldHandler = null;
            	if(isNewQuerySync) {
            		syncDataToSdyFieldHandler = this.syncNewDataToSdyFieldHandler(soOutstockEntity,
                            detailEntity,
                            operate,
                            currencyList,
                            shopInfoList,
                            customerInfoList,
                            companyEntities,
                            skuVOList,
                            bomChildrenSkuDTOS,
                            parentSkuList,
                            soB2cEntities,
                            soInfoEntities,
                            soB2cReceiverEntityList,
                            dictBasicEntityList,
                            partitionEntityList,
                            countryEntityList,
                            dictGlobalEntityList,
                            deptList
                    );
            	}else {
            		syncDataToSdyFieldHandler = this.syncDataToSdyFieldHandler(soOutstockEntity,
                            detailEntity,
                            operate,
                            currencyList,
                            shopInfoList,
                            customerInfoList,
                            companyEntities,
                            skuVOList,
                            bomChildrenSkuDTOS,
                            parentSkuList,
                            soB2cEntities,
                            soInfoEntities,
                            soB2cReceiverEntityList,
                            dictBasicEntityList,
                            partitionEntityList,
                            countryEntityList,
                            dictGlobalEntityList,
                            deptList
                    );
            	}
            	String sourceId = detailEntity.getId();
            	resultList.put(sourceId, syncDataToSdyFieldHandler);
            	if(isSavePush) {
            		WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
                    wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                    wmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SO_OUTSTOCK.getCode());
                    wmsPushMsgEntity.setSourceId(sourceId);
                    wmsPushMsgEntity.setSourceCode(soOutstockEntity.getCode() + "_" + detailEntity.getSkuNo());
                    wmsPushMsgEntity.setSyncOperate(operate);
    				wmsPushMsgEntity.setPushData(JSON.toJSONString(syncDataToSdyFieldHandler));
                    wmsPushMsgService.save(wmsPushMsgEntity);
            	}
            }
            
        }
		return resultList;
	}
}
