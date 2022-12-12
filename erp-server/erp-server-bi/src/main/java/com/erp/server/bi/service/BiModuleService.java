package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiModuleEntity;

/**
 * 模块表(BiModule)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:31:14
 */
public interface BiModuleService  extends IService<BiModuleEntity> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BiModuleEntity queryById(String id);

    /**
     * 分页查询
     *
     * @param
     * @param
     * @return 查询结果
     */
    PagingVO<BiModuleEntity> queryByPage();

    /**
     * 新增数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    Boolean insert(BiModuleEntity biModule);

    /**
     * 修改数据
     *
     * @param biModule 实例对象
     * @return 实例对象
     */
    Boolean update(BiModuleEntity biModule);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(String id);

}
