package com.erp.server.plm.service;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.file.dto.FileDTO.FileSizeInfo;
import com.erp.model.plm.dto.SkuStdRetailPriceDTO;
import com.erp.model.plm.entity.SkuStdRetailPriceEntity;

/**
 * <p>
 * sku标准零售价表 服务类
 * </p>
 *
 * @author shukai
 * @since 2026-03-16
 */
public interface SkuStdRetailPriceService extends SuperService<SkuStdRetailPriceEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SkuStdRetailPriceDTO.AddDTO dto);
    
    List<BaseResultDTO.AddDTO> batchAdd(List<SkuStdRetailPriceDTO.AddDTO> dtoList , boolean isValidateCNY, boolean checkAdd);

    /**
    * 修改
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return
    */
    Boolean update(SkuStdRetailPriceDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author shukai
    * @date: 2026-03-16
    * @param pagingParamDTO
    * @return PagingVO<SkuStdRetailPriceDTO.ListDTO>>
    */
    PagingVO<SkuStdRetailPriceDTO.ListDTO> paging(PagingDTO<SkuStdRetailPriceDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @return List<SkuStdRetailPriceDTO.TabListDTO>>
    */
    List<SkuStdRetailPriceDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author shukai
    * @date: 2026-03-16
    * @param id
    * @return
    */
    SkuStdRetailPriceDTO.ViewDTO view(String id);
    
    /**
    * 导出Excel
    * @author shukai
    * @date: 2026-03-16
    * @param dto
    * @param response
    * @return
    */
    void exportList(SkuStdRetailPriceDTO.ExportDTO dto, HttpServletResponse response);
    
    Boolean importExcel(FileSizeInfo excelFile, HttpServletResponse response) throws Exception;
    
    Boolean exportExcel(SkuStdRetailPriceDTO.ExportDTO dto);
    
    Boolean setting(SkuStdRetailPriceDTO.SettingDTO dto);
}
