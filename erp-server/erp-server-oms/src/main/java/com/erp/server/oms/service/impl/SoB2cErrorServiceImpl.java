package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.business.annotation.DataIdempotent;
import com.common.business.annotation.DistributeLocker;
import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.enums.OmsPlatformEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.ConditionElement;
import com.common.core.exception.ServiceException;
import com.common.core.server.rule.SpElServer;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.message.constant.RedisKeyConstant;
import com.erp.model.dmp.entity.CfgConditionEntity;
import com.erp.model.dmp.entity.RuleConditionEntity;
import com.erp.model.dmp.entity.RulePromptWordEntity;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.model.oms.dto.SoB2cErrorDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cErrorEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.enums.SoB2cErrorTypeEnum;
import com.erp.model.tms.dto.LogisticsSupplierDTO;
import com.erp.model.tms.dto.TransferDeclareDTO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.tms.feign.LogisticsAuthFeign;
import com.erp.rpc.wms.feign.SoB2cDeliveryFeign;
import com.erp.sdk.oms.amz.spapi.client.StringUtil;
import com.erp.server.oms.mapper.SoB2cErrorMapper;
import com.erp.server.oms.service.SoB2cErrorService;
import com.erp.server.oms.service.SoB2cLogisticsService;
import com.erp.server.oms.service.SoB2cService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_SO_B2C_ABNORMAL_POOLS;

/**
 * <p>
 * B2C销售订单异常表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-12-20
 */
@Slf4j
@Service
public class SoB2cErrorServiceImpl extends ServiceImpl<SoB2cErrorMapper, SoB2cErrorEntity> implements SoB2cErrorService {

    @Lazy
    @Resource
    private SoB2cService soB2cService;
    @Resource
    private SoB2cDeliveryFeign soB2cDeliveryFeign;

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private LogisticsAuthFeign logisticsAuthFeign;
    @Resource
    private SpElServer spElServer;

    @Transactional(rollbackFor = Exception.class)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Override
    @DistributeLocker(businessType = RedisKeyConstant.SO_B2C_DELIVERY_KEY,keyName = "addDTO.mainId",waiteTime = 20)
    public Boolean add(SoB2cErrorDTO.AddDTO addDTO) {
        //记录是否已存在
        SoB2cErrorEntity soB2cErrorEntity = this.getByMainIdAndType(addDTO.getMainId(),addDTO.getType());
        if (Objects.nonNull(soB2cErrorEntity)){
            soB2cErrorEntity.setParamJson(addDTO.getParamJson());
            soB2cErrorEntity.setMessage(addDTO.getMessage());
        }else {
            soB2cErrorEntity = new SoB2cErrorEntity();
            soB2cErrorEntity.setParamJson(addDTO.getParamJson());
            soB2cErrorEntity.setMessage(addDTO.getMessage());
            soB2cErrorEntity.setMainId(addDTO.getMainId());
            soB2cErrorEntity.setType(addDTO.getType());
            soB2cErrorEntity.setDetailId(StringUtils.isNotBlank(addDTO.getDetailId()) ? addDTO.getDetailId() : "");
        }
        boolean save = super.saveOrUpdate(soB2cErrorEntity);
        if(!save) {
            throw new ServiceException("B2C销售订单异常单保存失败");
        }
        soB2cService.addSignError(soB2cErrorEntity.getMainId(),soB2cErrorEntity.getType());
        return true;
    }




