package com.erp.server.wms.service;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PdaWarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 仓库仓位表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-22
 */
public interface WarehouseLocationService extends SuperService<WarehouseLocationEntity> {

    /**
     * 仓位下拉列表
     * @return
     */
    List<WarehouseLocationDTO.LocationListDTO> select(String warehouseId);
    List<WarehouseLocationDTO.WarehouseLocationListDTO> selectByWarehouseIds(List<String> warehouseIds);
    /**
     * 引用仓位分区
     * @param ids
     * @return
     */
    void quoteLocation(List<String> ids);

    /**
     * 根据仓库仓位id获取详细信息（包含分区信息）
     * @param id
     * @return
     */
    WarehouseLocationDTO.LocationDetailDTO findById(String id);

    /**
     * 根据仓库id和仓位编码获取
     * @param warehouseId
     * @param code
     * @return
     */
    WarehouseLocationEntity findByWarehouseIdAndCode(String warehouseId, String code);

    /**
     * 根据id获取
     *
     * @param id
     * @return
     * @author hyj
     * @date 2024/4/24 10:37
     */
    WarehouseLocationEntity findLocationById(String id);

    /**
     * @description: 根据仓库id和库位集合查询
     * @author Will
     * @date: 2023/8/2 17:51
     * @param listParam
     * @return List<WarehouseLocationEntity>
     */
    List<WarehouseLocationEntity> listByWarehouseIdAndCode(List<WarehouseLocationDTO.WarehouseLocationSearchParamDTO> listParam);

    /**
     * 所有仓位
     * @return
     */
    List<WarehouseLocationDTO.LocationSelectDTO> all();

    /**
     * 获取仓库的仓位信息
     * @param warehouseIds
     * @return
     */
    List<WarehouseLocationEntity> list(List<String> warehouseIds);

    /**
     * @description: 根据仓库ids查询
     * @author Will
     * @date: 2023/8/3 16:16
     * @param warehouseIds
     * @return List<WarehouseLocationEntity>
     */
    List<WarehouseLocationEntity> listByWarehouseIds(List<String> warehouseIds);


    /**
     * 根据仓库id获取仓位
     * @param warehouseId
     * @param returnType
     * @param areaId
     * @return
     */
    List<BaseDropDownDTO.CommonDTO> getWarehouseArea(String warehouseId, WarehouseLocationTypeEnum returnType, String areaId);

    /**
     * 分页查询
     * @param dto
     * @return
     */
    PagingVO<WarehouseLocationDTO.PagingViewDTO> paging(PagingDTO<WarehouseLocationDTO.PagingParamDTO> dto);

    /**
     * 库位库区对应关系
     * @return
     */
    Map<String, String> locationAreaMap();

    /**
     * 获取仓库加仓库下的区位
     * @Author Luo_WG
     * @Date 2023/10/17 16:15
     * @return java.util.List<com.erp.model.wms.dto.PdaWarehouseLocationDTO.WarehouseAreaDTO>
     **/
    List<PdaWarehouseLocationDTO.WarehouseAreaDTO> listWarehouseArea();

    /**
     * 新增仓位
     * @Author Luo_WG
     * @Date 2023/10/17 17:34
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean addWarehouseLocation(PdaWarehouseLocationDTO.WarehouseLocationAddDTO dto);

    /**
     * 根据仓库id和库区code查询库区信息
     * @param warehouseId
     * @param warehouseAreaCode
     */
    WarehouseLocationEntity findArea(String warehouseId, String warehouseAreaCode);
    /**
     * @description: 仓位远程查询（分页型）
     * @author Will
     * @date: 2024/5/16 15:18
     * @param dto
     * @return PagingVO<LocationListDTO>
     */
    PagingVO<WarehouseLocationDTO.LocationListDTO> pagingSelect(PagingDTO<WarehouseLocationDTO.SelectDTO> dto);


    /**
     * 根据名称或编号进行查询仓位
     * @param warehouseLocation
     * @return
     */
    WarehouseLocationEntity findByWarehouseCode(String warehouseLocation);

    /**
     * 根据仓库ids和库位code查询库位列表
     * @param warehouseIds
     * @param warehouseLocation
     * @return
     */
    List<WarehouseLocationEntity> findByWarehouseIdsAndCode(List<String> warehouseIds, String warehouseLocation);

    /**
     * 根据仓库id和库位code查询库位
     * @param warehouseId       仓库id
     * @param warehouseLocation 库位code
     * @param type              库位类型(库位/库区)
     * @return                  库位信息
     */
    WarehouseLocationEntity getWarehouseLocation(String warehouseId, String warehouseLocation, WarehouseLocationTypeEnum type);
}
