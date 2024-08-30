package com.erp.server.wms.service;

import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PdaWarehouseLocationDTO;
import com.erp.model.wms.dto.WarehouseLocationDTO;
import com.erp.model.wms.dto.pickingstrategy.WarehouseAreaDTO;
import com.erp.model.wms.entity.WarehouseLocationEntity;
import com.erp.model.wms.enums.WarehouseLocationTypeEnum;
import com.erp.model.wms.vo.WarehouseLocationExportVo;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
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
     * 库区分页
     * @param dto dto
     */
    PagingVO<WarehouseAreaDTO.PagingView> areaPaging(PagingDTO<WarehouseAreaDTO.PagingParam> dto);

    void addArea(WarehouseAreaDTO.Add dto);

    void updateArea(WarehouseAreaDTO.Update dto);

    WarehouseAreaDTO.View viewArea(String id);

    void deleteArea(List<String> ids);

    void updateStatusArea(UpdateStateDTO.BatchUpdateDTO dto);

    /**
     * 仓位管理高级查询
     * @param dto 查询参数
     * @return 仓位信息列表
     * @date: 2024-05-31
     * @author: tanmujin
     */
    PagingVO<WarehouseLocationDTO.ViewDto> pagingByParam(PagingDTO<WarehouseLocationDTO.SearchParamDTO> dto);

    /**
     * 批量删除
     *
     * @param idsDto
     * @return void
     * @date: 2024-05-31
     * @author: tanmujin
     */
    List<String> deleteBatch(WarehouseLocationDTO.IdsDto idsDto);

    /**
     * 导入仓位Excel
     *
     * @param file     上传的文件
     * @param response
     * @return 上传失败的条目信息
     * @date: 2024-05-31
     * @author: tanmujin
     */
    void importExcel(MultipartFile file, HttpServletResponse response);

    /**
     * 回收仓位
     *
     * @param idsDto
     * @return void
     * @date: 2024-05-31
     * @author: tanmujin
     */
    List<BatchResultDTO> recycle(WarehouseLocationDTO.IdsDto idsDto);

    /**
     * 更新仓位状态：启用/禁用
     * @param dto
     * @return void
     * @date: 2024-05-31
     * @author: tanmujin
     */
    void updateDisabled(WarehouseLocationDTO.updateStatusDto dto);

    /**
     * 查询操作日志
     * @param dto
     * @return 操作日志列表
     * @date: 2024-05-31
     * @author: tanmujin
     */
//    PagingVO<OperateLogDTO.ListDTO> listOperateLog(PagingDTO<OperateLogDTO.SearchDTO> dto);

    /**
     * 导出仓位信息
     * @param dto 导出excel的参数
     * @return void
     * @date: 2024-05-31
     * @author: tanmujin
     */
    void exportExcel(WarehouseLocationDTO.exportParamDto dto);

    /**
     * 新增仓位
     * @param addDTO
     * @return void
     * @date: 2024-05-31
     * @author: tanmujin
     */
    void add(WarehouseLocationDTO.AddDTO addDTO);

    /**
     * 编辑更新仓位信息
     * @param dto
     * @return void
     * @date: 2024-05-31
     * @author: tanmujin
     */
    void update(WarehouseLocationDTO.updateDto dto);

    /**
     * 通过仓库ID查询仓位列表
     * @param warehouseId 仓库ID
     * @return 仓位列表
     * @date: 2024-06-03
     * @author: tanmujin
     */
    List<WarehouseLocationDTO.ViewDto> listAreaByWarehouseId(String warehouseId);

    /**
     * tab名称及其包含的数据量统计
     * @param
     * @return WarehouseLocationDTO.tabDto
     * @date: 2024-06-03
     * @author: tanmujin
     */
    List<WarehouseLocationDTO.tabDto> tabList();

    void downloadTemplate(HttpServletResponse response);


    /**
     * 根据名称或编号进行查询仓位
     * @param warehouseLocation
     * @return
     */
    WarehouseLocationEntity findByWarehouseCode(String warehouseLocation);

    void updateLocationStatus(String warehouseId, String warehouseLocation, String status);

    /**
     * 根据仓位名称查询
     * @param warehouseLocationName 仓位名称
     * @return 仓位列表
     * @date: 2024-06-13
     * @author: tanmujin
     */
    List<WarehouseLocationEntity> listByLocationName(String warehouseLocationName);

    /**
     * 根据仓库ids和库位code查询库位列表
     * @param warehouseIds
     * @param warehouseLocationList
     * @return
     */
    List<WarehouseLocationEntity> listByWarehouseIdsAndCodeList(List<String> warehouseIds, List<String> warehouseLocationList);

    /**
     * 查询库区
     * @param warehouseId 仓库ID
     * @param areaTypeCode 库区类型
     * @return 库区列表
     * @date: 2024-06-25
     * @author: tanmujin
     */
    List<WarehouseLocationDTO.CoreDTO> listArea(String warehouseId, String areaTypeCode);

    /**
     * 查询所有库区
     *
     * @date: 2024-06-26
     * @author: tanmujin
     */
    List<WarehouseLocationDTO.CoreDTO> listAllArea();

    List<WarehouseLocationDTO.ReplenishAreaDTO> listArea(BaseIdsDTO.IdsDTO idsDTO);

    /**
     * 查询库区下的仓位
     */
    List<WarehouseLocationEntity> listLocation(String warehouseId, String warehouseArea);

    WarehouseLocationEntity findWarehouseArea(String warehouseId, String warehouseLocation);

    List<WarehouseLocationDTO.MappingDTO> listArea2LocationMapping(String warehouseId);

    /**
     * 导出库位
     */
    PagingVO<WarehouseLocationExportVo> exportWarehouseLocation(PagingDTO<WarehouseLocationDTO.exportParamDto> dto);
    WarehouseLocationEntity getWarehouseLocation(String warehouseId, String warehouseLocation, WarehouseLocationTypeEnum type);
}
