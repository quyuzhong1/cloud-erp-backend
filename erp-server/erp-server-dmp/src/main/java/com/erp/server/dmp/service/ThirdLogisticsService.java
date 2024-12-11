package com.erp.server.dmp.service;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.entity.ThirdLogisticsEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.ThirdLogisticsDTO;

/**
 * <p>
 * 三方渠道表 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-12-11
 */
public interface ThirdLogisticsService extends SuperService<ThirdLogisticsEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-12-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ThirdLogisticsDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-12-11
    * @param dto
    * @return
    */
    Boolean update(ThirdLogisticsDTO.UpdateDTO dto);


    PagingVO<ThirdLogisticsDTO.PageSelectDTO> pagingSelect(PagingDTO<ThirdLogisticsDTO.SelectDTO> dto);
}
