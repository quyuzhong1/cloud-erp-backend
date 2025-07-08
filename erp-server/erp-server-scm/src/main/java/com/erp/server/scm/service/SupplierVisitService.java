package com.erp.server.scm.service;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.common.business.service.SuperService;
import com.erp.model.scm.dto.SupplierVisitDTO;
import com.erp.model.scm.entity.SupplierVisitEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 供应商拜访表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface SupplierVisitService extends SuperService<SupplierVisitEntity> {

    
    
    /**
     * 添加供应商现场考察
     * @author yl
     * @date 2023-03-21 10:26
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean add(SupplierVisitDTO.AddDTO dto);
    /**
     * 编辑供应商现场考察
     * @author yl
     * @date 2023-03-21 10:26
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean update(SupplierVisitDTO.UpdateDTO dto);

    
    /**
     * 获取到供应商现场考察
     * @author yl
     * @date 2023-03-21 11:32
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.SupplierVisitDTO.PagingViewDTO>
     */
    PagingVO<SupplierVisitDTO.PagingViewDTO> paging(PagingDTO<BaseIdDTO> dto);

    List<SupplierVisitDTO.TabListDTO> tabList(PermissionsDTO dto);

    SupplierVisitDTO.ViewDTO view(String id);

    void exportList(SupplierVisitDTO.PagingParamDTO dto, HttpServletResponse response);

    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    PagingVO<SupplierVisitDTO.ListDTO> pagingList(PagingDTO<SupplierVisitDTO.PagingParamDTO> dto);

    void downloadTemplate(HttpServletResponse response);

    void batchImportVisit(List<SupplierVisitDTO.ImportAddDTO> addList);
}
