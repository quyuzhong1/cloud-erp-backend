package com.erp.server.wms.schedule;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.common.business.wrapper.QueryParam;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.dto.SysDepartmentDTO;
import com.erp.model.sys.entity.*;
import com.erp.model.wms.entity.*;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.rpc.oms.feign.SoInfoFeign;
import com.erp.rpc.oms.feign.SoReturnFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.SoOutstockFeign;
import com.erp.server.wms.kingdee.SyncKingdeeSoOutstockService;
import com.erp.server.wms.kingdee.SyncSoReturnInstockService;
import com.erp.server.wms.service.*;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class SdyDataSyncJob {
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
    private SyncSoReturnInstockService syncSoReturnInstockService;
    @Resource
    private SoReturnFeign soReturnFeign;
    @Resource
    private SoReturnReceiveService soReturnReceiveService;
    @Resource
    private DictBasicService dictBasicService;

    @XxlJob("syncSdySoOutstock")
    public void syncSdySoOutstock() {
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

        List<SoOutstockEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            if (StringUtils.isNotBlank(queryParamsStr)) {
                List<QueryParam> queryParams = JSONUtil.toList(queryParamsStr, QueryParam.class);
                QueryWrapper<SoOutstockEntity> queryWrapper = (QueryWrapper<SoOutstockEntity>) QueryParam.getQueryWrapper(queryParams);
                Page<SoOutstockEntity> page = soOutstockService.page(new Page<>(currentPage, pageSize), queryWrapper);
                list = page.getRecords();
            } else {
                list = soOutstockService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            }
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            if (CollUtil.isEmpty(ids)) {
                return;
            }
            List<SoOutstockDetailEntity> soOutstockDetailEntityList = soOutstockDetailService.listByMainIds(ids);

            //B2C订单
            List<SoOutstockEntity> b2cEntity = list.stream().filter(req -> OrderTypeEnum.B2C.getCode().equals(req.getOrderType())).collect(Collectors.toList());
            List<String> b2cSoIds = b2cEntity.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
            List<SoB2cEntity> soB2cEntities = soB2cFeign.listByIds(b2cSoIds);

            //B2B订单
            List<SoOutstockEntity> b2bEntity = list.stream().filter(req -> OrderTypeEnum.B2B.getCode().equals(req.getOrderType())).collect(Collectors.toList());
            List<String> b2bSoIds = b2bEntity.stream().map(req -> req.getSoId()).distinct().collect(Collectors.toList());
            List<SoInfoEntity> soInfoEntities = soInfoFeign.listSoInfoByIds(b2bSoIds);

            //客户
            List<String> customerIds = list.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
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
            List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(orgList);

            //产品信息
            List<String> skuNos = soOutstockDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
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

            for (SoOutstockEntity soOutstockEntity : list) {
                List<SoOutstockDetailEntity> detailEntityList = soOutstockDetailEntityList.stream().filter(req -> req.getMainId().equals(soOutstockEntity.getId())).collect(Collectors.toList());
                syncKingdeeSoOutstockService.syncDataToSdy(soOutstockEntity,
                        detailEntityList,
                        SyncOperateEnum.OPERATE_APPROVE.getCode(),
                        currencyList,
                        shopInfoList,
                        customerInfoList,
                        companyEntities,
                        skuVOList,
                        bomChildrenSkuDTOS,
                        parentSkuList,
                        soB2cEntities,
                        soInfoEntities,
                        Collections.emptyList(),
                        dictBasicEntityList,
                        partitionEntityList,
                        countryEntityList,
                        dictGlobalEntityList,
                        deptList
                );
            }
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "结束时间：" + LocalDateTime.now());
        }
    }

    @XxlJob("SyncSoReturnInstockJob")
    public void SyncSoReturnInstockJob() {
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

        List<SoReturnInstockEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            if (StringUtils.isNotBlank(queryParamsStr)) {
                List<QueryParam> queryParams = JSONUtil.toList(queryParamsStr, QueryParam.class);
                QueryWrapper<SoReturnInstockEntity> queryWrapper = (QueryWrapper<SoReturnInstockEntity>) QueryParam.getQueryWrapper(queryParams);
                Page<SoReturnInstockEntity> page = soReturnInstockService.page(new Page<>(currentPage, pageSize), queryWrapper);
                list = page.getRecords();
            } else {
                list = soReturnInstockService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            }
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<SoReturnInstockDetailEntity> detailEntityList = soReturnInstockDetailService.listDetailByMainIds(ids);


            List<String> skuNos = detailEntityList.stream().map(req -> req.getSkuNo()).collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
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
            if (CollectionUtils.isEmpty(b2bReturnInstockList)){
                List<String> b2bSoIds = b2cReturnInstockList.stream().map(SoReturnInstockEntity::getSoId)
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
                    SoB2cReceiverEntity receiverEntity = receiverEntityList.stream().filter(e -> e.getId().equalsIgnoreCase(entity.getSoId())).findFirst().orElse(null);
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

                syncSoReturnInstockService.syncDataToSdy(entity,
                        detailEntities,
                        SyncOperateEnum.OPERATE_APPROVE.getCode(),
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
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "结束时间：" + LocalDateTime.now());
        }

    }
}
