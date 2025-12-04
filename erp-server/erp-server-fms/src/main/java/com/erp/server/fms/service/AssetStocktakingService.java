package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetStocktakingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetStocktakingDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 资产盘点表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetStocktakingService extends SuperService<AssetStocktakingEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetStocktakingDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetStocktakingDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author wuht
    * @date: 2025-10-11
    * @param pagingParamDTO
    * @return PagingVO<AssetStocktakingDTO.ListDTO>>
    */
    PagingVO<AssetStocktakingDTO.ListDTO> paging(PagingDTO<AssetStocktakingDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return List<AssetStocktakingDTO.TabListDTO>>
    */
    List<AssetStocktakingDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    AssetStocktakingDTO.ViewDTO view(String id);

    /**
    * 新增并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return BaseResultDTO.AddDTO
    */
    BaseResultDTO.AddDTO addAndSubmit(AssetStocktakingDTO.AddDTO dto);

    /**
    * 修改并提交审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    void updateAndSubmit(AssetStocktakingDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author wuht
     * @date: 2025-10-11
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
    * 审核
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
    * 反审核
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
    * 删除
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);
    /**
    * 作废
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @param remark
    * @return
    */
    BatchResultDTO invalid(String id, String remark);

    /**
    * 撤销
    * @author wuht
    * @date: 2025-10-11
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
    * 导出Excel
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @param response
    * @return
    */
    void exportList(AssetStocktakingDTO.ExportDTO dto, HttpServletResponse response);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, AssetStocktakingEntity entity);

    /**
    * 生成卡片编码
    * @author wuht
    * @date: 2025-10-31
    * @return String
    */
    String generateCardCode();

    /**
    * 获取资产盘点单分页数据（用于异步导出）
    * @author wuht
    * @date: 2025-10-31
    * @param dto 分页参数
    * @return PagingVO<AssetStocktakingDTO.ListDTO>
    */
    PagingVO<AssetStocktakingDTO.ListDTO> getAssetStocktakingPageData(PagingDTO<AssetStocktakingDTO.ExportDTO> dto);

    /**
    * 资产盘点表下拉列表
    * @author wuht
    * @date: 2025-11-04
    * @param keyword 关键字（支持盘点单号、来源单号模糊查询）
    * @return List<AssetStocktakingDTO.DropDownDTO>
    */
    List<AssetStocktakingDTO.DropDownDTO> dropDownList(String keyword);

    /**
    * 获取流程变量Map
    * @author wuht
    * @date: 2025-11-06
    * @param entity
    * @return Map<String, Object>
    */
    Map<String, Object> getVariablesMap(AssetStocktakingEntity entity);
}
