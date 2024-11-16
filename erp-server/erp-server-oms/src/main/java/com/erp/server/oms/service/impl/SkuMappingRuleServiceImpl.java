package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.erp.model.oms.enums.ListingMatchResultEnum;
import com.erp.model.plm.dto.BomDTO;
import com.erp.model.plm.enums.BomTypeEnum;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
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
import com.erp.server.oms.convert.SkuMappingConverter;
import com.erp.server.oms.convert.SkuMappingRuleConverter;
import com.erp.server.oms.mapper.SkuMappingRuleMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
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
    @Resource
    private OperateLogService operateLogService;

    @Resource
    private SkuMappingService skuMappingService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ListingInfoService listingInfoService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(SkuMappingRuleDTO.AddDTO addDTO) {
        SkuMappingRuleEntity skuMappingRuleEntity = new SkuMappingRuleEntity();
        BeanMapperUtils.copy(addDTO, skuMappingRuleEntity);
        // 数据处理
        handleData(addDTO,skuMappingRuleEntity);

        log.info("开始新增sku对照表匹配规则");
        boolean save = super.save(skuMappingRuleEntity);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }

        // 操作日志
        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "sku对照表匹配规则" , skuMappingRuleEntity.getId());
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
//        handleSamePriority(skuMappingRuleEntity);
        // 数据处理
        handleData(updateDTO,skuMappingRuleEntity);
        log.info("编辑 开始修改sku对照表匹配规则数据，id：【{}】", old.getId());
        boolean save = super.updateById(skuMappingRuleEntity);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录sku对照表匹配规则日志数据，id：【{}】", skuMappingRuleEntity.getId());
        String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), skuMappingRuleEntity.getId(), "sku对照表匹配规则");
        SkuMappingRuleDTO.LogDTO oldView = this.buildLogDTO(old);
        skuMappingRuleEntity.setDisabled(old.getDisabled());
        SkuMappingRuleDTO.LogDTO newView = this.buildLogDTO(skuMappingRuleEntity);
        operateLogService.addModuleOperateLogByObj(oldView, newView, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), skuMappingRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SkuMappingRuleEntity> listOrderByPriorityAndUpdateTime() {
        return lambdaQuery().orderByAsc(SkuMappingRuleEntity::getPriority).orderByDesc(SkuMappingRuleEntity::getUpdateTime).list();
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
        String msg =  CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), old.getId(), "sku对照表匹配规则");
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
        if(SkuMappingRuleEnum.MATCH_COMBINE.equals(skuMappingRuleEnum)){
            SkuMappingRuleDTO.RuleDTO ruleDTO = ruleTestDTO.getRuleDTO();
            if(CollectionUtils.isEmpty(ruleDTO.getRuleContentList())){
                throw new ServiceException("规则详情不能为空");
            }
            SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO = ruleDTO.getRuleContentList().get(0);
            if(StringUtils.isEmpty(ruleConditionsDTO.getChildCombineSplitSymbol()) || StringUtils.isEmpty(ruleConditionsDTO.getChildQtySplitSymbol())){
                throw new ServiceException("拆分符号不能为空");
            }
            List<String> splitList = this.splitWithoutDelimiter(result,ruleConditionsDTO.getChildCombineSplitSymbol());
            String desc = "";
            for (String childrenSku : splitList) {
                SkuMappingRuleDTO.SplitSkuDTO splitSkuDTO = this.splitByLastSymbol(childrenSku,ruleConditionsDTO.getChildQtySplitSymbol());
                if(StringUtils.isEmpty(desc)){
                    desc = splitSkuDTO.desc();
                }else {
                    desc = desc+";"+splitSkuDTO.desc();
                }
            }
            list.add(desc);
        }else{
            List<String> regexList = skuMappingRuleEnum.getRegexMethod().apply(ruleTestDTO.getRuleDTO());
            //匹配规则为空，返回原结果
            if(CollectionUtils.isEmpty(regexList)){
                list.add(result);
            }
            // 记录循环次数
            int count = 1;
            for(String regex : regexList){
                result = skuMappingRuleEnum.getHandleRegexMethod().apply(ruleTestDTO.getRuleType(),regex,result);
                // 判断是否需要添加结果到列表
                if (!skuMappingRuleEnum.equals(SkuMappingRuleEnum.IGNORE_PREFIXES_AND_SUFFIXES) || count % 2 == 0) {
                    list.add(result);
                }
                count++;
            }
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
        if(SkuMappingRuleEnum.MATCH_COMBINE.equals(skuMappingRuleEnum)) {
            SkuMappingRuleDTO.RuleDTO ruleDTO = commonDTO.getRuleDTO();
            if (CollectionUtils.isEmpty(ruleDTO.getRuleContentList())) {
                throw new ServiceException("规则详情不能为空");
            }
            SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO = ruleDTO.getRuleContentList().get(0);
            if (StringUtils.isEmpty(ruleConditionsDTO.getChildCombineSplitSymbol()) || StringUtils.isEmpty(ruleConditionsDTO.getChildQtySplitSymbol())) {
                throw new ServiceException("拆分符号不能为空");
            }
        }
        List<String> regexList = skuMappingRuleEnum.getRegexMethod().apply(commonDTO.getRuleDTO());
        String regex = regexList.toString().substring(1, regexList.toString().length() - 1);
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
        }else{
            skuMappingRuleEntity.setExtendRuleContent(new HashMap<>());
        }
    }

