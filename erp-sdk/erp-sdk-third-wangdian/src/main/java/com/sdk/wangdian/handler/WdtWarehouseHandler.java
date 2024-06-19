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
import com.sdk.wangdian.dto.ErpWarehouseDto;
import com.sdk.wangdian.dto.WdtWarehouseDto;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.setting.SettingAPI;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 旺店通仓库数据处理器
 * @date 2024-05-23
 * @author tanmujin
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.WDT)
@BusinessType(BusinessTypeEnum.WDT_WAREHOUSE)
public class WdtWarehouseHandler implements IBusinessHandler<WdtWarehouseDto, ErpWarehouseDto> {

    @Resource
    private WangDianClientService clientService;

    @Override
    public PlatformDataDTO<WdtWarehouseDto, ErpWarehouseDto> pullHandle(JobTaskDTO data) {
        List<WdtWarehouseDto> sourceDataList = download(data);
        List<ErpWarehouseDto> targetDataList = convert(sourceDataList);
        return new PlatformDataDTO<>(sourceDataList, targetDataList);
    }

    @Override
    public PlatformDataDTO<WdtWarehouseDto, ErpWarehouseDto> cleanHandle(List<WdtWarehouseDto> sourceDataList) {
        List<ErpWarehouseDto> targetDataList = convert(sourceDataList);
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
    public WdtWarehouseDto downloadDetail(WdtWarehouseDto dto, JSONObject extendObj) {
        return dto;
    }

    public List<WdtWarehouseDto> download(JobTaskDTO task) {
        WarehouseQueryRequest query = new WarehouseQueryRequest();
//        query.setWarehouseNo("");
//        query.setWarehouseName("");
//        query.setType(WarehouseQueryRequest.TYPE_INNER);
//        query.setSubType(WarehouseQueryRequest.SUB_TYPE_WDT);
//        query.setStartTime(task.getLastTime().minusMinutes(15).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
//        query.setEndTime(task.getNextTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

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
                return BeanMapperUtils.copyList(WdtWarehouseDto.class, warehouseList);
            }
            if (response == null || response.getWarehouseList().isEmpty()) {
                return BeanMapperUtils.copyList(WdtWarehouseDto.class, warehouseList);
            }
            warehouseList.addAll(response.getWarehouseList());
            Integer totalCount = response.getTotal();
            if (totalCount <= (pager.getPageNo() + 1) * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return JSON.parseObject(JSON.toJSONString(warehouseList), new TypeReference<List<WdtWarehouseDto>>(){});
    }

    public List<ErpWarehouseDto> convert(List<WdtWarehouseDto> sourceDataList) {
        List<ErpWarehouseDto> targetList = new ArrayList<>();
        for (WdtWarehouseDto source : sourceDataList) {
            source.setUniqueId(String.valueOf(source.getWarehouse_id()));
            ErpWarehouseDto target = new ErpWarehouseDto();
            target.setUniqueId(source.getUniqueId());
            target.setDisabled(source.getIs_disabled());
            target.setSysType(PlatformDictEnum.WDT.getCode());
            target.setWarehouseId(String.valueOf(source.getWarehouse_id()));
            target.setType(source.getType());
            target.setSubType(source.getSub_type());
            target.setCode(source.getWarehouse_no());
            target.setName(source.getName());
            target.setAddress(source.getAddress());
            target.setContacts(source.getContact());
            target.setTelNumber(source.getMobile());
            target.setTelno(source.getTelno());
            target.setZip(source.getZip());
            target.setProvince(source.getProvince());
            target.setCity(source.getCity());
            target.setDistrict(source.getDistrict());
            target.setCreated(source.getCreated());
            target.setModified(source.getModified());
            target.setRemark(source.getRemark());
            targetList.add(target);
        }
        return targetList;
    }
}
