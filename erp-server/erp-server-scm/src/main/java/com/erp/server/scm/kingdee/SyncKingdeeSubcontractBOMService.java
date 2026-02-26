package com.erp.server.scm.kingdee;

import com.erp.model.dmp.entity.DmpPushTaskEntity;
import com.erp.model.scm.dto.SubcontractBOMDTO;

import java.util.Map;

/**
 * @Author: wtr
 * @Date: 2026/1/19 8:39
 * @Param:
 * @Return:
 * @Description:
 **/
public interface SyncKingdeeSubcontractBOMService {

    DmpPushTaskEntity syncDataToKingdee(SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String syncOperate);

    Map<String , Object> newSyncDataToKingdee(SubcontractBOMDTO.KingdeeSubcontractBOMDTO dto, String syncOperate);
}
