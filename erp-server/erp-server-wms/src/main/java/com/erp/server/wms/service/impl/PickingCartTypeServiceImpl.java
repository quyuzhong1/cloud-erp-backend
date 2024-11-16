package com.erp.server.wms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.MathUtil;
import com.erp.model.wms.dto.PickingCartTypeDTO;
import com.erp.model.wms.entity.PickingCartEntity;
import com.erp.model.wms.entity.PickingCartTypeEntity;
import com.erp.server.wms.mapper.PickingCartTypeMapper;
import com.erp.server.wms.service.PickingCartService;
import com.erp.server.wms.service.PickingCartTypeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    @Resource
    private PickingCartService pickingCartService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean batchUpdate(List<PickingCartTypeDTO.BatchUpdateDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.TIME_NOT_NULL,"拣货车类型");
        }

        Map<String, List<PickingCartTypeDTO.BatchUpdateDTO>> map = list.stream().collect(Collectors.groupingBy(PickingCartTypeDTO.BatchUpdateDTO::getName));
        for (Map.Entry<String, List<PickingCartTypeDTO.BatchUpdateDTO>> entry : map.entrySet()) {
            if (entry.getValue().size() > 1) {
                throw new ServiceException(CharSequenceUtil.format("拣货车名称【{}】不能重复",entry.getKey()));
            }
        }

        List<PickingCartTypeEntity> pickingCartTypeList = BeanMapperUtils.copyList(PickingCartTypeEntity.class, list);

        Integer index = MathUtil.ONE;
        for (PickingCartTypeEntity typeEntity : pickingCartTypeList) {
            typeEntity.setIndex(index);
            index++;
        }
        //删除非修改的数据
        List<String> updateIdList = list.stream().filter(obj -> CharSequenceUtil.isNotBlank(obj.getId())).map(PickingCartTypeDTO.BatchUpdateDTO::getId).distinct().collect(Collectors.toList());
        deleteByUpdateIdList(updateIdList);

        return this.saveOrUpdateBatch(pickingCartTypeList);
    }

    @Override
    public List<PickingCartTypeDTO.ListDTO> select(PickingCartTypeDTO.SelectDTO selectDTO) {
        return baseMapper.select(selectDTO);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        PickingCartTypeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到拣货车类型数据"));
        //拣货车被使用不支持删除
        List<PickingCartEntity> list = pickingCartService.listByTypeId(entity.getId());
        if (CollectionUtils.isNotEmpty(list)) {
            throw  new ServiceException(CharSequenceUtil.format("拣货车类型【{}】已被使用不支持删除",entity.getName()));
        }
        // 删除主单数据
        log.info("删除 开始删除委外发料单主单数据，id：【{}】", id);
        super.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public Boolean checkIsUsed(String id) {
        PickingCartTypeEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到拣货车类型数据"));
        //拣货车被使用不支持删除
        List<PickingCartEntity> list = pickingCartService.listByTypeId(entity.getId());
        if (CollectionUtils.isNotEmpty(list)) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }

    /**
     * 删除未更新的数据
     * @author will
     * @date 2024/6/20 18:14
     * @param updateIdList
     */
    private void deleteByUpdateIdList(List<String> updateIdList) {
        List<PickingCartTypeEntity> deleteList = new ArrayList<>();
        if (CollectionUtils.isEmpty(updateIdList)) {
            deleteList  = this.list();
        } else {
            deleteList =  lambdaQuery().notIn(PickingCartTypeEntity::getId,updateIdList).list();
        }
        if (CollectionUtils.isEmpty(deleteList)) {
            return;
        }
        deleteList.forEach(obj -> delete(obj.getId()));
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(PickingCartTypeEntity pickingCartTypeEntity) {
    // TODO 验证数据 & 数据赋值
    }


}
