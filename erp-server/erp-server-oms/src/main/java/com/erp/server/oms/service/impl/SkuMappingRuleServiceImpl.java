package com.erp.server.oms.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.constant.EnumMessage;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.erp.model.oms.dto.SkuMappingRuleDTO;
import com.erp.model.oms.entity.SkuMappingRuleEntity;
import com.erp.model.oms.enums.SkuMappingRuleEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.server.oms.convert.SkuMappingRuleConverter;
import com.erp.server.oms.mapper.SkuMappingRuleMapper;
import com.erp.server.oms.service.CommonService;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.SkuMappingRuleService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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

    @GlobalTransactional(rollbackFor = Exception.class)
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
        SkuMappingRuleEntity skuMappingRuleEntity =  BeanMapperUtils.map(SkuMappingRuleEntity.class, updateDTO);

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
        operateLogService.addModuleOperateLogByObj(old, skuMappingRuleEntity, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), skuMappingRuleEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<SkuMappingRuleEntity> listOrderByPriority() {
        return lambdaQuery().orderByAsc(SkuMappingRuleEntity::getPriority).list();
    }

    @Override
    public Boolean enableOrDisable(SkuMappingRuleDTO.StatusDTO dto) {
        SkuMappingRuleEntity old = super.getById(dto.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "sku对照表匹配规则"));
        old.setDisabled(dto.getDisabled());
        boolean save = super.updateById(old);
        if(!save) {
            throw new ServiceException("sku对照表匹配规则保存失败");
        }
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", commonService.getUserInfo().getUserName(), old.getId(), "sku对照表匹配规则");
        operateLogService.addModuleOperateLogByObj(old, old, ModuleTypeEnum.SKU_MAPPING_RULE.getCode(), old.getId(), msg);
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
        skuMappingRuleEntity.setRuleRegex(regexList.toString());

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
            skuMappingRuleEntity.setExtendRuleRegex(extendRegexList.toString());
        }
    }
}
