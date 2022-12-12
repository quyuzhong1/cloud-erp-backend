package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiSubjectRefLayoutEntity;
import com.erp.server.bi.mapper.BiSubjectRefLayoutMapper;
import com.erp.server.bi.service.BiSubjectRefLayoutService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专题与布局关系表(BiSubjectRefLayout)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:32:40
 */
@Service
public class BiSubjectRefLayoutServiceImpl extends ServiceImpl<BiSubjectRefLayoutMapper, BiSubjectRefLayoutEntity> implements BiSubjectRefLayoutService {
    @Resource
    private BiSubjectRefLayoutMapper biSubjectRefLayoutMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BiSubjectRefLayoutEntity queryById(Integer id) {
        return null;
    }

    @Override
    public PagingVO<BiSubjectRefLayoutEntity> queryByPage() {
        return null;
    }


    /**
     * 新增数据
     *
     * @param biSubjectRefLayout 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean insert(BiSubjectRefLayoutEntity biSubjectRefLayout) {
        return true;
    }

    /**
     * 修改数据
     *
     * @param biSubjectRefLayout 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(BiSubjectRefLayoutEntity biSubjectRefLayout) {
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
