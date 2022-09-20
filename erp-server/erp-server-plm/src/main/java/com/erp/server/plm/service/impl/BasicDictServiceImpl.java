package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.BasicDictDTO;
import com.erp.model.plm.entity.BasicDictEntity;
import com.erp.server.plm.mapper.BasicDictMapper;
import com.erp.server.plm.service.BasicDictService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * plm 字典表 服务实现类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Service
public class BasicDictServiceImpl extends ServiceImpl<BasicDictMapper, BasicDictEntity> implements BasicDictService {

    /**
     * 保存或者修改plm 字典表
     *
     * @param dtos
     * @return java.lang.Boolean
     * @author yl
     * @date 2022-09-16 11:38
     */
    @Override
    public Boolean saveOrUpdateDict(List<BasicDictDTO> dtos) {
        List<BasicDictEntity> saveList = BeanMapper.copyList(dtos, BasicDictEntity.class);
        return this.saveOrUpdateBatch(saveList);
    }


    /**
     * 根据属性获取对应的值
     *
     * @param type
     * @return java.util.List<com.erp.model.plm.entity.BasicDictEntity>
     * @author yl
     * @date 2022-09-16 14:27
     */
    @Override
    public List<BasicDictEntity> listByType(String type) {
        LambdaQueryWrapper<BasicDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicDictEntity::getType, type);
        queryWrapper.orderByDesc(BasicDictEntity::getCreateTime);
        return this.list(queryWrapper);
    }
}
