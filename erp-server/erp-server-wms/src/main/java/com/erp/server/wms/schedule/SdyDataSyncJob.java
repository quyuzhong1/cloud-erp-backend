package com.erp.server.wms.schedule;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.business.dto.ShudiyunB2cOrderDTO;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.OrderTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.SyncOperateEnum;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.entity.DictBasicEntity;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.OrderSubTypeEnum;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.sys.dto.CurrencyDTO;
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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    @XxlJob("syncSdySoOutstock")
    public void syncSdySoOutstock() {
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

        List<SoOutstockEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            list = soOutstockService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
            if (CollUtil.isEmpty(list)) {
                return;
            }

            List<String> ids = list.stream().map(req -> req.getId()).collect(Collectors.toList());
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
            List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();

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
                        dictBasicEntityList);
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
        if (StrUtil.isNotBlank(jobParam)) {
            JSONObject jsonParam = JSONUtil.parseObj(jobParam);
            createStartTime = jsonParam.getLocalDateTime("createStartTime", LocalDateTime.now().minusMonths(1));
            createEndTime = jsonParam.getLocalDateTime("createEndTime", LocalDateTime.now());
            jsonParam.getInt("pageSize", 1000);
        }
        //总条数
        int currentPage = 0;

        List<SoReturnInstockEntity> list = new ArrayList<>();
        while (true) {
            XxlJobHelper.log("===========当前页数：" + currentPage + "开始时间：" + LocalDateTime.now());
            int offset = currentPage * pageSize;
            list = soReturnInstockService.queryToSdy(createStartTime.toLocalDate(), createEndTime.toLocalDate(), pageSize, offset);
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
            List<BaseIdDTO.CodeDTO> companyEntities = sysUserFeign.getAccountingCompanyList(financialOrganization);

            List<DictBasicEntity> dictBasicEntityList = FeignQuery.create(DictBasicEntity.class).eq(DictBasicEntity::getType, DictBasicTypeEnum.SALES_PLATFORM.getType()).list();


            List<String> soReturnIds = list.stream().filter(req -> SourceTypeEnum.SO_RETURN.getCode().equals(req.getSourceType())).map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
            List<SoReturnEntity> soReturnEntityList = soReturnFeign.listByIds(soReturnIds);

            List<String> receiveIds = list.stream().filter(req -> SourceTypeEnum.SO_RETURN_RECEIVE.getCode().equals(req.getSourceType())).map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
            List<SoReturnReceiveEntity> soReturnReceiveEntityList = soReturnReceiveService.listByIds(receiveIds);

            List<String> returnIds = list.stream().map(req -> req.getSourceId()).distinct().collect(Collectors.toList());
            List<SoReturnEntity> receiveReturnList = soReturnFeign.listByIds(returnIds);

            for (SoReturnInstockEntity entity : list) {
                List<SoReturnInstockDetailEntity> detailEntities = detailEntityList.stream().filter(req -> req.getMainId().equals(entity.getId())).collect(Collectors.toList());

                syncSoReturnInstockService.syncDataToSdy(entity,
                        detailEntities, SyncOperateEnum.OPERATE_APPROVE.getCode(),
                        skuVOList,
                        bomChildrenSkuDTOS,
                        currencyList,
                        parentSkuList,
                        customerInfoList,
                        companyEntities,
                        dictBasicEntityList,
                        soReturnEntityList,
                        soReturnReceiveEntityList,
                        receiveReturnList);
            }
            currentPage++;
            XxlJobHelper.log("===========当前页数：" + currentPage + "结束时间：" + LocalDateTime.now());
        }

    }
}
