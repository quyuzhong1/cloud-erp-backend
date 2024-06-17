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
import com.sdk.wangdian.dto.ErpVirtualWarehouseDto;
import com.sdk.wangdian.dto.WdtVirtualWarehouseDto;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.setting.SettingAPI;
import com.sdk.wangdian.sdk.api.setting.dto.VirtualWarehouseQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.VirtualWarehouseQueryResponse;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryRequest;
import com.sdk.wangdian.sdk.api.setting.dto.WarehouseQueryResponse;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 旺店通虚拟仓数据处理器
 * @date 2024-05-23
 * @author hyj
 */
@Slf4j
@Component
@PlatformCategoryType(PlatformCategoryEnum.THIRD_SYSTEM)
@PlatformType(PlatformDictEnum.WDT)
@BusinessType(BusinessTypeEnum.WDT_VIRTUAL_WAREHOUSE)
public class WdtVirtualWarehouseHandler implements IBusinessHandler<WdtVirtualWarehouseDto, ErpVirtualWarehouseDto> {

    @Resource
    private WangDianClientService clientService;

    @Override
    public PlatformDataDTO<WdtVirtualWarehouseDto, ErpVirtualWarehouseDto> pullHandle(JobTaskDTO data) {
        List<WdtVirtualWarehouseDto> sourceDataList = download(data);
        List<ErpVirtualWarehouseDto> targetDataList = convert(sourceDataList);
        return new PlatformDataDTO<>(sourceDataList, targetDataList);
    }

    @Override
    public PlatformDataDTO<WdtVirtualWarehouseDto, ErpVirtualWarehouseDto> cleanHandle(List<WdtVirtualWarehouseDto> sourceDataList) {
        List<ErpVirtualWarehouseDto> targetDataList = convert(sourceDataList);
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
    public WdtVirtualWarehouseDto downloadDetail(WdtVirtualWarehouseDto dto, JSONObject extendObj) {
        return dto;
    }

    public List<WdtVirtualWarehouseDto> download(JobTaskDTO task) {
        VirtualWarehouseQueryRequest query = new VirtualWarehouseQueryRequest();

        Pager pager = new Pager();
        pager.setPageNo(0);
        pager.setPageSize(200);
        pager.setCalcTotal(true);

        SettingAPI settingAPI = clientService.get(SettingAPI.class);
        List<VirtualWarehouseQueryResponse.VirtualWarehouseDto> warehouseList = new ArrayList<>();
        boolean hasNext = true;
        int pageSize = 200;

        //分页循环拉取数据
        while (hasNext) {
            VirtualWarehouseQueryResponse response = null;
            try {
                response = settingAPI.queryVirtualWarehouse(query, pager);
            } catch (Exception e) {
                log.error("拉取旺店通虚拟仓数据失败，原因【{}】", e.getMessage(), e);
                return BeanMapperUtils.copyList(WdtVirtualWarehouseDto.class, warehouseList);
            }
            if (response == null || response.getVirtualWarehouseList().isEmpty()) {
                return BeanMapperUtils.copyList(WdtVirtualWarehouseDto.class, warehouseList);
            }
            warehouseList.addAll(response.getVirtualWarehouseList());
            Integer totalCount = response.getTotal();
            if (totalCount <= (pager.getPageNo() + 1) * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
        }
        return JSON.parseObject(JSON.toJSONString(warehouseList), new TypeReference<List<WdtVirtualWarehouseDto>>(){});
    }

    public List<ErpVirtualWarehouseDto> convert(List<WdtVirtualWarehouseDto> sourceDataList) {
        List<ErpVirtualWarehouseDto> targetList = new ArrayList<>();
        for (WdtVirtualWarehouseDto source : sourceDataList) {
            source.setUniqueId(String.valueOf(source.getVirtual_warehouse_id()));
            ErpVirtualWarehouseDto target = new ErpVirtualWarehouseDto();
            target.setUniqueId(source.getUniqueId());
            target.setDisabled(source.getIs_disabled());
            target.setSysType(PlatformDictEnum.WDT.getCode());
            target.setWarehouseId(String.valueOf(source.getVirtual_warehouse_id()));
            target.setName(source.getVirtual_warehouse_name());
            target.setCode(source.getVirtual_warehouse_no());
            target.setCreated(source.getCreated());
            target.setModified(source.getModified());
            target.setRemark(source.getRemark());
            targetList.add(target);
        }
        return targetList;
    }
}
