package com.sdk.wangdian.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.common.business.annotation.BusinessType;
import com.common.business.annotation.PlatformCategoryType;
import com.common.business.annotation.PlatformType;
import com.common.business.dto.JobTaskDTO;
import com.common.business.dto.PlatformDataDTO;
import com.common.business.enums.BusinessTypeEnum;
import com.common.business.enums.PlatformCategoryEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.handler.IBusinessHandler;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.dmp.enums.PlatformEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.sdk.wangdian.dto.ErpShopDto;
import com.sdk.wangdian.dto.ErpShopDto;
import com.sdk.wangdian.dto.WdtShopDto;
import com.sdk.wangdian.dto.WdtShopDto;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.setting.SettingAPI;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.commons.compiler.util.Benchmark;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 旺店通仓库数据处理器
 * @date 2024-05-24
 * @author hyj
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.WDT)
@BusinessType(BusinessTypeEnum.WDT_SHOP)
public class WdtShopHandler implements IBusinessHandler<WdtShopDto, ErpShopDto> {

    @Resource
    private WangDianClientService clientService;

    @Override
    public PlatformDataDTO<WdtShopDto, ErpShopDto> pullHandle(JobTaskDTO data) {
        List<WdtShopDto> sourceDataList = download(data);
        List<ErpShopDto> targetDataList = convert(sourceDataList);
        return new PlatformDataDTO<>(sourceDataList, targetDataList);
    }

    @Override
    public PlatformDataDTO<WdtShopDto, ErpShopDto> cleanHandle(List<WdtShopDto> sourceDataList) {
        List<ErpShopDto> targetDataList = convert(sourceDataList);
        return new PlatformDataDTO<>(sourceDataList, targetDataList);
    }

    @Override
    public String getTargetPlatform() {
        return PlatformEnum.WANGDIAN.getDesc();
    }

    @Override
    public Boolean getIsSendMq() {
        return Boolean.TRUE;
    }

    @Override
    public WdtShopDto downloadDetail(WdtShopDto dto, JSONObject extendObj) {
        return dto;
    }

    public List<WdtShopDto> download(JobTaskDTO task) {
        WarehouseQueryRequest query = new WarehouseQueryRequest();
//        query.setWarehouseNo("");
//        query.setWarehouseName("");
        query.setType(WarehouseQueryRequest.TYPE_INNER);
        query.setSubType(WarehouseQueryRequest.SUB_TYPE_WDT);
        query.setStartTime(task.getLastTime().minusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        query.setEndTime(task.getNextTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        queryRequest.setHideDelete();

        Pager pager = new Pager();
        pager.setPageNo(0);
        pager.setPageSize(200);
        pager.setCalcTotal(true);

        SettingAPI settingAPI = clientService.get(SettingAPI.class);
        List<WarehouseQueryResponse.WarehouseDto> warehouseList = new ArrayList<>();
        boolean hasNext = true;
        int pageSize = 200;

        //分页循环拉取数据
        while (hasNext) {
            WarehouseQueryResponse response = null;
            try {
                response = settingAPI.queryWarehouse(query, pager);
            } catch (Exception e) {
                log.error("拉取旺店通仓库数据失败，原因【{}】", e.getMessage(), e);
                return BeanMapperUtils.copyList(WdtShopDto.class, warehouseList);
            }
            if (response == null || response.getWarehouseList().isEmpty()) {
                return BeanMapperUtils.copyList(WdtShopDto.class, warehouseList);
            }
            warehouseList.addAll(response.getWarehouseList());
            Integer totalCount = response.getTotal();
            if (totalCount <= pager.getPageNo() * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return JSON.parseObject(JSON.toJSONString(warehouseList), new TypeReference<List<WdtShopDto>>(){});
    }

    public List<ErpShopDto> convert(List<WdtShopDto> sourceDataList) {
        List<ErpShopDto> targetList = new ArrayList<>();
        for (WdtShopDto source : sourceDataList) {
            ErpShopDto target = new ErpShopDto();
            BeanUtils.copyProperties(source,target);

            target.setPlatformId(source.getPlatform_id());
            target.setSubPlatformId(source.getSub_platform_id());
            target.setDisabled(source.getIs_disabled());
            target.setSysType(ThirdSysTypeEnum.WANGDIAN.getCode());
            target.setShopId(String.valueOf(source.getShop_id()));
            target.setAccountId(source.getAccount_id());
            target.setGroupId(source.getGroup_id());
            target.setCode(source.getShop_no());
            target.setName(source.getShop_name());
            target.setContacts(source.getContact());
            target.setTelNumber(source.getMobile());
            target.setAuthState(source.getAuth_state());
            target.setAuthTime(source.getAuth_time());
            targetList.add(target);
        }
        return targetList;
    }
}
