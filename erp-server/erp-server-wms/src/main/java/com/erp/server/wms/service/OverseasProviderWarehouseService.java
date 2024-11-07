package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.ThirdWarehouseDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasProviderWarehouseEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;

import java.util.List;

/**
 * <p>
 * 海外物流商仓库 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
public interface OverseasProviderWarehouseService extends SuperService<OverseasProviderWarehouseEntity> {

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/12/5 11:49
     * @param dto
     * @param mainId
     * @return java.lang.Boolean
     **/
    Boolean update(OverseasProviderDTO.UpdateDTO dto, String mainId);

    /**
     * 根据仓库id查询绑定关系
     * @Author Luo_WG
     * @Date 2023/11/17 12:18
     * @param warehouseId
     * @return com.erp.model.wms.entity.OverseasProviderWarehouseEntity
     **/
    OverseasProviderWarehouseEntity getByWarehouseId(String warehouseId);

    /**
     * 根据仓库id查询绑定关系(未禁用)
     **/
    OverseasProviderWarehouseEntity getByWarehouseIdWithNotDisabled(String warehouseId);

    /**
     * 根据仓库ids查询绑定关系
     * @Author Luo_WG
     * @Date 2023/11/17 12:18
     * @param warehouseIds
     * @return com.erp.model.wms.entity.OverseasProviderWarehouseEntity
     **/
    List<OverseasProviderWarehouseEntity> listByWarehouseIds(List<String> warehouseIds);

    /**
     * @Description 根据erp系统的仓库id 获取数据
     * @author lambda
     * @date 2023-12-13 16:08
     * @Param  warehouseIds
     * @Return
     */
    List<OverseasProviderWarehouseDTO.ViewDTO> listByWarehouseIdList(List<String> warehouseIds);

    /**
     * 根据第三方仓库信息查询
     **/
    OverseasProviderWarehouseEntity getByPlatform(String mainId,String platformWarehouseCode);

    /**
     * 根据主表id查询详情信息
     * @Author Luo_WG
     * @Date 2023/11/22 15:52
     * @param mainIds
     * @return java.util.List<com.erp.model.wms.entity.OverseasProviderWarehouseEntity>
     **/
    List<OverseasProviderWarehouseEntity> listByMainIds(List<String> mainIds);

    List<OverseasProviderWarehouseEntity> listByPlatformWarehouseCode(List<String> warehouseCodeList,String platform);

    /**
     * 根据仓库ID信息查询海外仓平台
     *
     * @Author Jim
     * @Date 2023/12/05
     **/
    OverseasProviderEntity findPlatformByWarehouseId(String destWarehouseId);

    PagingVO<ThirdWarehouseDTO.PageSelectDTO> pagingSelect(PagingDTO<OverseasProviderWarehouseDTO.SelectDTO> dto);
    Boolean feignBind(OverseasProviderDTO.FeignDTO feignDTO);


    /**
     * 是否API对接仓库
     * @param destWarehouseId
     * @return
     */
    Boolean isApiWarehouse(String destWarehouseId);
}
