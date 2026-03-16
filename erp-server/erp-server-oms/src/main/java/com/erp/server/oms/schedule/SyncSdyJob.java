package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.common.core.entity.BaseEntity;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.SoB2cDeliveryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.kingdee.SyncSoB2cService;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 同步数帝云
 */
@Component
@Slf4j
public class SyncSdyJob {
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDetailService soB2cDetailService;
    @Resource
    private SysUserFeign sysUserFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private SyncSoB2cService syncSoB2cService;
    @Resource
    private ListingInfoService listingInfoService;
    @Resource
    private ShopInfoService shopInfoService;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private SoInfoService soInfoService;
    @Resource
    private SoDetailService soDetailService;
    @Resource
    private SyncKingdeeSoService syncKingdeeSoService;
    @Resource
    private SoChangeDetailService soChangeDetailService;
    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;
    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;
    @Resource
    private SoB2cReceiverService soB2cReceiverService;


    @XxlJob("syncSdySoB2c")
    public void syncSdySoB2c() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        List<String> platformList = new LinkedList<>();
        String queryParamsStr = "";
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            String platformListStr = jsonParam.getStr("platformList");
            if (StringUtils.isNotBlank(platformListStr)){
                platformList = Arrays.stream(platformListStr.split(",")).collect(Collectors.toList());
            }
            jsonParam.getInt("pageSize", 1000);
            queryParamsStr = jsonParam.getStr("queryParams");
        }

        //总条数
        int currentPage = 0;

        List<SoB2cEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;

            if (StringUtils.isNotBlank(queryParamsStr)){
                List<QueryParam> queryParams = JSONUtil.toList(queryParamsStr, QueryParam.class);
                QueryWrapper<SoB2cEntity> queryWrapper = (QueryWrapper<SoB2cEntity>) QueryParam.getQueryWrapper(queryParams);
                Page<SoB2cEntity> page = soB2cService.page(new Page<>(currentPage + 1, pageSize), queryWrapper);
                list = page.getRecords();
            } else {
                list = soB2cService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset, platformList);
            }

            if (CollUtil.isEmpty(list)) {
                XxlJobHelper.log("===========当前页数：" + currentPage + "， 结果为空结束时间：" + LocalDateTime.now());
                return;
            }

            List<String> soIds = list.stream().map(BaseEntity::getId).collect(Collectors.toList());
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(soIds);

            List<String> warehouseIds = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
            List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIds);

            //平台sku映射信息
            List<String> platformSkuNoList = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getPlatformSkuNo).distinct().collect(Collectors.toList());
            List<ListingInfoEntity> listingInfoEntities = listingInfoService.lambdaQuery().in(ListingInfoEntity::getPlatformSkuNo, platformSkuNoList).list();

            //币别
            List<String> currency = list.stream().map(SoB2cEntity::getCurrency).distinct().collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currency);

            //店铺
            List<String> shopIds = list.stream().map(SoB2cEntity::getShopId).distinct().collect(Collectors.toList());
            List<ShopInfoEntity> shopInfoList = new ArrayList<>();
            if (CollUtil.isNotEmpty(shopIds)) {
                shopInfoList = shopInfoService.lambdaQuery().in(ShopInfoEntity::getId, shopIds).list();
            }

            List<String> tradeCurrency = shopInfoList.stream().map(ShopInfoEntity::getTradeCurrency).distinct().collect(Collectors.toList());
            List<DictCurrencyEntity> dictCurrencyEntities = new ArrayList<>();
            if (CollUtil.isNotEmpty(tradeCurrency)) {
                dictCurrencyEntities = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, tradeCurrency).list();
            }

            //客户
            List<String> customerIdList = shopInfoList.stream().map(ShopInfoEntity::getCustomerId).distinct().collect(Collectors.toList());
            List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
            if (CollUtil.isNotEmpty(customerIdList)) {
                customerInfoList = customerInfoService.lambdaQuery().in(CustomerInfoEntity::getId, customerIdList).list();
            }

            List<String> orgList = customerInfoList.stream().map(CustomerInfoEntity::getFinancialOrganization).distinct().collect(Collectors.toList());
            List<String> salseOrgIds = shopInfoList.stream().map(ShopInfoEntity::getSalesOrgId).distinct().collect(Collectors.toList());
            orgList.addAll(salseOrgIds);

            List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();

            if (CollUtil.isNotEmpty(orgList)) {
                companyEntities = sysUserFeign.getAccountingCompanyList(orgList);
            }

            List<String> skuNos = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuNo).distinct().collect(Collectors.toList());
            List<String> skuIds = soB2cDetailEntityList.stream().map(SoB2cDetailEntity::getSkuId).distinct().collect(Collectors.toList());

            // 速卖通官方发货
            Map<String, List<AliexpressDeliveryEntity>> aliexpressDeliveryMap = new HashMap<>();
            List<AliexpressDeliveryDetailEntity> detailAliexpressDeliveryList = new LinkedList<>();
            List<String> aliExpressSoIds = list.stream()
                    .filter(e -> PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(e.getDictPlatform()) && e.hasPlatformWarehouseOrder())
                    .map(BaseEntity::getId).distinct().collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(aliExpressSoIds)){
                List<AliexpressDeliveryEntity> aliexpressDeliveryList = FeignQuery.create(AliexpressDeliveryEntity.class)
                        .in(AliexpressDeliveryEntity::getSoId, soIds)
                        .list();
                aliexpressDeliveryMap  = aliexpressDeliveryList.stream().collect(Collectors.groupingBy(AliexpressDeliveryEntity::getSoId));
                if (!CollectionUtils.isEmpty(aliexpressDeliveryList)) {
                    List<String> mainIds = aliexpressDeliveryList.stream().map(BaseEntity::getId).collect(Collectors.toList());
                    detailAliexpressDeliveryList = FeignQuery.create(AliexpressDeliveryDetailEntity.class)
                            .in(AliexpressDeliveryDetailEntity::getMainId, mainIds)
                            .list();
                    skuNos.addAll(detailAliexpressDeliveryList.stream().map(AliexpressDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList()));
                    skuIds.addAll(detailAliexpressDeliveryList.stream().map(AliexpressDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList()));
                }
            }

            // 自发货
            List<String> selfAddSoIds = list.stream()
                    .filter(e -> !e.hasPlatformWarehouseOrder())
                    .map(BaseEntity::getId).distinct().collect(Collectors.toList());
            Map<String, List<SoB2cDeliveryEntity>> soB2cDeliveryEntityMap = new HashMap<>();
            List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntityList = new LinkedList<>();
            if (CollectionUtils.isNotEmpty(selfAddSoIds)){
                // 最新已发货单
                List<SoB2cDeliveryEntity> soB2cDeliveryEntityList = soB2cDeliveryFeign.listBySourceId(selfAddSoIds)
                        .stream()
                        .filter(e -> e.getStatus().equalsIgnoreCase(SoB2cDeliveryStatusEnum.SHIPPED.getCode()))
                        .collect(Collectors.toList());
                soB2cDeliveryEntityMap = soB2cDeliveryEntityList.stream().collect(Collectors.groupingBy(SoB2cDeliveryEntity::getSourceId));
                if (!CollectionUtils.isEmpty(soB2cDeliveryEntityList)) {
                    List<String> mainIds = soB2cDeliveryEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
                    soB2cDeliveryDetailEntityList = FeignQuery.create(SoB2cDeliveryDetailEntity.class)
                            .in(SoB2cDeliveryDetailEntity::getMainId, mainIds)
                            .list();
                    skuNos.addAll(soB2cDeliveryDetailEntityList.stream().map(SoB2cDeliveryDetailEntity::getSkuNo).distinct().collect(Collectors.toList()));
                    skuIds.addAll(soB2cDeliveryDetailEntityList.stream().map(SoB2cDeliveryDetailEntity::getSkuId).distinct().collect(Collectors.toList()));
                }
            }

            // 出库单
            List<SoOutstockEntity> outstockEntityList = FeignQuery.create(SoOutstockEntity.class)
                    .in(SoOutstockEntity::getSoId, soIds)
                    .eq(SoOutstockEntity::getInvalidStatus, false)
                    .list();

            //产品信息
            List<SkuVO> skuVOList = new ArrayList<>();
            if (CollUtil.isNotEmpty(skuNos)) {
                skuVOList = plmTaskFeign.listAllStatusSkuBySkuNos(skuNos);
            }
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = new ArrayList<>();
            if (CollUtil.isNotEmpty(skuIds)) {
                bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
            }
            //父类产品
            List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
            List<ProductDetailEntity> parentSkuList = new ArrayList<>();
            if (CollUtil.isNotEmpty(parentSkuId)) {
                parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                        .in(ProductDetailEntity::getId, parentSkuId)
                        .list();
            }

            List<SoB2cReceiverEntity> soB2cReceiverEntityList = soB2cReceiverService.listByMainIds(soIds);

            List<DictBasicEntity> omsAllDictList = FeignQuery.create(DictBasicEntity.class)
                    .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                            DictBasicTypeEnum.SDY_SUB_PLATFORM.getType()
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

            // 虚拟商品
            List<String> noInventorySkuIdList = plmTaskFeign.getNoInventorySku()
                    .stream()
                    .map(SkuVO::getSkuId).distinct().collect(Collectors.toList());


            for (SoB2cEntity soB2cEntity : list) {
                SoB2cReceiverEntity receiverEntity = soB2cReceiverEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).findFirst().orElse(null);
                if (ObjectUtils.isEmpty(receiverEntity)) {
                    XxlJobHelper.log("===========数据异常：未找到SoB2cReceiverEntity：{}", soB2cEntity.getCode());
                    continue;
                }
                // 不出库发货虚拟商品推送
                if (soB2cEntity.getIsNotOutbound()){
                    List<SoB2cDetailEntity> noInventorySkuDetailList = soB2cDetailEntityList.stream()
                            .filter(e -> noInventorySkuIdList.contains(e.getSkuId()) && e.getMainId().equals(soB2cEntity.getId()))
                            .collect(Collectors.toList());
                    if (CollectionUtils.isNotEmpty(noInventorySkuDetailList)) {
                        // 原始同步数帝云
                        syncSoB2cService.syncDataToSdy(soB2cEntity,
                                noInventorySkuDetailList,
                                SyncOperateEnum.OPERATE_APPROVE.getCode(),
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
                                deptList
                        );
                    }
                    continue;
                }

                List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());
                // 平台仓订单(平台销售出库单)
                if (soB2cEntity.hasPlatformWarehouseOrder()) {
                    // 并且已出库
                    if (outstockEntityList.stream().anyMatch(e->e.getSoId().equalsIgnoreCase(soB2cEntity.getId()))){
                        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(soB2cEntity.getDictPlatform())){
                            // 速卖通推送
                            syncAliExpressDelivery(soB2cEntity,
                                    aliexpressDeliveryMap,
                                    detailAliexpressDeliveryList,
                                    detailEntityList,
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
                                    deptList
                            );
                        } else {
                            // 其他平台推送
                            // 原始同步数帝云
                            syncSoB2cService.syncDataToSdy(soB2cEntity,
                                    detailEntityList,
                                    SyncOperateEnum.OPERATE_APPROVE.getCode(),
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
                                    deptList
                            );
                        }
                    }
                    // 未出库跳过
                    continue;
                }


                List<String> cueWarehouseIds = detailEntityList.stream().map(SoB2cDetailEntity::getWarehouseId).distinct().collect(Collectors.toList());
                List<OverseasProviderWarehouseDTO.ViewDTO> collect = overseasWarehouseList.stream().filter(e -> cueWarehouseIds.contains(e.getWarehouseId())).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(collect)) {
                    // 海外仓出库单出库推送
                    if (outstockEntityList.stream().anyMatch(e->e.getSoId().equalsIgnoreCase(soB2cEntity.getId()))){
                        // 原始同步数帝云
                        syncSoB2cService.syncDataToSdy(soB2cEntity,
                                detailEntityList,
                                SyncOperateEnum.OPERATE_APPROVE.getCode(),
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
                                deptList
                        );
                    }
                    // 未出库跳过
                    continue;
                }
                // 自发货出库
                if (outstockEntityList.stream().anyMatch(e->e.getSoId().equalsIgnoreCase(soB2cEntity.getId()))){
                    selfAddSoB2cDelivery(soB2cEntity,
                            soB2cDeliveryEntityMap,
                            soB2cDeliveryDetailEntityList,
                            detailEntityList,
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
                            deptList);
                }
            }
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "处理数量："+ list.size() +" 结束时间：" + LocalDateTime.now());
        }
    }


    private void selfAddSoB2cDelivery(SoB2cEntity soB2cEntity,
                                      Map<String, List<SoB2cDeliveryEntity>> soB2cDeliveryEntityMap,
                                      List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntityList,
                                      List<SoB2cDetailEntity> soB2cDetailEntityList,
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
                                      List<SysDepartmentEntity> deptList) {
        List<SoB2cDeliveryEntity> soB2cDeliveryEntityList = soB2cDeliveryEntityMap.get(soB2cEntity.getId());
        if (CollectionUtils.isEmpty(soB2cDeliveryEntityList)){
            return;
        }
        for (SoB2cDeliveryEntity soB2cDeliveryEntity : soB2cDeliveryEntityList) {
            List<SoB2cDeliveryDetailEntity> allDeliveryDetail = soB2cDeliveryDetailEntityList.stream().filter(e -> e.getMainId().equalsIgnoreCase(soB2cDeliveryEntity.getId())).collect(Collectors.toList());
            for (SoB2cDeliveryDetailEntity soB2cDeliveryDetailEntity : allDeliveryDetail) {
                // 自发货推送
                syncSoB2cService.hisSyncSelfDataToSdy(
                        soB2cEntity,
                        soB2cDetailEntityList,
                        soB2cDeliveryEntity,
                        allDeliveryDetail,
                        soB2cDeliveryDetailEntity,
                        SyncOperateEnum.OPERATE_APPROVE.getCode(),
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
                        deptList);
            }
        }
    }

    private void syncAliExpressDelivery(SoB2cEntity soB2cEntity,
                                        Map<String, List<AliexpressDeliveryEntity>> aliexpressDeliveryMap,
                                        List<AliexpressDeliveryDetailEntity> detailAliexpressDeliveryList,
                                        List<SoB2cDetailEntity> soB2cDetailEntityList,
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
        List<AliexpressDeliveryEntity> aliexpressDeliveryList = aliexpressDeliveryMap.get(soB2cEntity.getId());
        if (CollectionUtils.isEmpty(aliexpressDeliveryList)){
            return;
        }
        for (AliexpressDeliveryEntity aliexpressDeliveryEntity : aliexpressDeliveryList) {
            List<AliexpressDeliveryDetailEntity> aliexpressDeliveryDetailEntityList = detailAliexpressDeliveryList.stream().filter(e -> e.getMainId().equalsIgnoreCase(aliexpressDeliveryEntity.getId())).collect(Collectors.toList());
            for (AliexpressDeliveryDetailEntity aliexpressDeliveryDetailEntity : aliexpressDeliveryDetailEntityList) {
                syncSoB2cService.hisSyncAliExpressDataToSdyFieldHandler(
                        soB2cEntity,
                        soB2cDetailEntityList,
                        aliexpressDeliveryEntity,
                        aliexpressDeliveryDetailEntityList,
                        aliexpressDeliveryDetailEntity,
                        SyncOperateEnum.OPERATE_APPROVE.getCode(),
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
                        deptList);
            }
        }
    }

    @XxlJob("syncSdySoInfo")
    public void syncSdySoInfo() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        String queryParamsStr = "";
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
            queryParamsStr = jsonParam.getStr("queryParams");
        }

        //总条数
        int currentPage = 0;
        List<SoInfoEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            if (StringUtils.isNotBlank(queryParamsStr)) {
                List<QueryParam> queryParams = JSONUtil.toList(queryParamsStr, QueryParam.class);
                QueryWrapper<SoInfoEntity> queryWrapper = (QueryWrapper<SoInfoEntity>) QueryParam.getQueryWrapper(queryParams);
                Page<SoInfoEntity> page = soInfoService.page(new Page<>(currentPage + 1, pageSize), queryWrapper);
                list = page.getRecords();
            } else {
                list = soInfoService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            }
            if (CollUtil.isEmpty(list)) {
                XxlJobHelper.log("===========当前页数：" + currentPage + "， 结果为空结束时间：" + LocalDateTime.now());
                return;
            }
            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<SoDetailEntity> soDetailEntities = soDetailService.listBaseByMainIdList(ids);

            //产品信息
            List<String> skuNos = soDetailEntities.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listAllStatusSkuBySkuNos(skuNos);
            List<String> skuIds = soDetailEntities.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomChildBySkuIds(skuIds);
            //父类产品
            List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
            List<ProductDetailEntity> parentSkuList = new ArrayList<>();
            if (CollUtil.isNotEmpty(parentSkuId)) {
                parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                        .in(ProductDetailEntity::getId, parentSkuId)
                        .list();
            }

            List<String> customerIds = list.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
            List<CustomerInfoEntity> customerInfoEntities = new ArrayList<>();
            if (CollUtil.isNotEmpty(customerIds)) {
                customerInfoEntities = customerInfoService.listByIds(customerIds);
            }

            //组织信息
            List<String> orgIdList = new ArrayList<>();
            List<String> orgIds = customerInfoEntities.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
            orgIdList.addAll(orgIds);
            List<String> salesOrgIds = list.stream().map(req -> req.getSalesOrgId()).collect(Collectors.toList());
            orgIdList.addAll(salesOrgIds);
            List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(orgIdList);

            List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
            List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

            List<String> currencyIds = soDetailEntities.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIds);

            List<String> soDetailIds = soDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<SoChangeDetailEntity> soChangeDetailEntities = soChangeDetailService.listBySoDetailIdList(soDetailIds);

            List<DictBasicEntity> omsAllDictList = FeignQuery.create(DictBasicEntity.class)
                    .in(DictBasicEntity::getType, Arrays.asList(DictBasicTypeEnum.SALES_PLATFORM.getType(),
                            DictBasicTypeEnum.SDY_SUB_PLATFORM.getType()
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
            for (SoInfoEntity soInfoEntity : list) {
                List<SoDetailEntity> detailEntityList = soDetailEntities.stream().filter(req -> req.getMainId().equals(soInfoEntity.getId())).collect(Collectors.toList());

                syncKingdeeSoService.syncDataToSdy(soInfoEntity,
                        detailEntityList,
                        SyncOperateEnum.OPERATE_APPROVE.getCode(),
                        skuVOList,
                        bomChildrenSkuDTOS,
                        parentSkuList,
                        customerInfoEntities,
                        companyEntities,
                        currencyList,
                        soChangeDetailEntities,
                        omsAllDictList,
                        partitionEntityList,
                        countryEntityList,
                        dictGlobalEntityList,
                        deptList
                );
            }
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "处理数量："+ list.size() +" 结束时间：" + LocalDateTime.now());
        }
    }
}
