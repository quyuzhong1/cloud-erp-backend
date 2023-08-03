package com.erp.server.wms.service.impl;

import com.common.business.dto.base.BaseIdDTO;
import com.erp.model.wms.dto.StocktakingTaskDetailDTO;
import com.erp.model.wms.entity.StocktakingTaskDetailEntity;
import com.erp.server.wms.mapper.StocktakingTaskDetailMapper;
import com.erp.server.wms.service.StocktakingTaskDetailService;
import com.common.business.service.SuperServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 盘点任务明细表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-07-31
 */
@Service
public class StocktakingTaskDetailServiceImpl extends SuperServiceImpl<StocktakingTaskDetailMapper, StocktakingTaskDetailEntity> implements StocktakingTaskDetailService {


    @Override
    public Boolean exportExcel(BaseIdDTO dto, HttpServletResponse response) {
        return null;
    }

    @Override
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        return null;
    }

    /**
     * 更新明细
     * @author yl
     * @date 2023-08-03 17:59
     * @param dto
     * @return
     */
    @Override
    public Boolean updateDetail(StocktakingTaskDetailDTO.UpdateDTO dto) {
        return null;
    }
}
