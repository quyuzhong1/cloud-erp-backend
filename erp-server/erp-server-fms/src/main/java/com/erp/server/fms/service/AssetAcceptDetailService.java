package com.erp.server.fms.service;
import com.erp.model.fms.entity.AssetAcceptDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.fms.dto.AssetAcceptDetailDTO;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 * 资产验收表明细表 服务类
 * </p>
 *
 * @author wuht
 * @since 2025-10-11
 */
public interface AssetAcceptDetailService extends SuperService<AssetAcceptDetailEntity> {

    /**
    * 新增
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(AssetAcceptDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author wuht
    * @date: 2025-10-11
    * @param dto
    * @return
    */
    Boolean update(AssetAcceptDetailDTO.UpdateDTO dto);

    Integer getAcceptQtyByDetailId(@RequestBody String detailId);
}
