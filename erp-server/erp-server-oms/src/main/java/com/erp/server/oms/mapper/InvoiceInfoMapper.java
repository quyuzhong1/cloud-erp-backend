package com.erp.server.oms.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.erp.model.oms.dto.InvoiceInfoDTO;
import com.erp.model.oms.entity.InvoiceInfoEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


/**
 * <p>
 * 上传记录 Mapper 接口
 * </p>
 *
 * @author zdy
 * @since 2025-03-07
 */
@Mapper
public interface InvoiceInfoMapper extends BaseMapper<InvoiceInfoEntity> {

    IPage<InvoiceInfoDTO.PagingViewDTO> paging(@Param("query") Page<InvoiceInfoDTO.PagingViewDTO> query, @Param("params") InvoiceInfoDTO.PagingParamDTO params);
    /**
     * 查询需要导出的URL
     * @author will
     * @date 2025/4/9 16:24
     * @param params
     * @return ExportResultDTO
     */
    List<InvoiceInfoDTO.ExportResultDTO> listExportXmlUrl(@Param("params") InvoiceInfoDTO.PagingParamDTO params, @Param("type")String type);
}
