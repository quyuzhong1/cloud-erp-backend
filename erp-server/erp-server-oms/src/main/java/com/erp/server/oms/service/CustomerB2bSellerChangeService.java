package com.erp.server.oms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import com.erp.model.oms.dto.excel.CustomerB2bSellerExcelDTO;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.erp.model.oms.entity.CustomerInfoEntity;

import java.util.List;

/**
 * <p>
 * b2b客户销售员变更单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-31
 */
public interface CustomerB2bSellerChangeService extends SuperService<CustomerB2bSellerChangeEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-01-31
    * @param dto
    * @return
    */
    BatchResultDTO add(CustomerB2bSellerChangeDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-01-31
    * @param dto
    * @return
    */
    Boolean update(CustomerB2bSellerChangeDTO.UpdateDTO dto);

    List<BatchResultDTO> batchAdd(List<CustomerB2bSellerChangeDTO.AddDTO> addDTOList);

    List<BatchResultDTO> batchAddAndSubmit(List<CustomerB2bSellerChangeDTO.AddDTO> addDTOList);

    BatchResultDTO addAndSubmit(CustomerB2bSellerChangeDTO.AddDTO addDTO);

    List<CustomerB2bSellerChangeEntity> listByMainId(String mainId);

    PagingVO<CustomerB2bSellerChangeDTO.ListDTO> paging(PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto);

    List<CustomerB2bSellerChangeDTO.TabFlagDTO> tabFlag();

    Boolean updateAndSubmit(CustomerB2bSellerChangeDTO.UpdateDTO dto);

    List<BatchResultDTO> batchSubmit(List<String> ids);

    List<BatchResultDTO> batchDelete(List<String> ids);

    List<BatchResultDTO> batchCancel(List<String> ids);

    BatchResultDTO approve(BaseApproveParamDTO baseApproveParamDTO,CustomerB2bSellerChangeEntity entity,CustomerInfoEntity customerInfo);

    Boolean approveEnd(BaseApproveParamDTO dto, CustomerB2bSellerChangeEntity entity,BatchResultDTO batchResultDTO,CustomerInfoEntity customerInfoEntity );

    void export(CustomerB2bSellerChangeDTO.ParamDTO dto);

    CustomerB2bSellerChangeEntity getByMainId(String businessId);

    PagingVO<CustomerB2bSellerExcelDTO> exportCustomerB2BSellerChange(PagingDTO<CustomerB2bSellerChangeDTO.ParamDTO> dto);
}
