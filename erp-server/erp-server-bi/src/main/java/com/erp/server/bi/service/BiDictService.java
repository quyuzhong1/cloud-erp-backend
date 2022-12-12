package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiDictEntity;

import java.util.List;
import java.util.Map;

/**
 * bi系统字典表(BiDict)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:24:02
 */
public interface BiDictService  extends IService<BiDictEntity> {

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    BiDictEntity queryById(String id);

    /**
     * 分页查询
     *
     * @param
     * @param
     * @return 查询结果
     */
    PagingVO<BiDictEntity> queryByPage();

    /**
     * 新增数据
     *
     * @param biDict 实例对象
     * @return 实例对象
     */
    Boolean insert(BiDictEntity biDict);

    /**
     * 修改数据
     *
     * @param biDict 实例对象
     * @return 实例对象
     */
    Boolean update(BiDictEntity biDict);

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    boolean deleteById(String id);

    List<Map<String,Object>> listByType(String type);
}
