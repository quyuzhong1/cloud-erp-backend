package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.ProductUnitDTO;
import com.erp.model.plm.entity.ProductUnitEntity;
import java.util.List;

/**
 * @Description 产品信息服务类
 * @Author Luo_WG
 * @Date 2022/9/27 15:14
 **/
public interface ProductUnitService extends IService<ProductUnitEntity> {

    /**
     * @Description 保存/修改产品单位-批量
     * @Author Luo_WG
     * @Date 2022/9/27 15:08
     * @param productUnitList:产品单位新增信息
     * @return java.lang.Boolean
     **/
    Boolean saveOrUpdateBatch(List<ProductUnitDTO> productUnitList) ;

    /**
     * @Description 查询产品单位
     * @Author Luo_WG
     * @Date 2022/9/27 15:02
     * @return java.util.List<com.erp.model.plm.entity.ProductUnitEntity>
     **/
    List<ProductUnitEntity> listProductUnit();

    /**
     * @Description 删除产品单位
     * @Author Luo_WG
     * @Date 2022/9/27 15:02
     * @param id:主键id
     * @return java.lang.Boolean
     **/
    Boolean delete(String id);

    /**
     * @Description 查询单位名称是否存在
     * @Author Luo_WG
     * @Date 2022/9/27 15:02
     * @param name 单位名称
     * @return java.lang.Boolean
     **/
    ProductUnitEntity checkUnitName(String name);

    /**
     * 设置占用
     * @Author Luo_WG
     * @Date 2023/6/14 11:36
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean setupOccupy(List<String> ids);
}
