package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.bi.dto.DmpRefundInfoDTO;
import com.erp.model.bi.dto.DmpRefundInfoSearchDTO;
import com.erp.model.dmp.entity.DmpRefundInfoEntity;

import javax.servlet.http.HttpServletResponse;

/**
 * 退款列表服务类
 */
public interface DmpRefundInfoService extends IService<DmpRefundInfoEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2022/12/14 9:29
     * @param dto
     * @return PagingVO<DmpRefundInfoDTO>
     */
    PagingVO<DmpRefundInfoDTO> paging(PagingDTO<DmpRefundInfoSearchDTO> dto);
    /**
     * @description: 导出
     * @author Will
     * @date: 2022/12/15 10:37
     * @param dto
     * @param response

     */
    void exportExcel(DmpRefundInfoSearchDTO dto, HttpServletResponse response);
}
