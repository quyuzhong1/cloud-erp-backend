package com.erp.server.sys.service.impl;

import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.DictGlobalAreaDTO;
import com.erp.model.sys.entity.DictGlobalAreaEntity;
import com.erp.server.sys.mapper.DictGlobalAreaMapper;
import com.erp.server.sys.service.DictGlobalAreaService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 区域表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Service
public class DictGlobalAreaServiceImpl extends SuperServiceImpl<DictGlobalAreaMapper, DictGlobalAreaEntity> implements DictGlobalAreaService {

    /**
     * 添加地区
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-05-11 14:57
     */

    @Override
    public Boolean addOrUpdate(List<DictGlobalAreaDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<DictGlobalAreaEntity> addOrList = BeanMapper.copyList(list, DictGlobalAreaEntity.class);
            return this.saveOrUpdateBatch(addOrList);
        }
        return Boolean.TRUE;
    }


    /**
     * 根据国家id获取地区信息
     *
     * @param countryIds
     * @return java.util.List<com.erp.model.sys.dto.DictGlobalAreaDTO.InfoDTO>
     * @author yl
     * @date 2023-05-15 11:39
     */
    @Override
    public List<DictGlobalAreaDTO.InfoDTO> listGlobalAreaByCountryIds(List<String> countryIds) {
        if (CollectionUtils.isEmpty(countryIds)) {
            return Collections.emptyList();
        }
        List<DictGlobalAreaDTO.InfoDTO> resultList = this.baseMapper.listByCountryIds(countryIds);
        return resultList;
    }

    /**
     * 根据区域ids获取地区列表
     * @author yl
     * @date 2023-08-22 15:48
     * @param ids
     * @return java.util.List<com.erp.model.sys.entity.DictGlobalAreaEntity>
     */
    @Override
    public List<DictGlobalAreaEntity> listGlobalAreaByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictGlobalAreaEntity::getId,ids).list();
    }

    /**
     * 获取地区列表
     * @author Jim
     * @date 2023-09-19 09:48
     * @return java.util.List<com.erp.model.sys.entity.DictGlobalAreaEntity>
     */
    @Override
    public List<DictGlobalAreaEntity> listGlobalArea() {
        return this.lambdaQuery().list();
    }


}
