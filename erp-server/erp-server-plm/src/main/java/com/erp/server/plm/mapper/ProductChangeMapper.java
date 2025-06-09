package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.SearchPagingDTO;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.vo.ProductChangePagingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 变更信息表(ProductChange)表数据库访问层
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@Mapper
public interface ProductChangeMapper extends BaseMapper<ProductChangeEntity> {


    IPage<ProductChangePagingVO> paging(Page query, @Param("searchList") List<String> changeSearch, @Param("params") SearchPagingDTO params);

    List<String> getChangeSearchCondition(@Param("searchKeyword") String searchKeyword);
}

