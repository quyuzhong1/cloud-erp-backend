package com.erp.server.plm.service;

import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.entity.SkuStdCostDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.SkuStdCostDetailDTO;
import com.erp.model.plm.entity.SkuStdCostEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * sku标准成本明细表 服务类
 * </p>
 *
 * @author Jim
 * @since 2025-08-08
 */
public interface SkuStdCostDetailService extends SuperService<SkuStdCostDetailEntity> {

    /**
     * 价格变更列表(校验列表是否可变更)
     * @param dto
     * @return
     */
    List<SkuStdCostDetailDTO.ListDTO> changeList(BaseIdsDTO.IdsDTO dto);

    /**
     * 修改变更
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO changeAdd(SkuStdCostDetailDTO.ChangeDTO dto);


    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    Boolean update(SkuStdCostDetailDTO.UpdateDTO dto);

    /**
     * 分页列表查询
     *
     * @param pagingParamDTO
     * @return PagingVO<SkuStdCostDetailDTO.ListDTO>>
     * @author Jim
     * @date: 2025-08-08
     */
    PagingVO<SkuStdCostDetailDTO.ListDTO> paging(PagingDTO<SkuStdCostDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     *
     * @param dto
     * @return List<SkuStdCostDetailDTO.TabListDTO>>
     * @author Jim
     * @date: 2025-08-08
     */
    List<SkuStdCostDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    SkuStdCostDetailDTO.ViewDTO view(String id);


    /**
     * 修改并提交审核
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    void updateAndSubmit(SkuStdCostDetailDTO.UpdateDTO dto);

    /**
     * 提交审核
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO submit(String id);

    /**
     * 审核
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
     * 反审核
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO disApprove(String id);

    /**
     * 删除
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO delete(String id);

    /**
     * 撤销
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    BatchResultDTO cancelProcess(String id);

    /**
     * 导出Excel
     *
     * @param dto
     * @param response
     * @return
     * @author Jim
     * @date: 2025-08-08
     */
    void exportList(SkuStdCostDetailDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 审核通过回调方法
     *
     * @param dto
     * @param entity
     * @return
     */
    Boolean approveEnd(ApproveOneDTO dto, SkuStdCostDetailEntity entity);

    /**
     * sku标准成本-导入
     * @author Jim
     * @date:  2025-08-08
     * @param excelFile
     * @param response
     */
    boolean importFile(MultipartFile excelFile, HttpServletResponse response);

}
