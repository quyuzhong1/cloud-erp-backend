package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.AutoGenerateBillDTO;
import com.erp.model.tms.dto.TmsDeclareBillDTO;
import com.erp.model.tms.entity.TmsDeclareBillEntity;

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
    Boolean update(TmsDeclareBillDTO.UpdateDTO dto,SourceTypeEnum sourceTypeEnum);


    List<TmsDeclareBillDTO.TabListDTO> tabList(SourceTypeEnum sourceTypeEnum,PermissionsDTO permissionsDTO);

    PagingVO<TmsDeclareBillDTO.PagingVO> paging(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);

    TmsDeclareBillDTO.StatisticsVO statisticsByFm(PermissionsDTO permissionsDTO);

    List<TmsDeclareBillDTO.DeliveryDTO> getCanGenerateDeliveryOrder(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    TmsDeclareBillDTO.ViewDTO view(String id);

    List<BatchResultDTO> updateToDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto,SourceTypeEnum sourceTypeEnum);

    List<BatchResultDTO> cancelDeclare(TmsDeclareBillDTO.UpdateDeclareStatusDTO dto);

    Boolean mergeDeclare(TmsDeclareBillDTO.MergeDeclareDTO dto);

    List<BatchResultDTO> cancelMerge(TmsDeclareBillDTO.MergeDeclareDTO dto);



    List<BatchResultDTO> delete(TmsDeclareBillDTO.DeleteDTO dto);

    Boolean addFmDeclare(TmsDeclareBillDTO.AddDTO dto);

    /**
     * 根据来源id查询报关单
     * @Author Luo_WG
     * @Date 2024/4/1 12:05
     * @param sourceIds
     * @return java.util.List<com.erp.model.tms.entity.TmsDeclareBillEntity>
     **/
    List<TmsDeclareBillEntity> listBySourceIds(List<String> sourceIds);

    List<TmsDeclareBillDTO.SoOutDTO> getCanGenerateSoOut(TmsDeclareBillDTO.QuerySourceDTO querySourceDTO);

    TmsDeclareBillDTO.StatisticsVO statisticsBySoOut(PermissionsDTO permissionsDTO);

    Boolean addB2BDeclare(TmsDeclareBillDTO.AddDTO dto);

    Boolean autoGenerateFirstMileDeclare(AutoGenerateBillDTO autoGenerateBillDTO);

    Boolean autoGenerateB2bDeclare(AutoGenerateBillDTO autoGenerateBillDTO);

    PagingVO<TmsDeclareBillDTO.ExportDTO> exportDeclareBillDeclare(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);

    PagingVO<TmsDeclareBillDTO.PagingVO> exportDeclareBill(PagingDTO<TmsDeclareBillDTO.PagingParamDTO> dto);
}
