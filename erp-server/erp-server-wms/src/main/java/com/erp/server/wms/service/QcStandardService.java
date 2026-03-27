package com.erp.server.wms.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcStandardDTO;
import com.erp.model.wms.entity.QcStandardEntity;

import java.util.List;

/**
 * 质检标准主表 Service 接口
 *
 * @author jack
 * @since 2026-03-22
 */
public interface QcStandardService extends IService<QcStandardEntity> {

    /**
     * 新增质检标准
     *
     * @param addDTO 新增参数
     */
    void add(QcStandardDTO.AddDTO addDTO);

    /**
     * 修改质检标准
     *
     * @param updateDTO 修改参数
     */
    void update(QcStandardDTO.UpdateDTO updateDTO);

    /**
     * 获取 Tab 列表统计
     *
     * @param permissionsDTO 查询参数
     * @return Tab 统计列表
     */
    java.util.List<QcStandardDTO.TabListDTO> tabList(PermissionsDTO permissionsDTO);

    /**
     * 分页查询
     *
     * @param pagingParamDTO 分页参数
     * @return 分页结果
     */
    PagingVO<QcStandardDTO.ListDTO> paging(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 导出查询
     *
     * @param pagingParamDTO 导出参数
     */
    void export(PagingDTO<QcStandardDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 删除单据
     * @param id 主键 ID
     * @return 业务结果
     */
    BatchResultDTO delete(String id);

    /**
     * 更新启用/禁用状态
     * @param params 状态更新参数
     * @return 业务结果
     */
    BatchResultDTO updateStatus(QcStandardDTO.UpdateStatusDTO params);

    /**
     * 查询记录详情
     * @param id 主键 ID
     * @return 详情 DTO
     */
    QcStandardDTO.ViewDTO view(String id);

    /**
     * 根据 SKU 编码复制质检标准数据 (不保存)
     * @param skuNo SKU 编码
     * @return 完整详情数据
     */
    QcStandardDTO.ViewDTO copyBySku(String skuNo);

    PagingVO<QcStandardDTO.ExportDTO> exportList(PagingDTO<QcStandardDTO.PagingParamDTO> dto);

    List<String> listSkuNoByUrl(String fileUrl);

    /**
     * 导入质检标准
     * @param fileUrl 文件URL
     */
    void genQcStandardByUrl(List<String> skuNos, String fileUrl);
}
