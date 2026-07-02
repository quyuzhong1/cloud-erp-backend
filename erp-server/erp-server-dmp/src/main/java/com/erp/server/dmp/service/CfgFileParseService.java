package com.erp.server.dmp.service;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.CfgFileParseDTO;
import com.erp.model.dmp.entity.CfgFileParseEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 月结文件解析配置服务。
 *
 * @author jack
 * @since 2026-06-29
 */
public interface CfgFileParseService extends SuperService<CfgFileParseEntity> {

    /**
     * 新增月结文件解析配置。
     *
     * @param dto 新增参数
     * @return 新增结果
     */
    BaseResultDTO.AddDTO add(CfgFileParseDTO.AddDTO dto);

    /**
     * 修改月结文件解析配置。
     *
     * @param dto 修改参数
     * @return 是否成功
     */
    Boolean update(CfgFileParseDTO.UpdateDTO dto);

    /**
     * 分页查询月结文件解析配置。
     *
     * @param pagingParamDTO 分页查询参数
     * @return 分页结果
     */
    PagingVO<CfgFileParseDTO.ListDTO> paging(PagingDTO<CfgFileParseDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 查询启用状态页签数量。
     *
     * @param dto 权限参数
     * @return 页签数量
     */
    List<CfgFileParseDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 查询月结文件解析配置详情。
     *
     * @param id 配置 ID
     * @return 配置详情
     */
    CfgFileParseDTO.ViewDTO view(String id);

    /**
     * 创建月结文件解析配置异步导出任务。
     *
     * @param dto 导出筛选条件
     * @return 是否创建成功
     */
    Boolean exportList(CfgFileParseDTO.ExportDTO dto);

    /**
     * 分页查询月结文件解析配置导出数据。
     *
     * @param pagingParamDTO 导出分页参数
     * @return 导出分页结果
     */
    PagingVO<CfgFileParseDTO.ListDTO> exportPaging(PagingDTO<CfgFileParseDTO.ExportDTO> pagingParamDTO);

    /**
     * 删除单条月结文件解析配置。
     *
     * @param id 配置 ID
     * @return 处理结果
     */
    BatchResultDTO delete(String id);

    /**
     * 更新单条月结文件解析配置启用状态。
     *
     * @param id 配置 ID
     * @param disabled true=停用，false=启用
     * @return 处理结果
     */
    BatchResultDTO updateDisabled(String id, Boolean disabled);
}
