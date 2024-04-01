package com.erp.server.tms.service;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsDeclareBillDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 报关单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
public interface TmsDeclareBillService extends SuperService<TmsDeclareBillEntity> {

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-27
    * @param dto
    * @return
    */
    Boolean update(TmsDeclareBillDTO.UpdateDTO dto);


    List<TmsDeclareBillDTO.TabListDTO> tabList(SourceTypeEnum sourceTypeEnum);

    PagingVO<TmsDeclareBillDTO.PagingVO> paging(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);

    TmsDeclareBillDTO.StatisticsVO statisticsByFm();

    List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    TmsDeclareBillDTO.ViewDTO view(String id);

    List<BatchResultDTO> updateToDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto);

    List<BatchResultDTO> cancelDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto);

    Boolean mergeDeclare(TmsDeclareBillDTO.MergeDeclareDTO dto);

    List<BatchResultDTO> cancelMerge(TmsDeclareBillDTO.MergeDeclareDTO dto);

    void export(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response);

    void exportDeclare(TmsDeclareBillDTO.PagingParamDTO pagingParamDTO, HttpServletResponse response) throws IOException;

    List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto);

    Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO dto);
}
