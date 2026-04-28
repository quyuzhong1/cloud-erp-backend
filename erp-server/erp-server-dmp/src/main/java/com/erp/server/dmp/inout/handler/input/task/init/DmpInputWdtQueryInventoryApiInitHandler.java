package com.erp.server.dmp.inout.handler.input.task.init;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.dmp.entity.DmpInputTaskEntity;
import com.erp.model.dmp.enums.InventorySyncModeEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.server.dmp.inout.dto.base.DmpInputTaskInitDTO;
import com.erp.server.dmp.inout.dto.request.DmpInputInitRequest;
import com.erp.server.dmp.inout.dto.response.DmpInputTaskResponse;
import com.erp.server.dmp.service.ThirdWarehouseService;
import com.sdk.wangdian.sdk.Pager;
import com.sdk.wangdian.sdk.api.wms.StockAPI;
import com.sdk.wangdian.sdk.api.wms.dto.StockSearch2Request;
import com.sdk.wangdian.sdk.api.wms.dto.StockSearch2Response;
import com.sdk.wangdian.server.WangDianClientService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * dmp输入init任务基础处理器下的旺店通api获取数据方式
 *
 * @author Administrator
 */
@Slf4j
@Service
@Scope("prototype")
public class DmpInputWdtQueryInventoryApiInitHandler extends DmpInputInitHandler {
    @Resource
    private ThirdWarehouseService thirdWarehouseService;
    @Resource
    private WangDianClientService clientService;

    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private InventoryFeign inventoryFeign;
    @Override
    public List<DmpInputTaskInitDTO> getInitData(DmpInputInitRequest dmpRequest, DmpInputTaskResponse dmpResponse) {
        List<DmpInputTaskInitDTO> dmpInputTaskInitDTOList = new ArrayList<>();
        ThirdWarehouseDTO.QueryMapParamDTO queryMapParamDTO = new ThirdWarehouseDTO.QueryMapParamDTO();
        queryMapParamDTO.setSysType(ThirdSysTypeEnum.WDT.getCode());
        queryMapParamDTO.setCategory(ThirdSysTypeEnum.WAREHOUSE.getCode());
        queryMapParamDTO.setInventorySyncMode(InventorySyncModeEnum.INVENTORY.getCode());
        //获取旺店通更新库存的仓库列表
        List<ThirdWarehouseDTO.QueryMapDTO> queryMapDTOList = thirdWarehouseService.listQueryMapping(queryMapParamDTO);
        if (CollectionUtils.isEmpty(queryMapDTOList)) {
            log.info("旺店通更新库存的仓库列表为空");
            return Collections.emptyList();
        }

        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        String startTime = dmpInputTaskEntity.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String endTime = dmpInputTaskEntity.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        StockAPI stockAPI = clientService.get(StockAPI.class);
        int pageSize = 1000;
        String batchNo = IdUtil.getSnowflake().nextIdStr();
        for (ThirdWarehouseDTO.QueryMapDTO queryMapDTO : queryMapDTOList) {
            List<StockSearch2Response.Detail> wdtDetails = getWdtDetails(queryMapDTO, startTime, endTime, pageSize, stockAPI, batchNo);
            List<InventoryQtyDTO.InventoryChangeDTO> erpDetails = getErpDetail(queryMapDTO);
            if (CollUtil.isEmpty(wdtDetails) && CollectionUtils.isEmpty(erpDetails)) {
                log.info("旺店通查询库存和ERP查询库存变更记录均为空, 仓库: {}, 批次: {}", queryMapDTO.getThirdCode(), batchNo);
                continue;
            }
            dmpInputTaskInitDTOList.add(DmpInputTaskInitDTO.initMsg(fillDataAndToJsonObject(wdtDetails, batchNo, queryMapDTO, dmpInputTaskEntity,erpDetails)));
        }
        return dmpInputTaskInitDTOList;
    }

    private List<InventoryQtyDTO.InventoryChangeDTO> getErpDetail(ThirdWarehouseDTO.QueryMapDTO queryMapDTO) {
        //根据时间查询变更记录
        InventoryQtyDTO.InventoryChangeQueryDTO inventoryChangeQueryDTO = new InventoryQtyDTO.InventoryChangeQueryDTO();
        inventoryChangeQueryDTO.setWarehouseId(queryMapDTO.getSysId());
        inventoryChangeQueryDTO.setStartTime(dmpInputTaskEntity.getStartTime());
        inventoryChangeQueryDTO.setEndTime(dmpInputTaskEntity.getEndTime());
        return inventoryFeign.listInventoryChangeByParam(inventoryChangeQueryDTO);
    }

    private static List<StockSearch2Response.Detail> getWdtDetails(ThirdWarehouseDTO.QueryMapDTO queryMapDTO, String startTime, String endTime, int pageSize, StockAPI stockAPI, String batchNo) {
        List<StockSearch2Response.Detail> detailList = new ArrayList<>();
        if (CharSequenceUtil.isBlank(queryMapDTO.getThirdCode())) {
            return detailList;
        }
        StockSearch2Request request = new StockSearch2Request();
        request.setWarehouseNo(queryMapDTO.getThirdCode());
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        Pager pager = new Pager();
        pager.setPageNo(0);
        pager.setPageSize(pageSize);
        pager.setCalcTotal(true);
        boolean hasNext = true;
        while (hasNext) {
            StockSearch2Response response;
            try {
                response = stockAPI.search2(request, pager);
                log.warn("旺店通查询库存, 仓库: {}, 批次: {}, 响应: {}", queryMapDTO.getThirdCode(), batchNo, JSONUtil.toJsonStr(response));
            } catch (Exception e) {
                log.error("旺店通查询库存异常, 仓库: {}, 异常信息: {}", queryMapDTO.getThirdCode(), e.getMessage(), e);
                hasNext = false;
                continue;
            }
            if (Objects.isNull(response) || CollectionUtils.isEmpty(response.getDetailList())) {
                hasNext = false;
                continue;
            }
            Integer totalCount = response.getTotal();
            if (totalCount <= (pager.getPageNo() + 1) * pageSize) {
                hasNext = false;
            }
            pager.setPageNo(pager.getPageNo() + 1);
            detailList.addAll(response.getDetailList());
        }
        return detailList;
    }

    /**
     * 设置亚马逊货件ID和转换JSON
     */
    private String fillDataAndToJsonObject(List<StockSearch2Response.Detail> wdtDetails, String batchNo, ThirdWarehouseDTO.QueryMapDTO queryMapDTO, DmpInputTaskEntity dmpInputTaskEntity, List<InventoryQtyDTO.InventoryChangeDTO> erpDetails) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("erpDetails", JSON.toJSONString(erpDetails));
        jsonObject.put("wdtDetails", JSON.toJSONString(wdtDetails));
        jsonObject.put("startTime", dmpInputTaskEntity.getStartTime());
        jsonObject.put("endTime", dmpInputTaskEntity.getEndTime());
        jsonObject.put("erpWarehouseId", queryMapDTO.getSysId());
        jsonObject.put("erpWarehouseName", queryMapDTO.getSysName());
        jsonObject.put("thirdWarehouseId", queryMapDTO.getThirdId());
        jsonObject.put("thirdWarehouseName", queryMapDTO.getThirdName());
        jsonObject.put("thirdWarehouseCode", queryMapDTO.getThirdCode());
        jsonObject.put("batchNo", batchNo);
        jsonObject.put("pkey", batchNo + "_" + queryMapDTO.getThirdCode() + "_" + queryMapDTO.getSysId());

        return jsonObject.toJSONString();
    }

}
