package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPushTaskFeignDTO;
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
import com.erp.model.dmp.dto.CfgSettingDTO;
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
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.SysDepartmentEntity;
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncDataToKingdee(SoOutstockEntity entity, String operate) {
        //生成任务
        return saveTask(entity, operate, this.newSyncDataToKingdee(entity, operate));
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncB2cDataToKingdee(SoOutstockEntity entity, String operate) {
        //生成任务
        return saveTask(entity, operate, this.newSyncB2cDataToKingdee(entity, operate));
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
    @GlobalTransactional(rollbackFor = Exception.class)
    public DmpPushTaskEntity syncWdtDataToKingdee(SoOutstockEntity entity, String operate) {
        //生成任务
        return saveTask(entity, operate, this.newSyncWdtDataToKingdee(entity, operate));
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
        if(CollUtil.isEmpty(list)) {
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
    @GlobalTransactional(rollbackFor = Exception.class)
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
                if (CollectionUtil.isNotEmpty(soDetailEntities)) {
                    SoDetailEntity detailEntity = soDetailEntities.stream().filter(soDetailEntity -> Objects.nonNull(soDetailEntity.getExchangeRate())).findFirst().orElse(null);
                    if (Objects.nonNull(detailEntity) && Objects.nonNull(detailEntity.getExchangeRate())) {
                        exchangeRate = detailEntity.getExchangeRate();
                    } else {
                        exchangeRate = BigDecimal.ONE;
                    }
                    entity.setCurrencyRate(exchangeRate);

                    //TODO 暂时设置为0等tms接通后补充
                    //先计算运费收入（原币）
//                if (Optional.ofNullable(soInfoEntity.getIsCollectShippingFee()).isPresent()) {
//                    //运费收入（本位币）
//                    entity.setShippingFee(soInfoEntity.getShippingFee().multiply(exchangeRate));
//                } else {
                    //运费收入（本位币）
                    entity.setShippingFee(BigDecimal.ZERO);
//                }

                    BigDecimal itemTotalCost = BigDecimal.ZERO;
                    BigDecimal orderTotalCost = BigDecimal.ZERO;
                    soDetailEntities.stream().forEach(
                            soDetailEntity -> {
                                itemTotalCost.add(Optional.ofNullable(soDetailEntity.getSaleCost()).orElse(BigDecimal.ZERO));
                                orderTotalCost.add(Optional.ofNullable(soDetailEntity.getAmount()).orElse(BigDecimal.ZERO));
                            }
                    );
                    entity.setItemTotalCost(itemTotalCost);
                    entity.setOrderTotalCost(orderTotalCost);
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
            if (CollectionUtil.isNotEmpty(dept)) {
                entity.setSaleDeptName(dept.get(0).getName());
            }
        }

        //获取销售出库单详情
        List<SoOutstockDetailEntity> details = soOutstockDetailService.listByMainIds(Arrays.asList(soOutstockEntity.getId()));

        //明细字段转换
        if (CollectionUtil.isEmpty(details)) {
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
        Map<String, ProductDetailEntity> idProductDetailMap = FeignQuery.getByIds(ProductDetailEntity.class , skuIdList)
        		.stream().collect(Collectors.toMap(ProductDetailEntity::getId, c -> c));
        details.forEach(soOutstockDetailEntity -> {
            BiDeliveryDetailItemEntity dmpOrderItemEntity = SoOutstockConverter.INSTANCE.soOutstockToDmpDeliveryItem(soOutstockDetailEntity);
            dmpOrderItemEntity.setDeliveryDetailId(entity.getId());
            dmpOrderItemEntity.setSaleOrderNo(finalSoId);
            dmpOrderItemEntity.setPlatformOrderId(finalSoCode);
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
                    dmpOrderItemEntity.setIsGift(soB2cDetailEntity.getAmount().compareTo(BigDecimal.ZERO) == 0 ? 1 : 2);
                    dmpOrderItemEntity.setAmount(soB2cDetailEntity.getAmount());
                }

                //B2B订单
                SoDetailEntity soDetailEntity = soDetailEntities.stream().filter(req -> req.getId().equals(soOutstockDetailEntity.getSoDetailId())).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soDetailEntity)) {
                    dmpOrderItemEntity.setCostPrice(soDetailEntity.getSaleCost());
                    dmpOrderItemEntity.setSellPrice(soDetailEntity.getPrice());
                    dmpOrderItemEntity.setIsGift(soDetailEntity.getIsGift() ? 1 : 2);
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
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Arrays.asList(entity.getId()));
        //销售单信息
        SoInfoEntity soInfoById = soInfoFeign.getSoInfoById(entity.getSoId());
        //销售单明细
        List<SoDetailEntity> soDetailEntitieList = new ArrayList<>();
        if (StringUtils.isNotBlank(soInfoById.getId())) {
            soDetailEntitieList = soInfoFeign.listSoDetailByMainIds(Arrays.asList(soInfoById.getId()));
        }

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(soInfoById.getSalesOrgId(), entity.getWarehouseOrgId()));
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Arrays.asList(entity.getCustomerId()));

        //查询供应商信息
        SupplierEntity supplierEntity = null;
        if (StringUtils.isNotBlank(entity.getCarrierId())) {
            supplierEntity = scmTaskFeign.getSupplierById(entity.getCarrierId());
        }
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(soInfoById.getCurrency()));
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getWarehouseId()));

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
            PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(customerInfoEntity.getPlatformType());
            String salesPlatformCode = salesPlatformEnum != null ? salesPlatformEnum.getKingdeeCode() : "";
            //平台类型
            resultMap.put("platformType", salesPlatformCode);
        }

        //部门
        if  (StringUtils.isNotBlank(soInfoById.getSalesDeptId())) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getSalesDeptId());
            dto.setOrgId(entity.getSalesOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtil.isNotEmpty(deptKingdee)) {
                resultMap.put("salesDeptCode", deptKingdee.getKingdeeDeptCode());
            }
        }

        //销售员
        String sellerId = entity.getSellerId();

        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(soInfoById.getSalesOrgId());
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getUserPostCode());
                resultMap.put("seller", kingSellerInfo.getUserName());
            }
        }
        //销售员
        String warehouseKeeperId = entity.getWarehouseKeeperId();
        if(StringUtils.isNotBlank(warehouseKeeperId)){
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
        String currency = StringUtils.isNotBlank(soInfoById.getCurrency()) ? soInfoById.getCurrency() : "CNY";
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
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Arrays.asList(entity.getWarehouseId()));

        List<Map<String, Object>> fEntityList = new ArrayList<>();
        List<String> soKingdeeDetailIdList = soDetailEntitieList.stream().map(req -> req.getKingdeeDetailId()).collect(Collectors.toList());
        resultMap.put("soKingdeeDetailIds", String.join(",", soKingdeeDetailIdList));
        for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
            Map<String, Object> map = new HashMap<>();
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
            BigDecimal taxPrice = MathUtil.multiply(soDetailEntity.getPrice(), multiplyTax);
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
            Boolean isPush = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), entity.getWarehouseId()))
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
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Arrays.asList(entity.getId()));
        String sellerId = entity.getSellerId();
        SysDepartmentUserNumberDTO deptUser = null;
        String deptId = "";
        if (StringUtils.isNotBlank(sellerId)) {
            deptUser = sysUserFeign.getDeptByUserId(sellerId);
        }
        if (Objects.nonNull(deptUser)) {
            deptId = deptUser.getDepartmentId();
        }

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(entity.getSalesOrgId(), entity.getWarehouseOrgId()));
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Arrays.asList(entity.getCustomerId()));

        //部门信息
        SysDepartmentDTO dept =StringUtils.isNotBlank(deptId)? sysUserFeign.getUserDeptById(deptId):null;
        //查询供应商信息
        SupplierEntity supplierEntity = null;
        if (StringUtils.isNotBlank(entity.getCarrierId())) {
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
            PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(customerInfoEntity.getPlatformType());
            String salesPlatformCode = salesPlatformEnum != null ? salesPlatformEnum.getKingdeeCode() : "";
            //平台类型
            resultMap.put("platformType", salesPlatformCode);
        }
        //销售部门
        if (ObjectUtil.isNotEmpty(dept)) {
            resultMap.put("salesDeptCode", dept.getCode());
        }
        //销售组织
        String salesOrgId = entity.getSalesOrgId();

        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(entity.getSalesOrgId());
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getDeptCode());
                resultMap.put("seller", kingSellerInfo.getUserName());
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

        //————————————————————财务信息SubHeadEntity——————————————————————
        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(currency)).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());
        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(salesOrgId)).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }


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
            BigDecimal taxPrice = MathUtil.multiply(detailEntity.getPrice(), multiplyTax);
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

            map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            map.put("remark", detailEntity.getRemark());
            //销售订单金蝶id
            map.put("soSyncKingdeeId",entity.getSyncKingdeeId());
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
        List<SoB2cDetailEntity> soB2cDetailList = soB2cFeign.listDetailByMainIds(Arrays.asList(soId));
        //获取销售出库单详情
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Arrays.asList(entity.getId()));
        String sellerId = entity.getSellerId();
        SysDepartmentUserNumberDTO deptUser = null;
        String deptId = "";
        if (StringUtils.isNotBlank(sellerId)) {
            deptUser = sysUserFeign.getDeptByUserId(sellerId);
        }
        if (Objects.nonNull(deptUser)) {
            deptId = deptUser.getDepartmentId();
        }

        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(soB2cEntity.getOrgId(), entity.getWarehouseOrgId()));
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Arrays.asList(entity.getCustomerId()));

        //查询供应商信息
        SupplierEntity supplierEntity = null;
        if (StringUtils.isNotBlank(entity.getCarrierId())) {
            supplierEntity = scmTaskFeign.getSupplierById(entity.getCarrierId());
        }
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(soB2cEntity.getCurrency()));
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getWarehouseId()));
        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Arrays.asList(entity.getWarehouseKeeperId()));
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
            PlatformDictEnum salesPlatformEnum = PlatformDictEnum.getByCode(customerInfoEntity.getPlatformType());
            String salesPlatformCode = salesPlatformEnum != null ? salesPlatformEnum.getKingdeeCode() : "";
            //平台类型
            resultMap.put("platformType", salesPlatformCode);
        }

        //部门
        if  (StringUtils.isNotBlank(deptId)) {
            DeptKingdeeDTO.FindDeptKingdeeDTO dto = new DeptKingdeeDTO.FindDeptKingdeeDTO();
            dto.setDeptId(entity.getSalesDeptId());
            dto.setOrgId(entity.getSalesOrgId());
            KingdeeDepartmentEntity deptKingdee = kingdeeFeign.getDeptKingdee(dto);
            if (ObjectUtil.isNotEmpty(deptKingdee)) {
                resultMap.put("salesDeptCode", deptKingdee.getKingdeeDeptCode());
            }
        }
        //销售组织
        String salesOrgId = soB2cEntity.getOrgId();
        //币别
        String currency = StringUtils.isNotBlank(soB2cEntity.getCurrency()) ? soB2cEntity.getCurrency() : "CNY";

        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgId(soB2cEntity.getOrgId());
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.XSY.getCode());
            //获取员工业务信息
            KingdeeOperatorRefPostDTO.OperatorDTO kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerCode", kingSellerInfo.getDeptCode());
                resultMap.put("seller", kingSellerInfo.getUserName());
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
        resultMap.put("logisticsChannelName",entity.getLogisticsChannelName());


        //————————————————————财务信息SubHeadEntity——————————————————————
        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(currency)).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());
        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(salesOrgId)).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }


        //————————————————————物料信息——————————————————————

        //是否支持下推仓位
        List<CfgSettingDTO.WarehouseLocationSettingDTO> pushKingdeeList = dmpTaskFeign.isPushKingdeeWarehouseLocation(Arrays.asList(entity.getWarehouseId()));

        List<Map<String, Object>> fEntityList = new ArrayList<>();
        for (SoOutstockDetailEntity detailEntity : soOutstockDetailEntityList) {
            SoB2cDetailEntity soB2cDetailEntity = soB2cDetailList.stream().filter(s -> s.getId().equals(detailEntity.getSoDetailId())).findFirst().orElse(null);
            Map<String, Object> map = new HashMap<>();
            map.put("skuNo", detailEntity.getSkuNo());
            map.put("actualQty", detailEntity.getActualQty());
            map.put("salesQty", Objects.nonNull(soB2cDetailEntity) ? soB2cDetailEntity.getQty() : detailEntity.getActualQty());
            map.put("planQty", detailEntity.getPlanQty());
            map.put("price", detailEntity.getPrice());
            BigDecimal taxRate = detailEntity.getTaxRate();

            //含税单价
            BigDecimal flagTaxRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(detailEntity.getPrice(), multiplyTax);
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
            Boolean isPush = pushKingdeeList.stream().filter(obj -> StrUtil.equals(obj.getWarehouseId(), entity.getWarehouseId()))
                    .map(CfgSettingDTO.WarehouseLocationSettingDTO::getIsPush).findFirst().orElse(Boolean.FALSE);
            if (isPush) {
                //仓位
                map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            }
            map.put("remark", detailEntity.getRemark());
            //销售订单金蝶id
            String soSyncKingdeeId=Objects.nonNull(soB2cDetailEntity)? soB2cDetailEntity.getKingdeeDetailId():"";
            map.put("soSyncKingdeeId",soSyncKingdeeId);
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
}
