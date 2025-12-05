package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * B2B三方发货单 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
public interface B2bThirdDeliveryService extends SuperService<B2bThirdDeliveryEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-11-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(B2bThirdDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-11-26
    * @param dto
    * @return
    */
    Boolean update(B2bThirdDeliveryDTO.UpdateDTO dto);


    List<B2bThirdDeliveryDTO.TabListDTO> tabList();

    PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> paging(PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto);

    void export(B2bThirdDeliveryDTO.PagingParamDTO dto);

    B2bThirdDeliveryDTO.ViewDTO view(B2bThirdDeliveryDTO.ViewQueryDTO dto);

    /**
     * 更新单据状态
     *
     * @param id
     * @param status
     * @param errorMsg
     * @param platformOrderCode 三方仓订单号
     * @param remark
     * @param trackNo
     */
    void updateStatus(String id, String status, String errorMsg, String platformOrderCode, String remark, String trackNo);

    /**
     * 生成三方发货单 销售出库单
     * @param id
     */
    BatchResultDTO generateB2bThirdDelivery(String id);

    /**
     * 发货拦截
     * @param id
     * @param remark
     * @return
     */
    BatchResultDTO deliveryIntercept(String id, String remark);

    /**
     * 取消发货
     * @param entity
     * @return
     */
    BatchResultDTO manualDelivery(B2bThirdDeliveryEntity entity);

    /**
     * 删除
     * @param entity
     * @return
     */
    BatchResultDTO delete(B2bThirdDeliveryEntity entity);
}
