package com.erp.server.bi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiSubjectRefLayoutEntity;

/**
 * 专题与布局关系表(BiSubjectRefLayout)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:32:40
 */
public interface BiSubjectRefLayoutService  extends IService<BiSubjectRefLayoutEntity> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BiSubjectRefLayoutEntity queryById(Integer id);

    /**
     * 分页查询
     *

     * @return 查询结果
     */
    PagingVO<BiSubjectRefLayoutEntity> queryByPage();

    /**
     * 新增数据
     *
     * @param biSubjectRefLayout 实例对象
     * @return 实例对象
     */
    Boolean insert(BiSubjectRefLayoutEntity biSubjectRefLayout);

    /**
     * 修改数据
     *
     * @param biSubjectRefLayout 实例对象
     * @return 实例对象
     */
    Boolean update(BiSubjectRefLayoutEntity biSubjectRefLayout);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(String id);

}
