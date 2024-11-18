package com.erp.server.wms.service.impl;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.service.impl.RedisService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.dto.DictBasicDTO;
import com.erp.model.wms.entity.DictBasicEntity;
import com.erp.server.wms.mapper.DictBasicMapper;
import com.erp.server.wms.service.DictBasicService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 字典表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-16
 */
@Service
@Slf4j
public class DictBasicServiceImpl extends SuperServiceImpl<DictBasicMapper, DictBasicEntity> implements DictBasicService {


    @Resource
    private RedisService redisService;

    /**
     * 保存或者修改字典信息
     *
     * @param list
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-03-17 12:21
     */
    @Override
    public Boolean saveOrUpdateDict(List<DictBasicDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        Boolean result = this.saveOrUpdateBatch(addList);

        return result;
    }


    /**
     * 根据key 获取字典数据
     *
     * @param key
     * @return
     * @author yl
     * @date 2023-03-17 14:16
     */
    @Override
    public List<DictBasicDTO.ListDTO> getByKey(String key) {
        List<DictBasicEntity> list = listByKey(key);
        List<DictBasicDTO.ListDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.ListDTO.class);
        return resultList;
    }


    /**
     * 根据key list 获取对应数据
     *
     * @param keyList
     * @return java.util.List<com.erp.model.scm.entity.DictBasicEntity>
     * @author yl
     * @date 2023-03-20 14:24
     */
    @Override
    public List<DictBasicEntity> getByKeyList(List<String> keyList) {
        if (CollectionUtils.isEmpty(keyList)) {
            return listAll();
        }
        List<DictBasicEntity> allList = this.lambdaQuery().in(DictBasicEntity::getType, keyList).list();
        return allList;
    }

    @Override
    public List<DictBasicDTO.DropDownDTO> listByType(String type, String remark) {
        List<DictBasicEntity> list = lambdaQuery().eq(DictBasicEntity::getType, type)
                .eq("processCondition".equalsIgnoreCase(type), DictBasicEntity::getRemark, remark)
                .orderByAsc(DictBasicEntity::getSort)
                .list();
        List<DictBasicDTO.DropDownDTO> result = list.stream().map(DictBasicDTO.DropDownDTO::new).collect(Collectors.toList());
        return result;
    }


    private List<DictBasicEntity> listByKey(String type) {
        if (CharSequenceUtil.isBlank(type)) {
            return Collections.emptyList();
        }
        List<DictBasicEntity> allList = this.lambdaQuery().
                eq(DictBasicEntity::getType,type).
                orderByAsc(DictBasicEntity::getSort).list();
        return  allList;
    }

    /**
     * 获取 所有的
     *
     * @return
     */
    private List<DictBasicEntity> listAll() {

//        String redisKey = RedisCacheConstants.WMS_DICT_KEY;
//        List<DictBasicEntity> dictList = redisService.getCacheList(redisKey);
//        if (CollectionUtils.isNotEmpty(dictList)) {
//            return dictList;
//        }
        List<DictBasicEntity> list = this.list();
//        if (CollectionUtils.isNotEmpty(list)) {
//            redisService.setCacheList(redisKey, list);
//        }
        return list;

    }


}
