package com.erp.server.workflow.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.wrapper.FeignQuery;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.RuleCompareEnum;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionUseTypeEnum;
import com.erp.server.workflow.mapper.CfgQueryOptionMapper;
import com.erp.server.workflow.service.CfgQueryOptionService;
import com.erp.server.workflow.service.WorkMenuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 查询option配置表(数大臣单据字段) 服务实现类
 * </p>
 *
 * @author hcg
 * @since 2025-05-15
 */
@Slf4j
@Service
public class CfgQueryOptionServiceImpl extends SuperServiceImpl<CfgQueryOptionMapper, CfgQueryOptionEntity> implements CfgQueryOptionService {

    @Resource
    private WorkMenuService workMenuService;

    @Override
    public List<CfgQueryOptionDTO.ListDTO> proDropDown(String bussinessKey,String useType) {
        return baseMapper.proDropDown(bussinessKey,useType);
    }

    @Override
    public List<CfgQueryOptionDTO.ListDTO> proDropDownByMain(String bussinessKey,String useType) {
        List<CfgQueryOptionEntity> list = lambdaQuery().eq(CfgQueryOptionEntity::getFieldBelongsType, CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode()).orderByDesc(CfgQueryOptionEntity::getId).list();
        List<CfgQueryOptionDTO.ListDTO> result = BeanMapper.copyList(list, CfgQueryOptionDTO.ListDTO.class);
        List<CfgQueryOptionDTO.ListDTO> listDTOS = baseMapper.proDropDownByMain(bussinessKey, useType);
        if(CollUtil.isNotEmpty(listDTOS)){
            result.addAll(listDTOS);
        }
        return result;
    }