    /** 
     * @description
     * @param dto
     * @author Lambda
     * @return 
     * @create 2023-12-20 11:24
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(SoB2cErrorDTO.DeleteDTO dto) {
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteByMainIds(List<String> mainIds)  {
        if (CollectionUtils.isEmpty(mainIds)) {
            return Boolean.TRUE;
        }
        Boolean result = baseMapper.deleteByMainIds(mainIds);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class , propagation = Propagation.REQUIRES_NEW)
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000, propagation = io.seata.tm.api.transaction.Propagation.REQUIRES_NEW)
    public String generateErrorOrder(String mainId, String type, String message, String paramJson, String returnJson,String code) {
        //先删除所有同类型异常再添加
        this.removeErrorOrder(mainId, type);
        SoB2cErrorEntity soB2cErrorEntity = new SoB2cErrorEntity();
        soB2cErrorEntity.setMainId(mainId);
        soB2cErrorEntity.setType(type);
        soB2cErrorEntity.setMessage(message);
        soB2cErrorEntity.setParamJson(paramJson);
        soB2cErrorEntity.setReturnJson(returnJson);
        soB2cErrorEntity.setCode(code);
        this.save(soB2cErrorEntity);
        soB2cService.addSignError(mainId,type);
        return soB2cErrorEntity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeErrorOrder(String mainId, String type) {
        SoB2cErrorDTO.DeleteDTO dto=new SoB2cErrorDTO.DeleteDTO();
        dto.setType(type);
        dto.setMainId(mainId);
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeAllTypeErrorOrder(String mainId) {
        if(StringUtils.isBlank(mainId)){
            return false;
        }
        List<SoB2cErrorEntity> list = this.lambdaQuery().eq(SoB2cErrorEntity::getMainId, mainId).list();
        SoB2cErrorDTO.DeleteDTO dto=new SoB2cErrorDTO.DeleteDTO();
        dto.setMainId(mainId);
        Boolean result = baseMapper.deleteB2cError(dto);
        if(result){
            if(CollectionUtils.isNotEmpty(list)){
                list.stream()
                        .filter(v -> null != v.getType())
                        .forEach(v ->  soB2cService.removeSignError(mainId,v.getType()));
            }
        }
        return result;
    }

    /**
     * @description
     * @param dto
     * @author Lambda
     * @return
     * @create 2023-12-22 9:08
     */
    @Override
    public SoB2cErrorDTO.ViewDTO info(SoB2cErrorDTO.InfoDTO dto) {
        SoB2cErrorDTO.ViewDTO viewDTO=new SoB2cErrorDTO.ViewDTO();
        SoB2cErrorEntity entity = this.getByMainIdAndType(dto.getId(),dto.getType());
        if(Objects.nonNull(entity)){
             BeanMapperUtils.copy(entity,viewDTO);

            if(SoB2cErrorTypeEnum.needPrompt(entity.getType())){
                Map<String,Map<String, Object>> map = this.handleMatchJson(Collections.singletonList(entity.getId()));
                Map<String,RulePromptWordEntity> rulePromptWordEntityMap = this.getRulePromptWord(map);
                if(Objects.nonNull(rulePromptWordEntityMap.get(entity.getId()))){
                    RulePromptWordEntity rulePromptWordEntity = rulePromptWordEntityMap.get(entity.getId());
                    viewDTO.setFailureReason(CharSequenceUtil.isNotBlank(rulePromptWordEntity.getTips()) ? rulePromptWordEntity.getTips() : entity.getMessage());
                    viewDTO.setSolution(rulePromptWordEntity.getSolution());
                }
            }
        }
        return viewDTO;
    }



    @Override
    public SoB2cErrorEntity getByMainIdAndType(String mainId, String type) {
       return this.lambdaQuery().eq(SoB2cErrorEntity::getMainId, mainId).
                eq(SoB2cErrorEntity::getType,type).
               orderByDesc(SoB2cErrorEntity::getCreateTime).
               last("LIMIT 1").
               one();
    }

    @Override
    public List<SoB2cErrorEntity> getByMainIdsAndType(List<String> mainIds, String errorType) {
        return this.lambdaQuery().in(SoB2cErrorEntity::getMainId, mainIds)
                .eq(SoB2cErrorEntity::getType, errorType)
                .orderByDesc(SoB2cErrorEntity::getCreateTime).list();
    }

    @Override
    public void deleteByCodeAndType(String soCode, String type) {
         baseMapper.deleteByCodeAndType(soCode,type);
    }

