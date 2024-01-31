package com.erp.server.oms.service;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.CustomerB2bSellerChangeEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.CustomerB2bSellerChangeDTO;
import org.apache.ibatis.annotations.Param;

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

    List<CustomerB2bSellerChangeDTO.ListDTO> paging(CustomerB2bSellerChangeDTO.ParamDTO dto);

    List<CustomerB2bSellerChangeDTO.TabFlagDTO> tabFlag();
}
