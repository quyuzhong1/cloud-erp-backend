package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiDictEntity;
import org.apache.commons.math3.util.Pair;

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

    /**
     * 根据类型获取到id 和名字
     * @author yl
     * @date 2022-12-13 10:05
     * @param type
     * @return java.util.List<org.apache.commons.math3.util.Pair>
     */
    List<Pair<String,String>> getCategory(String type);

    /**
     * 根据value值查询数据字段
     * @param dictValues
     * @return
     */
    Map<String, BiDictEntity> listByValues(List<String> dictValues);

    /**
     * 根据类型查询
     */
    List<BiDictEntity> listEntityByType(String type);

    BiDictEntity getByTypeValue(String type, String dashboardFlag);

    List<BiDictEntity> getByType(String type);
}
