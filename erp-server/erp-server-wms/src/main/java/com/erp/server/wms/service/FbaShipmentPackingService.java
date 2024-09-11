package com.erp.server.wms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.common.business.service.SuperService;
import com.erp.model.wms.dto.FbaShipmentPackingDTO;

import java.util.List;

/**
 * <p>
 * fba货件装箱信息 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-09-03
 */
public interface FbaShipmentPackingService extends SuperService<FbaShipmentPackingEntity> {

    void handle(FbaShipmentPackingDTO.PackingDTO data);

    List<FbaShipmentPackingEntity> getByMainIdAndBoxNo(String mainId, String boxNo);

    List<FbaShipmentPackingEntity> listByMains(List<String> mainIds);

    List<FbaShipmentPackingDTO.ViewDTO> listPacking(List<String> ids);

    void packingExport(FbaShipmentDTO.PagingParamDTO dto);

    PagingVO<FbaShipmentPackingDTO.ViewDTO> exportFbaShipmentPacking(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto);

    void updateCartonId(String cartonId, String fbaShipmentId, String fbaBoxNo);

    void generateByBindDTO(List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> detailList);
}
