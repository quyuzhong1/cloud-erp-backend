package com.erp.server.tms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.entity.TransferDeclareEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TransferDeclareDTO;

import java.util.List;

/**
 * <p>
 * 中转报关表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
public interface TransferDeclareService extends SuperService<TransferDeclareEntity> {

    /**
     * 分页列表查询
     * @Author Luo_WG
     * @Date 2024/1/20 14:50
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.tms.dto.TransferDeclareDTO.ListDTO>
     **/
    PagingVO<TransferDeclareDTO.ListDTO> paging(PagingDTO<TransferDeclareDTO.PagingParamDTO> dto);

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TransferDeclareDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-01-19
    * @param dto
    * @return
    */
    Boolean update(TransferDeclareDTO.UpdateDTO dto);

    /**
     * 根据渠道id查询报关信息
     * @Author Luo_WG
     * @Date 2024/1/19 14:41
     * @param ids
     * @return com.erp.model.tms.entity.TransferDeclareEntity
     **/
    TransferDeclareEntity checkExistByChannelIds(List<String> ids);

}
