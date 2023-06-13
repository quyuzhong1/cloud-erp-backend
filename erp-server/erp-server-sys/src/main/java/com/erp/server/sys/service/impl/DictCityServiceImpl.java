package com.erp.server.sys.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.common.business.service.SuperServiceImpl;
import com.common.core.utils.BeanMapper;
import com.erp.model.sys.dto.DictCityDTO;
import com.erp.model.sys.entity.DictCityEntity;
import com.erp.server.sys.mapper.DictCityMapper;
import com.erp.server.sys.service.DictCityService;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-03-21
 */
@Service
public class DictCityServiceImpl extends SuperServiceImpl<DictCityMapper, DictCityEntity> implements DictCityService {


    private String province = "province";

    private String city = "city";

    @Override
    public Boolean add(DictCityDTO.AddDTO dto) {
        List<DictCityEntity> batchList = new LinkedList<>();
        getSaveTree("0", batchList, dto);
        return this.saveBatch(batchList);
    }


    /**
     * 获取省城市
     *
     * @param countryCode
     * @return java.util.List<com.erp.model.sys.dto.DictCityDTO.ListDTO>
     * @author yl
     * @date 2023-05-11 16:30
     */
    @Override
    public List<DictCityDTO.ListDTO> listCity(String countryCode) {

        //根据 国家code 获取城市信息
        List<DictCityEntity> allList = this.listByCountryCode(countryCode);
        List<DictCityDTO.ListDTO> flagList = BeanMapper.copyList(allList, DictCityDTO.ListDTO.class);
        List<DictCityDTO.ListDTO> treeList = flagList.stream().
                filter(item -> "0".equals(item.getParentId())).
                map(obj -> {
                    obj.setChildrenList(getChildren(obj, flagList));
                    return obj;
                }).collect(Collectors.toList());

        return treeList;
    }


    /**
     * 获取子集信息
     *
     * @param item
     * @param flagList
     * @return java.util.List<com.erp.model.sys.dto.DictCityDTO.ListDTO>
     * @author yl
     * @date 2023-05-11 17:26
     */
    private List<DictCityDTO.ListDTO> getChildren(DictCityDTO.ListDTO item, List<DictCityDTO.ListDTO> flagList) {
        List<DictCityDTO.ListDTO> collect = flagList.stream().filter(city -> item.getId().equals(city.getParentId())).
                map(c -> {
                    c.setChildrenList(getChildren(c, flagList));
                    return c;
                }).collect(Collectors.toList());
        return CollectionUtils.isEmpty(collect) ? null : collect;

    }

    private List<DictCityEntity> listByCountryCode(String countryCode) {
        List<DictCityEntity> list = this.lambdaQuery().eq(DictCityEntity::getCountryCode, countryCode).list();
        return list;
    }


    /**
     * 保存树结构
     *
     * @param parentId
     * @param batchList
     * @param dto
     * @return void
     * @author yl
     * @date 2023-05-11 15:33
     */
    private void getSaveTree(String parentId, List<DictCityEntity> batchList, DictCityDTO.AddDTO dto) {
        DictCityEntity entity = new DictCityEntity();
        BeanMapper.copy(dto, entity);
        String id = IdWorker.getIdStr();
        entity.setParentId(parentId);
        entity.setId(id);
        if (parentId.equals("0")) {
            entity.setType(province);
        } else {
            entity.setType(city);
        }
        batchList.add(entity);
        List<DictCityDTO.AddDTO> subList = dto.getChildrenList();
        if (CollectionUtils.isNotEmpty(subList)) {
            for (DictCityDTO.AddDTO item1 : subList) {
                this.getSaveTree(id, batchList, item1);
            }
        }
    }
}
