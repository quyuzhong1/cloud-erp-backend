package com.erp.server.oms.service.impl;

import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cAbnormalDTO;
import com.erp.server.oms.service.SoB2cAbnormalService;
import com.erp.server.oms.service.SoB2cService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @description: b2c异常订单实现
 * @author Will
 * @date: 2024/4/22 9:06
 */
@Service
public class SoB2cAbnormalServiceImpl implements SoB2cAbnormalService {

    @Resource
    private SoB2cService soB2cService;

    @Override
    public PagingVO<SoB2cAbnormalDTO.ListDTO> abnormalPaging(PagingDTO<SoB2cAbnormalDTO.PagingParamDTO> pagingParamDTO) {
        return soB2cService.abnormalPaging(pagingParamDTO);
    }

    @Override
    public Boolean abnormalExportExcel(SoB2cAbnormalDTO.PagingParamDTO dto, HttpServletResponse response) {
        return soB2cService.abnormalExportExcel(dto,response);
    }

}
