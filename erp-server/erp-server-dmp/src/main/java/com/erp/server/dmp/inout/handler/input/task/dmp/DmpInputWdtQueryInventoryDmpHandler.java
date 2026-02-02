package com.erp.server.dmp.inout.handler.input.task.dmp;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.common.business.enums.ErpServerModuleEnum;
import com.common.core.utils.Md5Util;
import com.common.message.service.mq.MQProducerService;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.dmp.enums.InventoryBillStatusEnum;
import com.erp.model.dmp.enums.InventoryOrderTypeEnum;
import com.erp.model.dmp.enums.ThirdSysTypeEnum;
import com.erp.model.msg.dto.WarnMsgInfoDTO;
import com.erp.model.msg.enums.WarnMsgTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.wms.feign.InventoryFeign;
import com.erp.server.dmp.service.DictBasicService;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * dmp处理下一个扩展handler，如何订单收货人信息单独一张表，使用此handler即可，因有成员变量，最终实现类由spring管理需要是多例@Scope("prototype")
 *
 * @author Administrator
 */
@Service
@Scope("prototype")
public class DmpInputWdtQueryInventoryDmpHandler extends DmpInputDbConvertDmpHandler {

    @Resource
    private InventoryFeign inventoryFeign;
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private MQProducerService mqProducerService;
    @Resource
    private DictBasicService dictBasicService;
    @Override
    protected void afterConvertData(Map<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMaps) {
        super.afterConvertData(dmpInputDataDmpRelationMaps);
        // 重置数据
        for (Map.Entry<List<Map<String, Object>>, List<TreeMap<String, Object>>> dmpInputDataDmpRelationMap : dmpInputDataDmpRelationMaps.entrySet()) {
            List<TreeMap<String, Object>> dmpDataMaps = dmpInputDataDmpRelationMap.getValue();
            List<Map<String, Object>> mongoDataMaps = dmpInputDataDmpRelationMap.getKey();
            Map<String, Object> mongoData = mongoDataMaps.get(0);
            List<Map> detailList = JSONUtil.toList((String) dmpDataMaps.get(0).get("detailList"), Map.class);
            String erpWarehouseId = (String) mongoData.get("erpWarehouseId");

            InventoryQtyDTO.SkuInventoryStatusParamDTO dto = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
            dto.setWarehouseIdList(Collections.singletonList(erpWarehouseId));
            dto.setInventoryStatusList(Collections.singletonList(InventoryStatusEnum.USABLE.getCode()));
            //查询库存
            List<InventoryQtyDTO.InventoryDTO> inventoryDTOS = inventoryFeign.listWarehouseInventoryByParam(dto);
            //根据时间查询变更记录
            LocalDateTime startTime = LocalDateTime.parse((String) mongoData.get("startTime"));
            LocalDateTime endTime = LocalDateTime.parse((String) mongoData.get("endTime"));
            //根据时间查询变更记录
            InventoryQtyDTO.InventoryChangeQueryDTO inventoryChangeQueryDTO = new InventoryQtyDTO.InventoryChangeQueryDTO();
            inventoryChangeQueryDTO.setWarehouseId(erpWarehouseId);
            inventoryChangeQueryDTO.setStartTime(startTime);
            inventoryChangeQueryDTO.setEndTime(endTime);
            List<InventoryQtyDTO.InventoryChangeDTO> inventoryChangeDTOS = inventoryFeign.listInventoryChangeByParam(inventoryChangeQueryDTO);
            //合并旺店通变更库存和erp变更库存成一个列表
            List<String> skuNoList = inventoryChangeDTOS.stream().map(InventoryQtyDTO.InventoryChangeDTO::getSkuNo).collect(Collectors.toList());
            List<String> specNoList = detailList.stream().map(e -> (String) e.get("specNo")).collect(Collectors.toList());
            List<String> changeSkuNoList = Stream.concat(skuNoList.stream(), specNoList.stream()).filter(CharSequenceUtil::isNotBlank).distinct().collect(Collectors.toList());
            //过滤费用、服务类SKU，不同步旺店通更新
            List<SkuVO> noInventorySku = plmTaskFeign.getNoInventorySku();
            changeSkuNoList.removeAll(noInventorySku.stream().map(SkuVO::getSkuNo).collect(Collectors.toList()));
            List<TreeMap<String, Object>> updateDmpDataMaps = new ArrayList<>();
            if (CollUtil.isEmpty(changeSkuNoList)) {
                dmpInputDataDmpRelationMap.setValue(updateDmpDataMaps);
                continue;
            }
            for (String skuNo : changeSkuNoList) {
                InventoryQtyDTO.InventoryDTO inventoryDTO = inventoryDTOS.stream().filter(e -> e.getSkuNo().equals(skuNo)).findFirst().orElse(null);
                Map map = detailList.stream().filter(e -> e.get("specNo").equals(skuNo)).findFirst().orElse(new HashMap());
                TreeMap<String, Object> dmpDataMap = new TreeMap<>(map);
                Boolean defect = (Boolean) dmpDataMap.getOrDefault("defect", Boolean.FALSE);
                if (defect) {
                    continue;//只更新正品类型的数据
                }
                //库存量
                BigDecimal stockNum = (BigDecimal) dmpDataMap.getOrDefault("stockNum", BigDecimal.ZERO);
                int erpUsableQty = Objects.nonNull(inventoryDTO) ? inventoryDTO.getQty() : 0;
                //- 差异等于0：无需处理
                if (erpUsableQty == stockNum.intValue()) {
                    continue;
                }
                buildMap(dmpDataMap, erpWarehouseId, inventoryDTO, updateDmpDataMaps, mongoData);
            }
            dmpInputDataDmpRelationMap.setValue(updateDmpDataMaps);
            //对异常进行mq预警
            List<String> errorMsgList = updateDmpDataMaps.stream().filter(e -> e.get("billStatus").equals(InventoryBillStatusEnum.FAILED.getCode())).map(f -> (String)f.get("remark")).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(errorMsgList)) {
                String warnMsg = CharSequenceUtil.join("\n", errorMsgList);
                this.sendWarnMsg((String) mongoData.get("inputTaskId"),(String) mongoData.get("batchNo"), warnMsg);
            }
        }
    }

    private void sendWarnMsg(String inputTaskId,String batchNo, String warnMsg) {
        WarnMsgInfoDTO warnMsgInfo = new WarnMsgInfoDTO();
        warnMsgInfo.setBizName("旺店通库存同步预警");
        warnMsgInfo.setErpServerModuleEnum(ErpServerModuleEnum.ERP_SERVER_DMP);
        warnMsgInfo.setTitle("库存不足预警，批次号：" + batchNo);
        warnMsgInfo.setTableName("dmp_wdt_warehouse_inventory_record");
        warnMsgInfo.setTableId(inputTaskId);
        warnMsgInfo.setKeyInfo(warnMsg);
        List<DictBasicDTO.ViewDTO> viewDTOList = dictBasicService.getByKey("wdtUpdateInventoryUser");
        warnMsgInfo.setUserIdList(CollUtil.isNotEmpty(viewDTOList) ? viewDTOList.stream().map(DictBasicDTO.ViewDTO::getValue).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList()) : new ArrayList<>());
        warnMsgInfo.setWarnMsgTypeEnum(WarnMsgTypeEnum.SYS_EXCEPTION);
        mqProducerService.sendWarnMsg(warnMsgInfo);
    }


    private static void buildMap(TreeMap<String, Object> dmpDataMap, String erpWarehouseId, InventoryQtyDTO.InventoryDTO inventoryDTO, List<TreeMap<String, Object>> updateDmpDataMaps, Map<String, Object> mongoData) {
        String batchNo = (String) mongoData.get("batchNo");
        String erpWarehouseName = (String) mongoData.get("erpWarehouseName");
        String thirdWarehouseId = (String) mongoData.get("thirdWarehouseId");
        String thirdWarehouseName = (String) mongoData.get("thirdWarehouseName");
        String thirdWarehouseCode = (String) mongoData.get("thirdWarehouseCode");
        String inputTaskId = (String) mongoData.get("inputTaskId");
        String convertId = (String) mongoData.get("convertId");
        String nextLevelId = (String) mongoData.get("nextLevelId");
        String skuNo = Objects.nonNull(inventoryDTO) ? inventoryDTO.getSkuNo() : "";
        String skuId = Objects.nonNull(inventoryDTO) ? inventoryDTO.getSkuId() : "";
        int erpUsableQty = Objects.nonNull(inventoryDTO) ? inventoryDTO.getQty() : 0;
        //可用库存数量
        BigDecimal availableSendStock = (BigDecimal) dmpDataMap.getOrDefault("availableSendStock", BigDecimal.ZERO);
        //库存量
        BigDecimal stockNum = (BigDecimal) dmpDataMap.getOrDefault("stockNum", BigDecimal.ZERO);
        //锁定量
        BigDecimal lockNum = (BigDecimal) dmpDataMap.getOrDefault("lockNum", BigDecimal.ZERO);
        //第三方skuNo
        String thirdSkuNo = (String) dmpDataMap.getOrDefault("specNo", skuNo);
        if (CharSequenceUtil.isBlank(thirdSkuNo)) {
            return;//erp和旺店通都没有sku就不处理
        }
        dmpDataMap.put("sourcePlatform", ThirdSysTypeEnum.WDT.getCode());
        dmpDataMap.put("billStatus", DmpOutputTaskRecordStatusEnum.INIT.getCode());
        dmpDataMap.put("erpWarehouseId", erpWarehouseId);
        dmpDataMap.put("erpWarehouseName", erpWarehouseName);
        dmpDataMap.put("erpSkuNo", thirdSkuNo);
        dmpDataMap.put("erpSkuId", skuId);
        dmpDataMap.put("erpUsableQty", erpUsableQty);
        dmpDataMap.put("thirdWarehouseId", thirdWarehouseId);
        dmpDataMap.put("thirdWarehouseCode", thirdWarehouseCode);
        dmpDataMap.put("thirdWarehouseName", thirdWarehouseName);
        dmpDataMap.put("thirdSkuNo", thirdSkuNo);
        dmpDataMap.put("thirdStockQty", stockNum.intValue());
        dmpDataMap.put("thirdFreezeQty", lockNum.intValue());
        dmpDataMap.put("thirdUsableQty", availableSendStock.intValue());
        dmpDataMap.put("inputTaskId", inputTaskId);
        dmpDataMap.put("convertId", convertId);
        dmpDataMap.put("nextLevelId", nextLevelId);
        dmpDataMap.put("batchNo", batchNo);
        dmpDataMap.put("remark", "数大臣库存对比差异执行库存调整");
        if (erpUsableQty > stockNum.intValue()) {
            //其他入库
            dmpDataMap.put("orderType", InventoryOrderTypeEnum.IN_STOCK.getCode());
            dmpDataMap.put("qty", erpUsableQty - stockNum.intValue());
        } else if (erpUsableQty < stockNum.intValue()) {
            //其他出库
            int qty = stockNum.intValue() - erpUsableQty;
            dmpDataMap.put("orderType", InventoryOrderTypeEnum.OUT_STOCK.getCode());
            dmpDataMap.put("qty", qty);
            if (availableSendStock.intValue() < qty) {
                dmpDataMap.put("billStatus", InventoryBillStatusEnum.FAILED.getCode());
                dmpDataMap.put("remark", CharSequenceUtil.format("【{}】【{}】调整数量【{}】小于旺店通可用库存【{}】:旺店通总库存数量【{}】数大臣可用库存数量【{}】", skuNo, erpWarehouseName, qty,availableSendStock.intValue(), stockNum.intValue(), erpUsableQty));
            }
        }
        //重置pkey
        dmpDataMap.put("pkey", batchNo + "_" + thirdWarehouseCode + "_" + erpWarehouseId + "_" + thirdSkuNo);
        dmpDataMap.put("uniqueEncrypt", Md5Util.getMd5("all" + "_" + thirdWarehouseCode + "_" + thirdSkuNo + "_" + batchNo));
        dmpDataMap.put("dataEncrypt", Md5Util.getMd5(JSONUtil.toJsonStr(dmpDataMap)));
        updateDmpDataMaps.add(dmpDataMap);
    }
}
