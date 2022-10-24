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
        queryWrapper.orderByDesc(BasicDictEntity::getOrderIndex);
        return this.list(queryWrapper);
    }

    /**
     * 根据id集合批量查询字典信息
     * @Author Luo_WG
     * @Date 2022/10/22 19:50
     * @param list id集合
     * @return java.util.List<com.erp.model.plm.entity.BasicDictEntity>
     **/
    @Override
    public List<BasicDictEntity> listByIds(List<String> list) {
        LambdaQueryWrapper<BasicDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(BasicDictEntity::getId, list);
        return this.list(queryWrapper);
    }

    /**
     * @Description 根据名称查询字段是否存在
     * @Author Luo_WG
     * @Date 2022/9/29 11:02
     * @param type:字典类型
     * @param value:字典值
     * @return com.erp.model.plm.entity.BasicDictEntity
     **/
    public BasicDictEntity checkBasicDict(String type, String value) {
        LambdaQueryWrapper<BasicDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BasicDictEntity::getType, type);
        queryWrapper.eq(BasicDictEntity::getValue, value);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }
}
