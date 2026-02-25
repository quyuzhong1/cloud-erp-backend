package com.erp.server.plm.service;
import com.erp.model.plm.entity.ProductChangeDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.entity.ProductChangeEntity;

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
    * @return
    */
    Boolean add(ProductChangeEntity productChangeEntity,List<ProductChangeDetailDTO.AddDTO> detailDTOList);

    /**
    * 修改
    * @author lrp
    * @date: 2026-02-03
    * @return
    */
    Boolean update(ProductChangeEntity productChangeEntity ,List<ProductChangeDetailDTO.UpdateDTO> updateDTOList);

    List<ProductChangeDetailEntity> listByMains(List<String> mainIds);

    void deleteByMainId(String mainId);

    void checkData(ProductChangeEntity productChangeEntity, List<ProductChangeDetailEntity> detailEntityList);
}
