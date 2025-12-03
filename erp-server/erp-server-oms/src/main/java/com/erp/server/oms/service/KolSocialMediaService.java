package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolSocialMediaEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolSocialMediaDTO;
import java.util.List;

/**
 * <p>
 * 达人社媒数据表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
public interface KolSocialMediaService extends SuperService<KolSocialMediaEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolSocialMediaDTO.AddDTO dto);

    /**
    * 批量新增
    * @author wuhaotian
    * @date: 2025-12-03
    * @param dto
    * @return
    */
    List<BatchResultDTO> batchAdd(KolSocialMediaDTO.BatchAddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-12-01
    * @param dto
    * @return
    */
    Boolean update(KolSocialMediaDTO.UpdateDTO dto);


}
