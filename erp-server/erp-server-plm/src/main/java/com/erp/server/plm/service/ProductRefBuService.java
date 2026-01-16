package com.erp.server.plm.service;
import com.erp.model.plm.entity.ProductRefBuEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductRefBuDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 产品bu信息关联表 服务类
 * </p>
 *
 * @author lrp
 * @since 2026-01-16
 */
public interface ProductRefBuService extends SuperService<ProductRefBuEntity> {


    List<ProductRefBuEntity> listByBuId(String id);

    void addOrUpdate(String productId, String buId);

    List<ProductRefBuEntity> listByProductIds(List<String> productIdList);


    ProductRefBuEntity getByProductIds(String productId);
}
