package com.erp.server.dmp.service;
import com.common.core.controller.vo.ApiResult;
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
    * @param saveDTO
    * @return
    */
    boolean save(CfgAfterPlatformShopDTO.SaveDTO saveDTO);


    /**
    * 分页列表查询
    * @author wtr
    * @date: 2026-03-03
    * @param
    * @return PagingVO<CfgAfterPlatformShopDTO.ListDTO>>
    */
    List<CfgAfterPlatformShopDTO.ListDTO> view();

    List<CfgAfterPlatformShopDTO.CsAgentDTO> matchCsAgent(String dictPlatform,String shopId);

}
