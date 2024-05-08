package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpPullTaskFeignDTO;
import com.common.business.dto.DmpPushTaskFeignDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.*;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.oms.entity.*;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.*;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.SoDeliveryNoticeDetailEntity;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.dmp.feign.DmpMqFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.service.SoDeliveryNoticeDetailService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.WarehouseService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
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
    public void syncDataToKingdee(SoOutstockEntity entity, String operate) {
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
            sendMqAndSaveTask(entity, operate, resultMap);
            return;
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
        //部门信息
        SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoById.getSalesDeptId());
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
        //销售部门
        if (ObjectUtil.isNotEmpty(dept)) {
            resultMap.put("salesDeptCode", dept.getCode());
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

            map.put("warehouseLocation", detailEntity.getWarehouseLocation());
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

        //生成任务
        sendMqAndSaveTask(entity, operate, resultMap);
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
    public void syncB2cDataToKingdee(SoOutstockEntity entity, String operate) {
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
            sendMqAndSaveTask(entity, operate, resultMap);
            return;
        }
        //销售单信息
        SoB2cEntity soB2cEntity = soB2cFeign.getById(soId);
        if (Objects.isNull(soB2cEntity)) {
            return;
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

        //部门信息
        SysDepartmentDTO dept =StringUtils.isNotBlank(deptId)? sysUserFeign.getUserDeptById(deptId):null;
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
        //销售部门
        if (ObjectUtil.isNotEmpty(dept)) {
            resultMap.put("salesDeptCode", dept.getCode());
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

            map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            map.put("remark", detailEntity.getRemark());
            //销售订单金蝶id
            String soSyncKingdeeId=Objects.nonNull(soB2cDetailEntity)? soB2cDetailEntity.getKingdeeDetailId():"";
            map.put("soSyncKingdeeId",soSyncKingdeeId);
            map.put("FSrcType", "SAL_SaleOrder");
            map.put("FSrcBillNo", soB2cEntity.getCode());
            map.put("FSoorDerno", soB2cEntity.getCode());
            // 第三方单据编号
            if (StringUtils.isNotBlank(detailEntity.getPlatformCode())){
                map.put("FETHIRDBILLNO", detailEntity.getPlatformCode());
            }
            fEntityList.add(map);
        }
        resultMap.put("FEntity", fEntityList);

        //生成任务
        sendMqAndSaveTask(entity, operate, resultMap);
    }

    /**
     * @param entity
     * @param operate
     * @param resultMap
     * @description: 生成任务
     * @author Will
     * @date: 2023/10/16 9:17
     */
    private void sendMqAndSaveTask(SoOutstockEntity entity, String operate, Map<String, Object> resultMap) {
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
        dmpMqFeign.sendMqAndSaveTask(dmpSyncTaskDTO);
    }

    /**
     * 推送订单到mq
     *
     * @param entity
     * @param syncOperate
     */
    @Override
    public void syncOrderToDmp(SoOutstockEntity entity, String syncOperate) {
        //判断是否增加任务
        //判断是否需要推送记录
        //判断是否需要推送记录
        if (!dmpTaskFeign.needPushMQ(LocalDateTime.now())) {
            return;
        }
        Map<String, Object> resultMap = new HashMap<>();

        //业务id
        resultMap.put("id", entity.getId());
        //客户编号
        resultMap.put("code", entity.getCode());
        resultMap.put("operate", syncOperate);
        DmpPullTaskFeignDTO dto = new DmpPullTaskFeignDTO()
                .setMqData(JSON.toJSONString(resultMap))
                .setMqTopic(RocketMqTopic.SYNC_SO_OUTSTOCK_ORDER_TO_DMP_TOPIC)
                .setMqTag(RocketMqTagEnum.APPROVED_SO_OUTSTOCK_ORDER_TO_DMP_TAG.getName())
                .setSourceCode(entity.getCode())
                .setSourceId(entity.getId())
                .setSourceType(SourceTypeEnum.SO_OUTSTOCK.getCode())
                .setSourcePlatformName(PlatformEnum.ERP_WMS.getDesc())
                .setTargetPlatformName(PlatformEnum.ERP_DMP.getDesc())
                .setSyncOperate(syncOperate);
        log.info("推送消息开始：{}", dto.toString());
        String dmpPullTaskId = dmpTaskFeign.savePullTask(dto);
        resultMap.put("dmpSyncTaskId", dmpPullTaskId);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_SO_OUTSTOCK_ORDER_TO_DMP_TOPIC, RocketMqTagEnum.APPROVED_SO_OUTSTOCK_ORDER_TO_DMP_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (!result.getSendStatus().equals(SendStatus.SEND_OK)) {
                log.error("soReturn.syncDataToDmp 推送MQ失败 :" + resultMap.get("id"));
            }
            return Boolean.TRUE;
        });
    }
}