//    @Transactional(rollbackFor = Exception.class)
//    public void handleSamePriority(SkuMappingRuleEntity skuMappingRuleEntity){
//        List<SkuMappingRuleEntity> skuMappingRuleEntityList = this.listOrderByPriorityAndUpdateTime();
//        Collections.reverse(skuMappingRuleEntityList);
//        SkuMappingRuleEntity old = skuMappingRuleEntityList.stream().filter(v->v.getId().equals(skuMappingRuleEntity.getId())).findFirst().orElse(null);
//        // 优先级已全部创建完，不允许新增
//        if(skuMappingRuleEntityList.size() == OmsConstant.SKU_MAPPING_RULE_SIZE && Objects.isNull(old)){
//            throw new ServiceException("规则已满，不允许新增");
//        }
//        SkuMappingRuleEntity originalEntity = skuMappingRuleEntityList.stream().filter(v->v.getPriority().equals(skuMappingRuleEntity.getPriority())).findFirst().orElse(null);
//        //优先级有冲突
//        if(Objects.nonNull(originalEntity)){
//            if(Objects.nonNull(old)){
//                originalEntity.setPriority(old.getPriority());
//                this.updateById(originalEntity);
//            }else{
//                //判断低优先级能不能往下顺延，不能的话抛错
//                List<SkuMappingRuleEntity> lowList = skuMappingRuleEntityList.stream().filter(v->v.getPriority()>=skuMappingRuleEntity.getPriority()).collect(Collectors.toList());
//                if(lowList.size()>=OmsConstant.SKU_MAPPING_RULE_SIZE - skuMappingRuleEntity.getPriority()+1){
//                    throw new ServiceException("低优先级规则不能往下顺延，请调整优先级");
//                }
//                List<SkuMappingRuleEntity> updateList = new ArrayList<>();
//                for(int i = 0 ;i < lowList.size();i++){
//                    SkuMappingRuleEntity nowEntity = lowList.get(i);
//                    if(OmsConstant.SKU_MAPPING_RULE_SIZE.equals(nowEntity.getPriority())){
//                        continue;
//                    }
//                    SkuMappingRuleEntity previousEntity = i == 0 ?null:lowList.get(i-1);
//                    SkuMappingRuleEntity nextEntity = i +1 >= lowList.size() ?null:lowList.get(i+1);
//                    //下个优先级与当前优先级相邻
//                    if(Objects.isNull(previousEntity) || nowEntity.getPriority() +1 != previousEntity.getPriority()){
//                        //上个优先级与当前优先级不相邻，则不需要修改
//                        if(Objects.nonNull(nextEntity) && nextEntity.getPriority() != nowEntity.getPriority()-1){
//                            continue;
//                        }
//                        nowEntity.setPriority(nowEntity.getPriority()+1);
//                        updateList.add(nowEntity);
//                    }
//                }
//                this.updateBatchById(updateList);
//            }
//        }
//    }

    /**
     * 处理sku映射
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleSkuMapping(List<String> skuMappingIds) {
        //查询未匹配的SKU
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        if(CollectionUtils.isEmpty(skuMappingIds)){
            paramDTO.setMatchResult(ListingMatchResultEnum.FALSE.getCode());
        }else{
            paramDTO.setSkuMappingIds(skuMappingIds);
        }
        paramDTO.setIsExpire(false);
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
        //查询销售套装信息
        List<BomDTO.BomSku> bomSkus = plmTaskFeign.listAllBom(new ArrayList<>());
        bomSkus = bomSkus.stream().filter(v->v.getType().equals(BomTypeEnum.COMBINATION.getType())).collect(Collectors.toList());
        Map<String,String> bomSkuMap = bomSkus.stream().collect(Collectors.groupingBy(
                BomDTO.BomSku::getParentSkuId,
                        Collectors.mapping(
                                bomSku -> bomSku.getSkuNo() +"*" + bomSku.getQty(),
                                Collectors.joining("丨")
                        )
                ));

        Map<String,SkuVO> skuVOMap = skuVOList.stream().collect(Collectors.toMap(SkuVO::getSkuNo,skuVO -> skuVO));
        List<SkuMappingRuleEntity> skuMappingRuleEntityList = this.listOrderByPriorityAndUpdateTime();
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
            ruleLoop : for(SkuMappingRuleEntity skuMappingRuleEntity : skuMappingRuleEntityList){
                String handlePlatformSkuNo = platformSkuNo;
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
                        handlePlatformSkuNo = skuMappingExtendRuleEnum.getHandleRegexMethod().apply(extendRegex,handlePlatformSkuNo);
                    }
                }

                //获取枚举
                SkuMappingRuleEnum skuMappingRuleEnum = EnumMessage.getByCode(SkuMappingRuleEnum.class, skuMappingRuleEntity.getRuleType());
                if(Objects.isNull(skuMappingRuleEnum)){
                    throw new ServiceException("获取规则异常,");
                }
                if(SkuMappingRuleEnum.MATCH_COMBINE.equals(skuMappingRuleEnum)){
                    JSONObject jsonObject = new JSONObject(skuMappingRuleEntity.getRuleContent());
                    SkuMappingRuleDTO.RuleDTO ruleDTO = JSONObject.parseObject(jsonObject.toJSONString(),new TypeReference<SkuMappingRuleDTO.RuleDTO>() {}.getType());
                    SkuMappingRuleDTO.RuleConditionsDTO ruleConditionsDTO = ruleDTO.getRuleContentList().get(0);
                    List<String> splitList = this.splitWithoutDelimiter(handlePlatformSkuNo,ruleConditionsDTO.getChildCombineSplitSymbol());
                    StringBuilder matchStr = new StringBuilder();
                    for (String childrenSku : splitList) {
                        SkuMappingRuleDTO.SplitSkuDTO splitSkuDTO = this.splitByLastSymbol(childrenSku,ruleConditionsDTO.getChildQtySplitSymbol());
                        matchStr.append(splitSkuDTO.matchStr());
                    }
                    // 去掉最后一个字符
                    if (matchStr.length() > 0) {
                        matchStr.deleteCharAt(matchStr.length() - 1);
                    }
                    //匹配销售套装bom
                    for (Map.Entry<String, String> entry : bomSkuMap.entrySet()) {
                        if (compareSplitStrings(entry.getValue(),matchStr.toString())) {
                            SkuVO skuVO = skuVOList.stream().filter(v->v.getSkuId().equals(entry.getKey())).findFirst().orElse(null);
                            if(Objects.nonNull(skuVO)){
                                ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
                                listingInfoEntity.setId(listingInfoWithSkuMappingDTO.getListingId());
                                listingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
                                listingInfoEntity.setRemark("");
                                updateListingList.add(listingInfoEntity);
                                SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
                                skuMappingEntity.setId(listingInfoWithSkuMappingDTO.getTableId());
                                skuMappingEntity.setRuleId(skuMappingRuleEntity.getId());
                                skuMappingEntity.setProductSkuId(skuVO.getSkuId());
                                skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
                                skuMappingEntity.setProductName(skuVO.getSkuName());
                                skuMappingEntity.setListingId(listingInfoEntity.getId());
                                updateSkuMappingList.add(skuMappingEntity);
                                break ruleLoop;
                            }
                        }
                    }
                }else{
                    String ruleRegexArrStr = skuMappingRuleEntity.getRuleRegex();
                    String[] ruleRegexList = ruleRegexArrStr.split(", ");
                    for(String ruleRegex : ruleRegexList){
                        handlePlatformSkuNo = skuMappingRuleEnum.getHandleRegexMethod().apply(skuMappingRuleEntity.getRuleType(),ruleRegex,handlePlatformSkuNo);
                        if(skuVOMap.containsKey(handlePlatformSkuNo)){
                            ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
                            listingInfoEntity.setId(listingInfoWithSkuMappingDTO.getListingId());
                            listingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
                            listingInfoEntity.setRemark("");
                            updateListingList.add(listingInfoEntity);

                            SkuVO skuVO = skuVOMap.get(handlePlatformSkuNo);
                            SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
                            skuMappingEntity.setId(listingInfoWithSkuMappingDTO.getTableId());
                            skuMappingEntity.setRuleId(skuMappingRuleEntity.getId());
                            skuMappingEntity.setProductSkuId(skuVO.getSkuId());
                            skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
                            skuMappingEntity.setProductName(skuVO.getSkuName());
                            skuMappingEntity.setListingId(listingInfoEntity.getId());
                            updateSkuMappingList.add(skuMappingEntity);
                            break ruleLoop;
                        }
                    }
                }
            }
        }
        if(CollectionUtils.isNotEmpty(updateSkuMappingList)){
            skuMappingService.updateBatchById(updateSkuMappingList,2000);
            //记录更新日志
            Map<String,ListingInfoWithSkuMappingDTO> oldSkuMap = noMatchList.stream().collect(Collectors.toMap(v->v.getTableId(), Function.identity(),(v1,v2)->v1));
            for(SkuMappingEntity skuMappingEntitity : updateSkuMappingList){
                ListingInfoWithSkuMappingDTO listingInfoWithSkuMappingDTO = oldSkuMap.get(skuMappingEntitity.getId());
                SkuMappingEntity oldLogEntity = SkuMappingConverter.INSTANCE.copySkuMappingEntity(skuMappingEntitity);
                oldLogEntity.setProductSkuId(listingInfoWithSkuMappingDTO.getProductSkuId());
                oldLogEntity.setProductSkuNo(listingInfoWithSkuMappingDTO.getProductSkuNo());
                oldLogEntity.setProductName(listingInfoWithSkuMappingDTO.getProductName());
                String msg =  CharSequenceUtil.format("用户【{}】执行自动匹配规则，匹配前sku【{}】,匹配后sku【{}】", UserContext.getDefaultLoginUser().getUserName(),oldLogEntity.getProductSkuNo() , skuMappingEntitity.getProductSkuNo());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), skuMappingEntitity.getListingId(), "自动匹配");
             }
        }
        if(CollectionUtils.isNotEmpty(updateListingList)){
            listingInfoService.updateBatchById(updateListingList,2000);
        }
    }

    @Override
    public PagingVO<SkuMappingRuleDTO.ListDTO> paging(PagingDTO<SkuMappingRuleDTO.ParamsDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<SkuMappingRuleDTO.ListDTO> pageData = baseMapper.paging(query, dto.getParams());
        pageData.getRecords().forEach(v->{
            v.setRuleTypeName(EnumMessage.getNameByCode(SkuMappingRuleEnum.class,v.getRuleType()));
        });
        return new PagingVO(pageData);
    }

    /**
     * 根据字符拆分字符串，并确保第二部分是数字。
     *
     * @param input 输入字符串
     * @return 拆分后的字符串数组，如果不符合条件则第二个元素为1。
     */
    public  SkuMappingRuleDTO.SplitSkuDTO splitByLastSymbol(String input,String symbol) {
        // 查找最后一个星号的位置
        int lastAsteriskIndex = input.lastIndexOf(symbol);

        if (lastAsteriskIndex != -1) {
            String firstPart = input.substring(0, lastAsteriskIndex);
            String secondPart = input.substring(lastAsteriskIndex + 1);

            // 检查第二部分是否为数字
            if (StringUtils.isNumeric(secondPart)) {
                return new SkuMappingRuleDTO.SplitSkuDTO(firstPart, Integer.valueOf(secondPart));
            }
        }

        // 不符合拆分条件的情况
        return  new SkuMappingRuleDTO.SplitSkuDTO(input, 1);
    }
    public boolean compareSplitStrings(String a, String b) {
        // 分割字符串 a
        String[] partsA = a.split("丨");
        // 分割字符串 b
        String[] partsB = b.split("丨");

        // 检查分割后的部分是否相等，顺序可以不一致
        if (partsA.length == partsB.length) {
            Set<String> setA = new HashSet<>(Arrays.asList(partsA));
            Set<String> setB = new HashSet<>(Arrays.asList(partsB));

            return setA.equals(setB);
        }

        // 如果任何一个字符串没有被正确分割成两部分，返回 false
        return false;
    }


    /**
     * 按照给定的分隔符拆分字符串，特殊处理连续分隔符和末尾分隔符的情况。
     *
     * @param input 待拆分的字符串
     * @param delimiter 分隔符
     * @return 拆分后的字符串列表
     */
    public List<String> splitWithoutDelimiter(String input, String delimiter) {
        if(StringUtils.isBlank(input)){
            return new ArrayList<>();
        }
        if(StringUtils.isBlank(delimiter)){
            return Collections.singletonList(input);
        }

        List<String> result = new ArrayList<>();
        int start = 0;
        int delimiterLength = delimiter.length();

        int index = input.indexOf(delimiter, start);
        while (index >= 0) {
            // 添加当前分隔符之前的子字符串
            String addStr = "";
            if (index > start) {
                addStr = input.substring(start, index);
            }
            // 记录分隔符的起始位置
            int delimiterStart = index;
            // 跳过分隔符，处理连续分隔符
            index += delimiterLength;
            while (index <= input.length() - delimiterLength && input.substring(index, index + delimiterLength).equals(delimiter)) {
                index += delimiterLength;
            }
            // 更新 start 为最后一个分隔符之后的位置
            start = index;
            // 处理连续分隔符的特殊情况
            if (start > delimiterStart + delimiterLength) {
                addStr = addStr + input.substring(delimiterStart, start-1);
            }
            // 查找下一个分隔符
            index = input.indexOf(delimiter, start);
            result.add(addStr);
        }
        // 处理最后一个子字符串
        if (start < input.length()) {
            result.add(input.substring(start));
        }
        if (input.endsWith(delimiter)) {
            String lastStr = result.get(result.size() - 1);
            result.remove(result.size() - 1);
            result.add(lastStr + input.substring(start-delimiter.length()));
        }

        return result;
    }

}
