package com.erp.server.wms.kingdee.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.model.sys.dto.KingdeePostDTO;
import com.erp.model.sys.entity.KingdeeBusinessOperatorEntity;
import com.erp.model.sys.enums.KingdeeBusinessOperatorTypeEnum;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.sys.feign.KingdeeFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.WarehouseService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 同步销售退货单到金蝶
 *
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeSoReturnServiceImpl implements SyncKingdeeSoReturnService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private CustomerFeign customerFeign;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoInfoFeign soInfoFeign;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private SoReturnInstockService soReturnInstockService;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private KingdeeFeign kingdeeFeign;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Override
    public void syncDataToKingdee(SoReturnInstockEntity entity, String operate) {
        //更新同步状态为待同步
        soReturnInstockService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.TO_BE_SYNC.getCode(), "", operate);

        List<SoReturnInstockDetailEntity> returnInstockDetailEntities = soReturnInstockDetailService.listDetailByMainId(entity.getId());

        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerFeign.listCustomerByIds(Arrays.asList(entity.getCustomerId()));
        //退货单
        SoReturnEntity soReturnEntity = soReturnFeign.getSoReturnById(entity.getSourceId());
        //退货详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnFeign.listDetailByMainId(entity.getSourceId());
        //销售单
        SoInfoEntity soInfoEntity = new SoInfoEntity();
        if (ObjectUtils.isNotEmpty(soReturnEntity)) {
            soInfoEntity = soInfoFeign.getSoInfoById(soReturnEntity.getSourceId());
        }

        //销售单明细
        List<SoDetailEntity> soDetailEntitieList = soInfoFeign.listSoDetailByMainIds(Arrays.asList(soInfoEntity.getId()));
        //仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = warehouseService.listWarehouseByIds(Arrays.asList(entity.getWarehouseId()));
        List<String> orgIdList = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getOrgId).collect(Collectors.toList());
        orgIdList.add(entity.getSalesOrgId());
        orgIdList.add(entity.getInventoryOrgId());
        //组织信息
        List<BaseIdDTO.CodeDTO> accountingCompanyList = sysUserFeign.getAccountingCompanyList(orgIdList);
        //获取币别信息
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(Arrays.asList(soInfoEntity.getCurrency()));
        Map<String, Object> resultMap = new HashMap<>();
        //金蝶id
        resultMap.put("syncKingdeeId", entity.getSyncKingdeeId());
        //业务id
        resultMap.put("id", entity.getId());
        //客户编号
        resultMap.put("code", entity.getCode());
        //单据日期
        resultMap.put("billDate", entity.getBillDate());
        //组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //销售组织
            resultMap.put("salesOrgCode", salesOrgCode);
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //库存组织
            resultMap.put("inventoryOrgCode", inventoryOrgCode);
        }
        //销售员
        String sellerId = entity.getSellerId();
        String deptCode = "";
        String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse("");

        //当为空的时候 就取岗位表的
        KingdeePostDTO.FindUserKingdeePostInfoDTO findUserPostKingdee = new KingdeePostDTO.FindUserKingdeePostInfoDTO();
        findUserPostKingdee.setUserId(sellerId);
        findUserPostKingdee.setOrgCode(salesOrgCode);
        KingdeePostDTO.UserKingdeePostInfoDTO kingdeePost = kingdeeFeign.getUserKingdeePost(findUserPostKingdee);
        if (kingdeePost != null) {
            deptCode = kingdeePost.getKingdeeDeptCode();
        }
        resultMap.put("sellerDeptCode", deptCode);
        //获取业务员信息
        if (StringUtils.isNotBlank(sellerId)) {
            KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO findBusinessOperator = new KingdeeBusinessOperatorDTO.FindBusinessOperatorDTO();
            findBusinessOperator.setOrgCode(salesOrgCode);
            findBusinessOperator.setUserId(sellerId);
            findBusinessOperator.setBusinessOperatorType(KingdeeBusinessOperatorTypeEnum.YSY.getCode());
            //获取员工业务信息
            KingdeeBusinessOperatorEntity kingSellerInfo = kingdeeFeign.getBusinessOperator(findBusinessOperator);
            //销售员
            if (!Objects.isNull(kingSellerInfo)) {
                resultMap.put("sellerUserCode", kingSellerInfo.getKingdeePostCode());
            }
        }

        if (StringUtils.isNotBlank(entity.getWarehouseKeeperId())) {
            //仓管员编码
            FindUserDTO findUserDTO = sysUserFeign.getUserByUserId(entity.getWarehouseKeeperId());
            if (ObjectUtils.isNotEmpty(findUserDTO)) {
                resultMap.put("warehouseKeeperCode", findUserDTO.getCode());
            }
        }

        //客户
        if (CollectionUtils.isNotEmpty(customerInfoEntitieList)) {
            CustomerInfoEntity customerInfoEntity = customerInfoEntitieList.stream().filter(obj -> obj.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            resultMap.put("customerCode", customerInfoEntity.getCode());
            //收款条件
            List<DictBasicDTO.ViewDTO> collectionTermsList = customerFeign.getDictBasicByKey("collectionTerms");
            DictBasicDTO.ViewDTO viewDTO = collectionTermsList.stream().filter(req -> req.getValue().equals(customerInfoEntity.getCode())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            resultMap.put("collectionTerms", viewDTO.getRemark());

            //获取币别信息
            List<CurrencyDTO.ViewDTO> currencyListt = sysUserFeign.listByCurrency(Arrays.asList(customerInfoEntity.getCurrency()));
            CurrencyDTO.ViewDTO currencyDTO = currencyListt.stream().filter(req -> req.getId().equals(customerInfoEntity.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
            resultMap.put("currencyCode", currencyDTO.getKingdeeCode());
        }
    /*    //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soReturnEntity.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());*/

        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            BaseIdDTO.CodeDTO codeDTO = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getSalesOrgId())).findFirst().orElse(new BaseIdDTO.CodeDTO());
            resultMap.put("salesOrgCode", codeDTO.getCode());
        }
        List<String> soKingdeeDetailIdList = soDetailEntitieList.stream().map(req -> req.getKingdeeDetailId()).collect(Collectors.toList());

        resultMap.put("soKingdeeDetailIds", String.join(",", soKingdeeDetailIdList));
        //金蝶 FEntity:物料信息
        List<Map<String, Object>> list = new ArrayList<>();
        for (SoReturnInstockDetailEntity detailEntity : returnInstockDetailEntities) {
            SoReturnDetailEntity soReturnDetailEntity = returnDetailEntityList.stream().filter(req -> req.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoReturnDetailEntity());
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(soReturnDetailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Map<String, Object> map = new HashMap<>();
            //退货原因
            if (StringUtils.isNotBlank(detailEntity.getReturnReasonDict())) {
                resultMap.put("returnReason", ReturnReasonEnum.getEnum(detailEntity.getReturnReasonDict()).getKingdeeCode());
            }
            //销售订单金蝶id
            map.put("soSyncKingdeeId", entity.getSyncKingdeeId());
            //物料编码
            map.put("skuNo", detailEntity.getSkuNo());
            //退货数量
            map.put("returnQty", detailEntity.getRealQty());
            map.put("salesQty", soDetailEntity.getQty());
            //单价
            map.put("price", soDetailEntity.getPrice());
            //含税单价
            BigDecimal flagTaxRate = MathUtil.divide(soDetailEntity.getTaxRate(), MathUtil.BigDecimal_100);
            BigDecimal multiplyTax = MathUtil.add(flagTaxRate, MathUtil.BigDecimal_1);
            BigDecimal taxPrice = MathUtil.multiply(soDetailEntity.getPrice(), multiplyTax);
            //含税单价
            map.put("taxPrice", taxPrice);
            //是否赠品
            map.put("isGift", soDetailEntity.getIsGift());
            //金额
            map.put("amount", soDetailEntity.getAmount());
//            //税率
//            map.put("isGift", flagTaxRate);
            //退货类型
            map.put("returnType", detailEntity.getReturnTypeDict());
            //货主
            map.put("salesOrgCode", resultMap.get("salesOrgCode"));
            //仓库
            if (CollectionUtils.isNotEmpty(warehouseList)) {
                String warehouseCode = warehouseList.stream().filter(obj -> obj.getId().equals(entity.getWarehouseId())).map(WarehouseDTO.UpdateDTO::getKingdeeWarehouseCode).findFirst().orElse("");
                //仓库
                map.put("warehouseCode", warehouseCode);
            }
            //仓位
            map.put("warehouseLocation", detailEntity.getWarehouseLocation());
            //退货日期
            map.put("billDate", entity.getBillDate());
            String inventoryOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(entity.getInventoryOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            //库存组织
            map.put("inventoryOrgCode", inventoryOrgCode);
            //备注
            map.put("remark", detailEntity.getRemark());
            if (ObjectUtils.isNotEmpty(soReturnEntity)) {
                if (soReturnEntity.getSourceType().equals(SourceTypeEnum.SO_INFO.getCode())) {
                    //原单类型
                    map.put("FSrcBillTypeID", "SAL_SaleOrder");
                    //原单编号
                    map.put("FSrcBillNo", soReturnEntity.getSourceCode());
                }
            }

            list.add(map);
        }
        resultMap.put("FEntityList", list);
        resultMap.put("operate", operate);
        //异步推送mq
        CompletableFuture.supplyAsync(() -> {
            SendResult result = mQProducerService.syncClassMsg(RocketMqTopic.SYNC_KINGDEE_ERP_TOPIC, RocketMqTagEnum.KINGDEE_SO_RETURN_TAG.getName(), resultMap, String.valueOf(resultMap.get("id")));
            if (result.getSendStatus().equals(SendStatus.SEND_OK)) {
                //mq发送成更新业务表状态及时间
                return soReturnInstockService.updateSyncKingdeeStatus(entity.getId(), SyncStatusEnum.IN_SYNC.getCode(), "", operate);
            }
            return Boolean.TRUE;
        });
    }
}
