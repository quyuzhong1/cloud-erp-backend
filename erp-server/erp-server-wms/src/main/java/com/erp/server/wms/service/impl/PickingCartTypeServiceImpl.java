package com.erp.server.wms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.PickingCartTypeDTO;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.erp.server.wms.mapper.PickingCartTypeMapper;
import com.erp.server.wms.service.PickingCartTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 拣货车类型 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-06-20
 */
@Slf4j
@Service
public class PickingCartTypeServiceImpl extends SuperServiceImpl<PickingCartTypeMapper, PickingCartTypeEntity> implements PickingCartTypeService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchUpdate(List<PickingCartTypeDTO.batchUpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"拣货车类型");
        }
        List<PickingCartTypeEntity> pickingCartTypeList = BeanMapperUtils.copyList(PickingCartTypeEntity.class, list);

        Integer index = MathUtil.ONE;
        for (PickingCartTypeEntity typeEntity : pickingCartTypeList) {
            typeEntity.setIndex(index);
            index++;
        }
        //删除非修改的数据
        List<String> updateIdList = list.stream().filter(obj -> StrUtil.isNotBlank(obj.getId())).map(PickingCartTypeDTO.batchUpdateDTO::getId).distinct().collect(Collectors.toList());
        deleteByUpdateIdList(updateIdList);

        return this.saveOrUpdateBatch(pickingCartTypeList);
    }

    @Override
    public List<PickingCartTypeDTO.ListDTO> select(PickingCartTypeDTO.SelectDTO selectDTO) {
        return baseMapper.select(selectDTO);
    }

    /**
     * 删除未更新的数据
     * @author will
     * @date 2024/6/20 18:14
     * @param updateIdList
     */
    private void deleteByUpdateIdList(List<String> updateIdList) {
        if (CollectionUtils.isEmpty(updateIdList)) {
            return;
        }
        lambdaUpdate().in(PickingCartTypeEntity::getId,updateIdList).remove();
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PickingCartTypeEntity pickingCartTypeEntity) {
    // TODO 验证数据 & 数据赋值
    }


}
