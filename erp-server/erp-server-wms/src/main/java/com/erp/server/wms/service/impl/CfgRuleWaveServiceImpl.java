package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.ConditionElement;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.SoB2cDataTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.dto.CfgRuleWaveRecordDTO;
import com.erp.model.wms.dto.inventory.InOutStockDTO;
import com.erp.model.wms.dto.inventory.InventoryInOutStockDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRulePickingDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.CfgRuleWaveMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <p>
 * 波次规则 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class CfgRuleWaveServiceImpl extends SuperServiceImpl<CfgRuleWaveMapper, CfgRuleWaveEntity> implements CfgRuleWaveService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CfgRuleConditionService cfgRuleConditionService;

    @Autowired
    private SoB2cDeliveryService soB2cDeliveryService;

    @Autowired
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Autowired
    private SoB2cFeign soB2cFeign;

    @Resource
    private SpElServer spElServer;

    @Resource
    private WaveListService waveListService;


    @Resource
    private CfgRulePickingService cfgRulePickingService;


    @Resource
    private PickingCartTypeService pickingCartTypeService;

    @Resource
    private CfgRuleWaveRecordService cfgRuleWaveRecordService;


    @Resource
    private InventoryTransCoreService inventoryTransCoreService;

    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleWaveDTO.AddDTO addDTO) {
        CfgRuleWaveEntity cfgRuleWaveEntity = new CfgRuleWaveEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleWaveEntity);

        // 数据处理
        handleData(addDTO.getExecutionTimeList(), addDTO.getPickingCartTypeIdList(), cfgRuleWaveEntity);

        log.info("开始新增波次规则");
        boolean save = super.save(cfgRuleWaveEntity);
        if (!save) {
            throw new ServiceException("波次规则保存失败");
        }
        //保存规则条件
        cfgRuleConditionService.saveRuleCondition(cfgRuleWaveEntity.getId(), addDTO.getConditionList(), RuleTypeEnum.CFG_RULE_WAVE.getCode());

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "波次规则", cfgRuleWaveEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), cfgRuleWaveEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(cfgRuleWaveEntity.getId(), cfgRuleWaveEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(CfgRuleWaveDTO.UpdateDTO updateDTO) {
        CfgRuleWaveEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "波次规则"));
        CfgRuleWaveEntity cfgRuleWaveEntity = BeanMapperUtils.map(CfgRuleWaveEntity.class, updateDTO);

        // 数据处理
        handleData(updateDTO.getExecutionTimeList(), updateDTO.getPickingCartTypeIdList(), cfgRuleWaveEntity);
        log.info("编辑 开始修改波次规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(cfgRuleWaveEntity);
        if (!save) {
            throw new ServiceException("波次规则保存失败");
        }
        cfgRuleConditionService.updateRuleCondition(updateDTO.getId(), updateDTO.getConditionList(), ModuleTypeEnum.CFG_RULE_WAVE.getCode(), RuleTypeEnum.CFG_RULE_WAVE.getCode());

        // 记录主单操作日志
        log.info("编辑 开始记录波次规则日志数据，id：【{}】", cfgRuleWaveEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleWaveEntity.getId(), "波次规则");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWaveEntity, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), cfgRuleWaveEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgRuleWaveDTO.ListDTO> paging(PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto) {
        CfgRuleWaveDTO.PagingParamDTO params = dto.getParams();
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgRuleWaveDTO.ListDTO> pageData = baseMapper.paging(query, params);
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到波次规则数据"));

        String disabledName = disabled ? "禁用" : "启用";
        if (disabled.equals(entity.getDisabled())) {
            throw new ServiceException(StrUtil.format("波次规则已【{}】，不支持再次【{}】", disabledName, disabledName));
        }
        lambdaUpdate().eq(CfgRuleWaveEntity::getId, id)
                .set(CfgRuleWaveEntity::getDisabled, disabled)
                .update(new CfgRuleWaveEntity());
        String msg = StrUtil.format("【{}】波次规则【{}】", disabledName, entity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), id, StrUtil.format("{}操作", disabledName));
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到波次规则数据"));

        // 删除主单数据
        log.info("删除 开始删除波次规则主单数据，id：【{}】", id);
        super.removeById(id);

        //删除规则
        cfgRuleConditionService.removeByRuleIds(Arrays.asList(id));
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public CfgRuleWaveDTO.ViewDTO view(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到波次规则数据"));
        CfgRuleWaveDTO.ViewDTO view = BeanMapperUtils.map(CfgRuleWaveDTO.ViewDTO.class, entity);

        //执行时间
        if (ObjectUtil.isNotEmpty(entity.getExecutionTimeJson())) {
            List<LocalTime> executionTimeList = JSONUtil.parseArray(entity.getExecutionTimeJson()).stream().filter(obj -> ObjectUtil.isNotEmpty(obj))
                    .map(obj -> LocalTime.parse(obj.toString(), DateTimeFormatter.ofPattern("HH:mm"))).collect(Collectors.toList());
            view.setExecutionTimeList(executionTimeList);
        }

        String pickingCartTypeJson = entity.getPickingCartTypeJson();
        List<String> pickingCartTypeIdList = Arrays.stream(JSONUtil.parseArray(pickingCartTypeJson).stream().toArray(String[]::new)).collect(Collectors.toList());
        view.setPickingCartTypeIdList( pickingCartTypeIdList);

        //查询规则条件
        List<CfgRuleConditionEntity> ruleConditionEntities = cfgRuleConditionService.list(Wrappers.<CfgRuleConditionEntity>lambdaQuery()
                .eq(CfgRuleConditionEntity::getRuleId, id)
                .orderByAsc(CfgRuleConditionEntity::getIndex));
        List<CfgRuleConditionDTO.View> ruleConditions = BeanMapperUtils.copyList(CfgRuleConditionDTO.View.class, ruleConditionEntities);
        view.setConditionList(ruleConditions);
        return view;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean executeRule(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到波次规则数据"));

        if (entity.getDisabled()) {
            throw new ServiceException("规则已禁用不支持执行");
        }

        // 查询所有规则对应的规则条件
        List<CfgRuleConditionDTO.ConditionElementDTO> conditionList = cfgRuleConditionService.listByRuleIds(Arrays.asList(entity.getId()),RuleTypeEnum.CFG_RULE_WAVE.getCode());

        //查询所有待处理的发货单进行生成波次
        List<SoB2cDeliveryEntity> soB2cDeliveryList = soB2cDeliveryService.listWaitHandle();
        if (CollectionUtil.isEmpty(soB2cDeliveryList)) {
            return Boolean.TRUE;
        }
        //发货明细数据
        List<String> deliveryIdList = soB2cDeliveryList.stream().map(SoB2cDeliveryEntity::getId).distinct().collect(Collectors.toList());
        List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailList = soB2cDeliveryDetailService.listByMainIds(deliveryIdList);

        //渠道数据
        List<String> logisticsChannelIdList = soB2cDeliveryList.stream().map(SoB2cDeliveryEntity::getLogisticsChannelId).distinct().collect(Collectors.toList());
        List<LogisticsChannelEntity> list = FeignQuery.create(LogisticsChannelEntity.class).in(LogisticsChannelEntity::getId, logisticsChannelIdList).list();

        //销售订单
        List<String> b2cSoIdList = soB2cDeliveryList.stream().map(SoB2cDeliveryEntity::getSourceId).collect(Collectors.toList());
        SoB2cDTO.SoB2cDataParamDTO paramDTO = new SoB2cDTO.SoB2cDataParamDTO();
        paramDTO.setB2cSoIdList(b2cSoIdList);
        paramDTO.setDataTypeList(Arrays.asList(SoB2cDataTypeEnum.LOGISTIC.getCode(), SoB2cDataTypeEnum.RECEIVER.getCode()));
        SoB2cDTO.SoB2cDataDTO soB2cDataDTO = soB2cFeign.listSoB2cData(paramDTO);


        //符合规则的发货单数据
        List<SoB2cDeliveryEntity> compliantList = new ArrayList<>();
        for (SoB2cDeliveryEntity soB2cDeliveryEntity : soB2cDeliveryList) {
            //发货单明细数据
            List<SoB2cDeliveryDetailEntity> deliveryDetailList = soB2cDeliveryDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), soB2cDeliveryEntity.getId())).collect(Collectors.toList());
            if (CollectionUtil.isEmpty(deliveryDetailList)) {
                log.error("发货单【{}】未找到发货明细数据", soB2cDeliveryEntity.getCode());
                continue;
            }
            //渠道数据
            LogisticsChannelEntity channelEntity = list.stream().filter(obj -> StrUtil.equals(soB2cDeliveryEntity.getLogisticsChannelId(), obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(channelEntity)) {
                log.error("发货单【{}】未找到渠道数据", soB2cDeliveryEntity.getCode());
                continue;
            }

            Map<String, Object> map = handleRuleData(soB2cDataDTO, channelEntity, soB2cDeliveryEntity, deliveryDetailList);
            List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
            //获取到表达式,判断表达式是否匹配
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map);
            if (matchResult) {
                compliantList.add(soB2cDeliveryEntity);
            }
        }
        if (CollectionUtil.isEmpty(compliantList)) {
            log.info("未发现需要新增的波次列表数据");
            return Boolean.TRUE;
        }
        //生成拣货波次列表数据
        generatePickingWave(compliantList, soB2cDeliveryDetailList, entity);

        return Boolean.TRUE;
    }

    @Override
    public void autoExecuteRule(String time) {
        if (StrUtil.isBlank(time)) {
            //当前时间
            // time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            time = "02:00";
        }
        List<CfgRuleWaveEntity> cfgRuleWaveList = baseMapper.listRuleWaveByTime(time);
        if (CollectionUtil.isEmpty(cfgRuleWaveList)) {
            log.info("时间【{}】未找到符合条件的波次规则");
            return;
        }
        for (CfgRuleWaveEntity waveEntity : cfgRuleWaveList) {
            try {
                //执行规则
                executeRule(waveEntity.getId());
            } catch (Exception e) {
                CfgRuleWaveRecordDTO.AddDTO addDTO = new CfgRuleWaveRecordDTO.AddDTO();
                addDTO.setRuleWaveId(waveEntity.getId());
                addDTO.setExecutionTime(LocalTime.parse(time,DateTimeFormatter.ofPattern("HH:mm")));
                addDTO.setReturnMsg(e.getMessage());
                cfgRuleWaveRecordService.add(addDTO);
            }
        }
    }

    /**
     * 生成拣货波次列表数据
     *
     * @param compliantList
     * @param entity
     * @author will
     * @date 2024/6/26 11:39
     */
    private void generatePickingWave(List<SoB2cDeliveryEntity> compliantList, List<SoB2cDeliveryDetailEntity> allDetailList, CfgRuleWaveEntity entity) {
        if (CollectionUtil.isEmpty(compliantList)) {
            return;
        }

        //符合发货单数量小于最低单数
        if (MathUtil.compareTo(entity.getMinOrderQty(), compliantList.size()) > MathUtil.ZERO) {
            return;
        }
        //符合发货单的商品数量小于最低商品数量
        Integer orderTotalQty = allDetailList.stream().map(SoB2cDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
        if (MathUtil.compareTo(entity.getMinQty(), orderTotalQty) > MathUtil.ZERO) {
            return;
        }

        //按创建时间录入波次
        List<SoB2cDeliveryEntity> sortedList = compliantList.stream().sorted(Comparator.comparing(SoB2cDeliveryEntity::getCreateTime)).collect(Collectors.toList());
        Integer totalQty = MathUtil.ZERO;
        Integer orderQty = MathUtil.ZERO;

        WaveListDTO.AddDTO addDTO = new WaveListDTO.AddDTO();
        addDTO.setWaveType(entity.getWaveType());
        addDTO.setPickingType(entity.getPickingType());
        addDTO.setName(entity.getName());
        //需要更新发货单的异常状态
        List<SoB2cDeliveryEntity> updateDeliveryList = new ArrayList<>();
        //波次中的发货单
        List<String> deliveryIdList = new ArrayList<>();
        //需要新增的波次数据
        List<WaveListDTO.AddDTO> resultList = new ArrayList<>();
        for (SoB2cDeliveryEntity deliveryEntity : sortedList) {

            //发货明细商品数量合计
            Integer detailTotalQty = allDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), deliveryEntity.getId())).map(SoB2cDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            if (MathUtil.compareTo(detailTotalQty,entity.getMaxQty()) > MathUtil.ZERO) {
                log.warn("发货单【{}】下商品数量【{}】大于波次规则商品数量【{}】",deliveryEntity.getCode(),detailTotalQty,entity.getMaxQty());
                continue;
            }

            try {
                pickingInventory(deliveryEntity,allDetailList,updateDeliveryList);
            } catch (Exception e) {
                //添加波次生成的缺货异常
                deliveryEntity.setStatus(SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getStatus());
                deliveryEntity.setAbnormalCause(AbnormalCauseEnum.GENERATION_WAVE.getCode());
                updateDeliveryList.add(deliveryEntity);
                continue;
            }

            //商品总数超出最大数量后另起波次,或者发货单数量超过最大单数后另起波次
            if ((ObjectUtil.isNotEmpty(entity.getMaxQty()) && detailTotalQty + totalQty > entity.getMaxQty())
                    || orderQty > entity.getMaxOrderQty()) {
                addDTO.setDeliveryIdList(deliveryIdList);
                resultList.add(addDTO);

                //清空合计数据
                totalQty = MathUtil.ZERO;
                orderQty = MathUtil.ZERO;
                deliveryIdList = new ArrayList<>();
                addDTO = new WaveListDTO.AddDTO();
                addDTO.setWaveType(entity.getWaveType());
                addDTO.setPickingType(entity.getPickingType());
                addDTO.setName(entity.getName());
            }

            //添加发货单
            deliveryIdList.add(deliveryEntity.getId());

           //发货单商品数量
            totalQty = detailTotalQty + totalQty;

            //发货单订单数量
            orderQty++;
        }
        //添加波次生成的缺货异常
        if (CollectionUtil.isNotEmpty(updateDeliveryList)) {
            soB2cDeliveryService.updateBatchById(updateDeliveryList);
        }

        //添加最后一个波次
        if (CollectionUtil.isNotEmpty(deliveryIdList) && deliveryIdList.size() >= entity.getMinOrderQty()) {
            addDTO.setDeliveryIdList(deliveryIdList);
            resultList.add(addDTO);
        }
        if (CollectionUtil.isEmpty(resultList)) {
            return;
        }
        for (WaveListDTO.AddDTO waveAddDTO : resultList) {
            BaseResultDTO.AddDTO add = waveListService.add(waveAddDTO);
            //更新发货单状态
            soB2cDeliveryService.updateDeliveryStatus(waveAddDTO.getDeliveryIdList(),SoB2cDeliveryStatusEnum.GENERATE_WAVE.getStatus());
            //发货单日志
            List<Pair<String, String>> pairList = waveAddDTO.getDeliveryIdList().stream().map(obj -> new Pair<>(obj, "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(StrUtil.format("自动生成波次成功，波次号【{}】",add.getCode()), ModuleTypeEnum.DELIVERY_ORDER.getCode(), pairList, "自动生成波次");
        }
    }

    /**
     * 走拣货策略并且扣减库存
     * @author will
     * @date 2024/7/3 18:49
     * @param deliveryEntity
     * @param allDetailList
     * @return List<LocationInventoryResultDTO>
     */
    private void pickingInventory (SoB2cDeliveryEntity deliveryEntity,List<SoB2cDeliveryDetailEntity> allDetailList,List<SoB2cDeliveryEntity> updateDeliveryList) {

        //发货明细
        List<CfgRulePickingDTO.CfgExecutionDataDetailDTO> detailList = allDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), deliveryEntity.getId()))
                .map(v -> new CfgRulePickingDTO.CfgExecutionDataDetailDTO(v.getWarehouseId(), v.getSkuId(), v.getSkuNo(), v.getDeliveryQty()))
                .collect(Collectors.toList());

        //拣货规则
        CfgRulePickingDTO.CfgExecutionDataDTO executionData = new CfgRulePickingDTO.CfgExecutionDataDTO();
        executionData.setBillType(PickingBillTypeEnum.B2C.getCode());
        executionData.setDetails(detailList);
        List<LocationInventoryResultDTO> ruleOrderMatchResult = cfgRulePickingService.getRuleOrderMatchResult(executionData);

        //判断是否匹配仓位成功
        long count = ruleOrderMatchResult.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseLocation())).count();
        if (count > MathUtil.ZERO) {
            String skuNos = ruleOrderMatchResult.stream().filter(obj -> StrUtil.isBlank(obj.getWarehouseLocation())).map(LocationInventoryResultDTO::getSkuNo).collect(Collectors.joining(","));
            log.warn("发货单【{}】SKU【{}】匹配仓位不成功",deliveryEntity.getCode(),skuNos);
            //添加波次生成的缺货异常
            deliveryEntity.setStatus(SoB2cDeliveryStatusEnum.EXCEPTION_ORDER.getStatus());
            deliveryEntity.setAbnormalCause(AbnormalCauseEnum.GENERATION_WAVE.getCode());
            updateDeliveryList.add(deliveryEntity);
           return;
        }


        List<InOutStockDTO> inOutStockList = new ArrayList<>();
        for (LocationInventoryResultDTO resultDTO : ruleOrderMatchResult) {
            //扣减库存
            InOutStockDTO inOutStockDTO = new InOutStockDTO();
            inOutStockDTO.setSourceType(InventorySourceTypeEnum.SO_B2C_DELIVERY);
            inOutStockDTO.setSourceId(deliveryEntity.getId());
            inOutStockDTO.setSourceCode(deliveryEntity.getCode());
            //明细id，多个取第一个
            String sourceDetailId = allDetailList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), deliveryEntity.getId()) && StrUtil.equals(obj.getSkuId(), resultDTO.getSkuId()))
                    .map(SoB2cDeliveryDetailEntity::getId).findFirst().orElse("");
            inOutStockDTO.setSourceDetailId(sourceDetailId);
            inOutStockDTO.setSkuNo(resultDTO.getSkuNo());
            inOutStockDTO.setBillDate(LocalDate.now());
            inOutStockDTO.setSkuId(resultDTO.getSkuId());
            inOutStockDTO.setQty(resultDTO.getQuantity());
            inOutStockDTO.setWarehouseId(resultDTO.getWarehouseId());
            inOutStockDTO.setWarehouseLocation(resultDTO.getWarehouseLocation());
            inOutStockList.add(inOutStockDTO);
        }
        //添加冻结库存
        InventoryInOutStockDTO inventoryInOutStockDTO = new InventoryInOutStockDTO();
        inventoryInOutStockDTO.setParamList(inOutStockList);
        inventoryInOutStockDTO.setBusinessType(InventoryBusinessTypeEnum.SO_B2C_DELIVERY.getCode());
        //更新库存
        inventoryTransCoreService.approveByType(inventoryInOutStockDTO);
    }

    /**
     * 根据发货单数据格式话波次规则数据
     *
     * @param soB2cDeliveryEntity
     * @return Map<String, Object>
     * @author will
     * @date 2024/6/25 18:43
     */
    private Map<String, Object> handleRuleData(SoB2cDTO.SoB2cDataDTO soB2cDataDTO, LogisticsChannelEntity channelEntity
            , SoB2cDeliveryEntity soB2cDeliveryEntity, List<SoB2cDeliveryDetailEntity> deliveryDetailList) {
        Map<String, Object> map = new HashMap<>();

        //销售订单
        List<SoB2cEntity> list = soB2cDataDTO.getList();
        SoB2cEntity soB2cEntity = CollectionUtil.isEmpty(list) ? null : list.stream().filter(obj -> StrUtil.equals(obj.getId(), soB2cDeliveryEntity.getSourceId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(StrUtil.format("发货单【{}】未找到上游销售订单", soB2cDeliveryEntity.getCode()));
        }
        //销售订单物流信息
        List<SoB2cLogisticsEntity> logisticsList = soB2cDataDTO.getLogisticsList();
        SoB2cLogisticsEntity soB2cLogisticsEntity = CollectionUtil.isEmpty(logisticsList) ? null : logisticsList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), soB2cDeliveryEntity.getSourceId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(StrUtil.format("发货单【{}】未找到上游销售订单物流信息", soB2cDeliveryEntity.getCode()));
        }
        //销售订单买家信息
        List<SoB2cReceiverEntity> receiverList = soB2cDataDTO.getReceiverList();
        SoB2cReceiverEntity soB2cReceiverEntity = CollectionUtil.isEmpty(receiverList) ? null : receiverList.stream().filter(obj -> StrUtil.equals(obj.getMainId(), soB2cDeliveryEntity.getSourceId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(soB2cReceiverEntity)) {
            throw new ServiceException(StrUtil.format("发货单【{}】未找到上游销售订单买家信息", soB2cDeliveryEntity.getCode()));
        }
        List<Map<String, Object>> detailList = new ArrayList<>();
        for (SoB2cDeliveryDetailEntity detailEntity : deliveryDetailList) {
            Map<String, Object> detailMap = new HashMap<>();
            //仓库
            detailMap.put("deliveryWarehouseId", detailEntity.getWarehouseId());
            //平台
            detailMap.put("dictPlatform", soB2cDeliveryEntity.getDictPlatform());
            //店铺
            detailMap.put("shopId", soB2cDeliveryEntity.getShopId());
            //物流商
            detailMap.put("logisticsSupplierId", channelEntity.getMainId());
            //物流渠道
            detailMap.put("logisticsChannelId", channelEntity.getId());
            //国家
            detailMap.put("country", soB2cReceiverEntity.getCountry());
            //SKU
            detailMap.put("skuId", detailEntity.getSkuId());
            //包装尺寸长（cm）
            detailMap.put("length", soB2cLogisticsEntity.getLength());
            //包装尺寸宽（cm）
            detailMap.put("width", soB2cLogisticsEntity.getWidth());
            //包装尺寸高（cm）
            detailMap.put("height", soB2cLogisticsEntity.getHeight());
            //包装重量（g）
            detailMap.put("weight", soB2cLogisticsEntity.getWeight());
            //订单创建时间
            detailMap.put("createTime", soB2cLogisticsEntity.getCreateTime());
            //发货单-拣货类型
            detailMap.put("pickingType", soB2cDeliveryEntity.getPickingType());
            //发货单-创建时间
            detailMap.put("deliveryCreateTime", soB2cDeliveryEntity.getCreateTime());
            detailList.add(detailMap);
        }
        map.put("detailList", detailList);


        Object deliveryWarehouseId = spElServer.getByField("deliveryWarehouseId", detailList);
        map.put("deliveryWarehouseId", deliveryWarehouseId);
        Object dictPlatform = spElServer.getByField("dictPlatform", detailList);
        map.put("dictPlatform", dictPlatform);
        Object shopId = spElServer.getByField("shopId", detailList);
        map.put("shopId", shopId);
        Object logisticsSupplierId = spElServer.getByField("logisticsSupplierId", detailList);
        map.put("logisticsSupplierId", logisticsSupplierId);
        Object logisticsChannelId = spElServer.getByField("logisticsChannelId", detailList);
        map.put("logisticsChannelId", logisticsChannelId);
        Object country = spElServer.getByField("country", detailList);
        map.put("country", country);
        Object skuId = spElServer.getByField("skuId", detailList);
        map.put("skuId", skuId);
        Object length = spElServer.getByField("length", detailList);
        map.put("length", length);
        Object width = spElServer.getByField("width", detailList);
        map.put("width", width);
        Object height = spElServer.getByField("height", detailList);
        map.put("height", height);
        Object weight = spElServer.getByField("weight", detailList);
        map.put("weight", weight);
        Object createTime = spElServer.getByField("createTime", detailList);
        map.put("createTime", createTime);
        Object pickingType = spElServer.getByField("pickingType", detailList);
        map.put("pickingType", pickingType);
        Object deliveryCreateTime = spElServer.getByField("deliveryCreateTime", detailList);
        map.put("deliveryCreateTime", deliveryCreateTime);
        return map;
    }

    /**
     * 根据名称查询波次规则
     *
     * @param name
     * @return CfgRuleWaveEntity
     * @author will
     * @date 2024/6/25 10:19
     */
    private CfgRuleWaveEntity getByWaveName(String name) {
        return lambdaQuery().eq(CfgRuleWaveEntity::getName, name).last("limit 1").one();
    }

    /**
     * 新增修改处理数据
     */
    private void handleData(List<LocalTime> executionTimeList, List<String> pickingCartTypeIdList, CfgRuleWaveEntity cfgRuleWaveEntity) {
        //数量验证
        if (MathUtil.compareTo(cfgRuleWaveEntity.getMinOrderQty(), cfgRuleWaveEntity.getMaxOrderQty()) > MathUtil.ZERO) {
            throw new ServiceException(ApiError.CFG_RULE_WAVE_ORDER_QTY_COMPARE);
        }
        if (MathUtil.compareTo(cfgRuleWaveEntity.getMinQty(), cfgRuleWaveEntity.getMaxQty()) > MathUtil.ZERO) {
            throw new ServiceException(ApiError.CFG_RULE_WAVE_QTY_COMPARE);
        }
        //波次名称重复验证
        CfgRuleWaveEntity ruleWaveEntity = getByWaveName(cfgRuleWaveEntity.getName());
        if (ObjectUtil.isNotEmpty(ruleWaveEntity) && !StrUtil.equals(cfgRuleWaveEntity.getId(), ruleWaveEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }
        //拣货车类型
        cfgRuleWaveEntity.setPickingCartTypeJson(JSONUtil.toJsonStr(pickingCartTypeIdList));

        //自动执行
        if (StrUtil.equals(cfgRuleWaveEntity.getExecutionType(), ExecutionTypeEnum.AUTO.getCode())) {
            if (CollectionUtil.isEmpty(executionTimeList)) {
                throw new ServiceException("自动执行时执行时间不能为空");
            }
            List<String> timeList = executionTimeList.stream().filter(obj -> ObjectUtil.isNotEmpty(obj))
                    .map(obj -> obj.format(DateTimeFormatter.ofPattern("HH:mm"))).collect(Collectors.toList());
            cfgRuleWaveEntity.setExecutionTimeJson(JSONUtil.toJsonStr(timeList));
        } else {
            cfgRuleWaveEntity.setExecutionTimeJson(JSONUtil.toJsonStr(new JSONArray()));
        }
    }

    /**
     * 分页数据处理
     */
    private void fillList(List<CfgRuleWaveDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }
        List<String> pickingCartTypeIdList = list.stream().flatMap(obj -> Stream.of(JSONUtil.parseArray(obj.getPickingCartTypeJson()).stream().toArray(String[]::new)))
                .collect(Collectors.toList());
        List<PickingCartTypeEntity> pickingCartTypeList = pickingCartTypeService.listByIds(pickingCartTypeIdList);
        for (CfgRuleWaveDTO.ListDTO listDTO : list) {
            String pickingCartTypeName = pickingCartTypeList.stream().filter(obj -> listDTO.getPickingCartTypeJson().contains(obj.getId())).map(PickingCartTypeEntity::getName).collect(Collectors.joining(","));
            listDTO.setPickingCartTypeName(pickingCartTypeName);
        }
    }
}
