package com.erp.server.dmp.mapper;
import com.erp.model.dmp.entity.DmpFeishuUserInfoEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;

import com.erp.model.dmp.dto.DmpFeishuUserInfoDTO;
import com.common.business.dto.base.ApproveStatusQtyDTO;
import java.util.List;

/**
 * <p>
 * DMP飞书用户信息 Mapper 接口
 * </p>
 *
 * @author jack
 * @since 2026-01-13
 */
@Mapper
public interface DmpFeishuUserInfoMapper extends BaseMapper<DmpFeishuUserInfoEntity> {

    /**
    * 分页查询
    * @param query
    * @param params
    * @return
    */
    IPage<DmpFeishuUserInfoDTO.ListDTO> paging(Page query, @Param("params") DmpFeishuUserInfoDTO.PagingParamDTO params);

    /**
    * 状态数量
    * @param params
    * @return
    */
    List<ApproveStatusQtyDTO> listCount(@Param("params") DmpFeishuUserInfoDTO.PagingParamDTO params);

    /**
    * 导出Excel查询
    * @param params
    * @return
    */
    List<DmpFeishuUserInfoDTO.ListDTO> listExport(@Param("params") DmpFeishuUserInfoDTO.ExportDTO params);


    /**
    * 获取状态统计
    * @param searchParam
    * @return
    */
    List<DmpFeishuUserInfoDTO.TabListDTO> tabList(@Param("params") DmpFeishuUserInfoDTO.PagingParamDTO searchParam);
}
