package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.QcApplicationDTO;
import com.erp.model.wms.dto.QcApplicationSrmDTO;
import com.erp.model.wms.entity.QcApplicationEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 质检申请单主表 服务类
 * </p>
 *
 * @author will
 * @since 2026-03-20
 */
public interface QcApplicationSrmService extends SuperService<QcApplicationEntity> {



    /**
    * 分页列表查询
    * @author will
    * @date: 2026-03-20
    * @param pagingParamDTO
    * @return PagingVO<QcApplicationDTO.ListDTO>>
    */
    PagingVO<QcApplicationSrmDTO.ListDTO> srmPaging(PagingDTO<QcApplicationDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 状态统计
     * @author will
     * @date: 2026-03-20
     * @param dto
     * @return List<QcApplicationDTO.TabListDTO>>
     */
    List<QcApplicationDTO.TabListDTO> srmTabList(PermissionsDTO dto);
    /**
     * 导出
     * @author will
     * @date: 2026-03-20
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportList(QcApplicationDTO.PagingParamDTO dto, HttpServletResponse response);
    /**
     * 生成待入库质检单
     * @author will
     * @date: 2026-03-20
     * @param list
     * @return Boolean
     */
    Boolean generateWaitDeliveryRefQcApplication(ValidList<QcApplicationDTO.GeneratePoRefQcApplicationDTO> list);
}
