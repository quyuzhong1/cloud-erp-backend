package com.erp.server.dmp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapperUtils;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.enums.ApiError;
import com.erp.common.exception.ServiceException;
import com.erp.common.vo.PagingVO;
import com.erp.model.dmp.dto.CfgApiFieldMapDTO;
import com.erp.model.dmp.dto.CfgApiFieldMapValueDTO;
import com.erp.model.dmp.entity.CfgApiFieldMapEntity;
import com.erp.model.dmp.entity.CfgApiFieldMapValueEntity;
import com.erp.model.dmp.entity.PlatformEntity;
import com.erp.model.dmp.enums.ApiFieldTypeEnum;
import com.erp.model.dmp.vo.CfgApiFieldMapVO;
import com.erp.server.dmp.mapper.CfgApiFieldMapMapper;
import com.erp.server.dmp.service.CfgApiFieldMapService;
import com.erp.server.dmp.service.CfgApiFieldMapValueService;
import com.erp.server.dmp.service.PlatformService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2023/1/11 11:38
 */
@Service
public class CfgApiFieldMapServiceImpl extends ServiceImpl<CfgApiFieldMapMapper, CfgApiFieldMapEntity> implements CfgApiFieldMapService {

    @Resource
    private PlatformService platformService;

    @Resource
    private CfgApiFieldMapValueService cfgApiFieldMapValueService;

    @Override
    public PagingVO<CfgApiFieldMapVO> paging(PagingDTO<BaseSearchDTO> dto) {
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        BaseSearchDTO params = dto.getParams();
        IPage<CfgApiFieldMapVO> pageData = baseMapper.paging(query, params);
        return new PagingVO(pageData);
    }

