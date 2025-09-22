package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SampleLedgerEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SampleLedgerDTO;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 样品台账统计 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-08-21
 */
public interface SampleLedgerService extends SuperService<SampleLedgerEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SampleLedgerDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-08-21
    * @param dto
    * @return
    */
    Boolean update(SampleLedgerDTO.UpdateDTO dto);

    /**
     * 分页列表查询
     * @author wuhaotian
     * @date: 2025-08-21
     * @param pagingParamDTO
     * @return PagingVO<SampleLedgerDTO.ListDTO>>
     */
    PagingVO<SampleLedgerDTO.ListDTO> paging(PagingDTO<SampleLedgerDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @return List<SampleLedgerDTO.TabListDTO>>
     */
    List<SampleLedgerDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 状态统计（支持数量为0不显示）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto 权限参数
     * @param hideZeroQty 是否隐藏数量为0的记录
     * @return List<SampleLedgerDTO.TabListDTO>>
     */
    List<SampleLedgerDTO.TabListDTO> tabList(PermissionsDTO dto, Boolean hideZeroQty);

    /**
     * 异步导出
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto
     * @param response
     * @return
     */
    Boolean exportList(SampleLedgerDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 获取样品台账统计分页数据（用于异步导出）
     * @author wuhaotian
     * @date: 2025-08-21
     * @param dto 分页参数
     * @return 分页结果
     */
    PagingVO<SampleLedgerDTO.ListDTO> getSampleLedgerPageData(PagingDTO<SampleLedgerDTO.ExportDTO> dto);

    /**
     * 根据用户ID查询台账列表
     * @param dto 查询条件对象，包含用户ID、SKU编号等查询参数
     * @return 符合条件的台账实体列表，如果查询条件为空则返回空列表
     */
    List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerByUserId(SampleLedgerDTO.SearchDTO dto);

    PagingVO<SampleLedgerDTO.SkuAvailableQtyDTO> listSku(PagingDTO<SampleLedgerDTO.SearchDTO> pagingDTO);

    SampleLedgerDTO.SampleScrapView generateSampleScrapView( List<String> ids);

    SampleLedgerDTO.ExhibitionOrderView generateExhibitionOrderView( List<String> ids);

    SampleLedgerDTO.SampleBackView generateSampleBackInfo( List<String> ids);

    // ========== APP端专用方法 ==========

    /**
     * APP端标签页列表
     * @author wuhaotian
     * @date: 2025-09-15
     * @param dto 权限参数
     * @return 标签页列表
     */
    List<SampleLedgerDTO.TabListDTO> tabListApp(PermissionsDTO dto);

    /**
     * APP端分页查询
     * @author wuhaotian
     * @date: 2025-09-15
     * @param pagingParamDTO 分页参数
     * @return 分页结果
     */
    PagingVO<SampleLedgerDTO.ListDTO> pagingApp(PagingDTO<SampleLedgerDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 详情
     * @author wuhaotian
     * @date: 2025-09-15
     * @param id 样品台账ID
     * @return 详情信息
     */
    SampleLedgerDTO.ViewDTO view(String id);

    List<SampleLedgerDTO.SkuAvailableQtyDTO> listLedgerAll(SampleLedgerDTO.SearchAllDTO dto);
}
