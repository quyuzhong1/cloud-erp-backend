package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.ApiModuleTypeEnum;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.SoDetailEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.enums.BillTypeEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.entity.PurchaseOrderDetailEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.dto.SysDepartmentUserNumberDTO;
import com.erp.model.wms.dto.SoOutstockDetailDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.ReturnModeEnum;
import com.erp.model.wms.enums.ReturnOrderSourceEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.OmsTaskFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.ScmTaskFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 同步金蝶采购退货单
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
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoOutstockService soOutstockService;

    @Resource
    private SoOutstockDetailService soOutstockDetailService;

    @Resource
    private SoDeliveryNoticeDetailService soDeliveryNoticeDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WarehouseService warehouseService;

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
    public void syncDataToKingdee(SoOutstockEntity entity, String operate) {
        Map<String, Object> resultMap = new HashMap<>();
        //获取销售出库单详情
        List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(Arrays.asList(entity.getId()));
        //销售单信息
        SoInfoEntity soInfoById = soInfoFeign.getSoInfoById(entity.getSoId());
        //销售单明细
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByMainIds(Arrays.asList(soInfoById.getId()));
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(Arrays.asList(soInfoById.getSalesOrgId(),entity.getWarehouseOrgId()));
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Arrays.asList(entity.getCustomerId()));
        //部门信息
        SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoById.getSalesDeptId());
        //查询供应商信息
        SupplierEntity supplierEntity = scmTaskFeign.getSupplierById(entity.getCarrierId());
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(soInfoById.getCurrency()));
        //仓库
        List<WarehouseEntity> warehouseList = warehouseService.listByIds(Arrays.asList(entity.getWarehouseId()));
        //员工岗位
        List<KingdeePostDTO.UserKingdeePostInfoDTO> userKingdeePostInfoList = sysUserFeign.listUserKingdeePostByUserIds(Arrays.asList(entity.getWarehouseKeeperId()));

        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //退货单号
        resultMap.put("code", entity.getCode());
        //单据类型
        resultMap.put("orderType", entity.getOrderType());
        //单据日期
        resultMap.put("billDate", entity.getActualDeliveryDate());
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
            //平台类型
            resultMap.put("platformType", customerInfoEntity.getPlatformType().getKingdeeCode());
        }
        //销售部门
        if (ObjectUtil.isNotEmpty(dept)) {
            resultMap.put("salesDeptCode", dept.getCode());
        }
        if (CollectionUtils.isNotEmpty(userKingdeePostInfoList)) {
            //仓管员
            resultMap.put("warehouseKeeperCode", userKingdeePostInfoList.get(0).getKingdeeUserCode());
        }

        //仓管员
        resultMap.put("trackNo", entity.getWarehouseKeeperId());
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
                resultMap.put("warehouseOrgCode", warehouseOrgCode);
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
            fEntityList.add(map);
        }
        resultMap.put("FEntity", fEntityList);

        //操作（枚举SyncKingdeeOperateEnum）
        resultMap.put("operate", operate);

        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SO_OUTSTOCK_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return soOutstockService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(), "", operate);
            }
            return Boolean.TRUE;
        });
    }
}
