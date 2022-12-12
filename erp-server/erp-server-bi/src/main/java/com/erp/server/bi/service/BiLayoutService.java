package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiLayoutEntity;

/**
 * 布局表(BiLayout)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:28:27
 */
public interface BiLayoutService  extends IService<BiLayoutEntity> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BiLayoutEntity queryById(String id);

    /**
     * 分页查询
     *
     * @param
     * @param
     * @return 查询结果
     */
    PagingVO<BiLayoutEntity> queryByPage();

    /**
     * 新增数据
     *
     * @param biLayout 实例对象
     * @return 实例对象
     */
    Boolean insert(BiLayoutEntity biLayout);

    /**
     * 修改数据
     *
     * @param biLayout 实例对象
     * @return 实例对象
     */
    Boolean update(BiLayoutEntity biLayout);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(String id);

}
