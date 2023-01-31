package com.erp.server.bi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.server.bi.mapper.BiDictMapper;
import com.erp.server.bi.service.BiDictService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.math3.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * bi系统字典表(BiDict)表服务实现类
 *
 * @author yl
 * @since 2022-12-08 14:24:02
 */
@Service
public class BiDictServiceImpl extends ServiceImpl<BiDictMapper, BiDictEntity> implements BiDictService {
    @Resource
    private BiDictMapper biDictMapper;

    /**
     * 通过ID查询单条数据
     *
     * @param id 主键
     * @return 实例对象
     */
    @Override
    public BiDictEntity queryById(String id) {
        return null;
    }

    @Override
    public PagingVO<BiDictEntity> queryByPage() {
        return null;
    }


    /**
     * 新增数据
     *
     * @param biDict 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean insert(BiDictEntity biDict) {
        return this.save(biDict);
    }

    /**
     * 修改数据
     *
     * @param biDict 实例对象
     * @return 实例对象
     */
    @Override
    public Boolean update(BiDictEntity biDict) {
        return true;
    }

    /**
     * 通过主键删除数据
     *
     * @param id 主键
     * @return 是否成功
     */
    @Override
    public boolean deleteById(String id) {
        return true;
    }

    @Override
    public List<Map<String, Object>> listByType(String type) {
        LambdaQueryWrapper<BiDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(BiDictEntity::getId, BiDictEntity::getName);
        queryWrapper.eq(BiDictEntity::getType, type);
        return this.listMaps(queryWrapper);
    }

    /**
     * 根据 type 获取到对应的分类id 和分类名
     *
     * @param type
     * @return
     * @author yl
     * @date 2022-12-13 11:30
     */
    @Override
    public List<Pair<String, String>> getCategory(String type) {
        List<Map<String, Object>> mapList = listByType(type);
        List<Pair<String, String>> resultList = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            Pair<String, String> pair = new Pair<>(map.get("id").toString(), map.get("name").toString());
            resultList.add(pair);
        }
        return resultList;
    }

    @Override
    public List<BiDictEntity> listEntityByType(String type) {
        LambdaQueryWrapper<BiDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDictEntity::getType, type);
        queryWrapper.orderByAsc(BiDictEntity::getId);
        return this.list(queryWrapper);
    }

    /**
     * 根据类型都值 获取字典
     *
     * @param type
     * @param flag
     * @return com.erp.model.bi.entity.BiDictEntity
     * @author yl
     * @date 2022-12-26 9:43
     */
    @Override
    public BiDictEntity getByTypeValue(String type, String flag) {
        LambdaQueryWrapper<BiDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDictEntity::getType, type);
        queryWrapper.eq(BiDictEntity::getValue, flag);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    @Override
    public List<BiDictEntity> getByType(String type) {
        LambdaQueryWrapper<BiDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BiDictEntity::getType, type);
        return this.list(queryWrapper);
    }

    @Override
    public BiDictEntity getByTypeName(String type, String name) {
        return lambdaQuery().eq(BiDictEntity::getType, type)
                .eq(BiDictEntity::getName, name)
                .oneOpt().orElse(null);
    }

    @Override
    public Map<String, BiDictEntity> listByValues(List<String> dictValues) {
        if (CollectionUtils.isEmpty(dictValues)) {
            return new HashMap<>(0);
        }
        List<BiDictEntity> list = lambdaQuery().in(BiDictEntity::getValue, dictValues)
                .list();
        if (CollectionUtils.isEmpty(list)) {
            return new HashMap<>(0);
        }
        return list.stream().collect(Collectors.toMap(BiDictEntity::getValue, e -> e));
    }
}
