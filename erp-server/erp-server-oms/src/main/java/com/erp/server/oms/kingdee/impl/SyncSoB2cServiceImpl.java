package com.erp.server.oms.kingdee.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.oms.enums.SoB2cBillStatusEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.AliexpressDeliveryDetailEntity;
import com.erp.model.wms.entity.AliexpressDeliveryEntity;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.oms.kingdee.SyncSoB2cService;
import com.erp.server.oms.service.OmsPushMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SyncSoB2cServiceImpl implements SyncSoB2cService {
    @Resource
    private OmsPushMsgService omsPushMsgService;
    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;
    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

    private final static DateTimeFormatter localDateTime = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> syncDataToSdyFieldHandler(SoB2cEntity soB2cEntity,
                                                         List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                         SoB2cDetailEntity soB2cDetailEntity,
                                                         String operate,
                                                         List<SkuVO> skuVOList,
                                                         List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                         List<ProductDetailEntity> parentSkuList,
                                                         List<ListingInfoEntity> listingInfoEntities,
                                                         List<CurrencyDTO.ViewDTO> currencyList,
                                                         List<DictCurrencyEntity> dictCurrencyEntities,
                                                         List<ShopInfoEntity> shopInfoList,
                                                         List<CustomerInfoEntity> customerInfoList,
                                                         List<BaseIdDTO.CodeDTO> companyEntities,
                                                         SoB2cReceiverEntity receiverEntity,
                                                         List<DictBasicEntity> omsAllDictList,
                                                         List<DictPartitionEntity> partitionEntityList,
                                                         List<DictCountryEntity> countryEntityList,
                                                         List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                         List<SysDepartmentEntity> deptList
    ) {
        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        shudiyunB2cOrderDTO.setBiz_uni_key(soB2cEntity.getId() + soB2cDetailEntity.getId());

        Integer totalQty = soB2cDetailEntityList.stream().mapToInt(SoB2cDetailEntity::getQty).sum();
        shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);

        if ((null != soB2cEntity.getIsCancel() && Boolean.TRUE.equals(soB2cEntity.getIsCancel()))
                || (null != soB2cEntity.getInvalidStatus() && Boolean.TRUE.equals(soB2cEntity.getInvalidStatus()) )) {
            if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                // 取消商品数量（合计）
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
            }
        }
        shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty - shudiyunB2cOrderDTO.getTotal_canceled_goods_quantity());

        shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(soB2cEntity.getUpdateTime()));

        shudiyunB2cOrderDTO.setGoods_transaction_quantity(soB2cDetailEntity.getQty());
        shudiyunB2cOrderDTO.setPrice(soB2cDetailEntity.getPrice());
        shudiyunB2cOrderDTO.setGoods_transaction_amount(soB2cDetailEntity.getAmount());

        shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, soB2cEntity.getVersion(), soB2cDetailEntity.getVersion()));

        String targetPlatformSkuNo = soB2cDetailEntity.getPlatformSkuNo();
        if (StringUtils.isBlank(targetPlatformSkuNo)) {
            // 平台sku为空改为平台产品ID
            targetPlatformSkuNo = soB2cDetailEntity.getPlatformSpuNo();
        }
        boolean setBlankMskuName = false;
        shudiyunB2cOrderDTO.setMsku_code(targetPlatformSkuNo);
        String platformSkuName = listingInfoEntities.stream().filter(req -> req.getPlatform().equals(soB2cEntity.getDictPlatform())
                        && req.getPlatformSkuNo().equals(soB2cDetailEntity.getPlatformSkuNo()))
                .sorted(Comparator.comparing(ListingInfoEntity::getUpdateTime).reversed()) // 倒序排序
                .map(ListingInfoEntity::getPlatformSkuName).findFirst().orElse("");
        if (CharSequenceUtil.isBlank(platformSkuName)) {
            setBlankMskuName = true;
        } else {
            shudiyunB2cOrderDTO.setMsku_name(platformSkuName);
        }
        BigDecimal price = soB2cDetailEntity.getPrice();
        if (price != null && BigDecimal.ZERO.compareTo(price) == 0) {
            shudiyunB2cOrderDTO.setIs_gift(1);
        } else {
            shudiyunB2cOrderDTO.setIs_gift(0);
        }

        if (soB2cEntity.getDictPlatform().equalsIgnoreCase(PlatformDictEnum.ALI_EXPRESS.getCode())){
            shudiyunB2cOrderDTO.setBuyer_actual_payment(soB2cEntity.getAfterTaxAmount());
        } else {
            shudiyunB2cOrderDTO.setBuyer_actual_payment(soB2cEntity.getAmount());
        }

        // 公共处理
        commonHandle(soB2cEntity,
                soB2cDetailEntityList,
                skuVOList,
                bomChildrenSkuDTOS,
                parentSkuList,
                currencyList,
                dictCurrencyEntities,
                shopInfoList,
                customerInfoList,
                companyEntities,
                soB2cDetailEntity.getSkuId(),
                soB2cDetailEntity.getSkuNo(),
                setBlankMskuName,
                soB2cDetailEntity.getCurrency(),
                shudiyunB2cOrderDTO,
                receiverEntity,
                omsAllDictList,
                partitionEntityList,
                countryEntityList,
                dictGlobalEntityList,
                deptList
        );

        return JSONObject.parseObject(JSONObject.toJSONString(shudiyunB2cOrderDTO), Map.class);
    }

    /**
     * 公共转换
     */
    private static void commonHandle(SoB2cEntity soB2cEntity,
                                     List<SoB2cDetailEntity> soB2cDetailEntityList,
                                     List<SkuVO> skuVOList,
                                     List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                     List<ProductDetailEntity> parentSkuList,
                                     List<CurrencyDTO.ViewDTO> currencyList,
                                     List<DictCurrencyEntity> dictCurrencyEntities,
                                     List<ShopInfoEntity> shopInfoList,
                                     List<CustomerInfoEntity> customerInfoList,
                                     List<BaseIdDTO.CodeDTO> companyEntities,
                                     String skuId,
                                     String skuNo,
                                     boolean blankMskuNameSetting,
                                     String currency,
                                     ShudiyunB2cOrderDTO shudiyunB2cOrderDTO,
                                     SoB2cReceiverEntity receiverEntity,
                                     List<DictBasicEntity> omsAllDictList,
                                     List<DictPartitionEntity> partitionEntityList,
                                     List<DictCountryEntity> countryEntityList,
                                     List<DictGlobalAreaEntity> dictGlobalEntityList,
                                     List<SysDepartmentEntity> deptList

    ) {
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

        if (StringUtils.isBlank(skuId) || StringUtils.isBlank(skuNo)) {
            ServiceException.runError("未找到ERP sku未空: skuId={}, skuNo={}", skuId, skuNo);
        }
        // 业务单号
        shudiyunB2cOrderDTO.setBiz_no(soB2cEntity.getCode());
        if (soB2cEntity.getPayTime() != null) {
            shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(soB2cEntity.getPayTime()));
        } else {
            shudiyunB2cOrderDTO.setBiz_time(localDateTime.format(soB2cEntity.getPlatformOrderCreateTime()));
        }
        // 从交易子类型：线上订单=配货单, 其他=线下订单