    @Override
    public List<CfgQueryOptionDTO.cfgApproveSyncDropDownDTO> cfgApproveSyncDropDown(String bussinessKey,String useType,String fieldBelongsType) {
        //公共字段
        LambdaQueryWrapper<CfgQueryOptionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(CfgQueryOptionEntity::getFieldBelongsType, CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode());
        List<CfgQueryOptionEntity> common = baseMapper.selectList(queryWrapper);
        common.stream().forEach(item -> item.setConditionFieldName(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getName()+"-"+item.getConditionFieldName()));

        //查询业务key下的字段
        queryWrapper.clear();
        if (StringUtils.isNotBlank(fieldBelongsType)) {
            queryWrapper.eq(CfgQueryOptionEntity::getFieldBelongsType, fieldBelongsType);
        }
        queryWrapper.eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey);
        queryWrapper.eq(CfgQueryOptionEntity::getUseType, CfgQueryOptionUseTypeEnum.CFG_APPROVE_SYNC.getCode());
        queryWrapper.eq(CfgQueryOptionEntity::getExtendType,"");//扩展字段
        queryWrapper.orderByDesc(CfgQueryOptionEntity::getFieldBelongsType);
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = baseMapper.selectList(queryWrapper);
        cfgQueryOptionEntities.stream().forEach(item -> {
            if(item.getFieldBelongsType().equals(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode())){
                String name = CfgQueryOptionFieldBelongsTypeEnum.getName(item.getFieldBelongsType());
                item.setConditionFieldName(name +"-"+ item.getConditionFieldName());
            }else {
                item.setConditionFieldName(item.getTableCnName() +"-"+ item.getConditionFieldName());
            }
        });
        cfgQueryOptionEntities.addAll(common);
        //移除包含id字段
        List<String> excludeFields = Arrays.asList("id", "mainId");
        cfgQueryOptionEntities = cfgQueryOptionEntities.stream()
                .filter(e -> !excludeFields.contains(e.getConditionField()))
                .collect(Collectors.toList());
        return BeanUtil.copyToList(cfgQueryOptionEntities, CfgQueryOptionDTO.cfgApproveSyncDropDownDTO.class);
    }

    @Override
    public List<CfgQueryOptionDTO.TreeDTO> tree(String bussinessKey,String useType) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = this.list(new LambdaQueryWrapper<CfgQueryOptionEntity>()
                .eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey)
                .eq(CfgQueryOptionEntity::getUseType, useType)
                .eq(CfgQueryOptionEntity::getIsDeleted, false));
        List<CfgQueryOptionDTO.TreeDTO> resultList = new ArrayList<>(cfgQueryOptionEntities.size());
        Map<String, String> map = new HashMap<>();
        for (RuleCompareEnum item : RuleCompareEnum.values()) {
            map.put(item.getCode(), item.getName());
        }
        for (CfgQueryOptionEntity item : cfgQueryOptionEntities) {
            String conditionField = item.getConditionField();
            CfgQueryOptionDTO.TreeDTO tree = new CfgQueryOptionDTO.TreeDTO();
            tree.setConditionField(conditionField);
            String logicStr = item.getLogic();
            List<String> logicList = Arrays.asList(logicStr.split(","));
            List<CfgQueryOptionDTO.TreeDTO> childrenList = new ArrayList<>(logicList.size());
            for (String logic : logicList) {
                CfgQueryOptionDTO.TreeDTO children = new CfgQueryOptionDTO.TreeDTO();
                children.setConditionField(conditionField);
                children.setLogic(logic);
                children.setLogicName(map.getOrDefault(logic, ""));
                childrenList.add(children);
            }
            tree.setChildren(childrenList);
            resultList.add(tree);
        }
        return resultList;
    }

    @Override
    public List<CfgQueryOptionDTO.ViewDTO> getSystemfield(String bussinessKey,String useType) {
        List<CfgQueryOptionEntity> cfgQueryOptionEntities = baseMapper.getSystemfield(bussinessKey,useType);
        //field_belongs_type字段我想手动的放到viewDTO里面的字段，怎么处理

        List<CfgQueryOptionDTO.ViewDTO> viewDTOS = cfgQueryOptionEntities.stream().map(item -> {
            CfgQueryOptionDTO.ViewDTO viewDTO = BeanUtil.copyProperties(item, CfgQueryOptionDTO.ViewDTO.class);
            viewDTO.setSysParentId(item.getFieldBelongsType());
            return viewDTO;
        }).collect(Collectors.toList());

        viewDTOS.forEach(item -> {
            if (StrUtil.isNotBlank(item.getFieldType())) {
                item.setFieldTypeName(CfgQueryOptionFieldTypeEnum.valueOf(item.getFieldType().toUpperCase()).getName());
            }
            item.setUniqueCode(CharSequenceUtil.format("{}-{}", item.getFieldBelongsType(), item.getConditionField()));
        });
        return viewDTOS;
    }

    @Override
    public List<CfgQueryOptionEntity> listBySysFieldList(String bussinessKey,String useType, List<String> sysFieldList) {
        return lambdaQuery().eq(CfgQueryOptionEntity::getBussinessKey,bussinessKey)
                .eq(CfgQueryOptionEntity::getUseType,useType)
                .in(CfgQueryOptionEntity::getConditionField,sysFieldList)
                .list();
    }

    @Override
    public List<CfgQueryOptionEntity> listByMqParams(CfgQueryOptionDTO.MqParamsDTO mqParamsDTO) {
        return baseMapper.listByMqParams(mqParamsDTO);
    }


    @Override
    public Map<String, Object> getVariablesMapByBusinessKey(CfgQueryOptionDTO.VariablesParamsDTO dto) {
        if(Objects.isNull(dto) || StringUtils.isBlank(dto.getBusinessKey()) || CollUtil.isEmpty(dto.getVariablesMap())){
            return Collections.emptyMap();
        }
        //主表map
        Map<String, Object> variablesMap = dto.getVariablesMap();
        //获取配置明细
        List<CfgQueryOptionEntity> cfgQueryOptionList = lambdaQuery()
                .eq(CfgQueryOptionEntity::getUseType,dto.getUseType())
                .in(CfgQueryOptionEntity::getBussinessKey, dto.getBusinessKey())
                .list();
        if(CollUtil.isNotEmpty(cfgQueryOptionList)){
            //根据fieldBelongsType 进行分组
            Map<String, List<CfgQueryOptionEntity>> fieldBelongsTypeByMap = cfgQueryOptionList.stream().collect(Collectors.groupingBy(CfgQueryOptionEntity::getFieldBelongsType));
            //获取主表字段配置
            List<CfgQueryOptionEntity> mainCfgQueryOptionList = fieldBelongsTypeByMap.get(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode());

            //遍历
            for (Map.Entry<String, List<CfgQueryOptionEntity>> entry : fieldBelongsTypeByMap.entrySet()) {
                //common和主表不需要再查询
                if(entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.COMMON.getCode()) || entry.getKey().equals(CfgQueryOptionFieldBelongsTypeEnum.MAIN.getCode())){
                    continue;
                }
                List<CfgQueryOptionEntity> value = entry.getValue();
                //如果存在，则需要找对明细表里的关联字段，并根据该字段来进行FeignQuery查询出对应的明细列表
                CfgQueryOptionEntity refEntity = value.stream().filter(e -> StringUtils.isNotBlank(e.getParentId())).findFirst().orElse(null);
                if (Objects.isNull(refEntity)) {
                    continue;
                }
                //获取关联记录
                String parentId = refEntity.getParentId();
                CfgQueryOptionEntity mainEntity = mainCfgQueryOptionList.stream().filter(e -> e.getId().equals(parentId)).findFirst().orElse(null);
                String mainField = mainEntity.getConditionField();
                String mainValue = String.valueOf(variablesMap.get(mainField));

                String classpath = refEntity.getClasspath();
                classpath = classpath.replace("class ", "");
                Class<BaseEntity> clazz = null;
                try {
                    clazz = (Class<BaseEntity>) Class.forName(classpath);
                } catch (ClassNotFoundException e) {
                    throw new ServiceException(classpath + "实体不存在");
                }
                List<BaseEntity> detailList = FeignQuery.create(clazz)
                        .eq(refEntity.getConditionField(), mainValue)
                        .list();

                if (CollUtil.isNotEmpty(detailList)) {
                    //明细数据
                    variablesMap.put(entry.getKey() , BeanUtil.copyToList(detailList,Map.class));
                }
            }
        }
        return variablesMap;
    }

    @Override
    public void genBySql(CfgQueryOptionDTO.GenDTO dto) {
        if(Objects.nonNull(dto)){
            String model = dto.getModel();
            String tableName = dto.getTableName();
            String fieldBelongsType = dto.getFieldBelongsType();
            String bussinessKey = dto.getBussinessKey();
            String useType = dto.getUseType();

            //校验以上字段是否为空
            if(StrUtil.isBlank(model) || StrUtil.isBlank(tableName) || StrUtil.isBlank(fieldBelongsType) || StrUtil.isBlank(bussinessKey) || StrUtil.isBlank(useType)){
                System.out.println("参数不能为空");
                return;
            }
            String[] tableNames = tableName.split(",");
            String[] fieldBelongsTypes = fieldBelongsType.split(",");
            for (int i = 0; i < tableNames.length; i++) {
                //判断同一个单据下的fieldBelongsType 是否已存在。 不存在才新增，存在则跳过
                Integer count = lambdaQuery().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey)
                        .eq(CfgQueryOptionEntity::getFieldBelongsType,fieldBelongsTypes[i])
                        .eq(CfgQueryOptionEntity::getTableName,tableNames[i])
                        .eq(CfgQueryOptionEntity::getUseType,useType)
                        .count();
                if(count > 0){
                    continue;
                }
                saveFromSql(dto,tableNames[i],fieldBelongsTypes[i]);
            }
        }
    }


    private void saveFromSql(CfgQueryOptionDTO.GenDTO dto,String tableName,String fieldBelongsType) {
        String model = dto.getModel();
        String businessKey = dto.getBussinessKey();
        String useType = dto.getUseType();

        String sql = dto.getSql();
        String url = StrUtil.format(dto.getUrl(), model);
        try {
            List<CfgQueryOptionEntity> results = new ArrayList<>();
            Connection conn = DriverManager.getConnection(url, dto.getAccount(), dto.getPassword());
            String format = StrUtil.format(sql, tableName);
            PreparedStatement stmt = conn.prepareStatement(format);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String name = rs.getString("column_name");
                String comment = rs.getString("column_comment");
                String tableComment = rs.getString("table_comment");
                CfgQueryOptionEntity cfgQueryOption = new CfgQueryOptionEntity();
                cfgQueryOption.setConditionField(underlineToCamel(name));
                cfgQueryOption.setConditionFieldName(Objects.isNull(comment) ? "" : comment);
                cfgQueryOption.setBussinessKey(businessKey);
                cfgQueryOption.setFieldBelongsType(fieldBelongsType);
                cfgQueryOption.setValueType("String");
                cfgQueryOption.setClasspath("class com.erp.model."+model+".entity."+underlineToPascal(tableName)+"Entity");
                cfgQueryOption.setTableName(tableName);
                cfgQueryOption.setSysClassify(model);
                cfgQueryOption.setTableCnName(Objects.isNull(tableComment) ? "" : tableComment);
                cfgQueryOption.setUseType(useType);
                results.add(cfgQueryOption);
            }
            saveBatch(results);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("错误信息=="+e.getMessage());
        }
    }

    /**
     * 下划线命名转 Pascal 命名（驼峰命名，首字母大写）
     */
    public String underlineToPascal(String underScore) {
        String camel = underlineToCamel(underScore);
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }

    /**
     * 下划线命名转驼峰命名
     */
    public String underlineToCamel(String underScore) {
        StringBuilder camelCase = new StringBuilder();
        boolean nextUpper = false;

        for (int i = 0; i < underScore.length(); i++) {
            char c = underScore.charAt(i);
            if (c == '_') {
                nextUpper = true;
            } else {
                if (nextUpper && i > 0) {
                    camelCase.append(Character.toUpperCase(c));
                    nextUpper = false;
                } else {
                    camelCase.append(Character.toLowerCase(c));
                }
            }
        }

        return camelCase.toString();
    }

}