    @Override
    public void deleteErrorByMainIds(SoB2cErrorDTO.BatchDeleteDTO batchDeleteDTO) {
        if (Objects.isNull(batchDeleteDTO) || CollectionUtils.isEmpty(batchDeleteDTO.getMainIds())){
            return;
        }
        Boolean result = baseMapper.batchDeleteB2cError(batchDeleteDTO);
        if(result){
            soB2cService.batchRemoveSignError(batchDeleteDTO.getMainIds(),batchDeleteDTO.getType());
        }
    }

    @Override
    public void batchAddSoB2cError(SoB2cErrorDTO.BatchAdd batchAdd) {
        if (Objects.isNull(batchAdd) || CollectionUtils.isEmpty(batchAdd.getMainIds()) || StringUtil.isEmpty(batchAdd.getType())){
            return;
        }
        batchAdd.getMainIds().forEach(mainId ->{
            SoB2cErrorEntity soB2cErrorEntity = this.getByMainIdAndType(mainId,batchAdd.getType());
            if (Objects.nonNull(soB2cErrorEntity)){
                soB2cErrorEntity.setParamJson(batchAdd.getParamJson());
                soB2cErrorEntity.setMessage(batchAdd.getMessage());
                this.updateById(soB2cErrorEntity);
            }else {
                soB2cErrorEntity = new SoB2cErrorEntity();
                soB2cErrorEntity.setParamJson(batchAdd.getParamJson());
                soB2cErrorEntity.setMessage(batchAdd.getMessage());
                soB2cErrorEntity.setMainId(mainId);
                soB2cErrorEntity.setType(batchAdd.getType());
                this.save(soB2cErrorEntity);
            }
            soB2cService.addSignError(mainId,batchAdd.getType());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DistributeLocker(keyName = "dto.mainId")
    public Boolean deleteDetail(SoB2cErrorDTO.DeleteDetailDTO dto) {
        Boolean result = baseMapper.deleteB2cErrorByDetailId(dto);
        if (result){
            // 检查历史明细是否存在
            Integer count = this.lambdaQuery()
                    .eq(SoB2cErrorEntity::getMainId, dto.getMainId())
                    .eq(SoB2cErrorEntity::getType, dto.getType())
                    .count();
            if(0 == count){
                soB2cService.removeSignError(dto.getMainId(),dto.getType());
            }
        }
        return true;
    }

    @Override
    public void deleteAndAddErrorBatch(SoB2cErrorDTO.AddAndDeleteDTO addAndDeleteDTO) {
        for (SoB2cErrorDTO.DeleteDTO deleteDTO : addAndDeleteDTO.getDeleteDTOList()) {
            this.delete(deleteDTO);
        }

        for (SoB2cErrorDTO.AddDTO addDTO : addAndDeleteDTO.getAddDTOList()) {
            this.add(addDTO);
        }

        //给订单赋值第三方平台发货单号
        List<TransferDeclareDTO.ShippingOrderDTO> shippingOrderDTOList = BeanMapperUtils.copyList(TransferDeclareDTO.ShippingOrderDTO.class, addAndDeleteDTO.getShippingOrderDTO());
        soB2cService.updateShippingOrderNo(shippingOrderDTOList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @DataIdempotent
    public BatchResultDTO retryFalseDelivery(String soB2cId) {
        // 直接重新触发标记发货
        // 调用第三方平台SDK标记发货(独立事务)
        SoB2cEntity mainEntity = soB2cService.getById(soB2cId);
        if (null == mainEntity){
            return BatchResultDTO.fail(soB2cId, soB2cId, "订单不存在");
        }
        String errorType = SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode();
        SoB2cErrorEntity soB2cError = this.getByMainIdAndType(soB2cId, errorType);
        if (null == soB2cError){
            if (errorType.equalsIgnoreCase(mainEntity.getSignOrderError())){
                soB2cService.removeSignError(soB2cId, errorType);
                return BatchResultDTO.success(soB2cId, mainEntity.getCode(), "移除头部异常信息成功");
            }
            return BatchResultDTO.fail(soB2cId, mainEntity.getCode(), "无异常信息");
        }
        // 移除已有异常
        removeErrorOrder(soB2cId, errorType);

        String soCode = mainEntity.getCode();
        PlatformShipOrderDTO platformShipOrderDTO = new PlatformShipOrderDTO();
        platformShipOrderDTO.setSoB2cId(soB2cId);
        platformShipOrderDTO.setSubmitPlatformUniqueKey(mainEntity.convertSubmitPlatformUniqueKey());
        platformShipOrderDTO.setDictPlatform(mainEntity.getDictPlatform());
        platformShipOrderDTO.setFalseDeliveryFlag(true);
        try {
            soB2cDeliveryFeign.shipOrder(platformShipOrderDTO);
            return BatchResultDTO.success(soB2cId, soCode, "重新标记发货成功");
        } catch (Exception e) {
            log.error("【标记发货重试】销售单【{}】 标记发货失败 >>>错误信息{}", soCode, ExceptionUtil.stacktraceToString(e));
            // 独立异常
            SoB2cErrorDTO.AddDTO addError = new SoB2cErrorDTO.AddDTO(
                    soB2cId,
                    SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode(),
                    soB2cId,
                    e.getMessage(),
                    ExceptionUtil.stacktraceToString(e),
                    ""
            );
            this.add(addError);
            return BatchResultDTO.fail(soB2cId, soCode, e.getMessage());
        }
    }

    @Override
    public List<SoB2cErrorDTO.TypeCountDTO> getTypeCountDTO() {
        return this.baseMapper.getTypeCountDTO();
    }

    @Override
    public List<SoB2cErrorDTO.TypeCountDTO> getB2CErrorReport(List<String> typeList) {
        List<SoB2cErrorDTO.TypeCountDTO> typeCountDTO = this.baseMapper.getB2CErrorReport(typeList);
        if (CollUtil.isEmpty(typeCountDTO)){
            return typeCountDTO;
        }
        typeCountDTO.forEach(e ->{
            e.setTypeName(SoB2cErrorTypeEnum.getName(e.getType()));
        });
        return typeCountDTO;
    }

    @Override
    @DistributeLocker(keyName = "dto.mainId")
    public Boolean deleteAll(SoB2cErrorDTO.DeleteDetailDTO dto) {
        List<SoB2cErrorEntity> list = this.lambdaQuery().eq(SoB2cErrorEntity::getMainId, dto.getMainId()).eq(SoB2cErrorEntity::getType, dto.getType()).list();
        if (CollUtil.isEmpty(list)){
            return Boolean.TRUE;
        }
        List<String> errorIds = list.stream()
                .filter(e -> dto.getDetailIdList().contains(e.getDetailId()) && !Objects.equals(e.getDetailId(), CharSequenceUtil.EMPTY))
                .map(SoB2cErrorEntity::getId).distinct().collect(Collectors.toList());
        boolean result = this.removeByIds(errorIds);
        if (result && errorIds.size() == list.size()){
            soB2cService.removeSignError(dto.getMainId(),dto.getType());
        }
        return Boolean.TRUE;
    }

    @Override
    public Map<String,Map<String, Object>> handleMatchJson(List<String> soErrorIds) {
        if(CollectionUtils.isEmpty(soErrorIds)){
            return new HashMap<>();
        }
        List<SoB2cErrorEntity> list = this.listByIds(soErrorIds);
        if(CollectionUtils.isEmpty(list)){
            return new HashMap<>();
        }
        List<String> soIds = list.stream().map(SoB2cErrorEntity::getMainId).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList =soB2cService.listByIds(soIds);
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            return new HashMap<>();
        }
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(soIds);
        List<String> channelIds = soB2cLogisticsEntityList.stream().map(SoB2cLogisticsEntity::getLogisticsChannelId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsSupplierDTO.AuthChannelViewDTO> listAuthChannelViewList = logisticsAuthFeign.listAuthChannelView(channelIds);
        Map<String,Map<String, Object>> resultMap = new HashMap<>();
        list.forEach(v->{
            Map<String, Object> map = new HashMap<>();
            map.put("errorCode",v.getCode());
            map.put("errorMsg",v.getMessage());
            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e->Objects.equals(e.getId(),v.getMainId())).findFirst().orElse(null);
            if(Objects.nonNull(soB2cEntity)){
                map.put("salePlatform",soB2cEntity.getDictPlatform());
            }
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(e->Objects.equals(e.getMainId(),v.getMainId())).findFirst().orElse(null);
            if(Objects.nonNull(soB2cLogisticsEntity)){
                LogisticsSupplierDTO.AuthChannelViewDTO authChannelViewDTO = listAuthChannelViewList.stream().filter(e->Objects.equals(e.getChannelId(),soB2cLogisticsEntity.getLogisticsChannelId())).findFirst().orElse(null);
                if(Objects.nonNull(authChannelViewDTO)){
                    map.put("logisticsSupplier",authChannelViewDTO.getMainId());
                    map.put("logisticsPlatform",authChannelViewDTO.getLogisticsPlatform());
                    OmsPlatformEnum omsPlatformEnum = OmsPlatformEnum.getByCode(authChannelViewDTO.getLogisticsPlatform());
                    if(Objects.nonNull(omsPlatformEnum)){
                        map.put("thirdWarehouse",omsPlatformEnum.getCode());
                    }
                }
            }
            //深拷贝map
            Map<String,Object> detailMap = new HashMap<>(map);
            map.put("detailList", Collections.singletonList(detailMap));
            resultMap.put(v.getId(),map);
        });
        return resultMap;
    }

    @Override
    public Map<String,RulePromptWordEntity> getRulePromptWord(Map<String,Map<String, Object>> map) {
        if(Objects.isNull(map)){
            return new HashMap<>();
        }

        List<RulePromptWordEntity> ruleOrderApprovalList = FeignQuery.list(RulePromptWordEntity.class);
        ruleOrderApprovalList = ruleOrderApprovalList.stream().filter(v->!v.getDisabled()).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(ruleOrderApprovalList)){
            return new HashMap<>();
        }
        //根据index排序
        ruleOrderApprovalList = ruleOrderApprovalList.stream().sorted(Comparator.comparing(RulePromptWordEntity::getIndex)).collect(Collectors.toList());
        List<String> ruleIdList = ruleOrderApprovalList.stream().map(RulePromptWordEntity::getId).collect(Collectors.toList());
        //规则条件
        List<RuleConditionEntity> allRuleConditionList = this.listDbRuleIds(ruleIdList);
        if(CollectionUtils.isEmpty(allRuleConditionList)){
            return new HashMap<>();
        }
        Map<String,RulePromptWordEntity> resultMap = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> entry : map.entrySet()) {
            for (RulePromptWordEntity item : ruleOrderApprovalList) {
                String ruleId = item.getId();
                List<RuleConditionEntity> ruleConditionList = allRuleConditionList.stream().
                        filter(r -> r.getRuleId().equals(ruleId)).
                        sorted(Comparator.comparing(RuleConditionEntity::getIndex)).collect(Collectors.toList());
                List<ConditionElement> conditionElementList = BeanMapper.copyList(ruleConditionList, ConditionElement.class);
                //获取到表达式
                Boolean matchResult = spElServer.matchExpressionByConditionList(conditionElementList, entry.getValue(),"");
                if (matchResult) {
                    resultMap.put(entry.getKey(), item);
                    break;
                }
            }
        }
        return resultMap;
    }

    @Override
    public Boolean exportErrorPools() {
        downloadTaskFeign.saveDownloadTask("B2C异常销售订单错误池", EXPORT_OMS_SO_B2C_ABNORMAL_POOLS.getCode(),new SoB2cAbnormalDTO.PagingParamDTO());
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<SoB2cAbnormalDTO.PoolsDTO> exportSoB2CAbnormalPools(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> dto) {
        Page<SoB2cAbnormalDTO.PoolsDTO> page = this.baseMapper.exportSoB2CAbnormalPools(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return new PagingVO<>(page);
        }
        List<String> ids = page.getRecords().stream().map(SoB2cAbnormalDTO.PoolsDTO::getErrorIds).collect(Collectors.toList());
        Map<String,Map<String, Object>> map = this.handleMatchJson(ids);
        Map<String,RulePromptWordEntity> rulePromptWordEntityMap = this.getRulePromptWord(map);
        page.getRecords().forEach(v->{
            v.setModules(SoB2cErrorTypeEnum.getName(v.getType()));
            if(Objects.nonNull(rulePromptWordEntityMap.get(v.getErrorIds()))){
                RulePromptWordEntity rulePromptWordEntity = rulePromptWordEntityMap.get(v.getErrorIds());
                v.setTip(CharSequenceUtil.isNotBlank(rulePromptWordEntity.getTips()) ? rulePromptWordEntity.getTips() : v.getMessage());
                v.setNeedPrompt("是");
            }else{
                v.setNeedPrompt("否");
            }
            Map<String, Object> objectMap = map.get(v.getErrorIds());
            if(Objects.nonNull(objectMap)){
                Object logisticsPlatform = objectMap.get("logisticsPlatform");
                if(Objects.nonNull(logisticsPlatform)){
                    v.setSourceSystem(LogisticsPlatformEnum.getNameByCode(logisticsPlatform.toString()));
                }
            }

            if(v.getType().equals(SoB2cErrorTypeEnum.SIGN_DELIVERY.getCode())){
                v.setSourceSystem(PlatformDictEnum.getNameByCode(v.getDictPlatform()));
            }
            if(v.getType().equals(SoB2cErrorTypeEnum.ORDER_FORECAST.getCode())){
                v.setSourceSystem("保宏");
            }
        });

        return new PagingVO<>(page);
    }

    @Override
    public List<SoB2cErrorEntity> listSoB2cErrorByMainIds(List<String> errorSoIds) {
        if (CollectionUtils.isEmpty(errorSoIds)) {
            return Collections.emptyList();
        }
        List<SoB2cErrorEntity> soB2cErrorEntities = this.lambdaQuery()
                .in(SoB2cErrorEntity::getMainId, errorSoIds)
                .orderByDesc(SoB2cErrorEntity::getCreateTime)
                .list();
        return soB2cErrorEntities;
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(SoB2cErrorEntity soB2cErrorEntity) {
    // TODO 验证数据 & 数据赋值
    }

    public List<RuleConditionEntity> listDbRuleIds(List<String> ruleIdList) {
        if (CollectionUtils.isEmpty(ruleIdList)) {
            return Collections.emptyList();
        }
        List<RuleConditionEntity> allRuleConditionList = FeignQuery.create(RuleConditionEntity.class).in(RuleConditionEntity::getRuleId,ruleIdList).list();
        if (CollectionUtils.isEmpty(allRuleConditionList)) {
            return Collections.emptyList();
        }
        //根据index排序
        allRuleConditionList.sort(Comparator.comparing(RuleConditionEntity::getIndex));
        List<String> fieldList = allRuleConditionList.stream().map(RuleConditionEntity::getField).distinct().collect(Collectors.toList());
        //配置的字段
        List<CfgConditionEntity> cfgConditionList = FeignQuery.create(CfgConditionEntity.class).in(CfgConditionEntity::getConditionField,fieldList).list();
        for (RuleConditionEntity item : allRuleConditionList) {
            String fieldFlag = item.getField();
            String valueType = cfgConditionList.stream().filter(c -> c.getConditionField().equals(fieldFlag)).
                    findFirst().map(CfgConditionEntity::getValueType).orElse("String");
            item.setValueType(valueType);
        }
        return allRuleConditionList;
    }


}
