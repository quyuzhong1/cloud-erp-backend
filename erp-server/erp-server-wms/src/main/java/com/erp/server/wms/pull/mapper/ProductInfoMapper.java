package com.erp.server.wms.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 产品信息表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProductInfoMapper extends BaseMapper<ProductInfoEntity> {

    /**
     * 获取所有产品信息包括删除，用来同步到DMP
     * @Author Luo_WG
     * @Date 2023/4/19 16:22
     * @return java.util.List<com.erp.model.plm.entity.ProductInfoEntity>
     **/
    List<ProductInfoEntity> getProductInfoAll();

    /**
     * 获取所有非空的spu no
     * @return
     */
    List<String> getNotEmptySpuNos();

    /**
     * 根据id查询（包含已删除的数据）
     * @param id
     * @return
     */
    ProductInfoEntity selectById(String id);

    /**
     * 更新所有字段包括逻辑删除字段
     * @param productInfoEntity
     * @return
     */
    int updateAllById(ProductInfoEntity productInfoEntity);

    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    List<ProductInfoEntity> listProductInfoByIds(@Param("ids") List<String> ids);

    /**
     * 批量修改
     * @Author Luo_WG
     * @Date 2023/6/13 11:03
     * @param list
     * @return int
     **/
    int updateBatchSelective(List<ProductInfoEntity> list);

}
