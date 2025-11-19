package com.erp.server.plm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.erp.model.plm.dto.ProductRDTTeamDTO;
import com.erp.model.plm.entity.ProductRDTTeamEntity;
import com.erp.server.plm.mapper.ProductRDTTeamMapper;
import com.erp.server.plm.service.ProductRDTTeamService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 产品研发团队服务实现类
 * @Author Auto
 * @Date 2025/01/20
 **/
@Service
public class ProductRDTTeamServiceImpl extends ServiceImpl<ProductRDTTeamMapper, ProductRDTTeamEntity>
    implements ProductRDTTeamService {

    /**
     * 保存/修改产品研发团队
     * @param productRDTTeamList 产品研发团队新增信息
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean saveOrUpdateBatch(List<ProductRDTTeamDTO> productRDTTeamList) {
        List<ProductRDTTeamEntity> productRDTTeamEntities = BeanMapper.copyList(productRDTTeamList, ProductRDTTeamEntity.class);
        return this.saveOrUpdateBatch(productRDTTeamEntities);
    }

    /**
     * 查询产品研发团队
     * @return java.util.List<com.erp.model.plm.entity.ProductRDTTeamEntity>
     **/
    @Override
    public List<ProductRDTTeamEntity> listProductRDTTeam(){
        return this.list();
    }

    /**
     * 删除产品研发团队
     * @param id 主键id
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean delete(String id){
        ProductRDTTeamEntity entity = this.getById(id);
        if (entity.getOccupyStatus()) {
            throw new ServiceException(ApiError.ERROR_95168);
        }
        LambdaQueryWrapper<ProductRDTTeamEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductRDTTeamEntity::getId, id);
        return this.remove(queryWrapper);
    }

    /**
     * 查询研发团队名称是否存在
     * @param name 研发团队名称
     * @return ProductRDTTeamEntity
     **/
    @Override
    public ProductRDTTeamEntity checkRDTTeamName(String name){
        LambdaQueryWrapper<ProductRDTTeamEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ProductRDTTeamEntity::getName, name);
        queryWrapper.last("LIMIT 1");
        return this.getOne(queryWrapper);
    }

    /**
     * 设置占用
     * @Author Auto
     * @Date 2025/01/20
     * @param ids
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean setupOccupy(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Boolean.TRUE;
        }
        return lambdaUpdate().set(ProductRDTTeamEntity::getOccupyStatus, Boolean.TRUE).in(ProductRDTTeamEntity::getId, ids).update();
    }

    @Override
    public ProductRDTTeamEntity getByName(String name) {
        ProductRDTTeamEntity entity = lambdaQuery().eq(ProductRDTTeamEntity::getName, name)
                .last("limit 1")
                .one();
        return entity;
    }
}

