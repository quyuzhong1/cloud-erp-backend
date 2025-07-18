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
import com.erp.model.workflow.dto.CfgQueryOptionDTO;
import com.erp.model.workflow.entity.CfgQueryOptionEntity;
import com.erp.model.workflow.enums.CfgQueryOptionFieldBelongsTypeEnum;
import com.erp.model.workflow.enums.CfgQueryOptionFieldTypeEnum;
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
        return baseMapper.proDropDownByMain(bussinessKey,useType);
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
        queryWrapper.eq(CfgQueryOptionEntity::getUseType, useType);
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
        cfgQueryOptionEntities = cfgQueryOptionEntities.stream()
                .filter(e -> !e.getConditionField().contains("id") && !e.getConditionField().contains("Id"))
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

    @Transactional(rollbackFor =Exception.class)
    @Override
    public void genBySql(List<CfgQueryOptionDTO.GenListDTO>list) {
        if(CollUtil.isNotEmpty(list)){
            for (CfgQueryOptionDTO.GenListDTO dto : list) {
                String model = dto.getModel();
                String tableName = dto.getTableName();
                String fieldBelongsType = dto.getFieldBelongsType();
                String bussinessKey = dto.getBussinessKey();
                String useType = dto.getUseType();
                //判断同一个单据下的fieldBelongsType 是否已存在。 不存在才新增，存在则跳过
                Integer count = lambdaQuery().eq(CfgQueryOptionEntity::getBussinessKey, bussinessKey)
                        .eq(CfgQueryOptionEntity::getFieldBelongsType,fieldBelongsType)
                        .count();
                if(count > 0){
                    continue;
                }
                saveFromSql(bussinessKey, model,tableName,fieldBelongsType,useType);
            }
        }
    }


    private void saveFromSql(String businessKey, String model , String tableName, String fieldBelongsType, String useType) {
//        //不允许重复添加
//        Integer count = lambdaQuery().eq(CfgQueryOptionEntity::getBussinessKey, businessKey).eq(CfgQueryOptionEntity::getTableName, tableName).count();
//        if(count > 0){
//            return ;
//        }
//
//        String sql = "SELECT obj_description(cls.oid) AS table_comment,cls.relname, col.attnum AS ordinal_position, col.attname AS COLUMN_NAME, format_type(col.atttypid, col.atttypmod) AS data_type, NOT col.attnotnull AS is_nullable, des.description AS column_comment FROM pg_attribute col JOIN pg_class cls ON col.attrelid = cls.OID JOIN pg_namespace ns ON cls.relnamespace = ns.OID LEFT JOIN pg_description des ON des.objoid = col.attrelid AND des.objsubid = col.attnum WHERE cls.relname = '{}' AND col.attnum > 0 and col.attname not in ('create_user_id','create_user_name','create_time','update_user_id','update_user_name','update_time','version','is_deleted') AND NOT col.attisdropped ORDER BY col.attnum;";
//
//        String url = "jdbc:postgresql://172.16.100.60:32590/" + StrUtil.format("erp-{}", model) + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true&useSSL=false";
//        try {
//            List<CfgQueryOptionEntity> results = new ArrayList<>();
//            Connection conn = DriverManager.getConnection(url, "", "");
//            String format = StrUtil.format(sql, tableName);
//            PreparedStatement stmt = conn.prepareStatement(format);
//            ResultSet rs = stmt.executeQuery();
//            while (rs.next()) {
//                String name = rs.getString("column_name");
//                String comment = rs.getString("column_comment");
//                String tableComment = rs.getString("table_comment");
//                CfgQueryOptionEntity cfgQueryOption = new CfgQueryOptionEntity();
//                cfgQueryOption.setConditionField(underlineToCamel(name));
//                cfgQueryOption.setConditionFieldName(Objects.isNull(comment) ? "" : comment);
//                cfgQueryOption.setBussinessKey(businessKey);
//                cfgQueryOption.setFieldBelongsType(fieldBelongsType);
//                cfgQueryOption.setValueType("String");
//                cfgQueryOption.setClasspath("class com.erp.model."+model+".entity."+underlineToPascal(tableName)+"Entity");
//                cfgQueryOption.setTableName(tableName);
//                cfgQueryOption.setSysClassify(model);
//                cfgQueryOption.setTableCnName(Objects.isNull(tableComment) ? "" : tableComment);
//                cfgQueryOption.setUseType(useType);
//                results.add(cfgQueryOption);
//            }
//            saveBatch(results);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
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
