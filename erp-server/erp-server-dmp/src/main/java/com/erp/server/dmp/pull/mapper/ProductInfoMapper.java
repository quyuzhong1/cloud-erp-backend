package com.erp.server.dmp.pull.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductInfoMapper extends BaseMapper<ProductInfoEntity> {
    /**
     * 根据主键Id查询产品表信息
     * @Author Luo_WG
     * @Date 2023/4/19 16:25
     * @param ids
     * @return com.erp.model.plm.entity.ProductInfoEntity
     **/
    List<ProductInfoEntity> ListProductInfoByIds(@Param("ids") List<String> ids);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/6/13 11:03
     * @param record record
     * @return int
     **/
    int updateByPrimaryKeySelective(ProductInfoEntity record);

    /**
     * 批量修改
     * @Author Luo_WG
     * @Date 2023/6/13 11:03
     * @param list
     * @return int
     **/
    int updateBatchSelective(List<ProductInfoEntity> list);
}
