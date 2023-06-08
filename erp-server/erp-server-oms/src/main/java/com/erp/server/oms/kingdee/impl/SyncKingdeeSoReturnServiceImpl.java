package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.InvoiceTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncKingdeeStatusEnum;
import com.common.core.utils.MathUtil;
import com.common.message.constant.RocketMqTopic;
import com.common.message.enums.RocketMqTagEnum;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.oms.dto.CustomerAddressDTO;
import com.erp.model.oms.dto.CustomerContactDTO;
import com.erp.model.oms.dto.InvoiceDTO;
import com.erp.model.oms.dto.SellerDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.model.wms.enums.ReturnReasonEnum;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.oms.kingdee.SyncKingdeeCustomerService;
import com.erp.server.oms.kingdee.SyncKingdeeSoReturnService;
import com.erp.server.oms.service.*;
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
 * @Author Luo_WG
 * @Date 2023/5/25 10:53
 **/
@Slf4j
@Service
public class SyncKingdeeSoReturnServiceImpl implements SyncKingdeeSoReturnService {
    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnDetailService soReturnDetailService;

    @Resource
    private CustomerInfoService customerInfoService;

    @Resource
    private SoInfoService soInfoService;

    @Resource
    private SoReturnService soReturnService;

    @Resource
    private SoDetailService soDetailService;

    @Resource
    private MQProducerService mQProducerService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Override
    public void syncDataToKingdee(SoReturnEntity entity, String operate) {
        //客户信息
        List<CustomerInfoEntity> customerInfoEntitieList = customerInfoService.listByIds(Arrays.asList(entity.getCustomerId()));
        //退货单
        SoReturnEntity soReturnEntity = soReturnService.getById(entity.getId());
        //退货详情
        List<SoReturnDetailEntity> returnDetailEntityList = soReturnDetailService.listDetailByMainId(entity.getId());
        //销售单
        SoInfoEntity soInfoEntity = soInfoService.getById(entity.getSourceId());
        //销售单明细
        List<SoDetailEntity> soDetailEntitieList = soDetailService.listSoDetailByMainIds(Arrays.asList(soInfoEntity.getId()));
        //仓库
        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(entity.getWarehouseId()));
        //部门信息
        SysDepartmentDTO dept = sysUserFeign.getUserDeptById(soInfoEntity.getSalesDeptId());
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
        //销售部门
        if (ObjectUtil.isNotEmpty(dept)) {
            resultMap.put("salesDeptCode", dept.getCode());
        }
        //客户
        if (CollectionUtils.isNotEmpty(customerInfoEntitieList)) {
            CustomerInfoEntity customerInfoEntity = customerInfoEntitieList.stream().filter(obj -> obj.getId().equals(entity.getCustomerId())).findFirst().orElse(new CustomerInfoEntity());
            resultMap.put("customerCode", customerInfoEntity.getCode());
            //收款条件
            List<DictBasicDTO.ViewDTO> collectionTermsList = dictBasicService.getByKey("collectionTerms");
            DictBasicDTO.ViewDTO viewDTO = collectionTermsList.stream().filter(req -> req.getValue().equals(customerInfoEntity.getCode())).findFirst().orElse(new DictBasicDTO.ViewDTO());
            resultMap.put("collectionTerms", viewDTO.getRemark());
        }
        //结算币别
        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(soReturnEntity.getCurrency())).findFirst().orElse(new CurrencyDTO.ViewDTO());
        resultMap.put("currencyCode", viewDTO.getKingdeeCode());
        //结算组织
        if (CollectionUtils.isNotEmpty(accountingCompanyList)) {
            String salesOrgCode = accountingCompanyList.stream().filter(obj -> obj.getId().equals(soReturnEntity.getSalesOrgId())).map(BaseIdDTO.CodeDTO::getCode).findFirst().orElse(null);
            resultMap.put("salesOrgCode", salesOrgCode);
        }
        List<String> soKingdeeDetailIdList = soDetailEntitieList.stream().map(req -> req.getKingdeeDetailId()).collect(Collectors.toList());

        resultMap.put("soKingdeeDetailIds", String.join(",", soKingdeeDetailIdList));
        //金蝶 FEntity:物料信息
        List<Map<String,Object>> list = new ArrayList<>();
        for (SoReturnDetailEntity detailEntity : returnDetailEntityList) {
            SoDetailEntity soDetailEntity = soDetailEntitieList.stream().filter(req -> req.getId().equals(detailEntity.getSourceDetailId())).findFirst().orElse(new SoDetailEntity());
            Map<String,Object> map = new HashMap<>();
            //退货原因
            if (StringUtils.isNotBlank(detailEntity.getReturnReasonDict())) {
                resultMap.put("returnReason", ReturnReasonEnum.getEnum(detailEntity.getReturnReasonDict()).getKingdeeCode());
            }
            //销售订单金蝶id
            map.put("soSyncKingdeeId", soInfoEntity.getSyncKingdeeId());
            //物料编码
            map.put("skuNo", detailEntity.getSkuNo());
            //退货数量
            map.put("returnQty", detailEntity.getReturnQty());
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
            //退货日期
            map.put("billDate", entity.getBillDate());
            //备注
            map.put("remark", detailEntity.getRemark());
            if (entity.getSourceType().equals(SourceTypeEnum.SO_INFO.getCode())) {
                //原单类型
                map.put("FSrcBillTypeID", "SAL_SaleOrder");
                //原单编号
                map.put("FSrcBillNo", soInfoEntity.getCode());
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
                return soReturnService.updateSyncKingdeeStatus(entity.getId(), SyncKingdeeStatusEnum.IN_SYNC.getCode(),"", operate);
            }
            return Boolean.TRUE;
        });
    }
}
