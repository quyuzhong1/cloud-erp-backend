package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.ListingInfoParamDTO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.dto.SkuMappingRuleDTO;
import com.erp.model.oms.entity.ListingInfoEntity;
import com.erp.model.oms.entity.SkuMappingEntity;
import com.erp.model.oms.entity.SkuMappingRuleEntity;
import com.erp.model.oms.enums.SkuMappingRuleEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.constant.OmsConstant;
import com.erp.server.oms.convert.SkuMappingRuleConverter;
import com.erp.server.oms.mapper.SkuMappingRuleMapper;
import com.erp.server.oms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * sku对照表匹配规则 服务实现类
 * </p>
 *
 * @author lrp
 * @since 2023-12-21
 */
@Slf4j
@Service
public class SkuMappingRuleServiceImpl extends SuperServiceImpl<SkuMappingRuleMapper, SkuMappingRuleEntity> implements SkuMappingRuleService {
    @Autowired
    private OperateLogService operateLogService;
    @Autowired
    private CommonService commonService;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ListingInfoService listingInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SkuMappingRuleDTO.AddDTO addDTO) {
        SkuMappingRuleEntity existEntity = this.getByRuleType(addDTO.getRuleType());
        if(Objects.nonNull(existEntity)){
            throw new ServiceException("相同规则类型只能新建一个");
        }
        SkuMappingRuleEntity skuMappingRuleEntity = new SkuMappingRuleEntity();
        BeanMapperUtils.copy(addDTO, skuMappingRuleEntity);
        handleSamePriority(skuMappingRuleEntity);
        // 数据处理
        handleData(addDTO,skuMappingRuleEntity);

        log.info("开始新增sku对照表匹配规则");
        boolean save = super.save(skuMappingRuleEntity);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】id为【{}】", commonService.getUserInfo().getUserName(), "sku对照表匹配规则" , skuMappingRuleEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), skuMappingRuleEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(skuMappingRuleEntity.getId(), skuMappingRuleEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(SkuMappingRuleDTO.UpdateDTO updateDTO) {
        SkuMappingRuleEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku对照表匹配规则"));
        if(!old.getRuleType().equals(updateDTO.getRuleType())){
            throw new ServiceException("不能更改规则类型");
        }
        SkuMappingRuleEntity skuMappingRuleEntity =  BeanMapperUtils.map(SkuMappingRuleEntity.class, updateDTO);
        handleSamePriority(skuMappingRuleEntity);
        // 数据处理
        handleData(updateDTO,skuMappingRuleEntity);
        log.info("编辑 开始修改sku对照表匹配规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(skuMappingRuleEntity);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录sku对照表匹配规则日志数据，id：【{}】", skuMappingRuleEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), skuMappingRuleEntity.getId(), "sku对照表匹配规则");
        SkuMappingRuleDTO.LogDTO oldView = this.buildLogDTO(old);
        skuMappingRuleEntity.setDisabled(old.getDisabled());
        SkuMappingRuleDTO.LogDTO newView = this.buildLogDTO(skuMappingRuleEntity);
        operateLogService.addModuleOperateLogByObj(oldView, newView, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), skuMappingRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SkuMappingRuleEntity> listOrderByPriority() {
        return lambdaQuery().orderByAsc(SkuMappingRuleEntity::getPriority).list();
    }

    @Override
    public Boolean enableOrDisable(SkuMappingRuleDTO.StatusDTO dto) {
        SkuMappingRuleEntity old = super.getById(dto.getId());
        SkuMappingRuleDTO.LogDTO oldView = this.buildLogDTO(old);
        oldView.setDisabled(old.getDisabled());
        Optional.of(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku对照表匹配规则"));
        old.setDisabled(dto.getDisabled());
        boolean save = super.updateById(old);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }
        SkuMappingRuleDTO.LogDTO newView = this.buildLogDTO(old);
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), old.getId(), "sku对照表匹配规则");
        operateLogService.addModuleOperateLogByObj(oldView, newView, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), old.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public SkuMappingRuleDTO.ViewDTO view(String id) {

        SkuMappingRuleEntity entity = super.getById(id);
        SkuMappingRuleDTO.ViewDTO viewDTO =  SkuMappingRuleConverter.INSTANCE.entityToViewDto(entity);
        JSONObject jsonObject = new JSONObject(entity.getRuleContent());
        SkuMappingRuleDTO.RuleDTO ruleDTO = JSONObject.parseObject(jsonObject.toJSONString(),new TypeReference<SkuMappingRuleDTO.RuleDTO>() {}.getType());
        viewDTO.setRuleDTO(ruleDTO);
        Map<String,Object> extendRuleMap = entity.getExtendRuleContent();
        if(MapUtils.isNotEmpty(extendRuleMap)){
            JSONObject extendJsonObject = new JSONObject(extendRuleMap);
            SkuMappingRuleDTO.ExtendRuleDTO extendRuleDto = JSONObject.parseObject(extendJsonObject.toJSONString(),new TypeReference<SkuMappingRuleDTO.ExtendRuleDTO>() {}.getType());
            viewDTO.setExtendRuleDTO(extendRuleDto);
        }
        return viewDTO;
    }

    private SkuMappingRuleDTO.LogDTO buildLogDTO(SkuMappingRuleEntity entity){
        SkuMappingRuleDTO.LogDTO logDTO =  SkuMappingRuleConverter.INSTANCE.entityToLogDTO(entity);
        JSONObject jsonObject = new JSONObject(entity.getRuleContent());
        SkuMappingRuleDTO.RuleDTO ruleDTO = JSONObject.parseObject(jsonObject.toJSONString(),new TypeReference<SkuMappingRuleDTO.RuleDTO>() {}.getType());
        logDTO.setValidStartingSymbolPosition(ruleDTO.getValidStartingSymbolPosition());
        logDTO.setValidEndSymbolPosition(ruleDTO.getValidEndSymbolPosition());
        logDTO.setRuleContentList(ruleDTO.getRuleContentList());
        Map<String,Object> extendRuleMap = entity.getExtendRuleContent();
        if(MapUtils.isNotEmpty(extendRuleMap)){
            JSONObject extendJsonObject = new JSONObject(extendRuleMap);
            SkuMappingRuleDTO.ExtendRuleDTO extendRuleDto = JSONObject.parseObject(extendJsonObject.toJSONString(),new TypeReference<SkuMappingRuleDTO.ExtendRuleDTO>() {}.getType());
            logDTO.setExtendRuleContentList(extendRuleDto.getExtendRuleContentList());
        }
        return logDTO;
    }

    @Override
    public List<String> getSkuRuleTest(SkuMappingRuleDTO.RuleTestDTO ruleTestDTO) {
        List<String> list = new ArrayList<>();
        String result = ruleTestDTO.getSkuNo();
        //先执行扩展规则
        //处理扩展规则
        if(StringUtils.isNotBlank(ruleTestDTO.getExtendRuleType())){
            //获取枚举
            SkuMappingRuleEnum.SkuMappingExtendRuleEnum skuMappingExtendRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.SkuMappingExtendRuleEnum.class, ruleTestDTO.getExtendRuleType());
            if(Objects.isNull(skuMappingExtendRuleEnum)){
                throw new ServiceException("获取扩展规则异常,"+ruleTestDTO.getExtendRuleType());
            }
            List<String> extendRegexList = skuMappingExtendRuleEnum.getRegexMethod().apply(ruleTestDTO.getExtendRuleDTO());
            for(String extendRegex : extendRegexList){
                result = skuMappingExtendRuleEnum.getHandleRegexMethod().apply(extendRegex,result);
            }
        }
        //获取枚举
        SkuMappingRuleEnum skuMappingRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.class, ruleTestDTO.getRuleType());
        if(Objects.isNull(skuMappingRuleEnum)){
            throw new ServiceException("获取规则异常,");
        }
        List<String> regexList = skuMappingRuleEnum.getRegexMethod().apply(ruleTestDTO.getRuleDTO());
        //匹配规则为空，返回原结果
        if(CollectionUtils.isEmpty(regexList)){
            list.add(result);
        }
        for(String regex : regexList){
            String regexResult = skuMappingRuleEnum.getHandleRegexMethod().apply(regex,result);
            list.add(regexResult);
        }
        return list;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(SkuMappingRuleDTO.CommonDTO commonDTO,SkuMappingRuleEntity skuMappingRuleEntity) {
        //处理规则详情
        Map<String, Object> ruleMap = BeanUtil.beanToMap(commonDTO.getRuleDTO());
        skuMappingRuleEntity.setRuleContent(ruleMap);
        //获取枚举
        SkuMappingRuleEnum skuMappingRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.class, commonDTO.getRuleType());
        if(Objects.isNull(skuMappingRuleEnum)){
            throw new ServiceException("获取规则异常,"+skuMappingRuleEntity.getRuleType());
        }
        List<String> regexList = skuMappingRuleEnum.getRegexMethod().apply(commonDTO.getRuleDTO());
        String regex
                = regexList.toString().substring(1, regexList.toString().length() - 1);
        skuMappingRuleEntity.setRuleRegex(regex);

        //处理扩展规则
        if(StringUtils.isNotBlank(skuMappingRuleEntity.getExtendRuleType())){
            Map<String, Object> extendMap = BeanUtil.beanToMap(commonDTO.getExtendRuleDTO());
            skuMappingRuleEntity.setExtendRuleContent(extendMap);
            //获取枚举
            SkuMappingRuleEnum.SkuMappingExtendRuleEnum skuMappingExtendRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.SkuMappingExtendRuleEnum.class, commonDTO.getExtendRuleType());
            if(Objects.isNull(skuMappingExtendRuleEnum)){
                throw new ServiceException("获取扩展规则异常,"+skuMappingRuleEntity.getExtendRuleType());
            }
            List<String> extendRegexList = skuMappingExtendRuleEnum.getRegexMethod().apply(commonDTO.getExtendRuleDTO());
            String extendRegex = extendRegexList.toString().substring(1, extendRegexList.toString().length() - 1);
            skuMappingRuleEntity.setExtendRuleRegex(extendRegex);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleSamePriority(SkuMappingRuleEntity skuMappingRuleEntity){
        List<SkuMappingRuleEntity> skuMappingRuleEntityList = this.listOrderByPriority();
        Collections.reverse(skuMappingRuleEntityList);
        SkuMappingRuleEntity old = skuMappingRuleEntityList.stream().filter(v->v.getId().equals(skuMappingRuleEntity.getId())).findFirst().orElse(null);
        // 优先级已全部创建完，不允许新增
        if(skuMappingRuleEntityList.size() == OmsConstant.SKU_MAPPING_RULE_SIZE){
            throw new ServiceException("规则已满，不允许新增");
        }
        SkuMappingRuleEntity originalEntity = skuMappingRuleEntityList.stream().filter(v->v.getPriority().equals(skuMappingRuleEntity.getPriority())).findFirst().orElse(null);
        //优先级有冲突
        if(Objects.nonNull(originalEntity)){
            if(Objects.nonNull(old)){
                originalEntity.setPriority(old.getPriority());
                this.updateById(originalEntity);
            }else{
                //判断低优先级能不能往下顺延，不能的话抛错
                List<SkuMappingRuleEntity> lowList = skuMappingRuleEntityList.stream().filter(v->v.getPriority()>=skuMappingRuleEntity.getPriority()).collect(Collectors.toList());
                if(lowList.size()>=OmsConstant.SKU_MAPPING_RULE_SIZE - skuMappingRuleEntity.getPriority()+1){
                    throw new ServiceException("低优先级规则不能往下顺延，请调整优先级");
                }
                List<SkuMappingRuleEntity> updateList = new ArrayList<>();
                for(int i = 0 ;i < lowList.size();i++){
                    SkuMappingRuleEntity nowEntity = lowList.get(i);
                    if(OmsConstant.SKU_MAPPING_RULE_SIZE.equals(nowEntity.getPriority())){
                        continue;
                    }
                    SkuMappingRuleEntity previousEntity = i == 0 ?null:lowList.get(i-1);
                    SkuMappingRuleEntity nextEntity = i +1 >= lowList.size() ?null:lowList.get(i+1);
                    //下个优先级与当前优先级相邻
                    if(Objects.isNull(previousEntity) || nowEntity.getPriority() +1 != previousEntity.getPriority()){
                        //上个优先级与当前优先级不相邻，则不需要修改
                        if(Objects.nonNull(nextEntity) && nextEntity.getPriority() != nowEntity.getPriority()-1){
                            continue;
                        }
                        nowEntity.setPriority(nowEntity.getPriority()+1);
                        updateList.add(nowEntity);
                    }
                }
                this.updateBatchById(updateList);
            }
        }
    }

    /**
     * 处理sku映射
     */
    @Override
    public void handleSkuMapping() {
        //查询未匹配的SKU
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setMatchResult(false);
        // 查询ListingInfo和skuMapping的关系
        List<ListingInfoWithSkuMappingDTO> noMatchList = skuMappingService.findListDto(paramDTO);
        if(CollectionUtils.isEmpty(noMatchList)){
            return;
        }
        // 查询plm产品
        List<SkuVO> skuVOList = plmTaskFeign.listApproveSku();
        if(CollectionUtils.isEmpty(skuVOList)){
            return;
        }
        Map<String,SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuNo,skuVO -> skuVO));
        List<SkuMappingRuleEntity> skuMappingRuleEntityList = this.listOrderByPriority();
        //过滤掉已禁用
        skuMappingRuleEntityList = skuMappingRuleEntityList.stream().filter(skuMappingRuleEntity -> !skuMappingRuleEntity.getDisabled()).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(skuMappingRuleEntityList)){
            return;
        }
        // 匹配sku
        List<SkuMappingEntity> updateSkuMappingList = new ArrayList<>();
        List<ListingInfoEntity> updateListingList = new ArrayList<>();
        for(ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO : noMatchList){
            String platformSkuNo = listingInfoWithSkuMappingDTO.getPlatformSkuNo();
            for(SkuMappingRuleEntity skuMappingRuleEntity : skuMappingRuleEntityList){
                String afterHandlePlatformSkuNo = platformSkuNo;
                //先执行扩展规则
                //处理扩展规则
                if(StringUtils.isNotBlank(skuMappingRuleEntity.getExtendRuleType())){
                    //获取枚举
                    SkuMappingRuleEnum.SkuMappingExtendRuleEnum skuMappingExtendRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.SkuMappingExtendRuleEnum.class, skuMappingRuleEntity.getExtendRuleType());
                    if(Objects.isNull(skuMappingExtendRuleEnum)){
                        throw new ServiceException("获取扩展规则异常,"+skuMappingRuleEntity.getExtendRuleType());
                    }
                    String entityExtendRuleRegex = skuMappingRuleEntity.getExtendRuleRegex();
                    String[] extendRegexList = entityExtendRuleRegex.split(", ");
                    for(String extendRegex : extendRegexList){
                        afterHandlePlatformSkuNo = skuMappingExtendRuleEnum.getHandleRegexMethod().apply(extendRegex,afterHandlePlatformSkuNo);
                    }
                }
                //获取枚举
                SkuMappingRuleEnum skuMappingRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.class, skuMappingRuleEntity.getRuleType());
                if(Objects.isNull(skuMappingRuleEnum)){
                    throw new ServiceException("获取规则异常,");
                }
                String ruleRegexArrStr = skuMappingRuleEntity.getRuleRegex();
                String[] ruleRegexList = ruleRegexArrStr.split(", ");
                for(String ruleRegex : ruleRegexList){
                    afterHandlePlatformSkuNo = skuMappingRuleEnum.getHandleRegexMethod().apply(ruleRegex,afterHandlePlatformSkuNo);
                }
                if(skuVOMap.containsKey(afterHandlePlatformSkuNo)){
                    SkuVO skuVO = skuVOMap.get(afterHandlePlatformSkuNo);
                    SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
                    skuMappingEntity.setId(listingInfoWithSkuMappingDTO.getTableId());
                    skuMappingEntity.setRuleId(skuMappingRuleEntity.getId());
                    skuMappingEntity.setProductSkuId(skuVO.getSkuId());
                    skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
                    skuMappingEntity.setProductName(skuVO.getSkuName());
                    updateSkuMappingList.add(skuMappingEntity);

                    ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
                    listingInfoEntity.setId(listingInfoWithSkuMappingDTO.getListingId());
                    listingInfoEntity.setMatchResult(true);
                    updateListingList.add(listingInfoEntity);
                    break;
                }
            }
        }
        if(CollectionUtils.isNotEmpty(updateSkuMappingList)){
            skuMappingService.updateBatchById(updateSkuMappingList,2000);
        }
        if(CollectionUtils.isNotEmpty(updateListingList)){
            listingInfoService.updateBatchById(updateListingList,2000);
        }
    }

    public SkuMappingRuleEntity getByRuleType(String ruleType) {
        LambdaQueryWrapper<SkuMappingRuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuMappingRuleEntity::getRuleType, ruleType);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}
