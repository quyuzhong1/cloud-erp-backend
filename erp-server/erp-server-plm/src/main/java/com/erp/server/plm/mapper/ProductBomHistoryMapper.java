package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.erp.model.plm.entity.ProductBomHistoryEntity;
import com.erp.model.plm.vo.BomVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * bom 历史表(ProductBomHistory)表数据库访问层
 *
 * @author yl
 * @since 2023-01-11 14:04:50
 */
@Mapper
public interface ProductBomHistoryMapper extends BaseMapper<ProductBomHistoryEntity> {


    List<BomVO> getVersionList(@Param("bomId") String bomId);
}

