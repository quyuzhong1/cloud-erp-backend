package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.enums.PlatformDictEnum;
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
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.oms.enums.SoB2cDataTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.wms.dto.CfgRuleWaveDTO;
import com.erp.model.wms.dto.CfgRuleWaveRecordDTO;
import com.erp.model.wms.dto.WarehouseLocationReplenishDTO;
import com.erp.model.wms.dto.WaveListDTO;
import com.erp.model.wms.dto.pickingstrategy.CfgRuleConditionDTO;
import com.erp.model.wms.entity.*;
import com.erp.model.wms.enums.*;
import com.erp.rpc.oms.feign.ShopInfoFeign;
import com.erp.rpc.oms.feign.SoB2cFeign;
import com.erp.server.wms.mapper.CfgRuleWaveMapper;
import com.erp.server.wms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.util.Pair;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
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
    public static final String DELIVERY_WAREHOUSE_ID = "deliveryWarehouseId";
    public static final String SKU_NO = "skuNo";
    public static final String CREATE_TIME = "createTime";
    public static final String DELIVERY_CREATE_TIME = "deliveryCreateTime";
    public static final String HH_MM = "HH:mm";
    public static final String DICT_PLATFORM = "dictPlatform";
    public static final String SHOP_ID = "shopId";
    public static final String LOGISTICS_SUPPLIER_ID = "logisticsSupplierId";
    public static final String LOGISTICS_CHANNEL_ID = "logisticsChannelId";
    public static final String COUNTRY = "country";
    public static final String LENGTH = "length";
    public static final String WIDTH = "width";
    public static final String HEIGHT = "height";
    public static final String WEIGHT = "weight";
    public static final String PICKING_TYPE = "pickingType";
    public static final String UN_FIND_RULE_WAVE = "未找到波次规则数据";
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private CfgRuleConditionService cfgRuleConditionService;

    @Resource
    private SoB2cDeliveryService soB2cDeliveryService;

    @Resource
    private SoB2cDeliveryDetailService soB2cDeliveryDetailService;

    @Resource
    private SoB2cFeign soB2cFeign;

    @Resource
    private SpElServer spElServer;

    @Resource
    private WaveListService waveListService;
    @Resource
    private PickingCartTypeService pickingCartTypeService;

    @Resource
    private CfgRuleWaveRecordService cfgRuleWaveRecordService;
    @Resource
    private WarehouseLocationReplenishService warehouseLocationReplenishService;

    @Resource
    @Lazy
    private CfgRuleWaveService cfgRuleWaveService;
    @Resource
    private ShopInfoFeign shopInfoFeign;


    @GlobalTransactional(rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(CfgRuleWaveDTO.AddDTO addDTO) {
        CfgRuleWaveEntity cfgRuleWaveEntity = new CfgRuleWaveEntity();
        BeanMapperUtils.copy(addDTO, cfgRuleWaveEntity);

        long deliveryWarehouseCount = addDTO.getConditionList().stream().filter(obj -> CharSequenceUtil.equals(obj.getField(), DELIVERY_WAREHOUSE_ID)).count();
        if (deliveryWarehouseCount != MathUtil.ONE) {
            throw new ServiceException("规则条件【订单-发货仓库】必须设置且只能存在一条");
        }

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
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "波次规则", cfgRuleWaveEntity.getId());
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
        if (Objects.isNull(old)){
            throw new ServiceException(ApiError.NOT_EXIST_BILL, "波次规则");
        }
        CfgRuleWaveEntity cfgRuleWaveEntity = BeanMapperUtils.map(CfgRuleWaveEntity.class, updateDTO);

        long deliveryWarehouseCount = updateDTO.getConditionList().stream().filter(obj -> CharSequenceUtil.equals(obj.getField(), DELIVERY_WAREHOUSE_ID)).count();
        if (deliveryWarehouseCount != MathUtil.ONE) {
            throw new ServiceException("规则条件【订单-发货仓库】必须设置且只能存在一条");
        }

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
        handlePickingCartTypeJsonName(old);
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), cfgRuleWaveEntity.getId(), "波次规则");
        operateLogService.addModuleOperateLogByObj(old, cfgRuleWaveEntity, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), cfgRuleWaveEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgRuleWaveDTO.ListDTO> paging(PagingDTO<CfgRuleWaveDTO.PagingParamDTO> dto) {
        CfgRuleWaveDTO.PagingParamDTO params = dto.getParams();
        Page<CfgRuleWaveDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgRuleWaveDTO.ListDTO> pageData = baseMapper.paging(query, params);
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(UN_FIND_RULE_WAVE));

        String disabledName = disabled ? "禁用" : "启用";
        if (disabled.equals(entity.getDisabled())) {
            throw new ServiceException(CharSequenceUtil.format("波次规则已【{}】，不支持再次【{}】", disabledName, disabledName));
        }
        lambdaUpdate().eq(CfgRuleWaveEntity::getId, id)
                .set(CfgRuleWaveEntity::getDisabled, disabled)
                .update(new CfgRuleWaveEntity());
        String msg = CharSequenceUtil.format("【{}】波次规则【{}】", disabledName, entity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.CFG_RULE_WAVE.getCode(), id, CharSequenceUtil.format("{}操作", disabledName));
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.UPDATE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(UN_FIND_RULE_WAVE));

        if (!entity.getDisabled()) {
            throw new ServiceException("仅禁用规则允许删除");
        }

        // 删除主单数据
        log.info("删除 开始删除波次规则主单数据，id：【{}】", id);
        super.removeById(id);

        //删除规则
        cfgRuleConditionService.removeByRuleIds(Collections.singletonList(id));
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public CfgRuleWaveDTO.ViewDTO view(String id) {
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(UN_FIND_RULE_WAVE));
        CfgRuleWaveDTO.ViewDTO view = BeanMapperUtils.map(CfgRuleWaveDTO.ViewDTO.class, entity);

        //执行时间
        if (ObjectUtil.isNotEmpty(entity.getExecutionTimeJson())) {
            List<LocalTime> executionTimeList = JSONUtil.parseArray(entity.getExecutionTimeJson()).stream().filter(ObjectUtil::isNotEmpty)
                    .map(obj -> LocalTime.parse(obj.toString(), DateTimeFormatter.ofPattern(HH_MM))).collect(Collectors.toList());
            view.setExecutionTimeList(executionTimeList);
        }

        String pickingCartTypeJson = entity.getPickingCartTypeJson();
        List<String> pickingCartTypeIdList = Arrays.stream(JSONUtil.parseArray(pickingCartTypeJson).toArray(new String[0])).collect(Collectors.toList());
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
        CfgRuleWaveEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException(UN_FIND_RULE_WAVE));

        if (entity.getDisabled()) {
            throw new ServiceException("规则已禁用不支持执行");
        }

        // 查询所有规则对应的规则条件
        List<CfgRuleConditionDTO.ConditionElementDTO> conditionList = cfgRuleConditionService.listByRuleIds(Collections.singletonList(entity.getId()),RuleTypeEnum.CFG_RULE_WAVE.getCode());

        //查询所有待处理的发货单进行生成波次
        List<SoB2cDeliveryEntity> soB2cDeliveryList = soB2cDeliveryService.listWaitHandle();
        if (CollUtil.isEmpty(soB2cDeliveryList)) {
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
            //销售订单
            SoB2cEntity soB2cEntity = soB2cDataDTO.getList().stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soB2cDeliveryEntity.getSourceId())).findFirst().orElse(null);
            if (Objects.isNull(soB2cEntity)) {
                log.error("发货单【{}】未找到销售订单数据", soB2cDeliveryEntity.getCode());
                continue;
            }
            if(ObjectUtil.isNotEmpty(soB2cEntity.getIsIntercept()) &&  soB2cEntity.getIsIntercept()) {
                log.error("发货单【{}】未找到销售订单已拦截不支持生成波次", soB2cDeliveryEntity.getCode());
                continue;
            }

            //发货单明细数据
            List<SoB2cDeliveryDetailEntity> deliveryDetailList = soB2cDeliveryDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), soB2cDeliveryEntity.getId())).collect(Collectors.toList());
            if (CollUtil.isEmpty(deliveryDetailList)) {
                log.error("发货单【{}】未找到发货明细数据", soB2cDeliveryEntity.getCode());
                continue;
            }
            //渠道数据
            LogisticsChannelEntity channelEntity = list.stream().filter(obj -> CharSequenceUtil.equals(soB2cDeliveryEntity.getLogisticsChannelId(), obj.getId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(channelEntity)) {
                log.error("发货单【{}】未找到渠道数据", soB2cDeliveryEntity.getCode());
                continue;
            }

            Map<String, Object> map = handleRuleData(soB2cDataDTO, channelEntity, soB2cDeliveryEntity, deliveryDetailList);
            List<ConditionElement> conditionElementList = BeanMapper.copyList(conditionList, ConditionElement.class);
            //获取到表达式,判断表达式是否匹配
            Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, map,"");
            if (matchResult) {
                compliantList.add(soB2cDeliveryEntity);
            }
        }
        if (CollUtil.isEmpty(compliantList)) {
            log.info("未发现需要新增的波次列表数据");
            return Boolean.TRUE;
        }
        //判断是否是同类波次
        if (CharSequenceUtil.equals(entity.getWaveType(),PickingWaveTypeEnum.SAME_WAVE.getCode())) {
            //同类波次分组
            Map<Object, List<SoB2cDeliveryEntity>> sameWaveMap = groupSameWave(compliantList, soB2cDeliveryDetailList);
            sameWaveMap.entrySet().stream().forEach(obj -> generatePickingWave(obj.getValue(), soB2cDeliveryDetailList, entity));
        } else {
            //生成拣货波次列表数据
            generatePickingWave(compliantList, soB2cDeliveryDetailList, entity);
        }

        return Boolean.TRUE;
    }

    /**
     * 同类波次处理
     * @author will
     * @date 2024/7/19 11:57
     * @param compliantList
     * @param allDetailList
     * @return Map<Object,List<SoB2cDeliveryEntity>>
     */
    private Map<Object,List<SoB2cDeliveryEntity>> groupSameWave (List<SoB2cDeliveryEntity> compliantList,List<SoB2cDeliveryDetailEntity> allDetailList) {
        //添加波次
        for (SoB2cDeliveryEntity deliveryEntity :compliantList) {
            TreeMap<String, Integer> sameMap = new TreeMap<>();
            //发货明细
            List<SoB2cDeliveryDetailEntity> detailList = allDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), deliveryEntity.getId()))
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(detailList)) {
                log.error("发货单【{}】未找到明细数据",deliveryEntity.getCode());
                continue;
            }
            //添加同类波次Map
            handleSameMap(detailList,sameMap);
            String sameWaveStr = JSONUtil.toJsonStr(sameMap);
            deliveryEntity.setSameWaveStr(sameWaveStr);
        }
        return compliantList.stream().collect(Collectors.groupingBy(SoB2cDeliveryEntity::getSameWaveStr));
    }

    @Override
    public void autoExecuteRule(String time) {
        if (CharSequenceUtil.isBlank(time)) {
            //当前时间
            time = LocalTime.now().format(DateTimeFormatter.ofPattern(HH_MM));
        }
        List<CfgRuleWaveEntity> cfgRuleWaveList = baseMapper.listRuleWaveByTime(time);
        if (CollUtil.isEmpty(cfgRuleWaveList)) {
            log.info("时间【{}】未找到符合条件的波次规则");
            return;
        }
        for (CfgRuleWaveEntity waveEntity : cfgRuleWaveList) {
            try {
                //执行规则
                cfgRuleWaveService.executeRule(waveEntity.getId());
            } catch (Exception e) {
                CfgRuleWaveRecordDTO.AddDTO addDTO = new CfgRuleWaveRecordDTO.AddDTO();
                addDTO.setRuleWaveId(waveEntity.getId());
                addDTO.setExecutionTime(LocalTime.parse(time,DateTimeFormatter.ofPattern(HH_MM)));
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
        if (CollUtil.isEmpty(compliantList)) {
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
        Integer orderQty = MathUtil.ONE;

        WaveListDTO.AddDTO addDTO = new WaveListDTO.AddDTO();
        addDTO.setWaveType(entity.getWaveType());
        addDTO.setPickingType(entity.getPickingType());
        addDTO.setName(entity.getName());
        //拣货车类型
        List<String> pickingCartTypeIdList = Arrays.stream(JSONUtil.parseArray(entity.getPickingCartTypeJson()).stream().toArray(String[]::new)).collect(Collectors.toList());
        addDTO.setPickCartTypeIdList(pickingCartTypeIdList);
        //需要更新发货单的异常状态
        List<String> updateDeliveryList = new ArrayList<>();
        //波次中的发货单
        List<String> deliveryIdList = new ArrayList<>();
        //需要新增的波次数据
        List<WaveListDTO.AddDTO> resultList = new ArrayList<>();

        for (SoB2cDeliveryEntity deliveryEntity : sortedList) {

            //发货明细商品数量合计
            Integer detailTotalQty = allDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), deliveryEntity.getId())).map(SoB2cDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            if (MathUtil.compareTo(entity.getMaxQty(),MathUtil.ZERO) != MathUtil.ZERO && MathUtil.compareTo(detailTotalQty,entity.getMaxQty()) > MathUtil.ZERO) {
                log.warn("发货单【{}】下商品数量【{}】大于波次规则商品数量【{}】",deliveryEntity.getCode(),detailTotalQty,entity.getMaxQty());
                continue;
            }

            //发货明细
            List<SoB2cDeliveryDetailEntity> detailList = allDetailList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), deliveryEntity.getId()))
                    .collect(Collectors.toList());
            if (CollUtil.isEmpty(detailList)) {
                log.error("发货单【{}】未找到明细数据", deliveryEntity.getCode());
                continue;
            }
            List<String> skus = soB2cDeliveryService.generatePickingDetail(deliveryEntity, detailList,entity.getWaveType());
            if (CollUtil.isNotEmpty(skus)) {
                //生成缺货补货数据
                generateReplenish(detailList, deliveryEntity, skus);
                //添加波次生成的缺货异常
                updateDeliveryList.add(deliveryEntity.getId());
                continue;
            }
            //商品总数超出最大数量后另起波次,或者发货单数量超过最大单数后另起波次
            if ((MathUtil.compareTo(entity.getMaxQty(),MathUtil.ZERO) != MathUtil.ZERO && detailTotalQty + totalQty > entity.getMaxQty())
                    || orderQty > entity.getMaxOrderQty()) {

                //原波次数量和单数必须大于等于最小数量
                if (((MathUtil.compareTo(entity.getMinQty(),MathUtil.ZERO) != MathUtil.ZERO && totalQty >= entity.getMinQty()) || MathUtil.compareTo(entity.getMinQty(),MathUtil.ZERO) == MathUtil.ZERO)
                        &&  orderQty > entity.getMinOrderQty()) {
                    addDTO.setDeliveryIdList(deliveryIdList);
                    resultList.add(addDTO);
                }
                //清空合计数据
                totalQty = MathUtil.ZERO;
                orderQty = MathUtil.ONE;
                deliveryIdList = new ArrayList<>();
                addDTO = new WaveListDTO.AddDTO();
                addDTO.setWaveType(entity.getWaveType());
                addDTO.setPickingType(entity.getPickingType());
                addDTO.setName(entity.getName());
                addDTO.setPickCartTypeIdList(pickingCartTypeIdList);
            }
            //添加发货单
            deliveryIdList.add(deliveryEntity.getId());

           //发货单商品数量
            totalQty = detailTotalQty + totalQty;

            //发货单订单数量
            orderQty++;

        }
        //添加波次生成的缺货异常
        if (CollUtil.isNotEmpty(updateDeliveryList)) {
            soB2cDeliveryService.updateAbnormal(updateDeliveryList, AbnormalCauseEnum.GENERATION_WAVE);
        }

        //添加最后一个波次
        if (CollUtil.isNotEmpty(deliveryIdList) && deliveryIdList.size() >= entity.getMinOrderQty() && MathUtil.compareTo(totalQty,entity.getMinQty()) >= MathUtil.ZERO) {
            addDTO.setDeliveryIdList(deliveryIdList);
            resultList.add(addDTO);
        } else {
            if (CollUtil.isNotEmpty(deliveryIdList)) {
                //回滚库存
                soB2cDeliveryService.rollbackPickingInventory(deliveryIdList);
            }
        }
        if (CollUtil.isEmpty(resultList)) {
            return;
        }

        for (WaveListDTO.AddDTO waveAddDTO : resultList) {
            BaseResultDTO.AddDTO add = waveListService.add(waveAddDTO);
            //更新发货单状态
            soB2cDeliveryService.updateDeliveryStatus(waveAddDTO.getDeliveryIdList(),SoB2cDeliveryStatusEnum.GENERATE_WAVE.getStatus());
            //发货单日志
            List<Pair<String, String>> pairList = waveAddDTO.getDeliveryIdList().stream().map(obj -> new Pair<>(obj, "")).collect(Collectors.toList());
            operateLogService.batchAddModuleOperateLog(CharSequenceUtil.format("自动生成波次成功，波次号【{}】",add.getCode()), ModuleTypeEnum.DELIVERY_ORDER.getCode(), pairList, "自动生成波次");
        }
    }

    /**
     *  添加同类波次
     * @author will
     * @date 2024/7/12 14:24
     * @param detailList
     * @param sameMap
     */
    private void handleSameMap ( List<SoB2cDeliveryDetailEntity> detailList, TreeMap<String, Integer> sameMap) {
        if (CollUtil.isEmpty(detailList) || CollUtil.isNotEmpty(sameMap)) {
            return;
        }
        //标记同类波次
        Map<String, List<SoB2cDeliveryDetailEntity>> map = detailList.stream().collect(Collectors.groupingBy(SoB2cDeliveryDetailEntity::getSkuId));
        for (Map.Entry<String, List<SoB2cDeliveryDetailEntity>> entry : map.entrySet()) {
            String key = entry.getKey();
            //添加map数据
            Integer totalDeliveryQty = entry.getValue().stream().map(SoB2cDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            sameMap.put(key,totalDeliveryQty);
        }
    }

    /**
     * 生成缺货补货数据
     *
     * @param detailList
     * @param deliveryEntity
     * @param skus
     * @author will
     * @date 2024/7/5 16:53
     */
    private void generateReplenish (List<SoB2cDeliveryDetailEntity> detailList, SoB2cDeliveryEntity deliveryEntity, List<String> skus) {
        //根据sku、仓库合并生成数据
        Map<String, List<SoB2cDeliveryDetailEntity>> map = detailList.stream().filter(obj -> skus.contains(obj.getSkuNo())).collect(Collectors.groupingBy(obj -> obj.getSkuId().concat(obj.getWarehouseId())));
        for (Map.Entry<String, List<SoB2cDeliveryDetailEntity>> entry : map.entrySet()) {
            SoB2cDeliveryDetailEntity detailEntity = entry.getValue().get(0);

            WarehouseLocationReplenishDTO.AddDTO addReplenishDTO = new WarehouseLocationReplenishDTO.AddDTO();
            addReplenishDTO.setSkuId(detailEntity.getSkuId());
            addReplenishDTO.setSkuNo(detailEntity.getSkuNo());
            addReplenishDTO.setSourceId(deliveryEntity.getId());
            addReplenishDTO.setSourceCode(deliveryEntity.getCode());
            addReplenishDTO.setWarehouseId(detailEntity.getWarehouseId());
            addReplenishDTO.setSourceType(ReplenishTypeEnum.DELIVER_STOCK_OUT);
            //合计数量
            Integer qty = entry.getValue().stream().map(SoB2cDeliveryDetailEntity::getDeliveryQty).reduce(MathUtil.ZERO, Integer::sum);
            addReplenishDTO.setQty(qty);
            warehouseLocationReplenishService.add(addReplenishDTO);
        }
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
        SoB2cEntity soB2cEntity = CollUtil.isEmpty(list) ? null : list.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), soB2cDeliveryEntity.getSourceId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(soB2cEntity)) {
            throw new ServiceException(CharSequenceUtil.format("发货单【{}】未找到上游销售订单", soB2cDeliveryEntity.getCode()));
        }
        //销售订单物流信息
        List<SoB2cLogisticsEntity> logisticsList = soB2cDataDTO.getLogisticsList();
        SoB2cLogisticsEntity soB2cLogisticsEntity = CollUtil.isEmpty(logisticsList) ? null : logisticsList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), soB2cDeliveryEntity.getSourceId())).findFirst().orElse(null);
        if (ObjectUtil.isEmpty(soB2cLogisticsEntity)) {
            throw new ServiceException(CharSequenceUtil.format("发货单【{}】未找到上游销售订单物流信息", soB2cDeliveryEntity.getCode()));
        }
        //销售订单买家信息
        String country = "";
        if (PlatformDictEnum.TIK_TOK_FULLY.getCode().equals(soB2cDeliveryEntity.getDictPlatform())){
            String shopId = soB2cEntity.getShopId();
            if (CharSequenceUtil.isBlank(shopId)) {
                throw new ServiceException(CharSequenceUtil.format("发货单【{}】未找到上游销售订单店铺信息", soB2cDeliveryEntity.getCode()));
            }
            ShopInfoEntity shopInfoEntity = shopInfoFeign.getShopInfoById(shopId);
            if (Objects.isNull(shopInfoEntity)) {
                throw new ServiceException(CharSequenceUtil.format("发货单【{}】未找到上游销售订单店铺信息", soB2cDeliveryEntity.getCode()));
            }
            country = shopInfoEntity.getDictCountryCode();
        }else {
            List<SoB2cReceiverEntity> receiverList = soB2cDataDTO.getReceiverList();
            SoB2cReceiverEntity soB2cReceiverEntity = CollUtil.isEmpty(receiverList) ? null : receiverList.stream().filter(obj -> CharSequenceUtil.equals(obj.getMainId(), soB2cDeliveryEntity.getSourceId())).findFirst().orElse(null);
            if (ObjectUtil.isEmpty(soB2cReceiverEntity)) {
                throw new ServiceException(CharSequenceUtil.format("发货单【{}】未找到上游销售订单买家信息", soB2cDeliveryEntity.getCode()));
            }
            country =  soB2cReceiverEntity.getCountry();
        }

        List<Map<String, Object>> detailList = new ArrayList<>();
        for (SoB2cDeliveryDetailEntity detailEntity : deliveryDetailList) {
            Map<String, Object> detailMap = new HashMap<>();
            //仓库
            detailMap.put(DELIVERY_WAREHOUSE_ID, detailEntity.getWarehouseId());
            //平台
            detailMap.put(DICT_PLATFORM, soB2cDeliveryEntity.getDictPlatform());
            //店铺
            detailMap.put(SHOP_ID, soB2cDeliveryEntity.getShopId());
            //物流商
            detailMap.put(LOGISTICS_SUPPLIER_ID, channelEntity.getMainId());
            //物流渠道
            detailMap.put(LOGISTICS_CHANNEL_ID, channelEntity.getId());
            //国家
            detailMap.put(COUNTRY, country);
            //SKU
            detailMap.put(SKU_NO, detailEntity.getSkuNo());
            //包装尺寸长（cm）
            detailMap.put(LENGTH, Objects.nonNull(soB2cLogisticsEntity) ? soB2cLogisticsEntity.getLength() : CharSequenceUtil.EMPTY);
            //包装尺寸宽（cm）
            detailMap.put(WIDTH, Objects.nonNull(soB2cLogisticsEntity) ? soB2cLogisticsEntity.getWidth() : CharSequenceUtil.EMPTY);
            //包装尺寸高（cm）
            detailMap.put(HEIGHT, Objects.nonNull(soB2cLogisticsEntity) ? soB2cLogisticsEntity.getHeight() : CharSequenceUtil.EMPTY);
            //包装重量（g）
            detailMap.put(WEIGHT, Objects.nonNull(soB2cLogisticsEntity) ? soB2cLogisticsEntity.getWeight() : CharSequenceUtil.EMPTY);
            //订单创建时间
            detailMap.put(CREATE_TIME, Objects.nonNull(soB2cLogisticsEntity) ? soB2cLogisticsEntity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
            //发货单-拣货类型
            detailMap.put(PICKING_TYPE, soB2cDeliveryEntity.getPickingType());
            //发货单-创建时间
            detailMap.put(DELIVERY_CREATE_TIME, soB2cDeliveryEntity.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            detailList.add(detailMap);
        }
        map.put("detailList", detailList);


        Object deliveryWarehouseId = spElServer.getByField(DELIVERY_WAREHOUSE_ID, detailList);
        map.put(DELIVERY_WAREHOUSE_ID, deliveryWarehouseId);
        Object dictPlatform = spElServer.getByField(DICT_PLATFORM, detailList);
        map.put(DICT_PLATFORM, dictPlatform);
        Object shopId = spElServer.getByField(SHOP_ID, detailList);
        map.put(SHOP_ID, shopId);
        Object logisticsSupplierId = spElServer.getByField(LOGISTICS_SUPPLIER_ID, detailList);
        map.put(LOGISTICS_SUPPLIER_ID, logisticsSupplierId);
        Object logisticsChannelId = spElServer.getByField(LOGISTICS_CHANNEL_ID, detailList);
        map.put(LOGISTICS_CHANNEL_ID, logisticsChannelId);
        Object countryObj = spElServer.getByField(COUNTRY, detailList);
        map.put(COUNTRY, countryObj);
        Object skuNo = spElServer.getByField(SKU_NO, detailList);
        map.put(SKU_NO, skuNo);
        Object length = spElServer.getByField(LENGTH, detailList);
        map.put(LENGTH, length);
        Object width = spElServer.getByField(WIDTH, detailList);
        map.put(WIDTH, width);
        Object height = spElServer.getByField(HEIGHT, detailList);
        map.put(HEIGHT, height);
        Object weight = spElServer.getByField(WEIGHT, detailList);
        map.put(WEIGHT, weight);
        Object createTime = spElServer.getByField(CREATE_TIME, detailList);
        map.put(CREATE_TIME, createTime);
        Object pickingType = spElServer.getByField(PICKING_TYPE, detailList);
        map.put(PICKING_TYPE, pickingType);
        Object deliveryCreateTime = spElServer.getByField(DELIVERY_CREATE_TIME, detailList);
        map.put(DELIVERY_CREATE_TIME, deliveryCreateTime);
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
        if (MathUtil.compareTo(cfgRuleWaveEntity.getMinQty(),MathUtil.ZERO) != MathUtil.ZERO
                && MathUtil.compareTo(cfgRuleWaveEntity.getMaxQty(),MathUtil.ZERO) != MathUtil.ZERO
                && MathUtil.compareTo(cfgRuleWaveEntity.getMinQty(), cfgRuleWaveEntity.getMaxQty()) > MathUtil.ZERO) {
            throw new ServiceException(ApiError.CFG_RULE_WAVE_QTY_COMPARE);
        }
        //波次名称重复验证
        CfgRuleWaveEntity ruleWaveEntity = getByWaveName(cfgRuleWaveEntity.getName());
        if (ObjectUtil.isNotEmpty(ruleWaveEntity) && !CharSequenceUtil.equals(cfgRuleWaveEntity.getId(), ruleWaveEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_DUPLICATION_NAME);
        }
        //拣货车类型
        cfgRuleWaveEntity.setPickingCartTypeJson(JSONUtil.toJsonStr(pickingCartTypeIdList));
        handlePickingCartTypeJsonName(cfgRuleWaveEntity);
        //自动执行
        if (CharSequenceUtil.equals(cfgRuleWaveEntity.getExecutionType(), ExecutionTypeEnum.AUTO.getCode())) {
            if (CollUtil.isEmpty(executionTimeList)) {
                throw new ServiceException("自动执行时执行时间不能为空");
            }
            List<String> timeList = executionTimeList.stream().filter(ObjectUtil::isNotEmpty)
                    .map(obj -> obj.format(DateTimeFormatter.ofPattern(HH_MM))).collect(Collectors.toList());
            cfgRuleWaveEntity.setExecutionTimeJson(JSONUtil.toJsonStr(timeList));
        } else {
            cfgRuleWaveEntity.setExecutionTimeJson(JSONUtil.toJsonStr(new JSONArray()));
        }
        //清空数量
        if (ObjectUtil.isEmpty(cfgRuleWaveEntity.getMinQty())) {
            cfgRuleWaveEntity.setMinQty(MathUtil.ZERO);
        }
        if (ObjectUtil.isEmpty(cfgRuleWaveEntity.getMaxQty())) {
            cfgRuleWaveEntity.setMaxQty(MathUtil.ZERO);
        }
    }

    /**
     * 格式话拣货车名称
     * @author will
     * @date 2024/7/10 15:34
     * @param cfgRuleWaveEntity
     */
    private void handlePickingCartTypeJsonName (CfgRuleWaveEntity cfgRuleWaveEntity) {
        String pickingCartTypeJson = cfgRuleWaveEntity.getPickingCartTypeJson();
        List<String> pickingCartTypeIdList = Arrays.stream(JSONUtil.parseArray(pickingCartTypeJson).stream().toArray(String[]::new)).collect(Collectors.toList());
        List<PickingCartTypeEntity> pickingCartTypeList = pickingCartTypeService.listByIds(pickingCartTypeIdList);
        if (CollUtil.isEmpty(pickingCartTypeList)) {
            return;
        }
        List<String> nameList = pickingCartTypeList.stream().map(PickingCartTypeEntity::getName).collect(Collectors.toList());
        cfgRuleWaveEntity.setPickingCartTypeJsonName(JSONUtil.toJsonStr(nameList));
    }

    /**
     * 分页数据处理
     */
    private void fillList(List<CfgRuleWaveDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
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
