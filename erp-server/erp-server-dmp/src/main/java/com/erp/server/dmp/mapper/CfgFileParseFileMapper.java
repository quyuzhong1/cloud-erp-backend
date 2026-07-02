package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.CfgFileParseFileEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.CfgFileParseFileDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 清洗配置-文件识别规则子表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-06-29
 */
@Mapper
public interface CfgFileParseFileMapper extends BaseMapper<CfgFileParseFileEntity> {

}
