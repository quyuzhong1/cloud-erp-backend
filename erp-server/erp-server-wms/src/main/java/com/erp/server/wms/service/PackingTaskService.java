package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.WmsCartonDTO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.WmsCartonDetailDTO;
import com.erp.model.wms.dto.WmsCartonSpecDTO;
import com.erp.model.wms.entity.*;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.PackingTaskDTO;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 装箱任务表 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-07-02
 */
public interface PackingTaskService extends SuperService<PackingTaskEntity> {
    /**
    * 新增
    * @author zdy
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PackingTaskDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    Boolean update(PackingTaskDTO.UpdateDTO dto);

    /**
     * 发货单转换成装箱任务实体
     * @param soDeliveryNoticeEntity
     * @return
     */
    void addPackingByB2BDelivery(SoDeliveryNoticeEntity soDeliveryNoticeEntity);

    /**
     * 头程发货单转换装箱任务实体
     * @param firstMileDeliveryEntity
     * @return
     */
    void addPackingByFirstMileDelivery(FirstMileDeliveryEntity firstMileDeliveryEntity);

    /**
     * 装箱任务-分页查询
     * @param dto
     * @return
     */
    PagingVO<PackingTaskDTO.PagingViewDTO> paging(PagingDTO<PackingTaskDTO.PagingParamDTO> dto);

    /**
     * 按照分类进行统计
     * @param dto
     * @return
     */
    List<PackingTaskDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 根据数据来源查询任务列表
     * @param sourceId
     * @param sourceType
     * @return
     */
    List<PackingTaskEntity> listBySourceIdAndSourceType(String sourceId, String sourceType);

    /**
     * 装箱-详情(箱规+产品明细)
     * @param id
     * @return
     */
    WmsCartonSpecDTO.WmsCartonSpecView packingView(String id);

    /**
     * 快粘贴查询sku
     * @param id
     * @return
     */
    List<WmsCartonSpecDTO.GroupSkuDTO> listGroupSkuById(String id);

    /**
     * 装箱保存
     * @param dto
     * @param isDeleteCarton 是否需要删除装箱数据
     * @return
     */
    Boolean packingSave(WmsCartonSpecDTO.WmsCartonAdd dto, Boolean isDeleteCarton);

    void sendNoticeMsg(String taskId, String operation, String content);

    /**
     * 装箱详情
     * @param packedDetailDTO
     * @return
     */
    WmsCartonSpecDTO.ListPackingDTO listPacking(PackingTaskDTO.PackedDetailDTO packedDetailDTO);

    /**
     * 装箱模板
     * @param response
     */
    void downloadPackingTemplate(HttpServletResponse response);

    /**
     * 导入装箱
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);

    List<PackingTaskEntity> listBySourceCodes(List<String> sourceCodes);

    /**
     * 导出装箱任务
     *
     * @param dto
     */
    void exportPacking(PackingTaskDTO.PagingParamDTO dto);

    /**
     * 导出装箱清单
     *
     * @param dto
     */
    void exportPackingDetail(PackingTaskDTO.ExportDTO dto);

    /**
     * 删除装箱任务
     * @param entity
     * @return
     */
    BatchResultDTO delete(PackingTaskEntity entity);

    /**
     * 未装箱明细
     * @param id
     * @return
     */
    WmsCartonSpecDTO.NoPackingView notPackingDetailView(String id);

    /**
     * 已装箱明细
     * @param packedDetailDTO
     * @return
     */
    WmsCartonSpecDTO.PackedView packedDetailView(PackingTaskDTO.PackedDetailDTO packedDetailDTO);

    /**
     * 调整装箱-详情
     * @param adjustDTO
     * @return
     */
    WmsCartonDTO.WmsCartonView adjustPackingView(WmsCartonDTO.AdjustDTO adjustDTO);

    /**
     * 新增装箱 pda
     * @param dto
     * @return
     */
    WmsCartonDTO.PrintDTO pdaPackingSave(WmsCartonSpecDTO.AddDTO dto);

