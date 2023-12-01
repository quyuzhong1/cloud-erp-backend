package com.erp.server.wms.service;
import com.erp.model.wms.entity.FirstMileCartonEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FirstMileCartonDTO;

import java.util.List;

/**
 * <p>
 * 发货单箱规信息 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface FirstMileCartonService extends SuperService<FirstMileCartonEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    void add(FirstMileCartonDTO.AddDTO dto, String mainId);

    /**
    * 根据发货单id查询装箱信息
    * @author Luo_WG
    * @date: 2023-11-16
    * @param mainIds
    * @return
    */
    List<FirstMileCartonEntity> listByMainIds(List<String> mainIds);

    /**
     * 查询装箱数量
     * @Author Luo_WG
     * @Date 2023/11/28 19:04
     * @param mainId
     * @return java.util.List<com.erp.model.wms.dto.FirstMileCartonDTO.PackingQtyDTO>
     **/
    List<FirstMileCartonDTO.PackingQtyDTO> listPackingQtyByMainId(String mainId, Integer boxSpecNo);

    /**
     * 根据主表id删除箱规信息
     * @Author Luo_WG
     * @Date 2023/11/29 10:33
     * @param mainIds
     * @return java.lang.Boolean
     **/
    Boolean deleteByMainIds(List<String> mainIds);

}
