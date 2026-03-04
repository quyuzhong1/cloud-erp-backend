package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgAfterPlatformShopEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author wtr
 * @since 2026-03-03
 */
public interface CfgAfterPlatformShopService extends SuperService<CfgAfterPlatformShopEntity> {

    /**
    * 保存
    * @author wtr
    * @date: 2026-03-03
    * @param dto
    * @return
    */
    List<CfgAfterPlatformShopDTO.SaveDTO> save(CfgAfterPlatformShopDTO.SaveDTO dto);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2026-03-03
    * @param pagingParamDTO
    * @return PagingVO<CfgAfterPlatformShopDTO.ListDTO>>
    */
    PagingVO<CfgAfterPlatformShopDTO.ListDTO> paging(PagingDTO<CfgAfterPlatformShopDTO.PagingParamDTO> pagingParamDTO);

}
