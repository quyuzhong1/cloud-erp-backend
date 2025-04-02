package com.erp.server.oms.schedule;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.wrapper.FeignQuery;
import com.erp.model.oms.dto.FullyManagedDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.sys.entity.CfgCountryPartitionEntity;
import com.erp.model.sys.enums.DictValueEnum;
import com.erp.server.oms.service.*;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 同步军区映射
 */
@Component
@Slf4j
public class PartitionJob {

    @Resource
    private SoB2cReceiverService soB2cReceiverService;

    @Resource
    private SoInfoService soInfoService;
    @Resource
    private SoB2cExtendService soB2cExtendService;

    @Value("${spring.cloud.nacos.discovery.namespace}")
    private String namespace;

    @XxlJob("PartitionJob")
    public ReturnT<String> partitionJob() {
        List<CfgCountryPartitionEntity> cfgCountryPartitionEntityList = FeignQuery.list(CfgCountryPartitionEntity.class);
        syncB2c(cfgCountryPartitionEntityList);
        syncB2b(cfgCountryPartitionEntityList);
        syncFullyManaged(cfgCountryPartitionEntityList);
        return ReturnT.SUCCESS;
    }

    private void syncB2c(List<CfgCountryPartitionEntity> cfgCountryPartitionEntityList) {
        int page = 1;
        XxlJobHelper.log("PartitionJob B2C同步军区 执行开始");
        while (true) {
            Page query = new Page(page, 100000);
            XxlJobHelper.log("PartitionJob B2C同步军区 第{}页", page);
            IPage<SoB2cReceiverEntity> pageData = soB2cReceiverService.pagePartitionIsNull(query);
            List<SoB2cReceiverEntity> list = pageData.getRecords();
            List<SoB2cReceiverEntity> updateList = new ArrayList<>();
            for (SoB2cReceiverEntity soB2cReceiverEntity : list) {
                String country = soB2cReceiverEntity.getShopCountry();
                if(StringUtils.isBlank(country) || country.equals(DictValueEnum.ALL.getCode())){
                    country = soB2cReceiverEntity.getCustomerCountry();
                }
                if(StringUtils.isBlank(country) || country.equals(DictValueEnum.ALL.getCode())){
                    country = soB2cReceiverEntity.getCountry();
                }
                if(StringUtils.isBlank(country)){
                    continue;
                }
                String finalCountry = country;
                CfgCountryPartitionEntity cfgCountryPartitionEntity = cfgCountryPartitionEntityList.stream().filter(cfg -> cfg.getCountry().equals(finalCountry)).findFirst().orElse(null);
                if(Objects.nonNull(cfgCountryPartitionEntity)){
                    soB2cReceiverEntity.setPartitionId(cfgCountryPartitionEntity.getPartitionId());
                    updateList.add(soB2cReceiverEntity);
                }
            }
            if(CollectionUtils.isNotEmpty(updateList)){
                soB2cReceiverService.updateBatchById(updateList,5000);
            }
            if (pageData.getTotal() <= page* 100000L) {
                XxlJobHelper.log("PartitionJob B2C同步军区 同步结束");
                break;
            }
            page++;
        }
    }
    private void syncFullyManaged(List<CfgCountryPartitionEntity> cfgCountryPartitionEntityList) {
        int page = 1;
        XxlJobHelper.log("PartitionJob 全托管订单同步军区 执行开始");
        while (true) {
            Page query = new Page(page, 100000);
            XxlJobHelper.log("PartitionJob 全托管订单同步军区 第{}页", page);
            IPage<SoB2cExtendEntity> pageData = soB2cExtendService.pagePartitionIsNull(query);
            List<SoB2cExtendEntity> list = pageData.getRecords();
            List<SoB2cExtendEntity> updateList = new ArrayList<>();
            for (SoB2cExtendEntity soB2cExtendEntity : list) {
                String country = soB2cExtendEntity.getShopCountry();
                if(StringUtils.isBlank(country)){
                    continue;
                }
                String finalCountry = country;
                CfgCountryPartitionEntity cfgCountryPartitionEntity = cfgCountryPartitionEntityList.stream().filter(cfg -> cfg.getCountry().equals(finalCountry)).findFirst().orElse(null);
                if(Objects.nonNull(cfgCountryPartitionEntity)){
                    soB2cExtendEntity.setPartitionId(cfgCountryPartitionEntity.getPartitionId());
                    updateList.add(soB2cExtendEntity);
                }
            }
            if(CollectionUtils.isNotEmpty(updateList)){
                soB2cExtendService.updateBatchById(updateList,5000);
            }
            if (pageData.getTotal() <= page* 100000L) {
                XxlJobHelper.log("PartitionJob 全托管订单同步军区 同步结束");
                break;
            }
            page++;
        }
    }

    private void syncB2b(List<CfgCountryPartitionEntity> cfgCountryPartitionEntityList) {
        XxlJobHelper.log("PartitionJob B2b同步军区 执行开始");
        int page = 1;
        while (true) {
            Page query = new Page(page, 100000);
            XxlJobHelper.log("PartitionJob B2B同步军区 第{}页", page);
            IPage<SoInfoEntity> pageData = soInfoService.pagePartitionIsNull(query);
            List<SoInfoEntity> list = pageData.getRecords();
            List<SoInfoEntity> updateList = new ArrayList<>();
            for (SoInfoEntity soInfoEntity : list) {
                String country = soInfoEntity.getCustomerCountry();
                if(StringUtils.isNotBlank(soInfoEntity.getCustomerCountry()) && !soInfoEntity.getCustomerCountry().equals(DictValueEnum.ALL.getCode())){
                    country = soInfoEntity.getCustomerCountry();
                }
                if(StringUtils.isBlank(country)){
                    continue;
                }
                String finalCountry = country;
                CfgCountryPartitionEntity cfgCountryPartitionEntity = cfgCountryPartitionEntityList.stream().filter(cfg -> cfg.getCountry().equals(finalCountry)).findFirst().orElse(null);
                if(Objects.nonNull(cfgCountryPartitionEntity)){
                    soInfoEntity.setPartitionId(cfgCountryPartitionEntity.getPartitionId());
                    updateList.add(soInfoEntity);
                }
            }
            if(CollectionUtils.isNotEmpty(updateList)){
                soInfoService.updateBatchById(updateList,5000);
            }
            if (pageData.getTotal() <= page* 100000L) {
                XxlJobHelper.log("PartitionJob B2b同步军区 同步结束");
                break;
            }
            page++;
        }
    }
}
