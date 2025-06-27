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
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.dmp.dto.DmpReturnInstockDTO;
import com.erp.model.dmp.dto.DmpReturnInstockDetailDTO;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.CustomerInfoEntity;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.entity.SoInfoEntity;
import com.erp.model.oms.entity.SoReturnEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.model.wms.entity.SoReturnReceiveEntity;
import com.erp.model.wms.entity.WmsPushMsgEntity;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.kingdee.SyncSoReturnInstockService;
import com.erp.server.wms.service.DictBasicService;
import com.erp.server.wms.service.SoOutstockDetailService;
import com.erp.server.wms.service.SoOutstockService;
import com.erp.server.wms.service.SoReturnInstockDetailService;
import com.erp.server.wms.service.SoReturnInstockService;
import com.erp.server.wms.service.SoReturnReceiveService;
import com.erp.server.wms.service.WmsPushMsgService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyncSoReturnInstockServiceImpl implements SyncSoReturnInstockService {
    @Resource
    private WmsPushMsgService wmsPushMsgService;
    
    @Resource
    private SoOutstockService soOutstockService;
    @Resource
    private SoOutstockDetailService soOutstockDetailService;
    @Resource
    private SyncKingdeeSoOutstockService syncKingdeeSoOutstockService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private SoB2cFeign soB2cFeign;
    @Resource
    private SoInfoFeign soInfoFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private SoReturnInstockService soReturnInstockService;
    @Resource
    private SoReturnInstockDetailService soReturnInstockDetailService;
    @Resource
    private SoReturnFeign soReturnFeign;
    @Resource
    private SoReturnReceiveService soReturnReceiveService;
    @Resource
    private DictBasicService dictBasicService;

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
                                                         List<SoReturnEntity> soReturnEntityList,
                                                         List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                                                         List<SoReturnEntity> receiveReturnList,
                                                         String countryCode,
                                                         String partitionId,
                                                         String dictPlatform,
                                                         List<DictBasicEntity> omsAllDictList,
                                                         List<DictPartitionEntity> partitionEntityList,
                                                         List<DictCountryEntity> countryEntityList,
                                                         List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                         List<SysDepartmentEntity> deptList,
                                                         List<CfgCountryPartitionEntity> countryPartitionEntityList
    ) {

        DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter localDate = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 字典分组
        Map<String, List<DictBasicEntity>> dictGroupMap = omsAllDictList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
        // 销售平台
        List<DictBasicEntity> dictBasicEntityList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(), Collections.emptyList());
        // 数帝云军区一级部门映射
        List<DictBasicEntity> sdyPartitionDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(), Collections.emptyList());
        // 数帝云平台二级部门映射
        List<DictBasicEntity> sdyPlatformDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType(), Collections.emptyList());


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

            String subPlatformType = customerInfo.getPlatformType();
            if (StringUtils.isNotBlank(subPlatformType)) {
                DictBasicEntity dictBasicEntity = dictBasicEntityList.stream().filter(req -> req.getName().equals(subPlatformType)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(dictBasicEntity)) {
                	shudiyunB2cOrderDTO.setPlatform_id(dictBasicEntity.getRemark());
                	shudiyunB2cOrderDTO.setPlatform_name(dictBasicEntity.getRemark());
                    shudiyunB2cOrderDTO.setSubplatform_no(dictBasicEntity.getValue());
                    shudiyunB2cOrderDTO.setSubplatform_name(dictBasicEntity.getValue());
                }
            }
            shudiyunB2cOrderDTO.setShop_no(customerInfo.getCode());
            shudiyunB2cOrderDTO.setShop_name(customerInfo.getName());
        }

        // 父节点单号=平台退货订单号
        shudiyunB2cOrderDTO.setParent_node_no(rootNodeNoInitial);
        String targetRootNode = rootNodeNoInitial;
        if (StringUtils.isNotBlank(entity.getPlatformOrderCode())){
            targetRootNode = entity.getPlatformOrderCode();
        }
        shudiyunB2cOrderDTO.setRoot_node_no(targetRootNode);
        shudiyunB2cOrderDTO.setRoot_node_no_initial(targetRootNode);
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

