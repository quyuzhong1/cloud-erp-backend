package com.erp.server.srm.service;

import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.dto.DeliveryOrderDTO;
import com.erp.model.srm.dto.excel.DeliveryOrderExportExcelDTO;
import com.erp.model.srm.entity.DeliveryOrderDetailEntity;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import org.apache.commons.math3.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 送货单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
public interface DeliveryOrderService extends SuperService<DeliveryOrderEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryOrderDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    Boolean update(DeliveryOrderDTO.UpdateDTO dto);

    Integer countByPrint(String supplierId, boolean isPrint);

    Integer countByReceiveStatus(String supplierId, String code);

    PagingVO<DeliveryOrderDTO.ListDTO> paging(PagingDTO<DeliveryOrderDTO.ParamDTO> dto);

    List<DeliveryOrderDTO.TabListDTO> tabList(DeliveryOrderDTO.ParamDTO paramDTO);

    DeliveryOrderDTO.ViewDTO view(String id);

    List<DeliveryOrderDTO.PrintDTO> print(List<String> ids);

    List<BatchResultDTO> cancelPrint(List<String> ids);

    boolean delete(List<String> ids);

    boolean confirmPrint(List<String> ids);

    List<DeliveryOrderDTO.GenerateReceiveListDTO> listGenerateReceive(BaseIdsDTO.IdsDTO dto);

    DeliveryOrderDTO.TotalInfo pagingTotal(DeliveryOrderDTO.ParamDTO dto);

    /**
     * 生成送货单
     * @param dtos
     * @return
     */
    List<BatchResultDTO> addDeliveryOrder(List<DeliveryOrderDTO.AddDeliveryDTO> dtos);

    DeliveryOrderDTO.ViewDTO viewByCode(String code);

    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);

    void saveImport(List<Pair<DeliveryOrderEntity, List<DeliveryOrderDetailEntity>>> addList);

    Boolean updateReceiveStatus(List<String> ids);

    PagingVO<DeliveryOrderExportExcelDTO> exportSupplierDeliveryOrder(PagingDTO<DeliveryOrderDTO.ParamDTO> dto);
}