    /**
     * 新增装箱-详情
     * @param searchDTO
     * @return
     */
    WmsCartonDTO.WmsCartonView packingSaveView(WmsCartonDTO.CartonSearchDTO searchDTO);

    /**
     * 根据箱子查询装箱详情
     * @param cartonId
     * @return
     */
    WmsCartonDTO.WmsCartonView packingViewByCartonId(String cartonId);

    /**
     * 暂存本箱
     * @param dto
     * @return
     */
    WmsCartonDTO.PrintDTO stagingPacking(WmsCartonSpecDTO.AddDTO dto);

    /**
     * 根据外部单号进行查询箱规详情
     * @param requestDTO
     * @return
     */
    WmsCartonSpecDTO.CartonSpecDTO cartonSpecView(WmsCartonSpecDTO.SpecRequestDTO requestDTO);

    /**
     * 调整装箱保存
     * @param dto
     * @return
     */
    String adjustPackingSave(WmsCartonDTO.AdjustSaveDTO dto);

    /**
     * 箱规保存
     * @param dto
     */
    ApiResult<String> cartonSpecSave(WmsCartonSpecDTO.SpecSaveDTO dto);

    PackingTaskEntity getBySourceCode(String sourceCode);

    ApiResult<String> dimensionalWeight(DimensionalWeightDTO dto);

    /**
     * b2b拣货
     * @param packingTaskIds
     * @param sourceCodeList
     * @return
     */
    List<PackingTaskDTO.StatusDTO> selectPackingStatusByIds(List<String> packingTaskIds, List<String> sourceCodeList);


    /**
     * 模糊搜索-支持分页
     * @param requestDTO
     * @return
     */
    PagingVO<PackingTaskDTO.PackingTreeDTO> searchSourceCode(PagingDTO<PackingTaskDTO.SearchSourceCodeDTO> requestDTO);

    /**
     * 根据任务id获取箱规列表
     * @param taskId
     * @return
     */
    List<WmsCartonSpecDTO.SpecDTO> getCartonSpecByTaskId(String taskId);


    /**
     * 更新任务状态
     * @param groupSkuDTOS
     * @param packingTaskEntity
     */
    void updatePackingStatus(List<WmsCartonSpecDTO.GroupSkuDTO> groupSkuDTOS, PackingTaskEntity packingTaskEntity);

    void updateWeightStatus(PackingTaskEntity packingTaskEntity);

    String getOutBoxNoBase64(String outBoxNo);

    /**
     * 获取打印条码
     * @param cartonId
     * @return
     */
    WmsCartonDTO.PrintDTO getPrintBarCode(String cartonId);

    void addPackingByRequisition(RequisitionApplicationEntity entity);

    void updateDetailQty(Map<String, Integer> qtyMap);

    PagingVO<WmsCartonDetailDTO.ListPackingDetailDTO> exportPackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto);

    PagingVO<PackingTaskDTO.PagingViewDTO> exportPackingTask(PagingDTO<PackingTaskDTO.PagingParamDTO> dto);

    /**
     *  根据任务id更新装箱状态
     * @param taskId
     * @return
     */
    void updatePackingStatusByTaskId(String taskId);

    /**
     * 根据外部箱号查询装箱的基础信息和产品明细
     * @param outBoxNo
     * @return
     */
    WmsCartonDTO.OutBoxNoDTO getCartonDetailByOutBoxNo(String outBoxNo);

    /**
     * 导出未装箱明细
     * @param dto
     */
    void exportUnPackingDetail(PackingTaskDTO.ExportDTO dto);

    /**
     * 查询未装箱明细
     * @param dto
     * @return
     */
    PagingVO<WmsCartonSpecDTO.NoPackingViewDTO> unPackingTaskDetail(PagingDTO<PackingTaskDTO.ExportDTO> dto);
}
