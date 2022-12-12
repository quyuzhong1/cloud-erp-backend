package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;
import com.erp.server.bi.mapper.BiLayoutRefModuleMapper;
import com.erp.server.bi.service.BiLayoutRefModuleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 布局与模块关系表(BiLayoutRefModule)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
@Service("biLayoutRefModuleService")
public class BiLayoutRefModuleServiceImpl extends ServiceImpl<BiLayoutRefModuleMapper, BiLayoutRefModuleEntity> implements BiLayoutRefModuleService {
    @Resource
    private BiLayoutRefModuleMapper biLayoutRefModuleMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BiLayoutRefModuleEntity queryById(String id) {
        return null;
    }

    @Override
    public PagingVO<BiLayoutRefModuleEntity> queryByPage() {
        return null;
    }


    /**
     * 新增数据
     *
     * @param biLayoutRefModule 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean insert(BiLayoutRefModuleEntity biLayoutRefModule) {
        return true;
    }

    /**
     * 修改数据
     *
     * @param biLayoutRefModule 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(BiLayoutRefModuleEntity biLayoutRefModule) {
        return true;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public Boolean deleteById(String id) {
        return true;
    }
}
