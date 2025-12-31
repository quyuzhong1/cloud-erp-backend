package com.erp.server.wms.service;
import com.erp.model.wms.entity.AwdOutstockEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.AwdOutstockDTO;
import com.common.business.vo.PagingVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2025-12-22
 */
public interface AwdOutstockService extends SuperService<AwdOutstockEntity> {

    /**
    * 新增
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AwdOutstockDTO.AddDTO dto);

    /**
    * 修改
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @return
    */
    Boolean batchUpdateBillDate(List<AwdOutstockDTO.UpdateDTO> dtoList);


    /**
     * 下推调拨单弹窗
     * @param dto
     */
    List<AwdOutstockDTO.BatchUpdateBillDateViewDTO> batchUpdateBillDateView(BaseIdsDTO.IdsDTO dto);

    /**
     * 下推头程发货单
     * @param dto
     */
    boolean generateFirstMileDelivery(BaseIdsDTO.IdsDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2025-12-22
    * @param pagingParamDTO
    * @return PagingVO<AwdOutstockDTO.ListDTO>>
    */
    PagingVO<AwdOutstockDTO.ListDTO> paging(PagingDTO<AwdOutstockDTO.PagingParamDTO> pagingParamDTO);


    AwdOutstockDTO.ViewDTO view(String id);

    /**
    * 导出Excel
    * @author wtr
    * @date: 2025-12-22
    * @param dto
    * @param response
    * @return
    */
    void exportList(AwdOutstockDTO.ExportDTO dto, HttpServletResponse response);
}
