package com.erp.server.wms.service.impl;


import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.date.DateUtil;
import com.erp.model.wms.dto.VirtualInventoryDiffDTO;
import com.erp.model.wms.entity.VirtualInventoryEntity;
import com.erp.server.wms.mapper.VirtualInventoryMapper;
import com.erp.server.wms.service.VirtualInventoryDiffService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 库存差异 服务实现类
 *
 * @author will
 * @since 2024-06-03
 */
@Slf4j
@Service
public class VirtualInventoryDiffServiceImpl extends SuperServiceImpl<VirtualInventoryMapper, VirtualInventoryEntity> implements VirtualInventoryDiffService {

    @Override
    public PagingVO<VirtualInventoryDiffDTO.ListDTO> diffPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualInventoryDiffDTO.ListDTO> pageData = this.baseMapper.diffPaging(query, dto.getParams());
        // 填充名称
        fillPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public PagingVO<VirtualInventoryDiffDTO.ListDetailDTO> diffDetailPaging(PagingDTO<VirtualInventoryDiffDTO.SearchParamDetailDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<VirtualInventoryDiffDTO.ListDetailDTO> pageData = this.baseMapper.diffDetailPaging(query, dto.getParams());
        // 填充名称
        fillDetailPageData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public Boolean exportExcel(VirtualInventoryDiffDTO.SearchParamDTO dto, HttpServletResponse response) {
        PagingDTO<VirtualInventoryDiffDTO.SearchParamDTO> pagingParamDTO = new PagingDTO<>();
        pagingParamDTO.setParams(dto);
        pagingParamDTO.setPageSize(-1);
        PagingVO<VirtualInventoryDiffDTO.ListDTO> resultList = this.diffPaging(pagingParamDTO);
        List<VirtualInventoryDiffDTO.ListDTO> list = (List<VirtualInventoryDiffDTO.ListDTO>)resultList.getList();
        if (CollectionUtils.isEmpty(list)) {
            throw new ServiceException(ApiError.EXPORT_DATA_EMPTY);
        }
        //数据赋值处理
        fillPageData(list);
        String name = "库存差异列表信息";
        StringBuffer sb = new StringBuffer();
        String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
        sb.append(date);
        sb.append(name);
        String excelPath = "excel/virtualInventoryDiff.xlsx";
        try {
            new ExcelPrintUtils().patchExport(list, response, sb.toString(), excelPath);
        } catch (IOException e) {
            log.error("库存差异列表信息导出出错 >>>>>{}", e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }


    /**
    * 新增修改处理数据
    */
    private void handleData(VirtualInventoryEntity virtualInventoryEntity) {
    // TODO 验证数据 & 数据赋值
    }

    /**
     * 虚拟库存分页查询数据处理
     * @author will
     * @date 2024/6/3 15:24
     * @param list
     */
    private void fillPageData (List<VirtualInventoryDiffDTO.ListDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }

    }

    private void fillDetailPageData (List<VirtualInventoryDiffDTO.ListDetailDTO> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }

    }
}
