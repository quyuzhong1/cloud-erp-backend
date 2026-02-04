package com.erp.server.plm.service;
import com.erp.model.plm.entity.ProductChangeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 产品变更信息表 服务类
 * </p>
 *
 * @author lrp
 * @since 2026-02-03
 */
public interface ProductChangeDetailService extends SuperService<ProductChangeDetailEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProductChangeDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return
    */
    Boolean update(ProductChangeDetailDTO.UpdateDTO dto);


    /**
    * 分页列表查询
    * @author lrp
    * @date: 2026-02-03
    * @param pagingParamDTO
    * @return PagingVO<ProductChangeDetailDTO.ListDTO>>
    */
    PagingVO<ProductChangeDetailDTO.ListDTO> paging(PagingDTO<ProductChangeDetailDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @return List<ProductChangeDetailDTO.TabListDTO>>
    */
    List<ProductChangeDetailDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author lrp
    * @date: 2026-02-03
    * @param id
    * @return
    */
    ProductChangeDetailDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author lrp
    * @date: 2026-02-03
    * @param dto
    * @param response
    * @return
    */
    void exportList(ProductChangeDetailDTO.ExportDTO dto, HttpServletResponse response);
}
