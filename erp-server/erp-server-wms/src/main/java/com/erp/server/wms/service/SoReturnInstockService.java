package com.erp.server.wms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoB2cReturnDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cReturnDetailEntity;
import com.erp.model.oms.entity.SoB2cReturnEntity;
import com.erp.model.wms.dto.SoReturnInstockDTO;
import com.erp.model.wms.dto.SoReturnReceiveDTO;
import com.erp.model.wms.entity.SoReturnInstockDetailEntity;
import com.erp.model.wms.entity.SoReturnInstockEntity;
import com.erp.wms.aliexpress.model.returnorder.AliexpressReturnInstockDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 采购退货入库单
 * @author Luo_WG
 * @since 2023-05-10
 */
public interface SoReturnInstockService extends SuperService<SoReturnInstockEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnInstockDTO.PagingViewDTO>
     **/
    PagingVO<SoReturnInstockDTO.PagingView> paging(PagingDTO<SoReturnInstockDTO.PagingParam> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.listCount>
     **/
    List<SoReturnInstockDTO.StatusCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String add(SoReturnInstockDTO.Add dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(SoReturnInstockDTO.Update dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.SoReturnInstockDTO.ViewDTO
     **/
    SoReturnInstockDTO.View view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(SoReturnInstockDTO.Add dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(SoReturnInstockDTO.Update dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param entity
     * @param type
     * @param comment
     * @param isNeedProcess
     * @return java.lang.Boolean
     **/
    BatchResultDTO approve(SoReturnInstockEntity entity, String type, String comment, Boolean isNeedProcess);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return java.lang.Boolean
     **/
    BatchResultDTO disApprove(SoReturnInstockEntity entity, Boolean isPushKingDee);
    
    BatchResultDTO generateLogisticsBill(SoReturnInstockEntity entity);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @param remark remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * @description: 原子批量删除销售退货入库单
     * @author Will
     * @date: 2023/5/17 15:15
     * @param ids
     * @param returnDetails
     * @return List<BatchResultDTO>
     */
    List<BatchResultDTO> deleteByIds(List<String> ids, boolean returnDetails);

    /**
     * 删除单个实体
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param entity
     * @return BatchResultDTO
     **/
    BatchResultDTO deleteEntity(SoReturnInstockEntity entity);

    /**
     * 导出
     *
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     **/
    Boolean exportExcel(@RequestBody SoReturnInstockDTO.PagingParam dto);

    /**
     * 质检下推退货入库单-保存
     * @Author Luo_WG
     * @Date 2023/5/11 11:47
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean qcGenerateSoReturnInstockSave(List<SoReturnInstockDTO.GenerateSoReturnInstockView> list);

    /**
     * 签收下推退货入库单-保存
     * @Author Luo_WG
     * @Date 2023/5/11 11:47
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean receiveGenerateSoReturnInstockSave(List<SoReturnReceiveDTO.ReceiveGenerateSoReturnInstockView> list);

    /**
     * 根据单号查询
     * @Author Luo_WG
     * @Date 2023/6/28 19:53
     * @param codeList codeList
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockEntity>
     **/
    List<SoReturnInstockEntity> listByCode(List<String> codeList);

    /**
     * 根据来源单据id查询
     * @Author Luo_WG
     * @Date 2023/6/28 19:53
     * @param sourceIds sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoReturnInstockEntity>
     **/
    List<SoReturnInstockEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 更新金蝶状态等信息
     * @Author Luo_WG
     * @date: 2023/5/23 17:41
     * @param id
     * @param syncKingdeeId
     * @return Boolean
     **/
    Boolean updateSyncKingdeeId(String id, String syncKingdeeId);

    /**
     * 根据主键删除
     * @Author Luo_WG
     * @Date 2023/7/17 11:06
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean deleteByIds(List<String> ids);

    /**
     * 保存金蝶退货单
     * @Author Luo_WG
     * @Date 2023/7/17 11:06
     * @param instockEntity
     * @param detailEntityList
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean saveKingdeeSoReturn(SoReturnInstockEntity instockEntity, List<SoReturnInstockDetailEntity> detailEntityList, List<String> ids);

    /**
     * Pda:列表查询
     * @Author Luo_WG
     * @Date 2023/8/17 16:38
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoReturnInstockDTO.PdaPagingView>
     **/
    PagingVO<SoReturnInstockDTO.PdaPagingView> PdaPaging(PagingDTO<SoReturnInstockDTO.PdaPagingParam> dto);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/8/17 18:48
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoReturnInstockDTO.PdaSoReturnInstockCountDTO>
     **/
    List<SoReturnInstockDTO.PdaSoReturnInstockCountDTO> pdaListCount(PermissionsDTO dto);

    /**
     * PDA:新增
     * @Author Luo_WG
     * @Date 2023/8/28 18:45
     * @param dto
     * @return java.lang.String
     **/
    String pdaAdd(SoReturnInstockDTO.Add dto);

    /**
     * PDA:修改
     * @Author Luo_WG
     * @Date 2023/8/28 18:45
     * @param dto
     * @return java.lang.String
     **/
    Boolean pdaUpdate(SoReturnInstockDTO.Update dto);
    /**
     * @description: 下推加工单
     * @author Will
     * @date: 2023/8/28 14:13
     * @param ids
     * @return List<ViewGenerateMachineInfoDTO>
     */
    List<SoReturnInstockDTO.ViewGenerateMachineInfoDTO> viewGenerateMachineInfo(List<String> ids);
    /**
     * @description: 生成加工单
     * @author Will
     * @date: 2023/8/28 15:02
     * @param list
     * @return Boolean
     */
    Boolean generateMachineInfo(ValidList<SoReturnInstockDTO.GenerateMachineInfoDTO> list);

    /**
     * PDA:查询详情
     * @Author Luo_WG
     * @Date 2023/8/29 15:23
     * @param id
     * @return com.erp.model.wms.dto.SoReturnInstockDTO.View
     **/
    SoReturnInstockDTO.View pdaView(String id);

    /**
     * PDA:新增提交
     * @Author Luo_WG
     * @Date 2023/8/29 15:35
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaAddAndSubmit(SoReturnInstockDTO.Add dto);

    /**
     * pda:修改提交
     * @Author Luo_WG
     * @Date 2023/8/29 15:35
     * @param dto
     * @return java.lang.Boolean
     **/
    Boolean pdaUpdateAndSubmit(SoReturnInstockDTO.Update dto);

    PagingVO<SoReturnInstockDTO.PagingView> exportSoReturnInStock(PagingDTO<SoReturnInstockDTO.PagingParam> dto);

    PagingVO<SoReturnInstockDTO.SearchDTO> pagingSelect(PagingDTO<SoReturnInstockDTO.SelectDTO> searchDTO);

    SoReturnInstockEntity getByThirdCode(String thirdCode);
    SoReturnInstockEntity getBySourceId(String sourceId);

    void addByThirdWarehouse(SoReturnInstockEntity soReturnInstockEntity, List<SoReturnInstockDetailEntity> detailEntityList);

    List<SoReturnInstockEntity> queryToSdy(LocalDate toLocalDate, LocalDate toLocalDate1, Integer pageSize, int offset);
    /**
     * 下载模板
     * @author will
     * @date 2025/4/24 19:48
     * @param response
     * @return void
     */
    void downloadTemplate(HttpServletResponse response);
    /**
     * 导入
     * @author will
     * @date 2025/4/24 19:49
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    AliexpressReturnInstockDTO newSyncDataToCaiNiao(SoReturnInstockEntity entity, List<SoReturnInstockDetailEntity> detailEntityList, String syncOperate);

    /**
     * 下推退货入库单保存
     *
     * @param soB2cReturnEntity
     * @param detailEntityList
     * @param returnInstockDTOS
     * @param soB2cEntity
     * @param b2cDetailEntityList
     * @return
     */
    BatchResultDTO returnInstockSave(SoB2cReturnEntity soB2cReturnEntity, List<SoB2cReturnDetailEntity> detailEntityList, List<SoB2cReturnDTO.ReturnInstockDTO> returnInstockDTOS, SoB2cEntity soB2cEntity, List<SoB2cDetailEntity> b2cDetailEntityList);

    /**
     * 根据ID列表获取实体Map
     * @param ids
     * @return Map<String, SoReturnInstockEntity>
     */
    Map<String, SoReturnInstockEntity> mapByIds(List<String> ids);

}
