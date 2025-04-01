package com.erp.server.wms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson2.JSON;
import com.common.business.config.DocNoGenHelper;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.utils.ApplicationContextUtils;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.dto.VirtualTransFlowDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.VirtualFlowRefactorDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.dto.inventory.VirtualTransRuleDTO;
import com.erp.model.wms.entity.CfgVirtualTransRulesEntity;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.model.wms.entity.VirtualTransFlowEntity;
import com.erp.model.wms.entity.VirtualWarehouseEntity;
import com.erp.model.wms.enums.CfgSettingOrderTypeEnum;
import com.erp.model.wms.enums.DictBasicEnum;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.wms.config.VirtualInventoryHelper;
import com.erp.server.wms.service.*;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class VirtualFlowRefactorServiceImpl implements VirtualFlowRefactorService {

    @Resource
    private RequisitionApplicationService requisitionApplicationService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoDeliveryNoticeService soDeliveryNoticeService;

    @Resource
    private WarehouseService warehouseService;

    @Resource
    private VirtualTransFlowService virtualTransFlowService;

    @Resource
    private VirtualWarehouseService virtualWarehouseService;

    @Resource
    private VirtualInventoryService virtualInventoryService;

    @Resource
    private DocNoGenHelper docNoGenHelper;

    @Resource
    private CfgVirtualTransRulesService cfgVirtualTransRulesService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private VirtualInventoryHelper virtualInventoryHelper;

    @Override
    public void rebuildFlow(String jobParam) {
        List<String> orderTypeList = CharSequenceUtil.isNotBlank(jobParam) ? Arrays.asList(jobParam.split(",")) :
                Arrays.stream(CfgSettingOrderTypeEnum.values()).map(CfgSettingOrderTypeEnum::getCode).collect(Collectors.toList());
        log.info("VirtualFlowRefactorServiceImpl rebuildFlow start");
        List<String> finalOrderTypeList = orderTypeList;
        finalOrderTypeList.stream().parallel().forEach(obj -> {
            if ( CfgSettingOrderTypeEnum.B2B.getCode().equals(obj)) {
                //b2b
                rebuildB2bFlow();
            }
            if ( CfgSettingOrderTypeEnum.B2C.getCode().equals(obj)) {
                //b2c
                rebuildB2cFlow();
            }
            if ( CfgSettingOrderTypeEnum.FIRST_MILE.getCode().equals(obj)) {
                //firstMile
                rebuildFirstMileFlow();
            }
        });
        log.info("VirtualFlowRefactorServiceImpl rebuildFlow end");
    }

    /**
     * b2b流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildB2bFlow() {
       List<VirtualFlowRefactorDTO.OutInStockDTO> list =  soDeliveryNoticeService.rebuildB2bVirtualFlow();
       if (CollUtil.isEmpty(list)) {
           return;
       }
        Map<String, List<VirtualFlowRefactorDTO.OutInStockDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSourceType().getCode().concat(obj.getSourceId())));

        map.entrySet().stream().parallel().forEach(obj -> {
            List<VirtualFlowRefactorDTO.OutInStockDTO> value = obj.getValue();
            String sourceType = value.get(0).getSourceType().getCode();
            List<VirtualInventoryStockDTO.OutInStockDTO> params = BeanUtil.copyToList(value, VirtualInventoryStockDTO.OutInStockDTO.class);
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(params);
            //b2b需冻结
            if (SourceTypeEnum.SO_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_INFO_LOCK_ADD.getType());
            }
            //发货通知单需冻结
            if (SourceTypeEnum.SO_DELIVERY_NOTICE.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_DELIVERY_NOTICE_HANDLE.getType());
            }
            //加工单需出库
            if (SourceTypeEnum.MACHINE_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.MACHINE_INFO_CHILD_OUT.getType());
            }
            //直接调拨单需出库
            if (SourceTypeEnum.TRANSFER_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getType());
            }
            //销售出库单需出库
            if (SourceTypeEnum.SO_OUTSTOCK.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getType());
            }
            //更新库存
            ApplicationContextUtils.getBean(VirtualFlowRefactorServiceImpl.class).approve(dto);
        });
    }

    /**
     * b2c流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildB2cFlow() {
        List<VirtualFlowRefactorDTO.OutInStockDTO> list =  soB2cDeliveryService.rebuildB2cVirtualFlow();
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, List<VirtualFlowRefactorDTO.OutInStockDTO>> map = list.stream().collect(Collectors.groupingBy(obj -> obj.getSourceType().getCode().concat(obj.getSourceId()).concat(obj.getBusinessType())));
        map.entrySet().stream().parallel().forEach(obj -> {
            List<VirtualFlowRefactorDTO.OutInStockDTO> value = obj.getValue();
            String sourceType = value.get(0).getSourceType().getCode();
            List<VirtualInventoryStockDTO.OutInStockDTO> params = BeanUtil.copyToList(value, VirtualInventoryStockDTO.OutInStockDTO.class);
            VirtualInventoryStockDTO.StockParamDTO dto = new VirtualInventoryStockDTO.StockParamDTO();
            dto.setParamList(params);
            //b2c发货单需冻结
            if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(sourceType) && VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY.getCode().equals(value.get(0).getBusinessType())) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY.getType());
            }
            //发货通知单需冻结
            if (SourceTypeEnum.SO_B2C_DELIVERY.getCode().equals(sourceType) &&VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY_CANCEL.getCode().equals(value.get(0).getBusinessType())) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_B2C_DELIVERY_CANCEL.getType());
            }
            //直接调拨单需出库
            if (SourceTypeEnum.TRANSFER_INFO.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.TRANSFER_INFO_APPROVE.getType());
            }
            //销售出库单需出库
            if (SourceTypeEnum.SO_OUTSTOCK.getCode().equals(sourceType)) {
                dto.setBusinessType(VirtualInventoryBusinessTypeEnum.SO_OUT_STOCK.getType());
            }
            //更新库存
            ApplicationContextUtils.getBean(VirtualFlowRefactorServiceImpl.class).approve(dto);
        });
    }

    /**
     * 头程流水
     * @author will
     * @date 2025/3/31 10:52
     */
    private void rebuildFirstMileFlow() {
        List<VirtualFlowRefactorDTO.OutInStockDTO> list =  requisitionApplicationService.rebuildFirstMileVirtualFlow();
        if (CollUtil.isEmpty(list)) {
            return;
        }


    }











//---------------------------------------------------------------------------流水生成代码---------------------------------------------------------------------------------
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(VirtualInventoryStockDTO.StockParamDTO dto) {
        ValidatorUtil.validateEntity(dto);
        //BOM拆分
        List<VirtualInventoryStockDTO.OutInStockDTO> outInStockList = splitBom(dto.getParamList(), dto.getIsSplitBom());
        if (CollUtil.isEmpty(outInStockList)) {
            outInStockList = dto.getParamList();
        }
        VirtualInventoryStockService virtualInventoryStockService = virtualInventoryHelper.getInventoryService(InventoryBizTypeEnum.IN_OUT_STOCK);
        virtualInventoryStockService.approve(outInStockList, dto.getRules(), VirtualInventoryBusinessTypeEnum.getByCode(dto.getBusinessType()), CollUtil.isEmpty(dto.getRules()) ? true : false);
    }

    /**
     * 拆分BOM
     * @author will
     * @date 2024/7/21 16:02
     * @param paramList
     * @return List<OutInStockDTO>
     */
    private List<VirtualInventoryStockDTO.OutInStockDTO> splitBom (List<VirtualInventoryStockDTO.OutInStockDTO> paramList,Boolean isSplitBom) {
        List<VirtualInventoryStockDTO.OutInStockDTO> resultList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(isSplitBom) && !isSplitBom) {
            return resultList;
        }
        //是否拆分bom
        List<DictBasicDTO.ListDTO> list = dictBasicService.getByKey(DictBasicEnum.VIRTUAL_SPLIT_BOM.getKey());
        if (CollUtil.isEmpty(list) || !Boolean.valueOf(list.get(0).getValue())) {
            return resultList;
        }

        List<String> skuIdList = paramList.stream().map(VirtualInventoryStockDTO.OutInStockDTO::getSkuId).distinct().collect(Collectors.toList());
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listHistoryBomChildBySkuIds(skuIdList);
        if (CollUtil.isEmpty(bomChildrenSkuList)) {
            return resultList;
        }
        List<BomChildrenSkuDTO> bomList = bomChildrenSkuList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        if (CollUtil.isEmpty(bomList)) {
            return resultList;
        }
        for (VirtualInventoryStockDTO.OutInStockDTO outInStockDTO : paramList) {
            //bom信息
            List<BomChildrenSkuDTO> childList = bomList.stream().filter(obj -> CharSequenceUtil.equals(obj.getParentSkuId(), outInStockDTO.getSkuId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(childList)) {
                resultList.add(outInStockDTO);
                continue;
            }
            //优先取录入的bom版本，没有则取最新bom版本
            if (CharSequenceUtil.isNotBlank(outInStockDTO.getBomVersion())) {
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),outInStockDTO.getBomVersion())).collect(Collectors.toList());
            } else {
                String bomVersion = childList.stream().max(Comparator.comparing(BomChildrenSkuDTO::getBomVersion)).map(BomChildrenSkuDTO::getBomVersion).get();
                childList = childList.stream().filter(obj -> CharSequenceUtil.equals(obj.getBomVersion(),bomVersion)).collect(Collectors.toList());
            }
            if (CollUtil.isEmpty(childList)) {
                throw new ServiceException(CharSequenceUtil.format("SKU【】未找到版本为【{}】的BOM",outInStockDTO.getSkuId(),outInStockDTO.getBomVersion()));
            }
            for (BomChildrenSkuDTO bomChildrenSkuDTO: childList) {
                VirtualInventoryStockDTO.OutInStockDTO newOutInStockDTO = new VirtualInventoryStockDTO.OutInStockDTO();
                BeanMapperUtils.copy(outInStockDTO,newOutInStockDTO);
                newOutInStockDTO.setSkuId(bomChildrenSkuDTO.getSkuId());
                newOutInStockDTO.setSkuNo(bomChildrenSkuDTO.getSkuNo());
                newOutInStockDTO.setQty(bomChildrenSkuDTO.getQuantity() * outInStockDTO.getQty());
                resultList.add(newOutInStockDTO);
            }
        }
        return resultList;
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void approve(List<T> paramList, List<VirtualTransRuleDTO.StockParamDTO> ruleList, VirtualInventoryBusinessTypeEnum businessType, Boolean byType) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        log.warn("》》》库存交易按【{}】，入参：{}，业务类型：{}", Objects.equals(byType, Boolean.TRUE) ? "业务类型" : "自定义规则", JSON.toJSONString(paramList), businessType.getName());
        // 1.验证参数
        List<VirtualTransRuleDTO.StockParamDTO> stockParamList = ruleList;
        //如果走配置则取已配置的规则
        if(Objects.equals(byType,Boolean.TRUE)) {
            stockParamList = this.wrapTransactionRule(businessType);
        }
        // 2.业务处理，同一个操作产生的交易流水使用同一个关联交易号
        String transactionNo =  docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_XLS);
        this.stockHandler(paramList, businessType, stockParamList, transactionNo);

        stopwatch.stop();
        log.warn("结束库存交易，耗时【{}】秒", stopwatch.elapsed(TimeUnit.SECONDS));
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void stockHandler(List<T> paramList, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        // 获取忽略库存计算的sku
        List<String> ignoreInventorySkuIds = this.getIgnoreSkuIds();
        // 通过对sku id顺序执行, 避免多线程死锁
        Comparator<VirtualInventoryStockDTO.StockBaseDTO> comparing = Comparator.comparing(VirtualInventoryStockDTO.StockBaseDTO::getSkuId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getVirtualWarehouseId)
                .thenComparing(VirtualInventoryStockDTO.StockBaseDTO::getWarehouseId);
        paramList = paramList.stream().sorted(comparing).collect(Collectors.toList());
        for(VirtualInventoryStockDTO.StockBaseDTO baseParam : paramList) {
            VirtualInventoryStockDTO.OutInStockDTO param = (VirtualInventoryStockDTO.OutInStockDTO)baseParam;
            if(ignoreInventorySkuIds.contains(param.getSkuId())) {
                log.warn("sku id: {}，sku编号：{}产品属性是费用或服务，不参与库存出入库", param.getSkuId(), param.getSkuNo());
                continue;
            }
            this.singleHandler(param, businessType, transactionRuleParams, transactionNo);
        }
    }

    /**
     * 获取忽略库存计算的sku
     * @return  返回忽略的SKU ID列表
     */
    protected List<String> getIgnoreSkuIds() {
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        return ignoreInventorySkuIds;
    }

    /**
     * 查询配置的交易规则
     * @author will
     * @date 2024/6/4 11:24
     * @param businessType  业务类型
     * @return List<StockParamDTO> 交易规则
     */
    protected List<VirtualTransRuleDTO.StockParamDTO> wrapTransactionRule(VirtualInventoryBusinessTypeEnum businessType) {
        // 查询配置的交易规则
        List<CfgVirtualTransRulesEntity> list = cfgVirtualTransRulesService.findByDictBizType(businessType.getCode());
        if(CollUtil.isEmpty(list)) {
            return Collections.emptyList();
        }
        List<VirtualTransRuleDTO.StockParamDTO> ruleList = Lists.newArrayListWithExpectedSize(list.size());
        list.forEach(r->{
            VirtualTransRuleDTO.StockParamDTO stockParamDTO = new VirtualTransRuleDTO.StockParamDTO();
            stockParamDTO.setId(r.getId());
            stockParamDTO.setDictBizType(VirtualInventoryBusinessTypeEnum.getByCode(r.getDictBizType()));
            stockParamDTO.setWarehouseOption(InventoryWarehouseOptionEnum.getByCode(r.getWarehouseOption()));
            stockParamDTO.setInventoryStatus(InventoryStatusEnum.getByCode(r.getInventoryStatus()));
            stockParamDTO.setTransactionMode(InventoryModeEnum.getByCode(r.getTransactionMode()));
            ruleList.add(stockParamDTO);
        });
        return ruleList;
    }

    public <T extends VirtualInventoryStockDTO.StockBaseDTO> void singleHandler( VirtualInventoryStockDTO.OutInStockDTO param, VirtualInventoryBusinessTypeEnum businessType, List<VirtualTransRuleDTO.StockParamDTO> transactionRuleParams, String transactionNo) {
        if(CollUtil.isEmpty(transactionRuleParams)) {
            throw new ServiceException(ApiError.ERROR_99034.code, CharSequenceUtil.format(ApiError.ERROR_99034.msg, businessType.getName()));
        }
        log.warn("从配置读取库存交易规则，业务类型：【{}】，单据类型：【{}】，单据id：【{}】，单据日期：【{}】,SKU编号：【{}】,交易配置信息：【{}】", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), param.getBillDate(), param.getSkuNo(), JSONObject.toJSONString(transactionRuleParams));
        // 交易规则按照状态排序
        transactionRuleParams = transactionRuleParams.stream()
                .sorted(Comparator.comparing(x -> ObjectUtil.isNotEmpty(x.getInventoryStatus()) ? x.getInventoryStatus().getCode(): ""))
                .collect(Collectors.toList());
        for(VirtualTransRuleDTO.StockParamDTO stockParamDTO : transactionRuleParams) {
            //验证枚举是否必填
            checkTransRule(stockParamDTO);

            // 可能某个业务类型在同一个仓库即需要做入也需要做出，分别调用逻辑
            VirtualInventoryStockDTO.StockCoreDTO stockCoreDTO = BeanMapperUtils.map(VirtualInventoryStockDTO.StockCoreDTO.class, param);
            stockCoreDTO.setOperationMode(InventoryOperationModeEnum.APPROVE);

            InventoryModeEnum inventoryModeEnum = stockParamDTO.getTransactionMode();
            InventoryStatusEnum inventoryStatusEnum = stockParamDTO.getInventoryStatus();

            if(Objects.equals(InventoryModeEnum.IN_STOCK, inventoryModeEnum)) {
                // 入库
                this.inStockCore(stockCoreDTO, businessType, inventoryStatusEnum, stockParamDTO.getId(), transactionNo);
            } else if (Objects.equals(InventoryModeEnum.OUT_STOCK, inventoryModeEnum)) {
                // 出库
                this.outStockCore(stockCoreDTO, businessType, inventoryStatusEnum,  stockParamDTO.getId(), transactionNo);
            }
        }
    }

    public  void inStockCore(VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String transRuleId, String transactionNo) {
        // 实物仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || CharSequenceUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        VirtualInventoryEntity virtualInventoryEntity = virtualInventoryService.addOrUpdate(param.getVirtualWarehouseId(),param.getWarehouseId(),param.getSkuId(), param.getSkuNo(), inventoryStatusEnum.getCode(),param.getQty());
        // 登记交易流水
        VirtualTransFlowDTO.AddDTO transactionFlowDTO = wrapTransactionFlow(param, virtualInventoryEntity, businessType, inventoryStatusEnum, param.getQty(), warehouseInfo.getOrgId());
        transactionFlowDTO.setTransactionNo(transactionNo);
        transactionFlowDTO.setVirtualTransRuleId(transRuleId);
        VirtualTransFlowEntity transFlowEntity = virtualTransFlowService.add(transactionFlowDTO, transRuleId, InventoryModeEnum.IN_STOCK);
    }

    public  void outStockCore (VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryBusinessTypeEnum businessType, InventoryStatusEnum inventoryStatusEnum, String transRuleId,
                               String transactionNo) {
        //虚拟仓库信息
        VirtualWarehouseEntity virtualWarehouseEntity = virtualWarehouseService.getById(param.getVirtualWarehouseId());
        if (ObjectUtil.isEmpty(virtualWarehouseEntity)) {
            throw new ServiceException(ApiError.ERROR_VIRTUAL_WAREHOUSE_NOT_EXIST);
        }
        //实体仓库信息
        WarehouseDTO.UpdateDTO warehouseInfo = warehouseService.detailWithCache(param.getWarehouseId());
        if(Objects.isNull(warehouseInfo) || CharSequenceUtil.isEmpty(warehouseInfo.getId())) {
            throw new ServiceException(ApiError.ERROR_99002);
        }
        // 待出库数量
        Integer waitOutQty = param.getQty();
        log.info("交易业务：【{}】，来源单据：{}，单据id：【{}】，SKU编号：【{}】，库存状态：【{}】，开始走出库逻辑", businessType.getName(), param.getSourceType().getName(), param.getSourceId(), inventoryStatusEnum.getName(), param.getSkuNo());

        VirtualInventoryEntity found = virtualInventoryService.findVirtualInventoryStock(param.getVirtualWarehouseId(),param.getWarehouseId(),param.getSkuId(), inventoryStatusEnum.getCode());

        String inventoryStatusName = Optional.of(inventoryStatusEnum).map(InventoryStatusEnum::getName).orElse("");
        // 仓库负库存是否允许
        if(ObjectUtil.isNotEmpty(found) &&  found.getQty() < waitOutQty ) {
            String errMsg = CharSequenceUtil.format(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.msg, param.getSkuNo(),virtualWarehouseEntity.getName(), warehouseInfo.getName(), inventoryStatusName,found.getQty(),param.getQty());
            log.error(errMsg);
            throw new ServiceException(ApiError.ERROR_VIRTUAL_INVENTORY_INSUFFICIENT.code, errMsg);
        }
        // 登记交易流水（有可能一个操作产生多条，从多个库存明细中扣除）
        VirtualTransFlowDTO.AddDTO transactionFlowDTO = wrapTransactionFlow(param, found, businessType, inventoryStatusEnum, param.getQty(), warehouseInfo.getOrgId());
        transactionFlowDTO.setTransactionNo(transactionNo);
        VirtualTransFlowEntity transFlowEntity = virtualTransFlowService.add(transactionFlowDTO, transRuleId, InventoryModeEnum.OUT_STOCK);

    }
    /**
     * 验证枚举是否必填
     * @author will
     * @date 2024/6/4 17:37
     * @param stockParamDTO
     */
    private  void checkTransRule(VirtualTransRuleDTO.StockParamDTO stockParamDTO) {
        // 交易规则-选项错误
        InventoryWarehouseOptionEnum inventoryWarehouseOptionEnum = stockParamDTO.getWarehouseOption();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryWarehouseOptionEnum), () -> new ServiceException(ApiError.ERROR_99033));

        // 交易规则-库存状态错误
        InventoryStatusEnum inventoryStatusEnum = stockParamDTO.getInventoryStatus();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryStatusEnum), () -> new ServiceException(ApiError.ERROR_99036));

        // 交易规则-交易类型错误
        InventoryModeEnum inventoryModeEnum = stockParamDTO.getTransactionMode();
        ValidatorUtil.isTrue(Objects.nonNull(inventoryModeEnum), () -> new ServiceException(ApiError.ERROR_99038));
    }

    /**
     * 格式化流水数据
     * @author will
     * @date 2024/6/4 18:31
     * @param param
     * @param virtualInventoryEntity
     * @param businessType
     * @param inventoryStatusEnum
     * @param qty
     * @param orgId
     * @return AddDTO
     */
    protected VirtualTransFlowDTO.AddDTO wrapTransactionFlow(VirtualInventoryStockDTO.StockCoreDTO param, VirtualInventoryEntity virtualInventoryEntity, VirtualInventoryBusinessTypeEnum businessType,
                                                             InventoryStatusEnum inventoryStatusEnum, Integer qty, String orgId) {
        VirtualTransFlowDTO.AddDTO virtualTransFlowDTO = new VirtualTransFlowDTO.AddDTO();
        // 复制对象性能慢，改为手工赋值
        virtualTransFlowDTO.setOrgId(orgId);
        virtualTransFlowDTO.setVirtualWarehouseId(param.getVirtualWarehouseId());
        virtualTransFlowDTO.setWarehouseId(param.getWarehouseId());
        virtualTransFlowDTO.setSkuId(param.getSkuId());
        virtualTransFlowDTO.setSkuNo(param.getSkuNo());
        virtualTransFlowDTO.setSourceId(param.getSourceId());
        virtualTransFlowDTO.setSourceCode(param.getSourceCode());
        virtualTransFlowDTO.setSourceDetailId(param.getSourceDetailId());
        virtualTransFlowDTO.setVirtualInventoryId(virtualInventoryEntity.getId());
        virtualTransFlowDTO.setDictInventoryStatus(inventoryStatusEnum.getCode());
        virtualTransFlowDTO.setDictBizType(businessType.getCode());
        virtualTransFlowDTO.setBillDate(param.getBillDate());
        virtualTransFlowDTO.setSourceType(param.getSourceType().getCode());
        virtualTransFlowDTO.setQty(qty);
        virtualTransFlowDTO.setCurInventoryQty(virtualInventoryEntity.getAfterQty());
        virtualTransFlowDTO.setOperationMode(Objects.nonNull(param.getOperationMode()) ? param.getOperationMode().getCode() : "");
        return virtualTransFlowDTO;
    }
}
