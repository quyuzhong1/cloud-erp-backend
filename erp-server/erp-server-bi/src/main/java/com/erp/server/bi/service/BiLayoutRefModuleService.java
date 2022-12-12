package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;

/**
 * 布局与模块关系表(BiLayoutRefModule)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
public interface BiLayoutRefModuleService  extends IService<BiLayoutRefModuleEntity> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BiLayoutRefModuleEntity queryById(String id);

    /**
     * 分页查询
     *
     * @param
     * @param
     * @return 查询结果
     */
    PagingVO<BiLayoutRefModuleEntity> queryByPage();

    /**
     * 新增数据
     *
     * @param biLayoutRefModule 实例对象
     * @return 实例对象
     */
    Boolean insert(BiLayoutRefModuleEntity biLayoutRefModule);

    /**
     * 修改数据
     *
     * @param biLayoutRefModule 实例对象
     * @return 实例对象
     */
    Boolean update(BiLayoutRefModuleEntity biLayoutRefModule);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    Boolean deleteById(String id);

}
