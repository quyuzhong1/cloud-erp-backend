package com.erp.server.oms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoB2cReturnDTO;

import java.util.List;

/**
 * <p>
 * b2c退货订单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-09
 */
public interface SoB2cReturnService extends SuperService<SoB2cReturnEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-10-09
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoB2cReturnDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-10-09
    * @param dto
    * @return
    */
    Boolean update(SoB2cReturnDTO.UpdateDTO dto);


    PagingVO<SoB2cReturnDTO.PagingViewDTO> paging(PagingDTO<SoB2cReturnDTO.PagingParamDTO> dto);

    void markReturned(BaseIdsDTO.IdsDTO idsDTO);

    List<SoB2cReturnDTO.GenerateSoReturnNoticeView> generateSoReturnNoticeView(List<String> ids);

    void generateSoB2cReturnNotice(List<SoB2cReturnDTO.GenerateSoReturnNoticeView> list);
}
