package com.erp.server.dmp.service;

import com.common.business.enums.SyncKingdeeOmsStatusEnum;
import com.erp.model.dmp.dto.GoodcangDTO;
import com.erp.server.dmp.entity.DmpWarehouseInboundRecordEntity;
import com.common.business.service.SuperService;
import com.erp.server.dmp.utils.KingdeeApiUtils;

import java.util.Optional;

/**
 * <p>
 * 海外仓上架记录表 服务类
 * </p>
 *
 * @author Cloud
 * @since 2023-03-29
 */
public interface DmpWarehouseInboundRecordService extends SuperService<DmpWarehouseInboundRecordEntity> {

    /**
     *处理数据库业务
     * @param ext
     */
    Boolean saveOrder(GoodcangDTO.MessageDTO ext);

    /**
     * 保存金蝶调拨单
     * @param paramExt
     * @param apiUtils
     * @return
     */
    String addKingdeeTransferRecord(GoodcangDTO.MessageDTO paramExt, KingdeeApiUtils apiUtils);

    /**
     * 根据订单号查询
     * @param receivingCode
     * @return
     */
    Optional<DmpWarehouseInboundRecordEntity> getByOrderCode(String receivingCode);

    /**
     * 根据订单号更新同步状态
     * @param receivingCode
     * @param statusEnum
     * @return
     */

    Boolean updateSyncStatusByCode(String receivingCode, SyncKingdeeOmsStatusEnum statusEnum);

    /**
     * 提交金蝶调拨单
     *
     * @param saveId
     * @param receivingCode
     * @param apiUtils
     * @return
     */
    Boolean submitKingdeeTransferRecord(String saveId, String receivingCode, KingdeeApiUtils apiUtils);

    /**
     * 审核金蝶调拨单
     *
     * @param saveId
     * @param receivingCode
     * @param apiUtils
     * @return
     */
    Boolean auditKingdeeTransferRecord(String saveId, String receivingCode, KingdeeApiUtils apiUtils);

    /**
     * 推送直接调拨单到金蝶
     *
     * @param recordEntity
     * @param apiUtils
     */
    void pushDirectTransferToKingdee(DmpWarehouseInboundRecordEntity recordEntity, KingdeeApiUtils apiUtils);

}
