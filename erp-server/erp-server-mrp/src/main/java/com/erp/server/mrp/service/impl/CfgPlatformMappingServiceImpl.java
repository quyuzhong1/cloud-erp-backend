package com.erp.server.mrp.service.impl;


import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.validator.ValidList;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.mrp.dto.CfgPlatformMappingDTO;
import com.erp.model.mrp.entity.CfgPlatformMappingEntity;
import com.erp.model.mrp.enums.CfgRulePlatformTypeEnum;
import com.erp.model.mrp.enums.CfgRuleStockingModeEnum;
import com.erp.model.oms.dto.DictBasicDTO;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.oms.feign.CustomerFeign;
import com.erp.server.mrp.mapper.CfgPlatformMappingMapper;
import com.erp.server.mrp.service.CfgPlatformMappingService;
import com.erp.server.mrp.service.OperateLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 平台映射表 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-08-29
 */
@Slf4j
@Service
public class CfgPlatformMappingServiceImpl extends SuperServiceImpl<CfgPlatformMappingMapper, CfgPlatformMappingEntity> implements CfgPlatformMappingService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private CustomerFeign customerFeign;

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ValidList<CfgPlatformMappingDTO.UpdateDTO> updateList) {
        if (CollectionUtils.isEmpty(updateList)) {
            throw new ServiceException("平台配置不能为空");
        }
        List<CfgPlatformMappingEntity> oldList = list();
        // 数据处理
        List<CfgPlatformMappingEntity> list = handleData(updateList.getList(),oldList);

        //删除多余数据
        List<String> deleteIds = getDeleteIds(list, oldList);
        if (CollectionUtils.isNotEmpty(deleteIds)) {
            this.removeByIds(deleteIds);
        }
        //无数据则直接返回
        if (CollectionUtils.isEmpty(list)) {
            return Boolean.TRUE;
        }

        boolean save = super.saveOrUpdateBatch(list);
        if(!save) {
            throw new ServiceException("平台映射单保存失败");
        }

        //日志
        addOperateLog(list);
        return Boolean.TRUE;
    }

    /**
     * 添加日志
     * @author will
     * @date 2024/10/15 14:38
     * @param list 
     */
    private void addOperateLog (List<CfgPlatformMappingEntity> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        Map<String, List<CfgPlatformMappingEntity>> map = list.stream().collect(Collectors.groupingBy(CfgPlatformMappingEntity::getType));

        //平台信息
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

        //日志
        StringBuffer msg = new StringBuffer();
        for (Map.Entry<String, List<CfgPlatformMappingEntity>> entry : map.entrySet()) {
            List<CfgPlatformMappingEntity> value = entry.getValue();
            CfgPlatformMappingEntity entity = value.get(0);
            //平台集合
            List<String> platformList = value.stream().map(CfgPlatformMappingEntity::getPlatform).distinct().collect(Collectors.toList());
            String platformNames = platformViewList.stream().filter(obj -> platformList.contains(obj.getValue()))
                    .map(DictBasicDTO.ViewDTO::getName)
                    .collect(Collectors.joining(","));
            String content = StrUtil.format("补货建议平台【{}】，平台【{}】，备货模式【{}】，是否启用【{}】，定时生效【{}】;<br>", CfgRulePlatformTypeEnum.getName(value.get(0).getType()),platformNames,
                    CfgRuleStockingModeEnum.getName(entity.getStockingMode()),entity.getDisabled() ? "是":"否", entity.getEffectiveDate());
            msg.append(content);
        }
        if (StrUtil.isBlank(msg)) {
            return;
        }
        String minId = list.stream().min(Comparator.comparing(obj -> obj.getId())).map(CfgPlatformMappingEntity::getId).orElse("");
        operateLogService.addModuleOperateLog(msg.toString(), ModuleTypeEnum.REPLENISHMENT_SUGGESTION.getCode(), minId, "平台");
    }

    @Override
    public List<CfgPlatformMappingDTO.ListDTO> selectPlatformMapping(CfgPlatformMappingDTO.SelectDTO dto) {
        List<CfgPlatformMappingDTO.ListDTO> list = baseMapper.selectPlatformMapping(dto);
        handleSelectPaging(list);
        return list;
    }

    @Override
    public List<CfgPlatformMappingEntity> listByEffective() {
        return list(Wrappers.<CfgPlatformMappingEntity>lambdaQuery()
                .eq(CfgPlatformMappingEntity::getDisabled, false)
                .le(CfgPlatformMappingEntity::getEffectiveDate, LocalDate.now())
        );
    }

    @Override
    public List<CfgPlatformMappingEntity> listByPlatformType(String platformType) {
        List<CfgPlatformMappingEntity> list = lambdaQuery().eq(CfgPlatformMappingEntity::getDisabled, false)
                .le(CfgPlatformMappingEntity::getEffectiveDate, LocalDate.now())
                .eq(CfgPlatformMappingEntity::getType, platformType)
                .list();
        return list;
    }

    @Override
    public List<CfgPlatformMappingEntity> listAllByPlatformType(String platformType) {
        List<CfgPlatformMappingEntity> list = lambdaQuery()
                .eq(CfgPlatformMappingEntity::getType, platformType)
                .list();
        return list;
    }


    @Override
    public CfgPlatformMappingDTO.MainViewDTO view() {
        CfgPlatformMappingDTO.MainViewDTO mainViewDTO = new CfgPlatformMappingDTO.MainViewDTO();

        List<CfgPlatformMappingDTO.ViewDTO> viewList = new ArrayList<>();

        List<CfgPlatformMappingEntity> platformMappingList = this.list();
        if (CollectionUtils.isEmpty(platformMappingList)) {
            return mainViewDTO;
        }
        //最小id
        String minId = platformMappingList.stream().min(Comparator.comparing(obj -> obj.getId())).map(CfgPlatformMappingEntity::getId).orElse("");
        mainViewDTO.setId(minId);
        Map<String, List<CfgPlatformMappingEntity>> map = platformMappingList.stream().collect(Collectors.groupingBy(CfgPlatformMappingEntity::getType));
        for (Map.Entry<String, List<CfgPlatformMappingEntity>> entry : map.entrySet()) {
            List<CfgPlatformMappingEntity> value = entry.getValue();
            CfgPlatformMappingDTO.ViewDTO viewDTO = new CfgPlatformMappingDTO.ViewDTO();
            BeanMapperUtils.copy(value.get(0),viewDTO);
            List<String> platformList = value.stream().map(CfgPlatformMappingEntity::getPlatform).distinct().collect(Collectors.toList());
            viewDTO.setPlatformList(platformList);
            viewDTO.setTypeName(CfgRulePlatformTypeEnum.getName(viewDTO.getType()));
            viewList.add(viewDTO);
        }
        mainViewDTO.setViewList(viewList);
        return mainViewDTO;
    }

    @Override
    public CfgPlatformMappingDTO.ViewDTO getByPlatformType(String platformType) {
        CfgPlatformMappingDTO.ViewDTO viewDTO = new CfgPlatformMappingDTO.ViewDTO();

        List<CfgPlatformMappingEntity> cfgPlatformMappingList = this.listAllByPlatformType(platformType);
        if (CollectionUtils.isEmpty(cfgPlatformMappingList)) {
            return viewDTO;
        }
        BeanMapperUtils.copy(cfgPlatformMappingList.get(0),viewDTO);
        List<String> platformList = cfgPlatformMappingList.stream().map(CfgPlatformMappingEntity::getPlatform).distinct().collect(Collectors.toList());
        viewDTO.setPlatformList(platformList);
        viewDTO.setTypeName(CfgRulePlatformTypeEnum.getName(viewDTO.getType()));
        return viewDTO;
    }


    /**
     * 查询需要删除的数据
     */
    private List<String> getDeleteIds(List<CfgPlatformMappingEntity> newList, List<CfgPlatformMappingEntity> oldList) {
        List<String> newIds = newList.stream().filter(g -> StringUtils.isNotBlank(g.getId())).
                map(CfgPlatformMappingEntity::getId).collect(Collectors.toList());
        List<String> oldIds = oldList.stream().map(CfgPlatformMappingEntity::getId).collect(Collectors.toList());
        return oldIds.stream().filter(s -> !newIds.contains(s)).collect(Collectors.toList());
    }

    /**
    * 新增修改处理数据
    */
    private List<CfgPlatformMappingEntity> handleData(List<CfgPlatformMappingDTO.UpdateDTO> updateList,List<CfgPlatformMappingEntity> oldList) {
        List<CfgPlatformMappingEntity> list = new ArrayList<>();
        if (CollectionUtils.isEmpty(updateList)) {
            return list;
        }
        for (CfgPlatformMappingDTO.UpdateDTO updateDTO : updateList) {
            //非禁用时生效时间不能为空
            if (!updateDTO.getDisabled() && ObjectUtil.isEmpty(updateDTO.getEffectiveDate())) {
                throw new ServiceException("非禁用数据生效时间不能为空");
            }
            //补货建议平台
            long count = updateList.stream().filter(obj -> StrUtil.equals(obj.getType(), updateDTO.getType())).count();
            if (count > MathUtil.ONE) {
                throw new ServiceException(StrUtil.format("补货建议平台【{}】重复",CfgRulePlatformTypeEnum.getName(updateDTO.getType())));
            }
            for (String platform : updateDTO.getPlatformList()) {
                CfgPlatformMappingEntity entity = new CfgPlatformMappingEntity();
                BeanMapperUtils.copy(updateDTO,entity);
                //平台映射
                CfgPlatformMappingEntity old = oldList.stream().filter(obj -> StrUtil.equals(obj.getType(), updateDTO.getType()) && StrUtil.equals(obj.getPlatform(), platform)).findFirst().orElse(null);
                if (ObjectUtil.isNotEmpty(old)) {
                    entity.setId(old.getId());
                }
                entity.setPlatform(platform);
                list.add(entity);
            }
        }
        return list;
    }

    /**
     * 下拉数据处理
     */
    private void handleSelectPaging(List<CfgPlatformMappingDTO.ListDTO> records) {
        if (CollectionUtils.isEmpty(records)) {
            return;
        }
        //平台信息
        List<DictBasicDTO.ViewDTO> platformViewList = customerFeign.getDictBasicByKey(DictBasicTypeEnum.SALES_PLATFORM.getType());

       for (CfgPlatformMappingDTO.ListDTO listDTO : records) {
            //平台名称
           String platformName = platformViewList.stream().filter(obj -> StrUtil.equals(listDTO.getPlatform(), obj.getValue())).map(DictBasicDTO.ViewDTO::getName).findFirst().orElse("");
            listDTO.setPlatformName(platformName);
       }
    }
}
