package com.erp.server.srm.service;
import cn.hutool.json.JSONArray;
import com.erp.model.srm.entity.PayableDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PayableDetailDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author will
 * @since 2025-09-24
 */
public interface PayableDetailService extends SuperService<PayableDetailEntity> {

    /**
    * 新增
    * @author will
    * @date: 2025-09-24
    * @param detailList
    * @return
    */
    BaseResultDTO.AddDTO batchAdd(List<PayableDetailDTO.AddDTO> detailList,String mainId);

    /**
     * 根据主表id集合查询
     * @author will
     * @date 2025/9/26 09:08
     * @param mainIdList
     * @return List<PayableDetailEntity>
     */
    List<PayableDetailEntity> listMainIdList(List<String> mainIdList);
    /**
     * 根据主表id查询
     * @author will
     * @date 2025/9/30 17:21
     * @param mainId
     * @return void
     */
    Boolean removeByMainId(String mainId);
    /**
     * 更新金蝶明细id
     * @author will
     * @date 2025/9/30 17:32
     * @param list
     * @return void
     */
    void updateKingdeeDetailId(JSONArray list);
}
