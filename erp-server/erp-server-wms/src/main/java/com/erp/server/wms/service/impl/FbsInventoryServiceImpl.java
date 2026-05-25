package com.erp.server.wms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.FbsInventoryDTO;
import com.erp.model.wms.entity.FbsInventoryEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.wms.mapper.FbsInventoryMapper;
import com.erp.server.wms.service.FbsInventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_WMS_FBS_INVENTORY;

/**
 * <p>
 * FBS库存 服务实现类
 * </p>
 *
 * @author Cursor
 * @since 2026-05-25
 */
@Slf4j
@Service
public class FbsInventoryServiceImpl extends SuperServiceImpl<FbsInventoryMapper, FbsInventoryEntity> implements FbsInventoryService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Override
    public PagingVO<FbsInventoryDTO.ListDTO> paging(PagingDTO<FbsInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<FbsInventoryDTO.ListDTO> query = new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<FbsInventoryDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        return new PagingVO<>(pageData);
    }

    @Override
    public void exportList(FbsInventoryDTO.ExportDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("FBS库存导出", EXPORT_WMS_FBS_INVENTORY.getCode(), param);
    }

    @Override
    public FbsInventoryDTO.SummaryNumber summaryNumber(PagingDTO<FbsInventoryDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        return baseMapper.summaryNumber(pagingParamDTO.getParams());
    }

}
