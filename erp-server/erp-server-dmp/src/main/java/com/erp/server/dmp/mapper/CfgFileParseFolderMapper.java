package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.CfgFileParseFolderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.CfgFileParseFolderDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * 清洗配置-文件夹映射子表 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-06-29
 */
@Mapper
public interface CfgFileParseFolderMapper extends BaseMapper<CfgFileParseFolderEntity> {

}
