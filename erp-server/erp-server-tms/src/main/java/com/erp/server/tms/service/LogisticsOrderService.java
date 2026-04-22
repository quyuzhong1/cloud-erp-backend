package com.erp.server.tms.service;
import com.erp.model.dmp.dto.AfterSaleDTO;
import com.erp.model.tms.entity.LogisticsOrderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsOrderDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 物流下单表 服务类
 * </p>
 *
 * @author lei.nie
 * @since 2026-04-20
 */
public interface LogisticsOrderService extends SuperService<LogisticsOrderEntity> {

    /**
    * 新增
    * @author lei.nie
    * @date: 2026-04-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsOrderDTO.AddDTO dto);

    /**
    * 修改
    * @author lei.nie
    * @date: 2026-04-20
    * @param dto
    * @return
    */
    Boolean update(LogisticsOrderDTO.UpdateDTO dto);

    /**
    * 分页列表查询
    * @author lei.nie
    * @date: 2026-04-20
    * @param pagingParamDTO
    * @return PagingVO<LogisticsOrderDTO.ListDTO>>
    */
    PagingVO<LogisticsOrderDTO.ListDTO> paging(PagingDTO<LogisticsOrderDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lei.nie
    * @date: 2026-04-20
    * @param dto
    * @return List<LogisticsOrderDTO.TabListDTO>>
    */
    List<LogisticsOrderDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author lei.nie
    * @date: 2026-04-20
    * @param id
    * @return
    */
    LogisticsOrderDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author lei.nie
    * @date: 2026-04-20
    * @param dto
    * @param response
    * @return
    */
    void exportList(LogisticsOrderDTO.ExportDTO dto, HttpServletResponse response);

    List<AfterSaleDTO.LogisticsOrderResultDTO> addBatch(List<LogisticsOrderEntity> entityList);

    BatchResultDTO delete(String id);

    BatchResultDTO cancel(String id);

    List<LogisticsOrderDTO.ListDTO> getLogisticsOrderListByTrackNo(List<String> trackNoList);

    List<AfterSaleDTO.LogisticsOrderResultDTO> batchCancel(List<String> codeList);

    List<BatchResultDTO> printLogisticsWaybill(BaseIdsDTO.IdsDTO dto);

    /**
     * 上传物流面单
     *
     * @param dto LogisticsOrderDTO.UploadFileDTO
     * @return String
     */
    String uploadLogisticLabel(LogisticsOrderDTO.UploadFileDTO dto);
}
