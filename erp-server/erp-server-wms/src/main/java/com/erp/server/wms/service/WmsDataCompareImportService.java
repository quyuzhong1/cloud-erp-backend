package com.erp.server.wms.service;
import com.erp.model.wms.entity.WmsDataCompareImportEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WmsDataCompareImportDTO;

/**
 * <p>
 * 数据对比导入文件信息 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-03-20
 */
public interface WmsDataCompareImportService extends SuperService<WmsDataCompareImportEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(WmsDataCompareImportDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-03-20
    * @param dto
    * @return
    */
    Boolean update(WmsDataCompareImportDTO.UpdateDTO dto);


}
