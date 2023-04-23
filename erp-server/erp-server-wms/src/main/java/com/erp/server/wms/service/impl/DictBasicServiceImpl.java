package com.erp.server.wms.service.impl;

import com.common.business.service.RedisService;
import com.common.business.service.SuperServiceImpl;
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
    public Boolean saveOrUpdateDict(List<DictBasicDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return true;
        }
        List<DictBasicEntity> addList = BeanMapper.copyList(list, DictBasicEntity.class);
        Boolean result = this.saveOrUpdateBatch(addList);
        //当保存成功
//        if (result) {
//            String redisKey = RedisCacheConstants.WMS_DICT_KEY;
//            redisService.deleteObject(redisKey);
//        }
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
    public List<DictBasicDTO> getByKey(String key) {
        List<DictBasicEntity> list = listByKey(key);
        List<DictBasicDTO> resultList = BeanMapper.copyList(list, DictBasicDTO.class);
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
        List<DictBasicEntity> allList = listAll();
        return allList.stream().filter(l -> keyList.contains(l.getType())).collect(Collectors.toList());
    }


    private List<DictBasicEntity> listByKey(String type) {
        if (StringUtils.isBlank(type)) {
            return Collections.emptyList();
        }
        List<DictBasicEntity> allList = listAll();
        return allList.stream().filter(l -> l.getType().equals(type)).collect(Collectors.toList());
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
