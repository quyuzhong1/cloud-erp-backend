package com.erp.server.mrp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.mrp.entity.FbaInTransitDetailEntity;
import com.erp.model.mrp.vo.FbaInTransitDetailVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * fba在途明细 Mapper 接口
 * </p>
 *
 * @author liaohui
 * @since 2024-08-28
 */
@Mapper
public interface FbaInTransitDetailMapper extends BaseMapper<FbaInTransitDetailEntity> {

    Page<FbaInTransitDetailVO> fbaInTransitDetail(@Param("page") Page<FbaInTransitDetailVO> page,@Param("detailId") String detailId);
}