    @Override
    @Transactional
    public Boolean insert(CfgApiFieldMapDTO dto) {
        //同一个平台、模块下相同字段对应关系只能存在一个
        checkCfgApiFieldMap(dto);
        //查询平台名称
        PlatformEntity platformEntity = platformService.getById(Integer.valueOf(dto.getApiPlatformId()));
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.ERROR_97022);
        }
        CfgApiFieldMapEntity entity = new CfgApiFieldMapEntity();
        BeanMapperUtils.copy(dto,entity);
        entity.setApiPlatform(platformEntity.getName());
        boolean flag = this.save(entity);
        //新增成功则判断是否需要新增明细
        if (flag) {
            if (ApiFieldTypeEnum.FIELD_VALUE_MAP.getCode().equals(dto.getFieldType())) {
                List<CfgApiFieldMapValueDTO> valueList = dto.getValueList();
                if (CollectionUtils.isEmpty(valueList)) {
                    throw new ServiceException(ApiError.ERROR_97023);
                }
                valueList.forEach(obj-> obj.setFieldMapId(entity.getId()));
                List<CfgApiFieldMapValueEntity> detailList = BeanMapperUtils.copyList(CfgApiFieldMapValueEntity.class, valueList);
                cfgApiFieldMapValueService.saveBatch(detailList);
            }
        }
        return true;
    }

    @Override
    public Boolean batchAdd(List<CfgApiFieldMapDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.Default);
        }
        for(CfgApiFieldMapDTO dto: list) {
            //新增
            insert(dto);
        }
        return true;
    }

    @Override
    public void update(CfgApiFieldMapDTO dto) {
        //同一个平台、模块下相同字段对应关系只能存在一个
        checkCfgApiFieldMap(dto);
        //查询平台名称
        PlatformEntity platformEntity = platformService.getById(Integer.valueOf(dto.getApiPlatformId()));
        if (ObjectUtils.isEmpty(platformEntity)) {
            throw new ServiceException(ApiError.ERROR_97022);
        }
        //更新明细数据
        List<CfgApiFieldMapValueDTO> valueList = dto.getValueList();

        List<CfgApiFieldMapValueEntity> detailList = cfgApiFieldMapValueService.listByFieldMapId(dto.getId());

        //需要新增的明细数据
        List<CfgApiFieldMapValueDTO> addList = new ArrayList<>();
        //需要修改的明细数据
        List<CfgApiFieldMapValueDTO> updateList = new ArrayList<>();
        //需要删除的明细数据
        List<String> removeList = new ArrayList<>();
        //当保存数据为空时删除
        if (CollectionUtils.isEmpty(valueList)) {
            cfgApiFieldMapValueService.removeByFieldMapIds(Arrays.asList(dto.getId()));
        } else {
            if (CollectionUtils.isEmpty(detailList)) {
                //原明细数据为空时新增所有
                addList.addAll(valueList);
            } else {
                //需要新增数据
                List<CfgApiFieldMapValueDTO> add = valueList.stream().filter(obj -> ObjectUtils.isEmpty(obj.getFieldMapId())).collect(Collectors.toList());
                addList.addAll(add);
                //需要修改数据
                List<CfgApiFieldMapValueDTO> update = valueList.stream().filter(obj -> ObjectUtils.isNotEmpty(obj.getFieldMapId())).collect(Collectors.toList());
                updateList.addAll(update);
                if (CollectionUtils.isNotEmpty(update)) {
                    List<String> updateIds = update.stream().map(CfgApiFieldMapValueDTO::getId).collect(Collectors.toList());
                    //需要删除的数据
                    List<String> removeIds = detailList.stream().filter(obj -> !updateIds.contains(obj.getId())).map(CfgApiFieldMapValueEntity::getFieldMapId).collect(Collectors.toList());
                    removeList.addAll(removeIds);
                }

            }
        }
        //新增明细数据
        if (CollectionUtils.isNotEmpty(addList)) {
            cfgApiFieldMapValueService.saveBatch(BeanMapperUtils.copyList(CfgApiFieldMapValueEntity.class, addList));
        }
        //修改明细数据
        if (CollectionUtils.isNotEmpty(updateList)) {
            cfgApiFieldMapValueService.updateBatchById(BeanMapperUtils.copyList(CfgApiFieldMapValueEntity.class, updateList));
        }
        //删除明细数据
        if (CollectionUtils.isNotEmpty(removeList)) {
            cfgApiFieldMapValueService.removeByIds(removeList);
        }
        CfgApiFieldMapEntity entity = new CfgApiFieldMapEntity();
        BeanMapperUtils.copy(dto,entity);
        entity.setApiPlatform(platformEntity.getName());
        //更新字段映射数据
        this.updateById(entity);
    }

    @Override
    public void batchDelete(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            throw new ServiceException(ApiError.ERROR_1003);
        }
        //删除字段值对应关系数据
        cfgApiFieldMapValueService.removeByFieldMapIds(ids);
        //删除字段对应关系数据
        this.removeByIds(ids);
    }

    @Override
    public List<CfgApiFieldMapValueDTO> listDetails(String fieldMapId) {
        List<CfgApiFieldMapValueEntity> list = cfgApiFieldMapValueService.listByFieldMapId(fieldMapId);
        if (CollectionUtils.isEmpty(list)) {
            return  new ArrayList<>();
        }
        return BeanMapperUtils.copyList(CfgApiFieldMapValueDTO.class,list);
    }

    @Override
    public CfgApiFieldMapDTO getCfgApiFieldMapById(String id) {
        CfgApiFieldMapDTO dto = new CfgApiFieldMapDTO();
        if (StringUtils.isBlank(id)) {
            throw new ServiceException(ApiError.ERROR_1003);
        }
        CfgApiFieldMapEntity entity = this.getById(id);
        if (ObjectUtils.isEmpty(entity)) {
            return dto;
        }
        BeanMapperUtils.copy(entity,dto);
        if (ApiFieldTypeEnum.FIELD_VALUE_MAP.getCode().equals(entity.getFieldType())) {
            List<CfgApiFieldMapValueEntity> valueList = cfgApiFieldMapValueService.listByFieldMapId(id);
            if (CollectionUtils.isNotEmpty(valueList)) {
                dto.setValueList(BeanMapperUtils.copyList(CfgApiFieldMapValueDTO.class,valueList));
            }
        }
        return dto;
    }

    @Override
    public List<CfgApiFieldMapDTO> getByParams(CfgApiFieldMapDTO dto) {
        LambdaQueryWrapper<CfgApiFieldMapEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CfgApiFieldMapEntity::getApiPlatformId,dto.getApiPlatformId());
        queryWrapper.eq(CfgApiFieldMapEntity::getModuleType,dto.getModuleType());
        List<CfgApiFieldMapEntity> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return  new ArrayList<>();
        }
        return BeanMapperUtils.copyList(CfgApiFieldMapDTO.class,list);
    }


    /**
     * @description: 验证字段名称是否重复
     * @author Will
     * @date: 2023/1/11 14:30
     * @param dto
     */
    private void checkCfgApiFieldMap(CfgApiFieldMapDTO dto) {
        CfgApiFieldMapEntity selfEntity = getByCfgApiFieldMap(dto);
        if (ObjectUtils.isNotEmpty(selfEntity) && !selfEntity.getId().equals(dto.getId())) {
            throw new ServiceException(ApiError.ERROR_97021);
        }

    }

    /**
     * @description: 查询是否存在重复数据
     * @author Will
     * @date: 2023/1/11 14:27
     * @param dto
     * @return CfgApiFieldMapEntity
     */
    private CfgApiFieldMapEntity getByCfgApiFieldMap(CfgApiFieldMapDTO dto) {
        LambdaQueryWrapper<CfgApiFieldMapEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CfgApiFieldMapEntity::getApiPlatformId,dto.getApiPlatformId());
        queryWrapper.eq(CfgApiFieldMapEntity::getModuleType,dto.getModuleType());
        queryWrapper.eq(CfgApiFieldMapEntity::getSelfField,dto.getSelfField());
        queryWrapper.eq(CfgApiFieldMapEntity::getApiField,dto.getApiField());
        queryWrapper.last("limit 1");
        return this.getOne(queryWrapper);
    }

}
