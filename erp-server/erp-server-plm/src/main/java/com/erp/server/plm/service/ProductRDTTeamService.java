package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductRDTTeamDTO;
import com.erp.model.plm.entity.ProductRDTTeamEntity;

import java.util.List;

/**
 * 产品研发团队服务类
 * @Author Auto
 * @Date 2025/01/20
 **/
public interface ProductRDTTeamService extends IService<ProductRDTTeamEntity> {

    /**
     * 保存/修改产品研发团队-批量
     * @param productRDTTeamList 产品研发团队新增信息
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductRDTTeamDTO> productRDTTeamList);

    /**
     * 查询产品研发团队
     * @return java.util.List<com.erp.model.plm.entity.ProductRDTTeamEntity>
     **/
    List<ProductRDTTeamEntity> listProductRDTTeam();

    /**
     * 删除产品研发团队
     * @param id 主键id
     * @return java.lang.Boolean
     **/
    Boolean delete(String id);

    /**
     * 查询研发团队名称是否存在
     * @param name 研发团队名称
     * @return ProductRDTTeamEntity
     **/
    ProductRDTTeamEntity checkRDTTeamName(String name);

    /**
     * 设置占用
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean setupOccupy(List<String> ids);

    /**
     * 根据研发团队名称查询
     * @param name
     * @return ProductRDTTeamEntity
     */
    ProductRDTTeamEntity getByName(String name);
}

