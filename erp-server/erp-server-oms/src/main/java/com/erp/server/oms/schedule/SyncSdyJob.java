package com.erp.server.oms.schedule;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.kingdee.SyncKingdeeSoService;
import com.erp.server.oms.kingdee.SyncSoB2cService;
import com.erp.server.oms.service.*;
import com.google.common.collect.Lists;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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



    @XxlJob("syncSdySoB2c")
    public void syncSdySoB2c() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
        }

        //总条数
        int currentPage = 0;

        List<SoB2cEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            list = soB2cService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(ids);

            //产品信息
            List<String> skuNos = soB2cDetailEntityList.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
            List<String> skuIds = soB2cDetailEntityList.stream().map(req -> req.getSkuId()).distinct().collect(Collectors.toList());
            List<BomChildrenSkuDTO> bomChildrenSkuDTOS = plmTaskFeign.listBomBySkuIds(skuIds);
            //父类产品
            List<String> parentSkuId = bomChildrenSkuDTOS.stream().map(BomChildrenSkuDTO::getParentSkuId).distinct().collect(Collectors.toList());
            List<ProductDetailEntity> parentSkuList = new ArrayList<>();
            if (CollUtil.isNotEmpty(parentSkuId)) {
                parentSkuList = FeignQuery.create(ProductDetailEntity.class)
                        .in(ProductDetailEntity::getId, parentSkuId)
                        .list();
            }
            //平台sku映射信息
            List<String> platformSkuNoList = soB2cDetailEntityList.stream().map(req -> req.getPlatformSkuNo()).distinct().collect(Collectors.toList());
            List<ListingInfoEntity> listingInfoEntities = listingInfoService.lambdaQuery().in(ListingInfoEntity::getPlatformSkuNo, platformSkuNoList).list();

            //币别
            List<String> currency = list.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currency);

            //店铺
            List<String> shopIds = list.stream().map(req -> req.getShopId()).distinct().collect(Collectors.toList());
            List<ShopInfoEntity> shopInfoList = new ArrayList<>();
            if (CollUtil.isNotEmpty(shopIds)) {
                shopInfoList = shopInfoService.lambdaQuery().in(ShopInfoEntity::getId, shopIds).list();
            }

            List<String> tradeCurrency = shopInfoList.stream().map(req -> req.getTradeCurrency()).distinct().collect(Collectors.toList());
            List<DictCurrencyEntity> dictCurrencyEntities = new ArrayList<>();
            if (CollUtil.isNotEmpty(tradeCurrency)) {
                dictCurrencyEntities = FeignQuery.create(DictCurrencyEntity.class).in(DictCurrencyEntity::getId, tradeCurrency).list();
            }

            //客户
            List<String> customerIdList = shopInfoList.stream().map(req -> req.getCustomerId()).distinct().collect(Collectors.toList());
            List<CustomerInfoEntity> customerInfoList = new ArrayList<>();
            if (CollUtil.isNotEmpty(customerIdList)) {
                customerInfoList = customerInfoService.lambdaQuery().in(CustomerInfoEntity::getId, customerIdList).list();
            }

            List<String> orgList = new ArrayList<>();
            List<String> orgIds = customerInfoList.stream().map(req -> req.getFinancialOrganization()).distinct().collect(Collectors.toList());
            orgList.addAll(orgIds);
            List<String> salseOrgIds = shopInfoList.stream().map(req -> req.getSalesOrgId()).distinct().collect(Collectors.toList());
            orgList.addAll(salseOrgIds);

            List<BaseIdDTO.CodeDTO> companyEntities = new ArrayList<>();

            if (CollUtil.isNotEmpty(orgList)) {
                companyEntities = sysUserFeign.getAccountingCompanyList(orgList);
            }
            List<String> dictKeys = Lists.newArrayList(DictBasicTypeEnum.SALES_PLATFORM.getType());
            List<DictBasicEntity> dictBasicEntityList = dictBasicService.getByKeyList(dictKeys);

            List<String> platformTypeList = customerInfoList.stream().map(req -> req.getPlatformType()).distinct().collect(Collectors.toList());
            List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, platformTypeList).list();

            for (SoB2cEntity soB2cEntity : list) {
                List<SoB2cDetailEntity> detailEntityList = soB2cDetailEntityList.stream().filter(req -> req.getMainId().equals(soB2cEntity.getId())).collect(Collectors.toList());

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
                        dictBasicEntityList,
                        dictList
                );
            }
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "结束时间：" + LocalDateTime.now());
        }
    }

    @XxlJob("syncSdySoInfo")
    public void syncSdySoInfo() {
        String jobParam = XxlJobHelper.getJobParam();
        LocalDateTime createStartTime = null;
        LocalDateTime createEndTime = null;
        Integer pageSize = 1000;// 每页记录数
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
        }

        //总条数
        int currentPage = 0;
        List<SoInfoEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            list = soInfoService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            if (CollUtil.isEmpty(list)) {
                return;
            }
            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<SoDetailEntity> soDetailEntities = soDetailService.listBaseByMainIdList(ids);

            //产品信息
            List<String> skuNos = soDetailEntities.stream().map(req -> req.getSkuNo()).distinct().collect(Collectors.toList());
            List<SkuVO> skuVOList = plmTaskFeign.listBySkuNoList(skuNos);
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

            List<String> currencyIds = list.stream().map(req -> req.getCurrency()).distinct().collect(Collectors.toList());
            List<CurrencyDTO.ViewDTO> currencyList = sysUserFeign.listByCurrency(currencyIds);

            List<String> soDetailIds = soDetailEntities.stream().map(req -> req.getId()).collect(Collectors.toList());
            List<SoChangeDetailEntity> soChangeDetailEntities = soChangeDetailService.listBySoDetailIdList(soDetailIds);

            List<String> subPlatformType = customerInfoEntities.stream().map(req -> req.getPlatformType()).distinct().collect(Collectors.toList());
            List<DictBasicEntity> dictList = dictBasicService.lambdaQuery().eq(DictBasicEntity::getType, "sdySubPlatform").in(DictBasicEntity::getName, subPlatformType).list();

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
                        dictBasicEntityList,
                        currencyList,
                        soChangeDetailEntities,
                        "",
                        dictList
                );
            }
            XxlJobHelper.log("===========当前页数：" + currentPage + "结束时间：" + LocalDateTime.now());
            currentPage++;
        }
    }
}
