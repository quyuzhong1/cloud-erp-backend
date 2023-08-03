package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.StocktakingTaskDTO;
import com.erp.model.wms.entity.StocktakingTaskEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 盘点任务表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
public interface StocktakingTaskService extends SuperService<StocktakingTaskEntity> {

    /**
     * tab list
     * @param dto
     * @return
     */
    List<StocktakingTaskDTO.TabDTO> tabList(PermissionsDTO dto);

    /**
     * 分页
     * @author yl
     * @date 2023-08-03 17:26
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.StocktakingTaskDTO.PagingViewDTO>
     */
    PagingVO<StocktakingTaskDTO.PagingViewDTO> paging(PagingDTO<StocktakingTaskDTO.PagingParamDTO> dto);

    /**
     * 提交审核
     * @param ids
     * @return
     */
    Boolean submit(List<String> ids);

    /**
     * 详情
     * @author yl
     * @date 2023-08-03 17:35
     * @param id
     * @return com.erp.model.wms.dto.StocktakingTaskDTO.ViewDTO
     */
    StocktakingTaskDTO.ViewDTO view(String id);

    /**
     * 审核
     * @param dto
     * @return
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 撤销流程
     * @param ids
     * @return
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 分配用户
     * @author yl
     * @date 2023-08-03 17:36
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean assignUser(StocktakingTaskDTO.AssignUserDTO dto);

    /**
     * 导出excel
     * @author yl
     * @date 2023-08-03 17:37
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(StocktakingTaskDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 导入盘点任务
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    
    /**
     * 下载模板
     * @author yl
     * @date 2023-08-03 17:38
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);
}
