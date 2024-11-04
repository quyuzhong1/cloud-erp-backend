package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.threadlocal.UserContext;
import com.common.business.validator.ValidGroup;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ValidatorUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.wms.dto.inventory.*;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.inventory.*;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.wms.service.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存交易核心处理类
 * @since  2024-07-04
 * @author Edison.Qu
 */
@Slf4j
@Service
public class InventoryTransCoreServiceImpl implements InventoryTransCoreService {
    @Resource
    private PlmTaskFeign plmTaskFeign;
    @Resource
    private InventoryService inventoryService;
    @Resource
    private InventoryTradingService tradingService;
    @Resource
    private CfgTransactionRulesService cfgTransactionRulesService;
    @Resource
    private TransactionFlowService transactionFlowService;
    @Resource
    private WarehouseService warehouseService;
    @Resource
    private WarehouseLocationService warehouseLocationService;
    @Resource
    private SysUserFeign sysUserFeign;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryInOutStockDTO param) {
        List<TransactionRuleDTO> rules = this.getRulesByType(param.getBusinessType());
        InventoryInOutStockRuleDTO inOutStockParam = new InventoryInOutStockRuleDTO();

        inOutStockParam.setParamList(param.getParamList());
        inOutStockParam.setBusinessType(param.getBusinessType());
        inOutStockParam.setRules(rules);
        this.approveByRule(inOutStockParam);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByType(InventoryTransferDTO param) {
        List<TransactionRuleDTO> rules = this.getRulesByType(param.getBusinessType());
        InventoryTransferRuleDTO ruleDTO = new InventoryTransferRuleDTO();

        ruleDTO.setParamList(param.getParamList());
        ruleDTO.setBusinessType(param.getBusinessType());
        ruleDTO.setRules(rules);
        this.approveByRule(ruleDTO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryTransferRuleDTO param) {
        //校验业务参数
        this.checkTransferParam(param.getParamList());
        //校验规则
        this.checkRule(param.getRules());
        //解析库存交易数据
        List<InventoryTransactionDTO> transactionDtoList= this.parseTransactionFromTransfer(param.getBusinessType(),param.getParamList(),param.getRules());
        //执行库存交易
        tradingService.doTransactionList(transactionDtoList,InventoryTradingService.APPROVE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void approveByRule(InventoryInOutStockRuleDTO param) {
        //校验单据是否已经审批

        //校验业务参数
        this.checkInOutParam(param.getParamList());
        //校验规则
        this.checkRule(param.getRules());
        //解析库存交易数据
        List<InventoryTransactionDTO> transactionDtoList=this.parseTranactionFromInOut(param.getBusinessType(),param.getParamList(),param.getRules());
        //执行库存交易
        tradingService.doTransactionList(transactionDtoList,InventoryTradingService.APPROVE);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void unApprove(InventoryUnApproveDTO param) {
        List< TransactionFlowEntity> transactionFlowList = this.queryTransactionFlowList(param.getSourceType(),param.getBillId());
        List<InventoryTransactionDTO> transactionDtoList= this.parseTransactionForUnApprove(transactionFlowList);
        tradingService.doTransactionList(transactionDtoList,InventoryTradingService.UNAPPROVE);
    }

    @Override
    public void batchUnApprove(InventoryBatchUnApproveDTO param) {
        ValidatorUtil.validateEntity(param);
        param.getBillIds().forEach(billId->{
            InventoryUnApproveDTO inventoryUnApproveDTO = new InventoryUnApproveDTO();
            inventoryUnApproveDTO.setSourceType(param.getSourceType());
            inventoryUnApproveDTO.setBillId(billId);
            this.unApprove(inventoryUnApproveDTO);
        });
    }

    /**
     * 根据业务类型获取交易规则
     * @param businessType 业务类型
     * @return  交易规则
     */
    private List<TransactionRuleDTO> getRulesByType(String businessType) {
        // 查询配置的交易规则
        List<CfgTransactionRulesEntity> transactionRuleList = cfgTransactionRulesService.findByDictBizType(businessType);
        if(CollUtil.isEmpty(transactionRuleList)) {
            ServiceException.runError("未配置业务类型({})的交易规则", businessType);
        }

        List<TransactionRuleDTO> transactionRuleDTOS = Lists.newArrayList();
        for (CfgTransactionRulesEntity rule : transactionRuleList) {
            TransactionRuleDTO transactionRule = new TransactionRuleDTO();
            transactionRule.setId(rule.getId());
            transactionRule.setDictBizType(InventoryBusinessTypeEnum.getByCode(rule.getDictBizType()));
            transactionRule.setWarehouseOption(InventoryWarehouseOptionEnum.getByCode(rule.getWarehouseOption()));
            transactionRule.setInventoryStatus(InventoryStatusEnum.getByCode(rule.getInventoryStatus()));
            transactionRule.setTransactionMode(InventoryModeEnum.getByCode(rule.getTransactionMode()));

            transactionRuleDTOS.add(transactionRule);
        }
        return transactionRuleDTOS;
    }

    /**
     * 校验交易规则
     * @param rules 交易规则
     */
    private void checkRule(List<TransactionRuleDTO> rules) {
        if(CollUtil.isEmpty(rules)) {
            ServiceException.runError("交易规则不能为空");
        }
        for (TransactionRuleDTO rule : rules) {
            ValidatorUtil.validateEntity(rule);
        }
    }

    /**
     * 校验入库出库参数
     * @param inOutParam    入库出库参数
     */
    private void checkInOutParam(List<InOutStockDTO> inOutParam) {
        if(CollUtil.isEmpty(inOutParam)) {
            ServiceException.runError("入库出库参数不能为空");
        }
        for (InOutStockDTO inOutStockDTO : inOutParam) {
            ValidatorUtil.validateEntity(inOutStockDTO, ValidGroup.Update.class);
            // ...
        }
    }

    /**
     * 校验调拨参数
     * @param transferParam 调拨参数
     */
    private void checkTransferParam(List<TransferDTO> transferParam) {
        if(CollUtil.isEmpty(transferParam)) {
            ServiceException.runError("调拨参数不能为空");
        }
        for (TransferDTO transferDTO : transferParam) {
            ValidatorUtil.validateEntity(transferDTO);
            // ...
        }
    }

    /**
     * 查询交易流水
     * @param sourceType    业务类型
     * @param billId    单据id
     * @return  交易流水
     */
    private List<TransactionFlowEntity> queryTransactionFlowList(InventorySourceTypeEnum sourceType, String billId) {
        List<TransactionFlowEntity> result = Lists.newArrayList();
        QueryWrapper<TransactionFlowEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(TransactionFlowEntity::getSourceType, sourceType.getCode())
                .eq(TransactionFlowEntity::getSourceId, billId)
                .eq(TransactionFlowEntity::getIsUnapproved, false);
        result.addAll(transactionFlowService.list(wrapper));
        return result;
    }

    /**
     * 解析交易流水(入库出库)
     * @param inOutStockList    入库出库列表
     * @param rules 交易规则
     * @return  交易数据
     */
    private List<InventoryTransactionDTO> parseTranactionFromInOut(String businessType,List<InOutStockDTO> inOutStockList, List<TransactionRuleDTO> rules) {
        List<InventoryTransactionDTO> result = Lists.newArrayList();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WarehouseEntity> warehouseEntityList = warehouseService.listWarehouseWithCaches();
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();

        // 关联交易号
        String transactionNo = IdUtil.getSnowflake().nextIdStr();

        for(InOutStockDTO flow : inOutStockList) {
            for(TransactionRuleDTO rule : rules) {
                InventoryTransactionDTO transactionDTO = new InventoryTransactionDTO();

                // 交易头部信息
//                transactionDTO.setId(flow.getId());
                transactionDTO.setTransactionNo(transactionNo);
                transactionDTO.setTransactionRuleId(rule.getId());
                // 待检，在途设置为空仓位出入库
                boolean qcTransitNotLocation =  InventoryStatusEnum.NO_WAREHOUSE_LOCATION.contains(rule.getInventoryStatus());
                String  warehouseLocation = flow.getWarehouseLocation();
                if (qcTransitNotLocation || ObjectUtil.isNull(warehouseLocation)) {
                    warehouseLocation = "";
                }
                // 库存基础信息
                InventoryStockBaseDTO stockBaseDTO = new InventoryStockBaseDTO();
                stockBaseDTO.setSkuId(flow.getSkuId());
                stockBaseDTO.setSkuNo(flow.getSkuNo());
                stockBaseDTO.setOrgId(getOrgIdFromWarehouse(warehouseEntityList,flow.getWarehouseId()));
                stockBaseDTO.setWarehouseId(flow.getWarehouseId());
                stockBaseDTO.setWarehouseLocation(warehouseLocation);
                stockBaseDTO.setInventoryStatus(rule.getInventoryStatus());
                InventoryEntity inventoryEntity=inventoryService.getInventory(
                        stockBaseDTO.getSkuId(),
                        stockBaseDTO.getWarehouseId(),
                        stockBaseDTO.getWarehouseLocation(),
                        stockBaseDTO.getInventoryStatus().getCode());
                transactionDTO.setInventoryId(null==inventoryEntity?null:inventoryEntity.getId());

                // 交易明细信息
                transactionDTO.setSkuId(stockBaseDTO.getSkuId());
                transactionDTO.setSkuNo(stockBaseDTO.getSkuNo());
                transactionDTO.setOrgId(stockBaseDTO.getOrgId());
                transactionDTO.setWarehouseId(stockBaseDTO.getWarehouseId());
                transactionDTO.setWarehouseLocation(warehouseLocation);
                transactionDTO.setInventoryStatus(stockBaseDTO.getInventoryStatus().getCode());
                // 设置冗余信息部分
                transactionDTO.setOrgName(getOrgName(orgList,stockBaseDTO.getOrgId()));
                transactionDTO.setWarehouseName(getWarehouseInfo(warehouseEntityList,stockBaseDTO.getWarehouseId()).getName());
                if(ObjectUtil.isNotNull(transactionDTO.getWarehouseLocation())){
                    // 在途,待检空库位跳过校验
                    if(qcTransitNotLocation  || ObjectUtil.isNull(flow.getWarehouseLocation())){
                        transactionDTO.setWarehouseLocationName("空仓位");
                    }else {
                        transactionDTO.setWarehouseLocationName(getWarehouseLocationName(stockBaseDTO.getWarehouseId(),stockBaseDTO.getWarehouseLocation(), transactionDTO.getWarehouseName()));
                    }
                }
                transactionDTO.setInventoryStatusName(stockBaseDTO.getInventoryStatus().getName());

                // 交易时间 & 单据类型
                transactionDTO.setBillDate(flow.getBillDate());
                transactionDTO.setDictBizType(businessType);
                transactionDTO.setSourceType(flow.getSourceType().getCode());
                transactionDTO.setSourceTypeName(flow.getSourceType().getName());
                transactionDTO.setSourceId(flow.getSourceId());
                transactionDTO.setSourceCode(flow.getSourceCode());
                transactionDTO.setSourceDetailId(flow.getSourceDetailId());

                // 交易数量
                transactionDTO.setQty(flow.getQty() * rule.getTransactionMode().getCode());

                // 交易人员信息

                transactionDTO.setUserId(userInfo.getUid());
                transactionDTO.setUserName(userInfo.getUserName());

                result.add(transactionDTO);
            }
        }

        // 更新交易数据 是否忽略交易|是否允许负库存
        this.fillTransactionIgnoreOptions(result);
        return result;
    }

    /**
     * 解析交易流水(调拨)
     * @param transferList  调拨列表
     * @param rules         交易规则
     * @return              交易数据
     */
    private List<InventoryTransactionDTO> parseTransactionFromTransfer(String businessType,List<TransferDTO> transferList, List<TransactionRuleDTO> rules) {
        List<InventoryTransactionDTO> result = Lists.newArrayList();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WarehouseEntity> warehouseEntityList = warehouseService.listWarehouseWithCaches();
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();

        // 关联交易号
        String transactionNo = IdUtil.getSnowflake().nextIdStr();

        for(TransferDTO flow : transferList) {
            // 处理当前仓仓位，如果为null 设置为空
            if (ObjectUtil.isNull(flow.getCurWarehouseLocation())){
                flow.setCurWarehouseLocation("");
            }
            // 处理目的仓仓位，如果为null 设置为空
            if (ObjectUtil.isNull(flow.getTargetWarehouseLocation())){
                flow.setTargetWarehouseLocation(flow.getWarehouseLocation());
            }
            for(TransactionRuleDTO rule : rules) {
                InventoryTransactionDTO transactionDTO = new InventoryTransactionDTO();

                // 交易头部信息
                transactionDTO.setTransactionNo(transactionNo);
                transactionDTO.setTransactionRuleId(rule.getId());

                // 库存基础信息
                InventoryStockBaseDTO stockBaseDTO = new InventoryStockBaseDTO();
                stockBaseDTO.setSkuId(flow.getSkuId());
                stockBaseDTO.setSkuNo(flow.getSkuNo());
                if(rule.getWarehouseOption()==InventoryWarehouseOptionEnum.WAREHOUSE_CURRENT){
                    stockBaseDTO.setOrgId(getOrgIdFromWarehouse(warehouseEntityList,flow.getCurWarehouseId()));
                    stockBaseDTO.setWarehouseId(flow.getCurWarehouseId());
                    stockBaseDTO.setWarehouseLocation(flow.getCurWarehouseLocation());
                    stockBaseDTO.setInventoryStatus(rule.getInventoryStatus()); // 调出仓的库存状态

                }else {
                    stockBaseDTO.setOrgId(getOrgIdFromWarehouse(warehouseEntityList,flow.getTargetWarehouseId()));
                    stockBaseDTO.setWarehouseId(flow.getTargetWarehouseId());
                    stockBaseDTO.setWarehouseLocation(flow.getTargetWarehouseLocation());
                    stockBaseDTO.setInventoryStatus(rule.getInventoryStatus()); // 调入仓的库存状态
                }
                InventoryEntity inventoryEntity=inventoryService.getInventory(
                        stockBaseDTO.getSkuId(),
                        stockBaseDTO.getWarehouseId(),
                        stockBaseDTO.getWarehouseLocation(),
                        stockBaseDTO.getInventoryStatus().getCode());
                transactionDTO.setInventoryId(null==inventoryEntity?null:inventoryEntity.getId());

                // 交易明细信息
                transactionDTO.setSkuId(stockBaseDTO.getSkuId());
                transactionDTO.setSkuNo(stockBaseDTO.getSkuNo());
                transactionDTO.setOrgId(stockBaseDTO.getOrgId());
                transactionDTO.setWarehouseId(stockBaseDTO.getWarehouseId());
                transactionDTO.setWarehouseLocation(stockBaseDTO.getWarehouseLocation());
                transactionDTO.setInventoryStatus(stockBaseDTO.getInventoryStatus().getCode());
                // 设置冗余信息部分
                transactionDTO.setOrgName(getOrgName(orgList,stockBaseDTO.getOrgId()));
                transactionDTO.setWarehouseName(getWarehouseInfo(warehouseEntityList,stockBaseDTO.getWarehouseId()).getName());

                transactionDTO.setWarehouseLocationName(getWarehouseLocationName(stockBaseDTO.getWarehouseId(),stockBaseDTO.getWarehouseLocation(), transactionDTO.getWarehouseName()));

                transactionDTO.setInventoryStatusName(stockBaseDTO.getInventoryStatus().getName());

                // 交易时间 & 单据类型
                transactionDTO.setBillDate(flow.getBillDate());
                transactionDTO.setDictBizType(businessType);
                transactionDTO.setSourceType(flow.getSourceType().getCode());
                transactionDTO.setSourceTypeName(flow.getSourceType().getName());
                transactionDTO.setSourceId(flow.getSourceId());
                transactionDTO.setSourceCode(flow.getSourceCode());
                transactionDTO.setSourceDetailId(flow.getSourceDetailId());

                // 交易数量
                transactionDTO.setQty(flow.getQty() * rule.getTransactionMode().getCode());

                // 交易人员信息
                transactionDTO.setUserId(userInfo.getUid());
                transactionDTO.setUserName(userInfo.getUserName());

                result.add(transactionDTO);
            }
        }

        // 更新交易数据 是否忽略交易|是否允许负库存
        this.fillTransactionIgnoreOptions(result);
        return result;
    }

    /**
     * 解析交易流水
     * @param transactionFlowList   交易流水列表
     * @return  交易数据
     */
    private List<InventoryTransactionDTO> parseTransactionForUnApprove(List<TransactionFlowEntity> transactionFlowList) {
        List<InventoryTransactionDTO> result = Lists.newArrayList();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<WarehouseEntity> warehouseEntityList = warehouseService.listWarehouseWithCaches();
        List<BaseIdDTO> orgList = sysUserFeign.listAccountingCompany();

        transactionFlowList.forEach(flow->{
            InventoryTransactionDTO transactionDTO = new InventoryTransactionDTO();
            // 待检，在途 设置空库位出入库
            boolean qcTransitNotLocation =  InventoryStatusEnum.NO_WAREHOUSE_LOCATION.contains(InventoryStatusEnum.getByCode(flow.getDictInventoryStatus()));
            String  warehouseLocation = flow.getWarehouseLocation();
            if (qcTransitNotLocation || ObjectUtil.isNull(warehouseLocation)){
                warehouseLocation = "";
            }
            // 交易头部信息
            transactionDTO.setId(flow.getId());
            transactionDTO.setTransactionNo(flow.getTransactionNo());
            transactionDTO.setTransactionRuleId(flow.getTransactionRuleId());

            // 交易明细信息
            transactionDTO.setInventoryId(flow.getInventoryId());
            transactionDTO.setSkuId(flow.getSkuId());
            transactionDTO.setSkuNo(flow.getSkuNo());
            transactionDTO.setOrgId(flow.getOrgId());
            transactionDTO.setWarehouseId(flow.getWarehouseId());
            transactionDTO.setWarehouseLocation(warehouseLocation);
            transactionDTO.setInventoryStatus(flow.getDictInventoryStatus());

            transactionDTO.setOrgName(getOrgName(orgList,flow.getOrgId()));
            transactionDTO.setWarehouseName(getWarehouseInfo(warehouseEntityList,flow.getWarehouseId()).getName());
            if(ObjectUtil.isNotNull(flow.getWarehouseLocation())){
                // 在途,待检空库位跳过校验
                if(qcTransitNotLocation || ObjectUtil.isNull(flow.getWarehouseLocation())){
                    transactionDTO.setWarehouseLocationName("空仓位");
                }else {
                    transactionDTO.setWarehouseLocationName(getWarehouseLocationName(flow.getWarehouseId(),flow.getWarehouseLocation(), transactionDTO.getWarehouseName()));
                }
            }
            transactionDTO.setInventoryStatusName(InventoryStatusEnum.getByCode(flow.getDictInventoryStatus()).getName());

            // 交易时间 & 单据类型
            transactionDTO.setBillDate(flow.getBillDate());
            transactionDTO.setDictBizType(flow.getDictBizType());
            transactionDTO.setSourceType(flow.getSourceType());
            transactionDTO.setSourceTypeName(InventorySourceTypeEnum.getByCode(flow.getSourceType()).getName());
            transactionDTO.setSourceId(flow.getSourceId());
            transactionDTO.setSourceCode(flow.getSourceCode());
            transactionDTO.setSourceDetailId(flow.getSourceDetailId());

            // 交易数量
            transactionDTO.setQty(flow.getQty()*-1);

            // 交易人员信息
            transactionDTO.setUserId(userInfo.getUid());
            transactionDTO.setUserName(userInfo.getUserName());

            result.add(transactionDTO);
        });

        // 更新交易数据 是否忽略交易|是否允许负库存
        this.fillTransactionIgnoreOptions(result);
        return result;
    }


    /**
     * 更新交易数据：是否忽略交易|是否允许负库存
     * @param  paramList   交易数据
     */
    private void fillTransactionIgnoreOptions(List<InventoryTransactionDTO> paramList) {
        if(CollUtil.isEmpty(paramList)) {
            return;
        }
        // 查询仓库信息
        List<WarehouseEntity> warehouseEntityList = warehouseService.listWarehouseWithCaches();
        // 查询忽略库存的sku
        List<SkuVO> ignoreInventorySkuList = plmTaskFeign.getNoInventorySku();
        List<String> ignoreInventorySkuIds = Lists.newArrayList();
        if(CollUtil.isNotEmpty(ignoreInventorySkuList)) {
            ignoreInventorySkuIds = ignoreInventorySkuList.stream().map(SkuVO::getSkuId).distinct().collect(Collectors.toList());
        }
        List<String> finalIgnoreInventorySkuIds = ignoreInventorySkuIds;

        // 更新交易数据
        paramList.forEach(transactionDTO-> {
            // 是否允许负库存
            transactionDTO.setAllowNegativeInventory(this.getWarehouseInfo(warehouseEntityList, transactionDTO.getWarehouseId()).getAllowNegativeInventory());

            // 是否忽略交易
            transactionDTO.setIgnoreTransaction(finalIgnoreInventorySkuIds.contains(transactionDTO.getSkuId()));
        });
    }


    /**
     * 获取核算公司名称
     * @param orgId 核算公司id
     * @return  核算公司名称
     */
    private String getOrgName(List<BaseIdDTO> orgList, String orgId) {
        if(CollUtil.isEmpty(orgList)) {
            ServiceException.runError("查询核算公司列表为空");
        }
        BaseIdDTO org = orgList.stream().filter(o->o.getId().equals(orgId)).findFirst().orElse(null);
        if(null == org) {
            ServiceException.runError("核算公司(ID={})不存在", orgId);
        }
        return org.getName();
    }

    /**
     * 过滤并获取仓库信息
     * @param warehouseList 仓库列表
     * @param warehouseId   仓库id
     * @return  仓库名称
     */
    private WarehouseEntity getWarehouseInfo(List<WarehouseEntity> warehouseList, String warehouseId) {
        if(CollectionUtils.isEmpty(warehouseList)) {
            ServiceException.runError("查询仓库列表为空");
        }
        WarehouseEntity warehouseEntity = warehouseList.stream().filter(w->w.getId().equals(warehouseId)).findFirst().orElse(null);
        if(null == warehouseEntity) {
            ServiceException.runError("仓库信息(ID={})不存在", warehouseId);
        }

        return warehouseEntity;
    }

    /**
     * 获取货主公司id
     * @param warehouseId 仓库id
     * @return  核算公司id
     */
    private String getOrgIdFromWarehouse(List<WarehouseEntity> warehouseList,String warehouseId) {
        if(CollectionUtils.isEmpty(warehouseList)) {
            ServiceException.runError("查询仓库列表为空");
        }
        WarehouseEntity warehouseEntity = warehouseList.stream().filter(w->w.getId().equals(warehouseId)).findFirst().orElse(null);
        if(null == warehouseEntity) {
            ServiceException.runError("仓库信息(ID={})不存在", warehouseId);
        }
        return warehouseEntity.getOrgId();
    }

    /**
     * 获取库位名称
     * @param warehouseId       仓库id
     * @param warehouseLocation 仓位编号
     * @return  仓位名称
     */
    private String getWarehouseLocationName(String warehouseId, String warehouseLocation,String warehouseName) {
        QueryWrapper<WarehouseLocationEntity> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(WarehouseLocationEntity::getWarehouseId, warehouseId)
                .eq(WarehouseLocationEntity::getCode, warehouseLocation)
                .eq(WarehouseLocationEntity::getType,"location")
                .select(WarehouseLocationEntity::getName)
                .last("limit 1");
        WarehouseLocationEntity locationEntity = warehouseLocationService.getOne(wrapper);
        if(locationEntity != null) {
            return locationEntity.getName();
        }
        warehouseName = StrUtil.isNotBlank(warehouseLocation) ? warehouseName : "空仓位";
        log.error("库位信息不存在,仓库id：{}，仓库名称:{},仓位编号:{}",warehouseId, warehouseName,warehouseLocation);
        throw new ServiceException(ApiError.ERROR_WAREHOUSE_LOCATION_NOT_FOUND,warehouseName, warehouseLocation);
    }

}