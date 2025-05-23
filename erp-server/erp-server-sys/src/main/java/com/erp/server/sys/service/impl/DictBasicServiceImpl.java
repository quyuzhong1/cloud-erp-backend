package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.server.sys.mapper.DictBasicMapper;
import com.erp.server.sys.service.DictBasicService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author lambda
 * @since 2023-04-26
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {


    /**
     * 保存或者修改字典信息
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-04-26 15:16
     */
    @Override
    public Boolean addOrUpdate(List<DictBasicDTO.AddOrUpdateDTO> list) {
        if (CollectionUtils.isNotEmpty(list)) {
            List<DictBasicEntity> addOrList = BeanMapper.copyList(list, DictBasicEntity.class);
            this.saveOrUpdateBatch(addOrList);
        }
        return Boolean.TRUE;
    }


    /**
     * 根据类型获取字典值
     *
     * @param type
     * @return java.util.List<com.erp.model.sys.dto.DictBasicDTO.ViewDTO>
     * @author yl
     * @date 2023-04-26 15:33
     */
    @Override
    public List<DictBasicDTO.ViewDTO> listByType(String type) {
        List<DictBasicEntity> list = this.lambdaQuery().eq(DictBasicEntity::getType, type).list();
        List<DictBasicDTO.ViewDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.ViewDTO.class);
        return resultList;
    }


    /**
     * 根据值获取字典信息
     *
     * @param itemRoleValueList
     * @return java.util.List<com.erp.model.sys.entity.DictBasicEntity>
     * @author yl
     * @date 2023-04-27 14:58
     */
    @Override
    public List<DictBasicEntity> listByValues(List<String> itemRoleValueList) {
        if (CollectionUtils.isEmpty(itemRoleValueList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(DictBasicEntity::getValue,itemRoleValueList).list();
    }

    /**
     * 根据类型和值获取到对应信息
     *
     * @param type
     * @param value
     * @return DictBasicEntity
     * @author yl
     * @date 2023-06-28 16:25
     */
    @Override
    public DictBasicEntity getByTypeAndValue(String type, String value) {
        LambdaQueryWrapper<DictBasicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DictBasicEntity::getType, type);
        queryWrapper.eq(DictBasicEntity::getValue, value);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}
