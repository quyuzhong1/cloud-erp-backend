package com.erp.server.workflow.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.erp.model.workflow.dto.DictBasicDTO;
import com.erp.model.workflow.entity.DictBasicEntity;
import com.erp.server.workflow.mapper.DictBasicMapper;
import com.erp.server.workflow.service.DictBasicService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Cloud
 * @since 2023-04-21
 */
@Service
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {

    @Override
    public List<DictBasicDTO.DropDownDTO> listByType(String type, String remark) {
        List<DictBasicEntity> list = lambdaQuery().eq(DictBasicEntity::getType, type)
                .eq("processCondition".equalsIgnoreCase(type), DictBasicEntity::getRemark, remark)
                .list();
        return list.stream().map(DictBasicDTO.DropDownDTO::new).collect(Collectors.toList());
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
