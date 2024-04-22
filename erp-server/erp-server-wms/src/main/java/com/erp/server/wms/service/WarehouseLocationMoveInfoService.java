package com.erp.server.wms.service;
import com.erp.model.wms.entity.WarehouseLocationMoveInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.WarehouseLocationMoveInfoDTO;
import com.common.business.vo.PagingVO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 仓位移动主表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-08-24
 */
public interface WarehouseLocationMoveInfoService extends SuperService<WarehouseLocationMoveInfoEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return
    */
    String add(WarehouseLocationMoveInfoDTO.AddDTO dto);

    /**
     * 新增-pc端
     *
     * @param pcAddDTO
     * @author hyj
     * @date 2024/4/18 15:00
     */
    String pcAdd(WarehouseLocationMoveInfoDTO.PcAddDTO pcAddDTO);
    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return
    */
    Boolean update(WarehouseLocationMoveInfoDTO.UpdateDTO dto);
    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return
    */
    Boolean pcUpdate(WarehouseLocationMoveInfoDTO.PcUpdateDTO dto);

      /**
      * 分页列表查询
      * @author Luo_WG
      * @date: 2023-08-24
      * @param pagingParamDTO
      * @return PagingVO<WarehouseLocationMoveInfoDTO.ListDTO>>
      */
      PagingVO<WarehouseLocationMoveInfoDTO.ListDTO> paging(PagingDTO<WarehouseLocationMoveInfoDTO.PagingParamDTO> pagingParamDTO);
    /**
     * 列表查询-pc端
     * @author hyj
     * @date 2024/4/12 16:46
     * @param pagingParamDTO
     * @return ApiResult<PagingVO<WarehouseLocationMoveInfoDTO.ListDTO>>
     */
      PagingVO<WarehouseLocationMoveInfoDTO.ListDTO> pcPaging(PagingDTO<WarehouseLocationMoveInfoDTO.PagingParamDTO> pagingParamDTO);

     /**
     * 状态统计
     * @author Luo_WG
     * @date: 2023-08-24
     * @param dto
     * @return List<WarehouseLocationMoveInfoDTO.TabListDTO>>
     */
     List<WarehouseLocationMoveInfoDTO.PdaTabListDTO> tabList(PermissionsDTO dto);

     /**
      * pc端状态统计
      * @author hyj
      * @date 2024/4/12 10:54
      * @param dto
      * @return List<WarehouseLocationMoveInfoDTO.TabListDTO>>
      */
     List<WarehouseLocationMoveInfoDTO.PdaTabListDTO> pcTabList(PermissionsDTO dto);

     /**
     * 详情
     * @author Luo_WG
     * @date: 2023-08-24
     * @param id
     * @return
     */
     WarehouseLocationMoveInfoDTO.ViewDTO view(String id);
     /**
      * 详情
      * @author hyj
      * @date 2024/4/17 11:25
      * @param id
      */
     WarehouseLocationMoveInfoDTO.PcViewDTO pcView(String id);

     /**
     * 新增并提交审核
     * @author Luo_WG
     * @date: 2023-08-24
     * @param dto
     * @return
     */
     void addAndSubmit(WarehouseLocationMoveInfoDTO.AddDTO dto);

     /**
     * 修改并提交审核
     * @author Luo_WG
     * @date: 2023-08-24
     * @param dto
     * @return
     */
     void updateAndSubmit(WarehouseLocationMoveInfoDTO.UpdateDTO dto);

     /**
     * 提交审核
     * @author Luo_WG
     * @date: 2023-08-24
     * @param id
     * @return
     */
    BatchResultDTO submit(String id);

    /**
     * 提交审核
     *
     * @param id
     * @return
     * @author hyj
     * @date 2024/4/19 9:32
     */
    BatchResultDTO pcSubmit(String id);

    /**
    * 审核
    * @author Luo_WG
    * @date: 2023-08-24
    * @param dto
    * @return
    */
    BatchResultDTO approve(ApproveOneDTO dto);

    /**
     * 审核
     *
     * @param dto
     * @return
     * @author hyj
     * @date 2024/4/19 9:32
     */
    BatchResultDTO pcApprove(ApproveOneDTO dto);

    /**
    * 反审核
    * @author Luo_WG
    * @date: 2023-08-24
    * @param id
    * @return
    */
    BatchResultDTO disApprove(String id);

    /**
     * 反审核
     *
     * @param id
     * @return
     * @author hyj
     * @date 2024/4/19 9:32
     */
    BatchResultDTO pcDisApprove(String id);

    /**
    * 删除
    * @author Luo_WG
    * @date: 2023-08-24
    * @param id
    * @return
    */
    BatchResultDTO delete(String id);

    /**
    * 撤销
    * @author Luo_WG
    * @date: 2023-08-24
    * @param id
    * @return
    */
    BatchResultDTO cancelProcess(String id);

    /**
     * 撤销
     *
     * @param id
     * @return
     * @author hyj
     * @date 2024/4/19 10:37
     */
    BatchResultDTO pcCancelProcess(String id);

    /**
    * 审核通过回调方法
    * @param dto
    * @param entity
    * @return
    */
    Boolean approveEnd(ApproveOneDTO dto, WarehouseLocationMoveInfoEntity entity);

    /**
     * 作废
     * @Author Luo_WG
     * @Date 2023/9/4 15:56
     * @param ids
     * @param remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 导出列表
     * @author hyj
     * @date 2024/4/17 10:31
     * @param dto
     * @param response
     * @return Boolean
     */
    void listExport(WarehouseLocationMoveInfoDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * @description: 导入
     * @author hyj
     * @date 2024/4/17 10:31
     * @param excelFile
     * @param response
     * @return Boolean
     */
    WarehouseLocationMoveInfoDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 删除-pc端
     * @author hyj
     * @date 2024/4/19 9:25
     * @param id
     */
    BatchResultDTO pcDelete(String id);
}
