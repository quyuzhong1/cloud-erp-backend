package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.LogisticsAddressEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsAddressDTO;

/**
 * <p>
 * 物流地址表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-11-02
 */
public interface LogisticsAddressService extends SuperService<LogisticsAddressEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(LogisticsAddressDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-11-02
    * @param dto
    * @return
    */
    Boolean update(LogisticsAddressDTO.UpdateDTO dto);

    /**
     * 获取物流信息
     * @author yl
     * @date 2023-11-03 9:32
     * @param id
     * @return com.erp.model.tms.dto.LogisticsAddressDTO.ViewDTO
     */
    LogisticsAddressDTO.ViewDTO view(String id);

    /**
     * 地址分页
     * @param dto
     * @return
     */
    PagingVO<LogisticsAddressDTO.PagingViewDTO> paging(PagingDTO<LogisticsAddressDTO.PagingParamDTO> dto);
}
