package com.erp.server.tms.service;
import com.erp.model.tms.entity.BasicQueryLogisticsProviderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.BasicQueryLogisticsProviderDTO;
import com.common.business.vo.PagingVO;
import com.common.business.dto.ApproveDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 查询物流商信息表 服务类
 * </p>
 *
 * @author jack
 * @since 2026-03-31
 */
public interface BasicQueryLogisticsProviderService extends SuperService<BasicQueryLogisticsProviderEntity> {

    /**
     * 下拉查询物流商列表
     *
     * @param paramDTO 查询参数
     * @return 物流商列表
     * @author jack
     * @date: 2026-03-31
     */
    List<BasicQueryLogisticsProviderDTO.ListAllVO> listAll(BasicQueryLogisticsProviderDTO.ListAllParamDTO paramDTO);

}
