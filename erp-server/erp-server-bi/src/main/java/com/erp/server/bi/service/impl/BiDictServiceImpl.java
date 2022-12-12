package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.server.bi.mapper.BiDictMapper;
import com.erp.server.bi.service.BiDictService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * bi系统字典表(BiDict)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:24:02
 */
@Service("biDictService")
public class BiDictServiceImpl extends ServiceImpl<BiDictMapper, BiDictEntity> implements BiDictService {
    @Resource
    private BiDictMapper biDictMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BiDictEntity queryById(String id) {
        return null;
    }

    @Override
    public PagingVO<BiDictEntity> queryByPage() {
        return null;
    }


    /**
     * 新增数据
     *
     * @param biDict 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean insert(BiDictEntity biDict) {
        return this.save(biDict);
    }

    /**
     * 修改数据
     *
     * @param biDict 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(BiDictEntity biDict) {
        return true;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String id) {
        return true;
    }

    @Override
    public List<Map<String,Object>> listByType(String type) {
        LambdaQueryWrapper<BiDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiDictEntity::getId,BiDictEntity::getName);
        queryWrapper.eq(BiDictEntity::getType, type);
        return this.listMaps(queryWrapper);
    }
}
