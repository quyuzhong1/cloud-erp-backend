package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiLayoutEntity;
import com.erp.server.bi.mapper.BiLayoutMapper;
import com.erp.server.bi.service.BiLayoutService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 布局表(BiLayout)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:28:27
 */
@Service("biLayoutService")
public class BiLayoutServiceImpl extends ServiceImpl<BiLayoutMapper, BiLayoutEntity> implements BiLayoutService {
    @Resource
    private BiLayoutMapper biLayoutMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BiLayoutEntity queryById(String id) {
        return null;
    }

    @Override
    public PagingVO<BiLayoutEntity> queryByPage() {
        return null;
    }


    /**
     * 新增数据
     *
     * @param biLayout 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean insert(BiLayoutEntity biLayout) {
        return true;
    }

    /**
     * 修改数据
     *
     * @param biLayout 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(BiLayoutEntity biLayout) {
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
