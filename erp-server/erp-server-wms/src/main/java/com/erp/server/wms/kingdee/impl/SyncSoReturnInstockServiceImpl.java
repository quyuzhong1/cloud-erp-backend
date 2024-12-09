package com.erp.server.wms.kingdee.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncSoReturnInstockService;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnReceiveService;
import com.erp.server.wms.service.WmsPushMsgService;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SyncSoReturnInstockServiceImpl implements SyncSoReturnInstockService {
    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;

    @Resource
    private WmsPushMsgService wmsPushMsgService;

    @Resource
    private SoReturnFeign soReturnFeign;

    @Resource
    private SoReturnReceiveService soReturnReceiveService;

    @Override
    public Map<String, Object> syncDataToSdyFieldHandler(SoReturnInstockEntity entity,
                                                         SoReturnInstockDetailEntity detailEntity,
                                                         String operate,
                                                         List<SkuVO> skuVOList,
                                                         List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                         List<CurrencyDTO.ViewDTO> currencyList,
                                                         List<ProductDetailEntity> parentSkuList,
                                                         List<CustomerInfoEntity> customerInfoList,
                                                         List<BaseIdDTO.CodeDTO> companyEntities,
                                                         List<DictBasicEntity> dictBasicEntityList,
                                                         List<SoReturnEntity> soReturnEntityList,
                                                         List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                                                         List<SoReturnEntity> receiveReturnList) {

        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        //退货物流单号
        String rootNodeNoInitial = getRootNodeNoInitial(entity, soReturnEntityList, soReturnReceiveEntityList, receiveReturnList);

        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        shudiyunB2cOrderDTO.setBiz_uni_key(entity.getId() + detailEntity.getId());
        shudiyunB2cOrderDTO.setBiz_no(entity.getCode());
        if (entity.getBillDate() != null) {
            shudiyunB2cOrderDTO.setBiz_time(localDate.format(entity.getBillDate()));
        }
        //默认退货入库单
        shudiyunB2cOrderDTO.setTransaction_type("退货入库单");
        shudiyunB2cOrderDTO.setTransaction_sub_type("退货入库");
        shudiyunB2cOrderDTO.setBiz_status(ApproveStatusEnum.getName(entity.getApproveStatus()));
        shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, entity.getVersion(), detailEntity.getVersion()));

        //组织信息
        CustomerInfoEntity customerInfo = customerInfoList.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(customerInfo)) {

            String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(entity.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
            shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
            BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
            }

            if (customerInfo.getCurrency() == null) {
                shudiyunB2cOrderDTO.setSettlement_currency_code("");
            } else {
                shudiyunB2cOrderDTO.setSettlement_currency_code(customerInfo.getCurrency());
            }

            if (customerInfo.getTradeCurrency() == null) {
                shudiyunB2cOrderDTO.setSettlement_currency_code("");
            } else {
                shudiyunB2cOrderDTO.setTransaction_currency_code(customerInfo.getTradeCurrency());
            }

            shudiyunB2cOrderDTO.setPlatform_id(customerInfo.getPlatformType());
            String platformName = dictBasicEntityList.stream().filter(req -> req.getValue().equals(customerInfo.getPlatformType())).map(DictBasicEntity::getName).findFirst().orElse("");
            shudiyunB2cOrderDTO.setPlatform_name(platformName);
        }

        shudiyunB2cOrderDTO.setShop_no(entity.getCustomerId());
        shudiyunB2cOrderDTO.setShop_name(entity.getCustomerName());
        shudiyunB2cOrderDTO.setRoot_node_no(rootNodeNoInitial);
        shudiyunB2cOrderDTO.setGoods_no(detailEntity.getSkuNo());
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());
        shudiyunB2cOrderDTO.setGoods_name(skuVO.getSkuName());
        if (skuVO.getSpuNo() == null) {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSkuName());
        } else {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSpuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSpuName());
        }

        if (detailEntity.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            shudiyunB2cOrderDTO.setIs_gift(1);
        } else {
            shudiyunB2cOrderDTO.setIs_gift(0);
        }

        BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
            shudiyunB2cOrderDTO.setIs_comb(1);
        } else {
            bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO)) {
                shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
                BomChildrenSkuDTO finalBomChildrenSkuDTO = bomChildrenSkuDTO;
                String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
                shudiyunB2cOrderDTO.setSuite_name(skuName);
            }
        }

        if (OrderTypeEnum.B2B.getCode().equals(entity.getType())) {
            shudiyunB2cOrderDTO.setLogistic_company("【未知】");
            shudiyunB2cOrderDTO.setLogistic_company_code("【未知】");
        } else {
            shudiyunB2cOrderDTO.setLogistic_company("空");
            shudiyunB2cOrderDTO.setLogistic_company_code("空");
        }
        if (CharSequenceUtil.isBlank(entity.getReturnLogisticCode())) {
            shudiyunB2cOrderDTO.setDomestic_return_waybill_number("【未知】");
        } else {
            shudiyunB2cOrderDTO.setDomestic_return_waybill_number(entity.getReturnLogisticCode());
        }
        shudiyunB2cOrderDTO.setInternational_return_waybill_number("空");
        shudiyunB2cOrderDTO.setReturn_status(ApproveStatusEnum.getName(entity.getApproveStatus()));
        shudiyunB2cOrderDTO.setReturn_receipt_number(entity.getCode());
        shudiyunB2cOrderDTO.setReturned_quantity(detailEntity.getRealQty());

        shudiyunB2cOrderDTO.setRemark(detailEntity.getRemark());
        shudiyunB2cOrderDTO.setWarehouse_no(detailEntity.getWarehouseId());
        shudiyunB2cOrderDTO.setWarehouse_name(detailEntity.getWarehouseName());
        if (entity.getBillDate() != null) {
            shudiyunB2cOrderDTO.setReturn_receipt_time(localDate.format(entity.getBillDate()));
        }
        shudiyunB2cOrderDTO.setReturn_receipt_amount(detailEntity.getAmount());
        shudiyunB2cOrderDTO.setSuite_no("");
        shudiyunB2cOrderDTO.setSuite_name("");

        // 商品状态
        if (entity.getSourceType().equals(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode())) {
            shudiyunB2cOrderDTO.setGoods_status("平台收货");
        } else if (entity.getSourceType().equals(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode())) {
            shudiyunB2cOrderDTO.setGoods_status("第三方仓收货");
        } else {
            shudiyunB2cOrderDTO.setGoods_status("本地仓收货");
        }

        if (entity.getApproveTime() != null) {
            shudiyunB2cOrderDTO.setDelivery_time(localDateTime.format(entity.getApproveTime()));
        }
        shudiyunB2cOrderDTO.setGoods_transaction_quantity(detailEntity.getRealQty());
        shudiyunB2cOrderDTO.setUnit(skuVO.getUnitName());
        shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(skuVO.getRetailPrice());
        if (CollectionUtils.isNotEmpty(currencyList)) {
            shudiyunB2cOrderDTO.setTransaction_currency(currencyList.get(0).getName());
            shudiyunB2cOrderDTO.setTransaction_currency_code(currencyList.get(0).getId());
        }

        shudiyunB2cOrderDTO.setMsku_code(skuVO.getSpuNo());
        shudiyunB2cOrderDTO.setMsku_name(skuVO.getSpuName());
        shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
        shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());

        shudiyunB2cOrderDTO.setSource_system("SDC");
        shudiyunB2cOrderDTO.setRoot_node_no_initial(rootNodeNoInitial);

        return BeanUtil.beanToMap(shudiyunB2cOrderDTO);

    }

    @Override
    public void syncDataToSdy(SoReturnInstockEntity entity,
                              List<SoReturnInstockDetailEntity> detailEntities,
                              String operate,
                              List<SkuVO> skuVOList,
                              List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                              List<CurrencyDTO.ViewDTO> currencyList,
                              List<ProductDetailEntity> parentSkuList,
                              List<CustomerInfoEntity> customerInfoList,
                              List<BaseIdDTO.CodeDTO> companyEntities,
                              List<DictBasicEntity> dictBasicEntityList,
                              List<SoReturnEntity> soReturnEntityList,
                              List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                              List<SoReturnEntity> receiveReturnList) {
        for (SoReturnInstockDetailEntity detailEntity : detailEntities) {
            WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
            wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            wmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SO_RETURN_INSTOCK.getCode());
            wmsPushMsgEntity.setSourceId(detailEntity.getId());
            wmsPushMsgEntity.setSourceCode(entity.getCode() + "_" + detailEntity.getSkuNo());
            wmsPushMsgEntity.setSyncOperate(operate);
            wmsPushMsgEntity.setPushData(JSON.toJSONString(this.syncDataToSdyFieldHandler(entity, detailEntity, operate, skuVOList, bomChildrenSkuDTOS, currencyList, parentSkuList, customerInfoList, companyEntities, dictBasicEntityList, soReturnEntityList, soReturnReceiveEntityList, receiveReturnList)));
            wmsPushMsgService.save(wmsPushMsgEntity);
        }
    }

    private String getRootNodeNoInitial(SoReturnInstockEntity entity,
                                        List<SoReturnEntity> soReturnEntityList,
                                        List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                                        List<SoReturnEntity> receiveReturnList) {
        String rootNodeNoInitial = entity.getCode(); // 默认值是 entity.getCode()

        if (SourceTypeEnum.SO_RETURN.getCode().equals(entity.getSourceType())) {
            SoReturnEntity soReturnEntity = soReturnEntityList.stream().filter(req -> req.getId().equals(entity.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(soReturnEntity)) {
                rootNodeNoInitial = soReturnEntity.getCode();
            }
        } else if (SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(entity.getSourceType())) {
            String receiveSourceId = soReturnReceiveEntityList.stream().filter(req -> req.getId().equals(entity.getSourceId())).map(SoReturnReceiveEntity::getSourceId).findFirst().orElse("");
            if (CharSequenceUtil.isNotBlank(receiveSourceId)) {
                SoReturnEntity soReturnEntity = receiveReturnList.stream().filter(req -> req.getId().equals(receiveSourceId)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(soReturnEntity)) {
                    rootNodeNoInitial = soReturnEntity.getCode();
                }
            }
        }
        return rootNodeNoInitial;
    }

}