//        if (detailEntity.getReturnAmount().compareTo(BigDecimal.ZERO) <= 0) {
//            shudiyunB2cOrderDTO.setIs_gift(1);
//        } else {
//            shudiyunB2cOrderDTO.setIs_gift(0);
//        }

        BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
            shudiyunB2cOrderDTO.setIs_comb(1);
            shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getParentSkuNo());
            BomChildrenSkuDTO finalBomChildrenSkuDTO1 = bomChildrenSkuDTO;
            String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO1.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            shudiyunB2cOrderDTO.setSuite_name(skuName);
        } else {
            bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
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
        shudiyunB2cOrderDTO.setReturn_receipt_amount(detailEntity.getReturnAmount());

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

        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(detailEntity.getCurrency())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(viewDTO)) {
            shudiyunB2cOrderDTO.setTransaction_currency(viewDTO.getName());
            shudiyunB2cOrderDTO.setTransaction_currency_code(viewDTO.getId());
        }

        shudiyunB2cOrderDTO.setMsku_code(skuVO.getSpuNo());
        shudiyunB2cOrderDTO.setMsku_name(skuVO.getSpuName());
        shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
        shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());

        shudiyunB2cOrderDTO.setSource_system("SDC");

        // 国家编码
        // 国家名称
        String countryName = "";
        // 区域编码
        String regionCode = "";
        // 区域名称
        String regionName = "";
        // 军区编码
        String militaryRegionCode = "";
        // 军区名称
        String militaryRegionName = "";
        // 部门编码
        String departmentCode = "";
        // 部门名称
        String departmentName= "";

        // 国家信息为空替换为客户对应国家
        if (StringUtils.isBlank(countryCode) && null != customerInfo){
            countryCode = customerInfo.getCountryId();
            // 替换分区
            String finalCountryCode = countryCode;
            CfgCountryPartitionEntity cfgCountryPartitionEntity = countryPartitionEntityList.stream().filter(e -> e.getCountry().equalsIgnoreCase(finalCountryCode)).findFirst().orElse(null);
            if ( null != cfgCountryPartitionEntity){
                partitionId = cfgCountryPartitionEntity.getPartitionId();
            }
            dictPlatform = customerInfo.getPlatformType();
        }

        String finalCountryCode = countryCode;
        DictCountryEntity dictCountryEntity = countryEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalCountryCode)).findFirst().orElse(null);
        if (null != dictCountryEntity){
            countryName = dictCountryEntity.getShortNameCn();
            // 区域编码
            regionCode = dictCountryEntity.getSubregionCode();
            // 区域名称
            DictGlobalAreaEntity dictGlobalAreaEntity = dictGlobalEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(dictCountryEntity.getSubregionCode())).findFirst().orElse(null);
            if (null != dictGlobalAreaEntity){
                regionName = dictGlobalAreaEntity.getSubregionName();
            }
        }

        String finalPartitionId = partitionId;
        DictPartitionEntity dictPartitionEntity = partitionEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalPartitionId)).findFirst().orElse(null);
        if (null != dictPartitionEntity){
            // 军区编码
            militaryRegionCode = dictPartitionEntity.getCode();
            // 军区名称
            militaryRegionName = dictPartitionEntity.getName();
            // 军区一级部门映射
            DictBasicEntity sdyPartitionDeptEntity = sdyPartitionDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(dictPartitionEntity.getCode())).findFirst().orElse(null);
            // 销售平台二级部门映射
            String finalDictPlatform = dictPlatform;
            List<DictBasicEntity> sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(finalDictPlatform)).collect(Collectors.toList());

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
                    departmentCode = departmentDTO.getCode();
                    // 部门名称
                    departmentName = departmentDTO.getName();
                }
            }
        }
        // 国家编码
        shudiyunB2cOrderDTO.setCountry_code(countryCode);
        // 国家名称
        shudiyunB2cOrderDTO.setCountry(countryName);
        // 区域编码
        shudiyunB2cOrderDTO.setRegion_code(regionCode);
        // 区域名称
        shudiyunB2cOrderDTO.setRegion_name(regionName);
        // 军区编码
        shudiyunB2cOrderDTO.setMilitary_region_code(militaryRegionCode);
        // 军区名称
        shudiyunB2cOrderDTO.setMilitary_region_name(militaryRegionName);
        // 部门编码
        shudiyunB2cOrderDTO.setDepartment_code(departmentCode);
        // 部门名称
        shudiyunB2cOrderDTO.setDepartment_name(departmentName);



        return BeanUtil.beanToMap(shudiyunB2cOrderDTO);

    }
    
    @Override
    public Map<String, Object> syncNewDataToSdyFieldHandler(SoReturnInstockEntity entity,
                                                         SoReturnInstockDetailEntity detailEntity,
                                                         String operate,
                                                         List<SkuVO> skuVOList,
                                                         List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                         List<CurrencyDTO.ViewDTO> currencyList,
                                                         List<ProductDetailEntity> parentSkuList,
                                                         List<CustomerInfoEntity> customerInfoList,
                                                         List<BaseIdDTO.CodeDTO> companyEntities,
                                                         List<SoReturnEntity> soReturnEntityList,
                                                         List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                                                         List<SoReturnEntity> receiveReturnList,
                                                         String countryCode,
                                                         String partitionId,
                                                         String dictPlatform,
                                                         List<DictBasicEntity> omsAllDictList,
                                                         List<DictPartitionEntity> partitionEntityList,
                                                         List<DictCountryEntity> countryEntityList,
                                                         List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                         List<SysDepartmentEntity> deptList,
                                                         List<CfgCountryPartitionEntity> countryPartitionEntityList
    ) {

        // 字典分组
        Map<String, List<DictBasicEntity>> dictGroupMap = omsAllDictList.stream().collect(Collectors.groupingBy(DictBasicEntity::getType));
        // 销售平台
        List<DictBasicEntity> dictBasicEntityList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SALES_PLATFORM.getType(), Collections.emptyList());
        // 数帝云军区一级部门映射
        List<DictBasicEntity> sdyPartitionDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(), Collections.emptyList());
        // 数帝云平台二级部门映射
        List<DictBasicEntity> sdyPlatformDeptList = dictGroupMap.getOrDefault(DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType(), Collections.emptyList());


        //退货物流单号
        String rootNodeNoInitial = getRootNodeNoInitial(entity, soReturnEntityList, soReturnReceiveEntityList, receiveReturnList);

        DmpReturnInstockDTO.ViewDTO viewDto = new DmpReturnInstockDTO.ViewDTO();
        DmpReturnInstockDetailDTO.ViewDTO detailViewDto = new DmpReturnInstockDetailDTO.ViewDTO();

        String thirdReturnInstockId = entity.getId();
		viewDto.setThirdReturnInstockId(thirdReturnInstockId);
		viewDto.setThirdCreateTime(entity.getCreateTime());
		viewDto.setThirdUpdateTime(entity.getUpdateTime());
        if (SourceTypeEnum.WDT_RETURN_ORDER.getCode().equals(entity.getSourceType())){
            viewDto.setThirdCode(entity.getThirdCode());
        } else {
            viewDto.setThirdCode("");
        }
		detailViewDto.setThirdReturnInstockId(thirdReturnInstockId);
		detailViewDto.setThirdDetailCreateTime(detailEntity.getCreateTime());
		detailViewDto.setThirdDetailUpdateTime(detailEntity.getUpdateTime());
		detailViewDto.setThirdReturnInstockDetailId(detailEntity.getId());
		viewDto.setThirdReturnInstockCode(entity.getCode());
		LocalDate billDate = entity.getBillDate();
        if (billDate != null) {
        	viewDto.setReturnInstockTime(billDate.atStartOfDay());
        }
        viewDto.setReturnInstockStatus(ApproveStatusEnum.getName(entity.getApproveStatus()));
        detailViewDto.setDataStatus(new ShudiyunB2cOrderDTO().sdyStatusHandle(operate, entity.getVersion(), detailEntity.getVersion()));

        //组织信息
        CustomerInfoEntity customerInfo = customerInfoList.stream().filter(req -> req.getId().equals(entity.getCustomerId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(customerInfo)) {

            String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(entity.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
            viewDto.setSalesCompanyCode(salesOrgCode);
            BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
            	viewDto.setReceivingCompanyCode(sysAccountingCompanyEntity.getCode());
            	viewDto.setOrganizationCode(sysAccountingCompanyEntity.getCode());
            	viewDto.setOrganizationName(sysAccountingCompanyEntity.getName());
            }

            if (customerInfo.getCurrency() == null) {
            	detailViewDto.setSettlementCurrencyCode("");
            } else {
            	detailViewDto.setSettlementCurrencyCode(customerInfo.getCurrency());
            }

            if (customerInfo.getTradeCurrency() == null) {
            	detailViewDto.setCurrencyCode("");
            } else {
            	detailViewDto.setCurrencyCode(customerInfo.getTradeCurrency());
            }

            viewDto.setSourcePlatform(customerInfo.getPlatformType());
            String platformName = dictBasicEntityList.stream().filter(req -> req.getValue().equals(customerInfo.getPlatformType())).map(DictBasicEntity::getName).findFirst().orElse("");
            viewDto.setPlatformName(platformName);
            viewDto.setShopNo(customerInfo.getCode());
            viewDto.setShopName(customerInfo.getName());
        }

        viewDto.setPlatformReturnInstockCode(rootNodeNoInitial);
        String platformOrderCode = entity.getPlatformOrderCode();
        if(StringUtils.isBlank(platformOrderCode)) {
        	platformOrderCode = rootNodeNoInitial;
        }
        viewDto.setPlatformOrderCode(platformOrderCode);
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(new SkuVO());

//        if (detailEntity.getReturnAmount().compareTo(BigDecimal.ZERO) <= 0) {
//        	detailViewDto.setIsGift(1);
//        } else {
//        	detailViewDto.setIsGift(0);
//        }
        detailViewDto.setIsGift(0);

        BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
        	detailViewDto.setIsComb(1);
        	detailViewDto.setSuiteNo(bomChildrenSkuDTO.getParentSkuNo());
            BomChildrenSkuDTO finalBomChildrenSkuDTO1 = bomChildrenSkuDTO;
            String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO1.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
            detailViewDto.setSuiteName(skuName);
        } else {
            bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(detailEntity.getSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO)) {
            	detailViewDto.setSuiteNo(bomChildrenSkuDTO.getParentSkuNo());
                BomChildrenSkuDTO finalBomChildrenSkuDTO = bomChildrenSkuDTO;
                String skuName = parentSkuList.stream().filter(req -> req.getId().equals(finalBomChildrenSkuDTO.getParentSkuId())).map(ProductDetailEntity::getName).findFirst().orElse("");
                detailViewDto.setSuiteName(skuName);
            } else {
            	detailViewDto.setSuiteNo(skuVO.getSkuNo());
            	detailViewDto.setSuiteName(skuVO.getSkuName());
            }
        }

        if (OrderTypeEnum.B2B.getCode().equals(entity.getType())) {
        	viewDto.setLogisticCompanyName("【未知】");
        	viewDto.setLogisticCompanyCode("【未知】");
        } else {
        	viewDto.setLogisticCompanyName("空");
        	viewDto.setLogisticCompanyCode("空");
        }
        if (CharSequenceUtil.isBlank(entity.getReturnLogisticCode())) {
        	viewDto.setReturnLogisticCode("【未知】");
        } else {
        	viewDto.setReturnLogisticCode(entity.getReturnLogisticCode());
        }
        detailViewDto.setReturnInstockQty(detailEntity.getRealQty());

        detailViewDto.setRemark(detailEntity.getRemark());
        detailViewDto.setWarehouseNo(detailEntity.getWarehouseId());
        detailViewDto.setWarehouseName(detailEntity.getWarehouseName());
        detailViewDto.setReturnInstockAmount(detailEntity.getReturnAmount());

        // 商品状态
        if (entity.getSourceType().equals(SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode())) {
        	detailViewDto.setDetailStatus("平台收货");
        } else if (entity.getSourceType().equals(SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode())) {
        	detailViewDto.setDetailStatus("第三方仓收货");
        } else {
        	detailViewDto.setDetailStatus("本地仓收货");
        }

        detailViewDto.setDeliveryTime(entity.getApproveTime());
        
        detailViewDto.setUnit(skuVO.getUnitName());
        detailViewDto.setListPrice(skuVO.getRetailPrice());

        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(detailEntity.getCurrency())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(viewDTO)) {
        	detailViewDto.setCurrencyName(viewDTO.getName());
            detailViewDto.setCurrencyCode(viewDTO.getId());
        }

        if(skuVO.getSpuNo() == null) {
        	detailViewDto.setSpuNo(skuVO.getSkuNo());
            detailViewDto.setSpuName(skuVO.getSkuName());
        }else {
        	detailViewDto.setSpuNo(skuVO.getSpuNo());
            detailViewDto.setSpuName(skuVO.getSpuName());
        }
        detailViewDto.setPlatformSkuNo(skuVO.getSpuNo());
        detailViewDto.setPlatformSkuName(skuVO.getSpuName());
        detailViewDto.setSkuNo(skuVO.getSkuNo());
        detailViewDto.setSkuName(skuVO.getSkuName());

        // 国家编码
        // 国家名称
        String countryName = "";
        // 区域编码
        String regionCode = "";
        // 区域名称
        String regionName = "";
        // 军区编码
        String militaryRegionCode = "";
        // 军区名称
        String militaryRegionName = "";
        // 部门编码
        String departmentCode = "";
        // 部门名称
        String departmentName= "";

        // 国家信息为空替换为客户对应国家
        if (StringUtils.isBlank(countryCode) && null != customerInfo){
            countryCode = customerInfo.getCountryId();
            // 替换分区
            String finalCountryCode = countryCode;
            CfgCountryPartitionEntity cfgCountryPartitionEntity = countryPartitionEntityList.stream().filter(e -> e.getCountry().equalsIgnoreCase(finalCountryCode)).findFirst().orElse(null);
            if ( null != cfgCountryPartitionEntity){
                partitionId = cfgCountryPartitionEntity.getPartitionId();
            }
            dictPlatform = customerInfo.getPlatformType();
        }

        String finalCountryCode = countryCode;
        DictCountryEntity dictCountryEntity = countryEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalCountryCode)).findFirst().orElse(null);
        if (null != dictCountryEntity){
            countryName = dictCountryEntity.getShortNameCn();
            // 区域编码
            regionCode = dictCountryEntity.getSubregionCode();
            // 区域名称
            DictGlobalAreaEntity dictGlobalAreaEntity = dictGlobalEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(dictCountryEntity.getSubregionCode())).findFirst().orElse(null);
            if (null != dictGlobalAreaEntity){
                regionName = dictGlobalAreaEntity.getSubregionName();
            }
        }

        String finalPartitionId = partitionId;
        DictPartitionEntity dictPartitionEntity = partitionEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(finalPartitionId)).findFirst().orElse(null);
        if (null != dictPartitionEntity){
            // 军区编码
            militaryRegionCode = dictPartitionEntity.getCode();
            // 军区名称
            militaryRegionName = dictPartitionEntity.getName();
            // 军区一级部门映射
            DictBasicEntity sdyPartitionDeptEntity = sdyPartitionDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(dictPartitionEntity.getCode())).findFirst().orElse(null);
            // 销售平台二级部门映射
            String finalDictPlatform = dictPlatform;
            List<DictBasicEntity> sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(finalDictPlatform)).collect(Collectors.toList());

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
                    departmentCode = departmentDTO.getCode();
                    // 部门名称
                    departmentName = departmentDTO.getName();
                }
            }
        }
        // 国家编码
        detailViewDto.setCountryCode(countryCode);
        // 国家名称
        detailViewDto.setCountryName(countryName);
        // 区域编码
        detailViewDto.setRegionCode(regionCode);
        // 区域名称
        detailViewDto.setRegionName(regionName);
        // 军区编码
        detailViewDto.setMilitaryRegionCode(militaryRegionCode);
        // 军区名称
        detailViewDto.setMilitaryRegionName(militaryRegionName);
        // 部门编码
        detailViewDto.setDepartmentCode(departmentCode);
        // 部门名称
        detailViewDto.setDepartmentName(departmentName);

        viewDto.setDetailList(Arrays.asList(detailViewDto));

        return BeanUtil.beanToMap(viewDto);

    }

    @Override
    public void syncDataToSdy(SoReturnInstockEntity entity,
                              List<SoReturnInstockDetailEntity> detailEntities,
                              String operate) {
    	if(SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
    		this.syncBatchDataToSdy(Arrays.asList(entity), detailEntities, operate, true, true);
    	}else {
    		for (SoReturnInstockDetailEntity detailEntity : detailEntities) {
                WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
                wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                wmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SO_RETURN_INSTOCK.getCode());
                wmsPushMsgEntity.setSourceId(detailEntity.getId());
                wmsPushMsgEntity.setSourceCode(entity.getCode() + "_" + detailEntity.getSkuNo());
                wmsPushMsgEntity.setSyncOperate(operate);
                wmsPushMsgEntity.setPushData(JSON.toJSONString(DmpOutputConstant.getQuerySyncMap()));
                wmsPushMsgService.save(wmsPushMsgEntity);
            }
    	}
    }

    private String getRootNodeNoInitial(SoReturnInstockEntity entity,
                                        List<SoReturnEntity> soReturnEntityList,
                                        List<SoReturnReceiveEntity> soReturnReceiveEntityList,
                                        List<SoReturnEntity> receiveReturnList) {
        String rootNodeNoInitial = entity.getCode(); // 默认值是 entity.getCode()

        if (OrderTypeEnum.B2C.getCode().equals(entity.getType())) {
            if (SourceTypeEnum.PLATFORM_RETURN_INSTOCK.getCode().equals(entity.getSourceType())) {
                rootNodeNoInitial = entity.getSourceCode();
            } else if (SourceTypeEnum.WDT_RETURN_ORDER.getCode().equals(entity.getSourceType())) {
                if (CharSequenceUtil.isNotBlank(entity.getPlatformOrderCode())) {
                    rootNodeNoInitial = entity.getPlatformOrderCode();
                } else {
                    rootNodeNoInitial = entity.getSourceId();
                }
            } else if (SourceTypeEnum.SO_RETURN_INSTOCK.getCode().equals(entity.getSourceType())) {
                rootNodeNoInitial = entity.getSourceId();
            } else if (SourceTypeEnum.THIRD_WAREHOUSE_RETURN_INSTOCK.getCode().equals(entity.getSourceType())) {
                if (CharSequenceUtil.isNotBlank(entity.getPlatformOrderCode())) {
                    rootNodeNoInitial = entity.getPlatformOrderCode();
                } else {
                    rootNodeNoInitial = entity.getCode();
                }
            } else if (SourceTypeEnum.SAL_RETURNSTOCK.getCode().equals(entity.getSourceType())) {
                if (CharSequenceUtil.isNotBlank(entity.getSoReturnCode())) {
                    rootNodeNoInitial = entity.getSoReturnCode();
                }
            }
        }

        return rootNodeNoInitial;
    }

	@Override
	public Map<String ,Map<String, Object>> syncBatchDataToSdy(List<SoReturnInstockEntity> list,
			List<SoReturnInstockDetailEntity> detailEntityList, String operate , boolean isSavePush , boolean isNewQuerySync) {
		Map<String , Map<String, Object>> resultList = new HashMap<>();
		
		List<String> skuNos = detailEntityList.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
        List<SkuVO> skuVOList = plmTaskFeign.listAllStatusSkuBySkuNos(skuNos);
        List<String> skuIds = detailEntityList.stream().map(req -> req.getSkuId()).collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);

        List<String> currencyCodeList = detailEntityList.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyCodeList);
        //父类产品
        List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
        List<ProductDetailEntity> parentSkuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(parentSkuId)) {
            parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                    .in(ProductDetailEntity::getId, parentSkuId)
                    .list();
        }

        //客户
        List<String> customerIds = list.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
        List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
        if (CollUtil.isNotEmpty(customerIds)) {
            //组织
            customerInfoList = FeignQuery.create(CustomerInfoEntity.class)
                    .in(CustomerInfoEntity::getId, customerIds)
                    .list();
        }

        //组织
        List<String> financialOrganization = customerInfoList.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
        financialOrganization.addAll(list.stream().map(SoReturnInstockEntity::getSalesOrgId).distinct().collect(Collectors.toList()));
        List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(financialOrganization);

        List<String> soReturnIds = list.stream().filter(req -> SourceTypeEnum.SO_RETURN.getCode().equals(req.getSourceType())).map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoReturnEntity> soReturnEntityList = soReturnFeign.listByIds(soReturnIds);

        List<String> receiveIds = list.stream().filter(req -> SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(req.getSourceType())).map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoReturnReceiveEntity> soReturnReceiveEntityList = soReturnReceiveService.listByIds(receiveIds);

        List<String> returnIds = list.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
        List<SoReturnEntity> receiveReturnList = soReturnFeign.listByIds(returnIds);

        Map<String, List<SoReturnInstockEntity>> instockGroupMap = list.stream().collect(Collectors.groupingBy(SoReturnInstockEntity::getType));

        List<SoB2cEntity> soB2cEntityList = new LinkedList();
        List<SoB2cReceiverEntity> receiverEntityList = new LinkedList();
        // 查询B2C订单
        List<SoReturnInstockEntity> b2cReturnInstockList = instockGroupMap.get("B2C");
        if (CollectionUtils.isNotEmpty(b2cReturnInstockList)){
            List<String> b2cSoIds = b2cReturnInstockList.stream().map(SoReturnInstockEntity::getSoId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(b2cSoIds)) {
                receiverEntityList = FeignQuery.create(SoB2cReceiverEntity.class)
                        .in(SoB2cReceiverEntity::getMainId, b2cSoIds)
                        .list();

                soB2cEntityList = FeignQuery.create(SoB2cEntity.class)
                        .in(SoB2cEntity::getId, b2cSoIds)
                        .list();
            }
        }

        List<SoInfoEntity> soInfoEntityList = new LinkedList();
        // 查询B2B订单
        List<SoReturnInstockEntity> b2bReturnInstockList = instockGroupMap.get("B2B");
        if (CollectionUtils.isNotEmpty(b2bReturnInstockList)){
            List<String> b2bSoIds = b2bReturnInstockList.stream().map(SoReturnInstockEntity::getSoId)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
            soInfoEntityList = FeignQuery.create(SoInfoEntity.class)
                    .in(SoInfoEntity::getId, b2bSoIds)
                    .list();
        }

        List<DictBasicEntity> omsAllDictList = FeignQuery.create(DictBasicEntity.class)
                .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_SUB_PLATFORM.getType(),
                        DictBasicTypeEnum.SDY_PARTITION_LEVEL1_DEPT.getType(),
                        DictBasicTypeEnum.SDY_PLATFORM_LEVEL2_DEPT.getType()
                )).list();

        // 军区信息
        List<DictPartitionEntity> partitionEntityList = FeignQuery.create(DictPartitionEntity.class).list();

        // 国家信息
        List<DictCountryEntity> countryEntityList = FeignQuery.create(DictCountryEntity.class).list();

        // 子区域信息
        List<DictGlobalAreaEntity> dictGlobalEntityList = FeignQuery.create(DictGlobalAreaEntity.class).list();

        // 部门信息
        List<SysDepartmentEntity> deptList = sysUserFeign.getDeptEntityList();

        // 国家关联分区信息
        List<CfgCountryPartitionEntity> countryPartitionEntityList = FeignQuery.create(CfgCountryPartitionEntity.class).list();

        for (SoReturnInstockEntity entity : list) {
            // 国家
            String country = "";
            // 分区
            String partitionId = "";
            // 平台
            String dictPlatform = "";

            if ("B2B".equalsIgnoreCase(entity.getType())){
                SoInfoEntity soInfoEntity = soInfoEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(entity.getSoId())).findFirst().orElse(null);
                if (null != soInfoEntity){
                    partitionId = soInfoEntity.getPartitionId();
                    CustomerInfoEntity customerInfoEntity = customerInfoList.stream().filter(e -> e.getId().equalsIgnoreCase(soInfoEntity.getCustomerId())).findFirst().orElse(null);
                    if (null != customerInfoEntity){
                        country = customerInfoEntity.getCountryId();
                        dictPlatform = customerInfoEntity.getPlatformType();
                    }
                }
            } else if ("B2C".equalsIgnoreCase(entity.getType())){
                SoB2cReceiverEntity receiverEntity = receiverEntityList.stream().filter(e -> e.getMainId().equalsIgnoreCase(entity.getSoId())).findFirst().orElse(null);
                if (null != receiverEntity){
                    country = receiverEntity.getCountry();
                    partitionId = receiverEntity.getPartitionId();
                }
                SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(entity.getSoId())).findFirst().orElse(null);
                if (null != soB2cEntity){
                    dictPlatform = soB2cEntity.getDictPlatform();
                }
            }

            List<SoReturnInstockDetailEntity> detailEntities = detailEntityList.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());

            for(SoReturnInstockDetailEntity detailEntity : detailEntities) {
            	Map<String, Object> syncDataToSdyFieldHandler = null;
            	if(isNewQuerySync) {
            		syncDataToSdyFieldHandler = this.syncNewDataToSdyFieldHandler(entity,
                            detailEntity,
                            operate,
                            skuVOList,
                            bomChildrenSkuDTOS,
                            currencyList,
                            parentSkuList,
                            customerInfoList,
                            companyEntities,
                            soReturnEntityList,
                            soReturnReceiveEntityList,
                            receiveReturnList,
                            country,
                            partitionId,
                            dictPlatform,
                            omsAllDictList,
                            partitionEntityList,
                            countryEntityList,
                            dictGlobalEntityList,
                            deptList,
                            countryPartitionEntityList
                    );
            	}else {
            		syncDataToSdyFieldHandler = this.syncDataToSdyFieldHandler(entity,
                            detailEntity,
                            operate,
                            skuVOList,
                            bomChildrenSkuDTOS,
                            currencyList,
                            parentSkuList,
                            customerInfoList,
                            companyEntities,
                            soReturnEntityList,
                            soReturnReceiveEntityList,
                            receiveReturnList,
                            country,
                            partitionId,
                            dictPlatform,
                            omsAllDictList,
                            partitionEntityList,
                            countryEntityList,
                            dictGlobalEntityList,
                            deptList,
                            countryPartitionEntityList
                    );
            	}
            	String sourceId = detailEntity.getId();
            	resultList.put(sourceId, syncDataToSdyFieldHandler);
            	if(isSavePush) {
            		WmsPushMsgEntity wmsPushMsgEntity = new WmsPushMsgEntity();
                    wmsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                    wmsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SO_RETURN_INSTOCK.getCode());
                    wmsPushMsgEntity.setSourceId(sourceId);
                    wmsPushMsgEntity.setSourceCode(entity.getCode() + "_" + detailEntity.getSkuNo());
                    wmsPushMsgEntity.setSyncOperate(operate);
                    wmsPushMsgEntity.setPushData(JSON.toJSONString(syncDataToSdyFieldHandler));
                    wmsPushMsgService.save(wmsPushMsgEntity);
            	}
            }
        }
        
        return resultList;
	}

}
