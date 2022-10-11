package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.plm.dto.ProductSearchDTO;
import com.erp.model.plm.dto.StartItemSourceDTO;
import com.erp.model.plm.entity.ProjectInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 产品项目表 Mapper 接口
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
@Mapper
public interface ProjectInfoMapper extends BaseMapper<ProjectInfoEntity> {

    List<StartItemSourceDTO> listMap(@Param("sourceType") Integer sourceType);

    IPage paging(Page query, @Param("params") ProductSearchDTO params,@Param("archiveProductIds") List<String> archiveProductIds);

    IPage myCollectPaging(Page query, @Param("params") ProductSearchDTO params, @Param("productIds") List<String> productIds,@Param("archiveProductIds") List<String> archiveProductIds);
}
