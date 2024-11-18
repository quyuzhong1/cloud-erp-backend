package com.erp.server.plm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.plm.entity.BomOperateLogEntity;
import com.erp.model.plm.vo.BomOperateVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * bom 操作记录日志表(BomOperateLog)表数据库访问层
 *
 * @author yl
 * @since 2023-01-09 11:45:23
 */
@Mapper
public interface BomOperateLogMapper  extends BaseMapper<BomOperateLogEntity> {


    IPage<BomOperateVO> paging(Page<BaseIdDTO> query, @Param("bomId") String bomId);
}