//        if (OrderSubTypeEnum.ONLINE_ORDER.getCode().equalsIgnoreCase(soB2cEntity.getTransactionSubType())) {
//            shudiyunB2cOrderDTO.setTransaction_type("配货单");
//            shudiyunB2cOrderDTO.setTransaction_sub_type("线上订单");
//        } else {
//            shudiyunB2cOrderDTO.setTransaction_type("线下订单");
//            shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.getName(soB2cEntity.getTransactionSubType()));
//        }
        shudiyunB2cOrderDTO.setTransaction_type("配货单");
        shudiyunB2cOrderDTO.setTransaction_sub_type(OrderSubTypeEnum.getName(soB2cEntity.getTransactionSubType()));

        if (CharSequenceUtil.isBlank(soB2cEntity.getBillStatus())) {
            soB2cEntity.setBillStatus(SoB2cBillStatusEnum.ENUM_WAIT_DISTRIBUTION.getCode());
        }
        shudiyunB2cOrderDTO.setBiz_status(SoB2cBillStatusEnum.getName(soB2cEntity.getBillStatus()));

        BigDecimal amount = soB2cDetailEntityList.stream().map(req -> req.getAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
        shudiyunB2cOrderDTO.setTotal_goods_transaction_amount(amount);
        //总优惠金额
        shudiyunB2cOrderDTO.setDiscount_deduction_amount(soB2cEntity.getTotalDiscount());

        //取消金额、数量
        if (soB2cEntity.getDictPlatform().equals(PlatformDictEnum.ALI_EXPRESS.getCode())
        ) {
            if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(soB2cEntity.getTotalCancelGoodsAmount());
            }
        } else {
            if ((Boolean.TRUE.equals(soB2cEntity.getIsCancel()) && soB2cEntity.getIsCancel() != null )
                    || (Boolean.TRUE.equals(soB2cEntity.getInvalidStatus()) && soB2cEntity.getInvalidStatus() != null)) {
                if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                    BigDecimal totalCancelGoodsAmount = soB2cDetailEntityList.stream().map(req -> req.getAmount()).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                    shudiyunB2cOrderDTO.setTotal_canceled_goods_amount(totalCancelGoodsAmount);
                }
            }
        }

        shudiyunB2cOrderDTO.setTaxation(soB2cEntity.getTotalTaxFee());

        shudiyunB2cOrderDTO.setTotal_freight(soB2cEntity.getShippingFee());
        String customerId = "";
        ShopInfoEntity shopInfo = shopInfoList.stream().filter(req -> req.getId().equals(soB2cEntity.getShopId())).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(shopInfo)) {
            String salesOrgCode = companyEntities.stream().filter(req -> req.getId().equals(shopInfo.getSalesOrgId())).map(req -> req.getCode()).findFirst().orElse("");
            shudiyunB2cOrderDTO.setSales_company_code(salesOrgCode);
            DictCurrencyEntity dictCurrencyEntity = dictCurrencyEntities.stream().filter(req -> req.getId().equals(shopInfo.getTradeCurrency())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(dictCurrencyEntity)) {
                shudiyunB2cOrderDTO.setTransaction_currency(dictCurrencyEntity.getName());
            }
            shudiyunB2cOrderDTO.setTransaction_currency_code(shopInfo.getTradeCurrency());
            shudiyunB2cOrderDTO.setSettlement_currency_code(shopInfo.getSettlementCurrency());
            customerId = shopInfo.getCustomerId();
        }

        String finalCustomerId = customerId;
        CustomerInfoEntity customerInfo = customerInfoList.stream().filter(req -> req.getId().equals(finalCustomerId)).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(customerInfo)) {
            shudiyunB2cOrderDTO.setShop_no(customerInfo.getCode());
            shudiyunB2cOrderDTO.setShop_name(customerInfo.getName());
            BaseIdDTO.CodeDTO sysAccountingCompanyEntity = companyEntities.stream().filter(req -> req.getId().equals(customerInfo.getFinancialOrganization())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(sysAccountingCompanyEntity)) {
                shudiyunB2cOrderDTO.setReceiving_company_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_code(sysAccountingCompanyEntity.getCode());
                shudiyunB2cOrderDTO.setOrganization_name(sysAccountingCompanyEntity.getName());
            }
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

        shudiyunB2cOrderDTO.setRoot_node_no(soB2cEntity.getPlatformCode());
        if (soB2cEntity.getPayTime() != null) {
            shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(soB2cEntity.getPayTime()));
        } else {
            shudiyunB2cOrderDTO.setRoot_node_create_time(localDateTime.format(soB2cEntity.getPlatformOrderCreateTime()));
        }
        shudiyunB2cOrderDTO.setGoods_no(skuNo);
        SkuVO skuVO = skuVOList.stream().filter(req -> req.getSkuId().equals(skuId)).findFirst().orElse(new SkuVO());
        shudiyunB2cOrderDTO.setGoods_name(skuVO.getSkuName());

        if (CharSequenceUtil.isNotBlank(skuVO.getSpuNo())) {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSpuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSpuName());
        } else {
            shudiyunB2cOrderDTO.setSpec_no(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setSpec_name(skuVO.getSkuName());
        }

        BomChildrenSkuDTO bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getParentSkuId().equals(skuId)).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(bomChildrenSkuDTO) && BomTypeEnum.COMBINATION.getType().equals(bomChildrenSkuDTO.getType())) {
            shudiyunB2cOrderDTO.setIs_comb(1);
            shudiyunB2cOrderDTO.setSuite_no(bomChildrenSkuDTO.getSkuNo());
            shudiyunB2cOrderDTO.setSuite_name(bomChildrenSkuDTO.getSkuName());
        } else {
            bomChildrenSkuDTO = bomChildrenSkuDTOS.stream().filter(req -> req.getSkuId().equals(skuId)).findFirst().orElse(null);
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

        shudiyunB2cOrderDTO.setSuite_no(skuVO.getSkuNo());
        shudiyunB2cOrderDTO.setSuite_name(skuVO.getSkuName());
        shudiyunB2cOrderDTO.setRemark(soB2cEntity.getRemark());
        shudiyunB2cOrderDTO.setGoods_status("未发货");
        // 商品状态
        if (SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
            shudiyunB2cOrderDTO.setGoods_status("已发货");
        }

        if ((Boolean.TRUE.equals(soB2cEntity.getIsCancel()) && soB2cEntity.getIsCancel() != null )
                || (Boolean.TRUE.equals(soB2cEntity.getInvalidStatus()) && soB2cEntity.getInvalidStatus() != null)) {
            if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())){
                shudiyunB2cOrderDTO.setGoods_status("已取消");
                shudiyunB2cOrderDTO.setBiz_status("已取消");
            }
        }

        shudiyunB2cOrderDTO.setUnit(skuVO.getUnitName());

        if (skuVO.getRetailPrice() != null) {
            shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(skuVO.getRetailPrice());
        } else {
            shudiyunB2cOrderDTO.setGoods_benchmark_selling_price(BigDecimal.ZERO);
        }

        CurrencyDTO.ViewDTO viewDTO = currencyList.stream().filter(req -> req.getId().equals(currency)).findFirst().orElse(null);
        if (ObjectUtil.isNotEmpty(viewDTO)) {
            shudiyunB2cOrderDTO.setTransaction_currency(viewDTO.getName());
            shudiyunB2cOrderDTO.setTransaction_currency_code(viewDTO.getId());
        }

        shudiyunB2cOrderDTO.setPost_amount(soB2cEntity.getShippingFee());

        shudiyunB2cOrderDTO.setSku_code(skuVO.getSkuNo());
        shudiyunB2cOrderDTO.setSku_name(skuVO.getSkuName());

        shudiyunB2cOrderDTO.setSource_system("SDC");
        shudiyunB2cOrderDTO.setRoot_node_no_initial(soB2cEntity.getPlatformCode());

        if (blankMskuNameSetting){
            shudiyunB2cOrderDTO.setMsku_name(skuVO.getSkuName());
        }

        // 手工单逻辑查询同步处理
        if (SourceTypeEnum.SELF_ADD.getCode().equals(soB2cEntity.getSourceType())) {
            shudiyunB2cOrderDTO.setMsku_code(skuVO.getSkuNo());
            shudiyunB2cOrderDTO.setMsku_name(skuVO.getSkuName());
            if (StringUtils.isBlank(soB2cEntity.getPlatformCode())){
                shudiyunB2cOrderDTO.setRoot_node_no_initial(soB2cEntity.getCode());
                shudiyunB2cOrderDTO.setRoot_node_no(soB2cEntity.getCode());
            }
        } else if (!soB2cEntity.hasPlatformWarehouseOrder()){
//            自发货平台：msku_code为空的时候：取产品id，产品id在为空，再取的ERP
//            自发货平台：msku_name为空的时候：取对照表的名称，取不到取系统sku_name名称
            if (StringUtils.isBlank(shudiyunB2cOrderDTO.getMsku_code())) {
                shudiyunB2cOrderDTO.setMsku_code(skuVO.getSkuNo());
            }
        }

        // 国家编码
        String countryCode = "";
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

        if (null != receiverEntity){
            String partitionId = receiverEntity.getPartitionId();
            DictPartitionEntity dictPartitionEntity = partitionEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(partitionId)).findFirst().orElse(null);
            if (null != dictPartitionEntity){
                // 军区编码
                militaryRegionCode = dictPartitionEntity.getCode();
                // 军区名称
                militaryRegionName = dictPartitionEntity.getName();
                // 军区一级部门映射
                DictBasicEntity sdyPartitionDeptEntity = sdyPartitionDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(dictPartitionEntity.getCode())).findFirst().orElse(null);
                // 销售平台二级部门映射
                List<DictBasicEntity> sdyPlatformDeptEntityList = sdyPlatformDeptList.stream().filter(e -> e.getName().equalsIgnoreCase(soB2cEntity.getDictPlatform())).collect(Collectors.toList());
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

            if (StringUtils.isNotBlank(receiverEntity.getCountry())){
                String country = receiverEntity.getCountry();
                countryCode = country;
                DictCountryEntity dictCountryEntity = countryEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(country)).findFirst().orElse(null);
                if (null != dictCountryEntity){
                    // 国家名称
                    countryName = dictCountryEntity.getShortNameCn();
                    // 区域编码
                    regionCode = dictCountryEntity.getRegionCode();
                    // 区域名称
                    DictGlobalAreaEntity dictGlobalAreaEntity = dictGlobalEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(dictCountryEntity.getSubregionCode())).findFirst().orElse(null);
                    if (null != dictGlobalAreaEntity){
                        regionName = dictGlobalAreaEntity.getRegionName();
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
        }
    }

    @Override
    public void syncDataToSdy(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soB2cDetailEntityList, String operate) {
    	if (CollUtil.isNotEmpty(soB2cDetailEntityList)) {
            for (SoB2cDetailEntity soB2cDetailEntity : soB2cDetailEntityList) {
                if (StringUtils.isBlank(soB2cDetailEntity.getSkuId()) || StringUtils.isBlank(soB2cDetailEntity.getSkuNo())) {
                    continue;
                }
                //同步B2B订单
                OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
                omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_DELIVERY_ORDER.getCode());
                omsPushMsgEntity.setSourceId(soB2cDetailEntity.getId());
                omsPushMsgEntity.setSourceCode(soB2cEntity.getCode() + "_" + soB2cDetailEntity.getSkuNo());
                omsPushMsgEntity.setSyncOperate(operate);
                Map<String, Object> map = new HashMap<>();
                map.put("isQuerySync", Boolean.TRUE);
                map.put("detailId", soB2cDetailEntity.getId());
                map.put("operate", operate);
                omsPushMsgEntity.setPushData(JSON.toJSONString(map));
                
//                omsPushMsgService.save(omsPushMsgEntity);
            }
        }
    }

    @Override
    public void syncDataToSdy(SoB2cEntity soB2cEntity,
                              List<SoB2cDetailEntity> detailEntityList,
                              String operate,
                              List<SkuVO> skuVOList,
                              List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                              List<ProductDetailEntity> parentSkuList,
                              List<ListingInfoEntity> listingInfoEntities,
                              List<CurrencyDTO.ViewDTO> currencyList,
                              List<DictCurrencyEntity> dictCurrencyEntities,
                              List<ShopInfoEntity> shopInfoList,
                              List<CustomerInfoEntity> customerInfoList,
                              List<BaseIdDTO.CodeDTO> companyEntities,
                              SoB2cReceiverEntity receiverEntity,
                              List<DictBasicEntity> omsAllDictList,
                              List<DictPartitionEntity> partitionEntityList,
                              List<DictCountryEntity> countryEntityList,
                              List<DictGlobalAreaEntity> dictGlobalEntityList,
                              List<SysDepartmentEntity> deptList
    ) {
        for (SoB2cDetailEntity soB2cDetailEntity : detailEntityList) {
            if (StringUtils.isBlank(soB2cDetailEntity.getSkuId()) || StringUtils.isBlank(soB2cDetailEntity.getSkuNo())) {
                continue;
            }
            //同步配货单
            OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
            omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_DELIVERY_ORDER.getCode());
            omsPushMsgEntity.setSourceId(soB2cDetailEntity.getId());
            omsPushMsgEntity.setSourceCode(soB2cEntity.getCode() + "_" + soB2cDetailEntity.getSkuNo());
            omsPushMsgEntity.setSyncOperate(operate);
            omsPushMsgEntity.setPushData(JSON.toJSONString(this.syncDataToSdyFieldHandler(soB2cEntity,
                    detailEntityList,
                    soB2cDetailEntity,
                    operate,
                    skuVOList,
                    bomChildrenSkuDTOS,
                    parentSkuList,
                    listingInfoEntities,
                    currencyList,
                    dictCurrencyEntities,
                    shopInfoList,
                    customerInfoList,
                    companyEntities,
                    receiverEntity,
                    omsAllDictList,
                    partitionEntityList,
                    countryEntityList,
                    dictGlobalEntityList,
                    deptList)));
            omsPushMsgService.save(omsPushMsgEntity);
        }
    }


    @Override
    public void syncSelfAddDataToSdy(SoB2cEntity mainEntity, String operate) {
        // 最新已发货单
        List<SoB2cDeliveryEntity> list = soB2cDeliveryFeign.listBySourceId(Collections.singletonList(mainEntity.getId()))
                .stream()
                .filter(e -> Objects.equals(e.getStatus(), SoB2cDeliveryStatusEnum.SHIPPED.getCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(list)) {
            log.error("【本地记录推送速帝云】未找到已发货的B2C发货单:so_code={}", mainEntity.getCode());
            return;
        }
        List<String> mainIds = list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SoB2cDeliveryDetailEntity> detailEntityList = FeignQuery.create(SoB2cDeliveryDetailEntity.class)
                .in(SoB2cDeliveryDetailEntity::getMainId, mainIds)
                .list();
        if (CollectionUtils.isNotEmpty(detailEntityList)) {
            for (SoB2cDeliveryDetailEntity detailEntity : detailEntityList) {
                //同步
                OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
                omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SELF_DELIVERY_ORDER.getCode());
                omsPushMsgEntity.setSourceId(detailEntity.getId());
                omsPushMsgEntity.setSourceCode(mainEntity.getCode() + "_" + detailEntity.getSkuNo());
                omsPushMsgEntity.setSyncOperate(operate);
                Map<String, Object> map = new HashMap<>();
                map.put("isQuerySync", Boolean.TRUE);
                map.put("detailId", detailEntity.getId());
                map.put("operate", operate);
                omsPushMsgEntity.setPushData(JSON.toJSONString(map));
//                omsPushMsgService.save(omsPushMsgEntity);
            }
        }
    }

    @Override
    public void syncAliExpressDataToSdy(SoB2cEntity mainEntity, String operate) {
        if (!PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(mainEntity.getDictPlatform())
                || !mainEntity.hasPlatformWarehouseOrder()
        ) {
            ServiceException.runError("非速卖通平台仓无法处理:{}", mainEntity.getCode());
        }
        List<AliexpressDeliveryEntity> list = FeignQuery.create(AliexpressDeliveryEntity.class)
                .eq(AliexpressDeliveryEntity::getSoId, mainEntity.getId())
                .list();
        if (CollectionUtils.isEmpty(list)) {
            log.warn("未找到速卖通平台仓发货单:{}", mainEntity.getCode());
            return;
        }
        List<String> mainIds = list.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<AliexpressDeliveryDetailEntity> detailEntityList = FeignQuery.create(AliexpressDeliveryDetailEntity.class)
                .in(AliexpressDeliveryDetailEntity::getMainId, mainIds)
                .list();

        if (CollUtil.isNotEmpty(detailEntityList)) {
            for (AliexpressDeliveryDetailEntity detailEntity : detailEntityList) {
                //同步【速卖通发货单】到【速帝云配货单】
                OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
                omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
                omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_ALIEXPRESS_DELIVERY_ORDER.getCode());
                omsPushMsgEntity.setSourceId(detailEntity.getId());
                omsPushMsgEntity.setSourceCode(mainEntity.getCode() + "_" + detailEntity.getSkuNo());
                omsPushMsgEntity.setSyncOperate(operate);
                Map<String, Object> map = new HashMap<>();
                map.put("isQuerySync", Boolean.TRUE);
                map.put("detailId", detailEntity.getId());
                map.put("operate", operate);
                omsPushMsgEntity.setPushData(JSON.toJSONString(map));
//                omsPushMsgService.save(omsPushMsgEntity);
            }
        }
    }

    @Override
    public Map<String, Object> syncSelfAddDataToSdyFieldHandler(SoB2cEntity soB2cEntity,
                                                                List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                                SoB2cDeliveryEntity soB2cDeliveryEntity,
                                                                List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntityList,
                                                                SoB2cDeliveryDetailEntity soB2cDeliveryDetailEntity,
                                                                String operate,
                                                                List<SkuVO> skuVOList,
                                                                List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                                List<ProductDetailEntity> parentSkuList,
                                                                List<ListingInfoEntity> listingInfoEntities,
                                                                List<CurrencyDTO.ViewDTO> currencyList,
                                                                List<DictCurrencyEntity> dictCurrencyEntities,
                                                                List<ShopInfoEntity> shopInfoList,
                                                                List<CustomerInfoEntity> customerInfoList,
                                                                List<BaseIdDTO.CodeDTO> companyEntities,
                                                                Map<String, Pair<BigDecimal, BigDecimal>> deliveryDetailPriceMap,
                                                                SoB2cReceiverEntity receiverEntity,
                                                                List<DictBasicEntity> omsAllDictList,
                                                                List<DictPartitionEntity> partitionEntityList,
                                                                List<DictCountryEntity> countryEntityList,
                                                                List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                                List<SysDepartmentEntity> deptList
    ) {
        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        shudiyunB2cOrderDTO.setBiz_uni_key(soB2cDeliveryEntity.getId() + soB2cDeliveryDetailEntity.getId());

        // 发货单明细总数量
        Integer totalQty = soB2cDeliveryDetailEntityList.stream().mapToInt(SoB2cDeliveryDetailEntity::getDeliveryQty).sum();
        shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);

        if ((null != soB2cEntity.getIsCancel() && Boolean.TRUE.equals(soB2cEntity.getIsCancel()))
                || (null != soB2cEntity.getInvalidStatus() && Boolean.TRUE.equals(soB2cEntity.getInvalidStatus()) )) {
            if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                // 取消商品数量（合计）
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
            }
        }
        shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty - shudiyunB2cOrderDTO.getTotal_canceled_goods_quantity());

        shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(soB2cDeliveryEntity.getUpdateTime()));

        shudiyunB2cOrderDTO.setGoods_transaction_quantity(soB2cDeliveryDetailEntity.getDeliveryQty());

        // 发货单明细对应订单明细ID
        SoB2cDetailEntity soB2cDetailEntity = soB2cDetailEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(soB2cDeliveryDetailEntity.getSourceDetailId())).findFirst().orElse(null);
        if (null == soB2cDetailEntity){
            ServiceException.runError("未找到B2C销售订单明细:发货单明细ID={}", soB2cDeliveryDetailEntity.getSourceDetailId());
        }

        // 自发货明细单价
        Pair<BigDecimal, BigDecimal> pairPrice = deliveryDetailPriceMap.get(soB2cDeliveryDetailEntity.getId());
        if (null == pairPrice){
            ServiceException.runError("未找到计算的明细单价:发货单明细ID={}", soB2cDeliveryDetailEntity.getSourceDetailId());
        }

        // 单价
        shudiyunB2cOrderDTO.setPrice(pairPrice.getKey());
        // 明细总价
        if(0 == soB2cDetailEntity.getPrice().compareTo(pairPrice.getKey())){
            shudiyunB2cOrderDTO.setGoods_transaction_amount(soB2cDetailEntity.getAmount());
        } else {
            shudiyunB2cOrderDTO.setGoods_transaction_amount(pairPrice.getValue());
        }

        shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, soB2cDeliveryEntity.getVersion(), soB2cDeliveryDetailEntity.getVersion()));

        String targetPlatformSkuNo = soB2cDetailEntity.getPlatformSkuNo();
        if (StringUtils.isBlank(targetPlatformSkuNo)) {
            // 平台sku为空改为平台产品ID
            targetPlatformSkuNo = soB2cDetailEntity.getPlatformSpuNo();
        }
        boolean setBlankMskuName = false;
        shudiyunB2cOrderDTO.setMsku_code(targetPlatformSkuNo);
        String platformSkuName = listingInfoEntities.stream().filter(req -> req.getPlatform().equals(soB2cEntity.getDictPlatform())
                        && req.getPlatformSkuNo().equals(soB2cDetailEntity.getPlatformSkuNo()))
                .sorted(Comparator.comparing(ListingInfoEntity::getUpdateTime).reversed()) // 倒序排序
                .map(ListingInfoEntity::getPlatformSkuName).findFirst().orElse("");
        if (CharSequenceUtil.isBlank(platformSkuName)) {
            setBlankMskuName = true;
        } else {
            shudiyunB2cOrderDTO.setMsku_name(platformSkuName);
        }
        BigDecimal newPrice = soB2cDetailEntity.getPrice();
        if (newPrice != null && BigDecimal.ZERO.compareTo(newPrice) == 0) {
            shudiyunB2cOrderDTO.setIs_gift(1);
        } else {
            shudiyunB2cOrderDTO.setIs_gift(0);
        }
        if (soB2cEntity.getDictPlatform().equalsIgnoreCase(PlatformDictEnum.ALI_EXPRESS.getCode())){
            shudiyunB2cOrderDTO.setBuyer_actual_payment(soB2cEntity.getAfterTaxAmount());
        } else {
            shudiyunB2cOrderDTO.setBuyer_actual_payment(soB2cEntity.getAmount());
        }

        // 公共处理
        commonHandle(soB2cEntity,
                soB2cDetailEntityList,
                skuVOList,
                bomChildrenSkuDTOS,
                parentSkuList,
                currencyList,
                dictCurrencyEntities,
                shopInfoList,
                customerInfoList,
                companyEntities,
                soB2cDeliveryDetailEntity.getSkuId(),
                soB2cDeliveryDetailEntity.getSkuNo(),
                setBlankMskuName,
                soB2cDetailEntity.getCurrency(),
                shudiyunB2cOrderDTO,
                receiverEntity,
                omsAllDictList,
                partitionEntityList,
                countryEntityList,
                dictGlobalEntityList,
                deptList
        );

        return JSONObject.parseObject(JSONObject.toJSONString(shudiyunB2cOrderDTO), Map.class);
    }


    @Override
    public Map<String, Object> syncAliExpressDataToSdyFieldHandler(SoB2cEntity soB2cEntity,
                                                                   List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                                   AliexpressDeliveryEntity aliexpressDeliveryEntity,
                                                                   List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList,
                                                                   AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity,
                                                                   String operate,
                                                                   List<SkuVO> skuVOList,
                                                                   List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
                                                                   List<ProductDetailEntity> parentSkuList,
                                                                   List<ListingInfoEntity> listingInfoEntities,
                                                                   List<CurrencyDTO.ViewDTO> currencyList,
                                                                   List<DictCurrencyEntity> dictCurrencyEntities,
                                                                   List<ShopInfoEntity> shopInfoList,
                                                                   List<CustomerInfoEntity> customerInfoList,
                                                                   List<BaseIdDTO.CodeDTO> companyEntities,
                                                                   SoB2cReceiverEntity receiverEntity,
                                                                   List<DictBasicEntity> omsAllDictList,
                                                                   List<DictPartitionEntity> partitionEntityList,
                                                                   List<DictCountryEntity> countryEntityList,
                                                                   List<DictGlobalAreaEntity> dictGlobalEntityList,
                                                                   List<SysDepartmentEntity> deptList
    ) {
        ShudiyunB2cOrderDTO shudiyunB2cOrderDTO = new ShudiyunB2cOrderDTO();

        shudiyunB2cOrderDTO.setBiz_uni_key(aliexpressDeliveryEntity.getId() + aliexpressDeliveryDetailEntity.getId());

        // 发货单明细总数量
        Integer totalQty = aliexpressDeliveryDetailEntityList.stream().mapToInt(AliexpressDeliveryDetailEntity::getOrderLineQty).sum();
        shudiyunB2cOrderDTO.setTotal_goods_quantity(totalQty);

        if ((null != soB2cEntity.getIsCancel() && Boolean.TRUE.equals(soB2cEntity.getIsCancel()))
                || (null != soB2cEntity.getInvalidStatus() && Boolean.TRUE.equals(soB2cEntity.getInvalidStatus()) )) {
            if (!SoB2cBillStatusEnum.ENUM_SHIPPED.getCode().equals(soB2cEntity.getBillStatus())) {
                // 取消商品数量（合计）
                shudiyunB2cOrderDTO.setTotal_canceled_goods_quantity(totalQty);
            }
        }
        shudiyunB2cOrderDTO.setOrder_quantity_to_be_shipped(totalQty - shudiyunB2cOrderDTO.getTotal_canceled_goods_quantity());

        shudiyunB2cOrderDTO.setRoot_node_modify_time(localDateTime.format(aliexpressDeliveryEntity.getUpdateTime()));

        shudiyunB2cOrderDTO.setGoods_transaction_quantity(aliexpressDeliveryDetailEntity.getOrderLineQty());

        // 单价
        shudiyunB2cOrderDTO.setPrice(aliexpressDeliveryDetailEntity.getProratedUnitPrice());
        // 明细总价
        shudiyunB2cOrderDTO.setGoods_transaction_amount(aliexpressDeliveryDetailEntity.getProratedAmount());

        shudiyunB2cOrderDTO.setStatus(shudiyunB2cOrderDTO.sdyStatusHandle(operate, aliexpressDeliveryEntity.getVersion(), aliexpressDeliveryDetailEntity.getVersion()));

        // 速卖通货品ID
        String scItemId = aliexpressDeliveryDetailEntity.getScItemId();
        shudiyunB2cOrderDTO.setMsku_code(scItemId);
        BigDecimal aliPrice = aliexpressDeliveryDetailEntity.getPrice();
        if (aliPrice != null && BigDecimal.ZERO.compareTo(aliPrice) == 0) {
            shudiyunB2cOrderDTO.setIs_gift(1);
        } else {
            shudiyunB2cOrderDTO.setIs_gift(0);
        }

        // 当前发货单税后金额
        shudiyunB2cOrderDTO.setBuyer_actual_payment(aliexpressDeliveryEntity.getAfterTaxAmount());

        // 公共处理
        commonHandle(soB2cEntity,
                soB2cDetailEntityList,
                skuVOList,
                bomChildrenSkuDTOS,
                parentSkuList,
                currencyList,
                dictCurrencyEntities,
                shopInfoList,
                customerInfoList,
                companyEntities,
                aliexpressDeliveryDetailEntity.getSkuId(),
                aliexpressDeliveryDetailEntity.getSkuNo(),
                true,
                aliexpressDeliveryDetailEntity.getCurrency(),
                shudiyunB2cOrderDTO,
                receiverEntity,
                omsAllDictList,
                partitionEntityList,
                countryEntityList,
                dictGlobalEntityList,
                deptList
        );
        return JSONObject.parseObject(JSONObject.toJSONString(shudiyunB2cOrderDTO), Map.class);
    }

    @Override
    public void syncSdyOrderHandler(SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> soB2cDetailEntityList, String operateEnum, String sourceType) {
        if (SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode().equalsIgnoreCase(sourceType)) {
            // 海外仓推送
            // B2C销售订单作为配货单
            syncDataToSdy(soB2cEntity, soB2cDetailEntityList, operateEnum);
            return;
        }
        if (soB2cEntity.hasPlatformWarehouseOrder() && PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(soB2cEntity.getDictPlatform())) {
            // 速卖通发货单作为配货单
            syncAliExpressDataToSdy(soB2cEntity, operateEnum);
            return;
        }
        // 手工和自发货订单
       if (SourceTypeEnum.SELF_ADD.getCode().equalsIgnoreCase(soB2cEntity.getSourceType()) || !soB2cEntity.hasPlatformWarehouseOrder()) {
            // 非海外仓自发货订单按B2C发货单推送
            syncSelfAddDataToSdy(soB2cEntity, operateEnum);
            return;
        }
        // 其他推送
        syncDataToSdy(soB2cEntity, soB2cDetailEntityList, operateEnum);
    }

    @Override
    public void syncSdyCancelOrder(SoB2cEntity mainEntity, List<SoB2cDetailEntity> detailList, String operateCode) {
        String sourceType;
        if (mainEntity.hasPlatformWarehouseOrder()) {
            // 平台仓订单(平台销售出库单)(扣可用库存)
            sourceType = SourceTypeEnum.PLATFORM_SO_OUT_STOCK.getCode();
        } else {
            List<String> warehouseIds = detailList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
            List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIds);
            if (CollectionUtils.isNotEmpty(overseasWarehouseList)) {
                // 海外仓出库单 (扣可用库存)
                sourceType = SourceTypeEnum.THIRD_WAREHOUSE_CREATE_OUTBOUND_BILL.getCode();
            } else {
                // B2C发货单-取消不需要推送
                log.warn("B2C销售订单自发订单取消状态：不推送：{}", mainEntity.getCode());
                return;
            }
        }
        syncSdyOrderHandler(mainEntity, detailList, operateCode, sourceType);
    }



    /**
     * 计算自发发货单明细单价
     *
     * @param deliveryDetailList    自发货单明细
     * @param soB2cDetailEntityList 销售订单明细
     * @param skuVOList sku列表
     * @param bomChildrenSkuDTOS bom信息
     * @return Map<自发货明细ID, 平分单价>
     */
    @Override
    public Map<String, Pair<BigDecimal, BigDecimal>> convertAllDeliveryDetailPrice(List<SoB2cDeliveryDetailEntity> deliveryDetailList,
                                                                 List<SoB2cDetailEntity> soB2cDetailEntityList,
                                                                 List<SkuVO> skuVOList,
                                                                 List<BomChildrenSkuDTO> bomChildrenSkuDTOS
    ) {
        // Map<自发货明细ID, 平均采购含税成本>
        Map<String, Pair<BigDecimal, BigDecimal>> resultMap = new HashMap<>();

        Map<String, SoB2cDetailEntity> detailEntityMap = soB2cDetailEntityList.stream().collect(Collectors.toMap(BaseEntity::getId, e -> e));

        Map<String, SkuVO> skuVoMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuId, e -> e));

        // 按明细ID分组
        Map<String, List<SoB2cDeliveryDetailEntity>> deliveryMap = deliveryDetailList
                .stream()
                .collect(Collectors.groupingBy(SoB2cDeliveryDetailEntity::getSourceDetailId));

        for (Map.Entry<String, List<SoB2cDeliveryDetailEntity>> entry : deliveryMap.entrySet()) {
            String soDetailId = entry.getKey();
            SoB2cDetailEntity soDetailEntity = detailEntityMap.get(soDetailId);
            if (null == soDetailEntity){
                ServiceException.runError("未找到订单明细:明细ID={}", soDetailId);
            }
            // 未拆分
            if (1 == entry.getValue().size()){
                SoB2cDeliveryDetailEntity b2cDeliveryDetailEntity = entry.getValue().get(0);
                if (b2cDeliveryDetailEntity.getSkuId().equalsIgnoreCase(soDetailEntity.getSkuId())){
                    resultMap.put(b2cDeliveryDetailEntity.getId(), new Pair<>(soDetailEntity.getPrice(), soDetailEntity.getAmount()));
                    continue;
                } else {
                    // 相同sku捆绑拆分
                    BigDecimal divAmount = soDetailEntity.getPrice()
                            .multiply(new BigDecimal(soDetailEntity.getQty()))
                                    .divide(new BigDecimal(b2cDeliveryDetailEntity.getDeliveryQty()), 4, RoundingMode.DOWN);
                    resultMap.put(b2cDeliveryDetailEntity.getId(), new Pair<>(divAmount, soDetailEntity.getAmount()));
                    continue;
                }
            }

            // 总单价
            BigDecimal lastPrice = soDetailEntity.getPrice();
            // 平台明细总价
            BigDecimal lastTotalPrice = soDetailEntity.getAmount();

            // 存在bom
            List<BomChildrenSkuDTO> bomList = bomChildrenSkuDTOS.stream().filter(e -> e.getParentSkuId().equalsIgnoreCase(soDetailEntity.getSkuId())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(bomList)){
                ServiceException.runError("未找到BOM:sku={}", soDetailEntity.getSkuId());
            }
            // 计算bom总成本
            BigDecimal totalCostAmount = BigDecimal.ZERO;
            // sku
            for (BomChildrenSkuDTO bomChildrenSkuDTO : bomList) {
                SkuVO skuVO = skuVoMap.get(bomChildrenSkuDTO.getSkuId());
                if (null == skuVO){
                    ServiceException.runError("未找sku信息:skuId={}", bomChildrenSkuDTO.getSkuId());
                }
                BigDecimal costPrice = ObjectUtils.isEmpty(skuVO.getActualTaxCost()) ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost();
                if (null == costPrice){
                    ServiceException.runError("未找到成本信息:skuId={}", bomChildrenSkuDTO.getSkuId());
                }
                if (0 == costPrice.compareTo(BigDecimal.ZERO)){
                    ServiceException.runError("成本信息为0:skuId={}", bomChildrenSkuDTO.getSkuId());
                }
                BigDecimal allItemPrice = costPrice.multiply(BigDecimal.valueOf(bomChildrenSkuDTO.getQuantity()));
                totalCostAmount = totalCostAmount.add(allItemPrice);
            }

            // 按明细ID时间排序
            List<SoB2cDeliveryDetailEntity> curDetailList = entry.getValue().stream().sorted(Comparator.comparing(SoB2cDeliveryDetailEntity::getId)).collect(Collectors.toList());
            for (int i = 0; i < curDetailList.size(); i++) {
                SoB2cDeliveryDetailEntity b2cDeliveryDetailEntity = curDetailList.get(i);
                BomChildrenSkuDTO curBom =  bomList.stream().filter(e -> e.getSkuId().equalsIgnoreCase(b2cDeliveryDetailEntity.getSkuId())).findFirst().orElse(null);
                if (null == curBom){
                    ServiceException.runError("未找到bom:skuId={},parentId={}", b2cDeliveryDetailEntity.getSkuId(), soDetailEntity.getSkuId());
                }
                SkuVO skuVO = skuVoMap.get(b2cDeliveryDetailEntity.getSkuId());
                if (i == curDetailList.size() - 1){
                    // 判断当前ERP sku在bom的数量是否大于1
                    if (1 < curBom.getQuantity()){
                        lastPrice = lastPrice.divide(BigDecimal.valueOf(curBom.getQuantity()), 4, RoundingMode.DOWN);
                    }
                    resultMap.put(b2cDeliveryDetailEntity.getId(), new Pair<>(lastPrice, lastTotalPrice));
                } else {
                    // 当前单价 = 明细单价 * (bom成本 * bom数量 / bom总成本) / bom数量
                    BigDecimal curPrice = lastPrice.multiply(skuVO.getActualTaxCost())
                            .divide(totalCostAmount, 4, RoundingMode.DOWN);
                    // 当前sku总价
                    BigDecimal curTotalPrice = curPrice.multiply(BigDecimal.valueOf(b2cDeliveryDetailEntity.getDeliveryQty()));
                    // 设置到结果
                    resultMap.put(b2cDeliveryDetailEntity.getId(), new Pair<>(curPrice, curTotalPrice));
                    // 剩余单价 = 当前单价 * bom数量
                    lastPrice = lastPrice.subtract(curPrice.multiply(BigDecimal.valueOf(curBom.getQuantity())));
                    // 剩余总价 = 当前总价 -（当前sku总价）
                    lastTotalPrice = lastTotalPrice.subtract(curTotalPrice);
                }
            }
        }
        return resultMap;
    }


    @Override
    public void hisSyncSelfDataToSdy(
            SoB2cEntity soB2cEntity,
            List<SoB2cDetailEntity> soB2cDetailEntityList,
            SoB2cDeliveryEntity soB2cDeliveryEntity,
            List<SoB2cDeliveryDetailEntity> allDeliveryDetail,
            SoB2cDeliveryDetailEntity soB2cDeliveryDetailEntity,
            String operate,
            List<SkuVO> skuVOList,
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
            List<ProductDetailEntity> parentSkuList,
            List<ListingInfoEntity> listingInfoEntities,
            List<CurrencyDTO.ViewDTO> currencyList,
            List<DictCurrencyEntity> dictCurrencyEntities,
            List<ShopInfoEntity> shopInfoList,
            List<CustomerInfoEntity> customerInfoList,
            List<BaseIdDTO.CodeDTO> companyEntities,
            SoB2cReceiverEntity receiverEntity,
            List<DictBasicEntity> omsAllDictList,
            List<DictPartitionEntity> partitionEntityList,
            List<DictCountryEntity> countryEntityList,
            List<DictGlobalAreaEntity> dictGlobalEntityList,
            List<SysDepartmentEntity> deptList

    ){
        Map<String, Pair<BigDecimal, BigDecimal>> deliveryDetailPriceMap = convertAllDeliveryDetailPrice(allDeliveryDetail, soB2cDetailEntityList, skuVOList, bomChildrenSkuDTOS);
        //同步配货单
            OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
            omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
            omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SELF_DELIVERY_ORDER.getCode());
            omsPushMsgEntity.setSourceId(soB2cDeliveryDetailEntity.getId());
            omsPushMsgEntity.setSourceCode(soB2cEntity.getCode() + "_" + soB2cDeliveryDetailEntity.getSkuNo());
            omsPushMsgEntity.setSyncOperate(operate);
            omsPushMsgEntity.setPushData(JSON.toJSONString(this.syncSelfAddDataToSdyFieldHandler(soB2cEntity,
                    soB2cDetailEntityList,
                    soB2cDeliveryEntity,
                    allDeliveryDetail,
                    soB2cDeliveryDetailEntity,
                    operate,
                    skuVOList,
                    bomChildrenSkuDTOS,
                    parentSkuList,
                    listingInfoEntities,
                    currencyList,
                    dictCurrencyEntities,
                    shopInfoList,
                    customerInfoList,
                    companyEntities,
                    deliveryDetailPriceMap,
                    receiverEntity, 
                    omsAllDictList, 
                    partitionEntityList, 
                    countryEntityList, 
                    dictGlobalEntityList, 
                    deptList)));
            omsPushMsgService.save(omsPushMsgEntity);
    }


    @Override
    public void hisSyncAliExpressDataToSdyFieldHandler(
            SoB2cEntity soB2cEntity,
            List<SoB2cDetailEntity> soB2cDetailEntityList,
            AliexpressDeliveryEntity aliexpressDeliveryEntity,
            List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList,
            AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity,
            String operate,
            List<SkuVO> skuVOList,
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS,
            List<ProductDetailEntity> parentSkuList,
            List<ListingInfoEntity> listingInfoEntities,
            List<CurrencyDTO.ViewDTO> currencyList,
            List<DictCurrencyEntity> dictCurrencyEntities,
            List<ShopInfoEntity> shopInfoList,
            List<CustomerInfoEntity> customerInfoList,
            List<BaseIdDTO.CodeDTO> companyEntities,
            SoB2cReceiverEntity receiverEntity,
            List<DictBasicEntity> omsAllDictList,
            List<DictPartitionEntity> partitionEntityList,
            List<DictCountryEntity> countryEntityList,
            List<DictGlobalAreaEntity> dictGlobalEntityList,
            List<SysDepartmentEntity> deptList
    ) {
        //同步配货单
        OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
        omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_ALIEXPRESS_DELIVERY_ORDER.getCode());
        omsPushMsgEntity.setSourceId(aliexpressDeliveryDetailEntity.getId());
        omsPushMsgEntity.setSourceCode(soB2cEntity.getCode() + "_" + aliexpressDeliveryDetailEntity.getSkuNo());
        omsPushMsgEntity.setSyncOperate(operate);
        omsPushMsgEntity.setPushData(JSON.toJSONString(this.syncAliExpressDataToSdyFieldHandler(soB2cEntity,
                soB2cDetailEntityList,
                aliexpressDeliveryEntity,
                aliexpressDeliveryDetailEntityList,
                aliexpressDeliveryDetailEntity,
                operate,
                skuVOList,
                bomChildrenSkuDTOS,
                parentSkuList,
                listingInfoEntities,
                currencyList,
                dictCurrencyEntities,
                shopInfoList,
                customerInfoList,
                companyEntities,
                receiverEntity,
                omsAllDictList,
                partitionEntityList,
                countryEntityList,
                dictGlobalEntityList,
                deptList)));
        omsPushMsgService.save(omsPushMsgEntity);
    }

}
