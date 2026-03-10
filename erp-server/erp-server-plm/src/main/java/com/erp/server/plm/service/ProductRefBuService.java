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

    List<ProductRefBuEntity> listByBuNames(List<String> buNames);

    ProductRefBuEntity getByProductIds(String productId);

    /**
     * 根据产品id删除产品与BU线的关联（删除产品时同步清理，避免删除BU线时误判仍有关联）
     *
     * @param productId 产品id
     */
    void removeByProductId(String productId);

    /**
     * 根据产品id列表批量删除产品与BU线的关联
     *
     * @param productIds 产品id列表
     */
    void removeByProductIds(List<String> productIds);
}
