package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiModuleEntity;
import com.erp.server.bi.mapper.BiModuleMapper;
import com.erp.server.bi.service.BiModuleService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 模块表(BiModule)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
@Service("biModuleService")
public class BiModuleServiceImpl extends ServiceImpl<BiModuleMapper, BiModuleEntity> implements BiModuleService {
    @Resource
    private BiModuleMapper biModuleMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BiModuleEntity queryById(String id) {
        return null;
    }

    @Override
    public PagingVO<BiModuleEntity> queryByPage() {
        return null;
    }


    /**
     * 新增数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean insert(BiModuleEntity biModule) {
        return true;
    }

    /**
     * 修改数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(BiModuleEntity biModule) {
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
