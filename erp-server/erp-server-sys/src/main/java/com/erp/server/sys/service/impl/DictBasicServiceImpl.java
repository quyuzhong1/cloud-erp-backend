package com.erp.server.sys.service.impl;

import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.model.sys.entity.DictBasicEntity;
import com.erp.server.sys.mapper.DictBasicMapper;
import com.erp.server.sys.service.DictBasicService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

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
        List<DictBasicDTO.ViewDTO> resultList = BeanMapper.copyList(list,DictBasicDTO.ViewDTO.class);
        return resultList;
    }
}
