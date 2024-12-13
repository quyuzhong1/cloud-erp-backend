package com.erp.server.wms.service;


import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.WarehouseDTO.WarehouseUpdateStateDTO;
import com.erp.model.wms.dto.excel.WarehouseExportExcelDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 仓库表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
public interface WarehouseService extends SuperService<WarehouseEntity> {
    /** 根据ids查询仓库
     * @description:
     * @author Will
     * @date: 2023/3/17 16:07
     * @param ids
     * @return List<WarehouseDTO>
     */
    List<WarehouseDTO.UpdateDTO> listWarehouseByIds(List<String> ids);

    /**
     * 查询所有审核通过并启用的仓库
     * @description:
     * @author Will
     * @date: 2023/3/21 14:27
     * @return List<WarehouseDTO>
     */
    List<WarehouseDTO.ListDTO> listApproveWarehouse();

    /**
     * @description:
     * @author Will
     * @date: 2024/5/23 18:21
     * @param dto
     * @return List<ListInventoryQtyDTO>
     */
    List<WarehouseDTO.ListInventoryQtyDTO> listWarehouseInventoryQty(WarehouseDTO.ListInventoryQtyParamDTO dto);


    /**
     * 添加仓库
     * @author yl
     * @date 2023-03-22 10:17
     * @param dto
     * @return com.erp.model.wms.entity.WarehouseEntity
     */
    String add(WarehouseDTO.AddDTO dto);


    /**
     * 修改仓库
     * @author yl
     * @date 2023-03-22 11:08
     * @param dto
     * @return java.lang.Boolean
     */
    String updateWarehouse(WarehouseDTO.UpdateDTO dto);


    /**
     * 提交并审核
     * @author yl
     * @date 2023-03-22 11:16
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(WarehouseDTO.AddDTO dto);


    /**
     * 仓库提交审核
     * @author yl
     * @date 2023-03-22 11:31
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);


    /**
     * 更改仓库状态
     * @author yl
     * @date 2023-03-22 11:43
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateStatus(WarehouseUpdateStateDTO dto);

    /**
     * 审核仓库
     * @author yl
     * @date 2023-03-22 11:45
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     */
    BatchResultDTO approve(WarehouseEntity entity, String type, String comment, Boolean isNeedProcess);


    /**
     * 反审核
     * @author yl
     * @date 2023-03-22 11:59
     * @param entity
     * @return java.lang.Boolean
     */
    BatchResultDTO disApprove(WarehouseEntity entity);

    /**
     * 批量删除
     * @author yl
     * @date 2023-03-22 12:12
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 获取仓库详情
     * @author yl
     * @date 2023-03-22 14:30
     * @param id
     * @return com.erp.model.wms.dto.WarehouseDTO.UpdateDTO
     */
    WarehouseDTO.UpdateDTO view(String id);


    /**
     * 分页获取仓库数据
     * @author yl
     * @date 2023-03-22 14:51
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseDTO.PagingViewDTO>
     */
    PagingVO<WarehouseDTO.PagingViewDTO> paging(PagingDTO<WarehouseDTO.PagingParamDTO> dto);

    /**
     * 导出仓库数据
     *
     * @param dto
     * @return void
     * @author yl
     * @date 2023-03-22 16:08
     */
    void exportWarehouse(WarehouseDTO.ExportDTO dto);

    /**
     * 下载仓库模板
     * @author yl
     * @date 2023-03-22 17:06
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);


    /**
     * 导入仓库数据
     * @author yl
     * @date 2023-03-22 17:17
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);


    /**
     * 修改并审核
     * @author yl
     * @date 2023-03-28 11:17
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(WarehouseDTO.UpdateDTO dto);
    /**
     * 更新同步状态
     * @author Will
     * @date: 2023/4/26 19:26
     * @param id
     */
    Boolean updateSyncKingdeeId(String id,String syncKingdeeId);

    /**
     * 根据id获取仓库详情带缓存
     * @param id
     * @return
     */
    WarehouseDTO.UpdateDTO detailWithCache(String id);

    /**
     * 根据金蝶仓库code 获取到对应仓库信息
     * @author yl
     * @date 2023-06-27 16:34
     * @param kingdeeWarehouseCodeList
     * @return java.util.List<com.erp.model.wms.entity.WarehouseEntity>
     */
    List<WarehouseEntity> listByKingdeeCodeList(List<String> kingdeeWarehouseCodeList);

    /**
     * @description: 出库列表(带参数)
     * @author Will
     * @date: 2023/7/20 15:32
     * @param dto
     * @return List<ListDTO>
     */
    List<WarehouseDTO.ListDTO> listWarehouseByParams(WarehouseDTO.ListParamDTO dto);

    /**
     * @description: 分页查询仓库列表(带参数) 不带权限控制的分页查询
     * @param dto
     * @return
     */
    PagingVO<WarehouseDTO.PagingNoPermissionDTO> pagingNoPermission(PagingDTO<WarehouseDTO.PagingDTO> dto);

    /**
     * 查询商品列表
     * @param dto
     * @return
     */
    PagingVO<WarehouseDTO.PagingProductViewDTO> pagingProduct(PagingDTO<WarehouseDTO.PagingProductDTO> dto);
    /**
     * 树状列表
     */
    List<WarehouseDTO.ListTreeDTO> listTree();
    /**
     * @description: 树状列表(有参)
     * @author Will
     * @date: 2023/9/6 15:56
     * @param dto
     * @return List<ListTreeDTO>
     */
    List<WarehouseDTO.ListTreeDTO> listTreeByParams(WarehouseDTO.ListParamDTO dto);

    /**
     * 仓库，库区，库位判断是否禁用
     * @param assertList
     * @return
     */
    void assertDisabled(List<WarehouseDTO.WarehouseDisabledAssertDTO> assertList);


    /**
     * 仓库列表(所有关联海外服务商)
     *
     * @Author Jim
     * @date 2023-11-29
     */
    List<WarehouseDTO.PullDownDTO> listOverseasWarehouse();

    /**
     * 根据名称获取仓库id
     *
     * @param nameList
     * @author hyj
     * @date 2024/4/18 10:26
     */
    List<WarehouseDTO.ListDTO> getByNames(List<String> nameList);
    /**
     * @description: 根据仓库名称集合查询
     * @author Will
     * @date: 2024/3/21 17:35
     * @param warehouseNameList
     * @return List<UpdateDTO>
     */
    List<WarehouseDTO.UpdateDTO> listWarehouseByNameList(List<String> warehouseNameList);
    /**
     * @description: 远程分页查询仓库信息
     * @author Will
     * @date: 2024/5/24 12:45
     * @param dto
     * @return PagingVO<ListDTO>
     */
    PagingVO<WarehouseDTO.ListDTO> selectPaging(PagingDTO<WarehouseDTO.SelectDTO> dto);

    /**
     * 查询所有仓库信息(不带权限控制) 不带权限控制的查询所有仓库信息
     * @return List<WarehouseEntity>    仓库信息
     */
    List<WarehouseEntity> listWarehouseWithCaches();


    PagingVO<WarehouseExportExcelDTO> exportWarehouse(PagingDTO<WarehouseDTO.ExportDTO> dto);
    
    /**
     * 检验启用和禁用时间
     * @param warehouseEntity
     */
    boolean checkOpenCloseTime(WarehouseEntity warehouseEntity);
}
