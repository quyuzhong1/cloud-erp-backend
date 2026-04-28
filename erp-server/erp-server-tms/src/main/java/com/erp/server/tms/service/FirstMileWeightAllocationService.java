package com.erp.server.tms.service;

import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.FirstMileWeightAllocationDTO;
import com.erp.model.tms.dto.TmsAsyncTaskRecordDTO;
import com.erp.model.tms.entity.FirstMileWeightAllocationEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 头程重量分摊 服务类
 * </p>
 *
 * @author tmj
 * @since 2024-08-20
 */
public interface FirstMileWeightAllocationService extends SuperService<FirstMileWeightAllocationEntity> {

    /**
     * 分页查询
     * @param dto
     * @return
     * @date: 2024-08-22
     * @author: tanmujin
     */
    PagingVO<FirstMileWeightAllocationDTO.ViewDTO> paging(PagingDTO<FirstMileWeightAllocationDTO.PagingParamDTO> dto);

    /**
     * 导出Excel
     *
     * @param dto
     * @return
     * @date: 2024-08-22
     * @author: tanmujin
     */
    void exportExcel(FirstMileWeightAllocationDTO.ExportParamDTO dto);

    /**
     * tab页统计
     *
     * @param
     * @param pagingParamDTO
     * @return
     * @date: 2024-08-22
     * @author: tanmujin
     */
    List<FirstMileWeightAllocationDTO.TabDTO> tabList(FirstMileWeightAllocationDTO.PagingParamDTO pagingParamDTO);

    /**
     * 重量重算
     * @param logisticsBillId 头程物流单ID
     * @date: 2024-08-22
     * @author: tanmujin
     */
    BatchResultDTO weightReCompute(String logisticsBillId);

    /**
     * 删除
     * @param id
     * @date: 2024-08-22
     * @author: tanmujin
     */
    BatchResultDTO deleteByLogisticsBillId(String id);

    /**
     * 根据物流单号查询重量分摊
     * @param logisticsBillIds 物流单号集合
     * @return
     * @date: 2024-08-23
     * @author: tanmujin
     */
    List<FirstMileWeightAllocationEntity> listByLogisticsBillIds(List<String> logisticsBillIds);

    /**
     * 根据发货单id获取重量分摊记录
     * @param sourceIds
     * @param statusList  CostAllocationStatusEnum
     * @return
     */
    List<FirstMileWeightAllocationEntity> listBySourceIds(List<String> sourceIds, List<String> statusList);

    /**
     * 新增重量分摊
     */
    BatchResultDTO add(String logisticsBillId) throws InterruptedException;

    /**
     * 根据sourceId删除
     * @param sourceId
     */
    void updateCalculateMonthBySourceId(String sourceId);

    /**
     * 查看商品重量
     * @param dto
     * @return
     */
    List<FirstMileWeightAllocationDTO.ViewProductWeightDTO> viewProductWeight(FirstMileWeightAllocationDTO.ViewProductWeightParamDTO dto);

    /**
     * 导入Excel
     * @param excelFile
     * @param response
     * @return
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 根据业务单号查询重量分摊
     * @param businessCodeList 业务单号
     * @param sourceCodeList 来源单号
     * @param transportNoList 物流单号
     * @return
     */
    List<FirstMileWeightAllocationEntity> listBySourceCodeList(List<String> businessCodeList, List<String> sourceCodeList, List<String> transportNoList);

    void downloadTemplate(HttpServletResponse response);

    /**
     * 游标分页查询可下推分摊的发货单ID
     * @param params 查询参数（含lastId游标、batchSize批大小）
     * @return 发货单ID列表
     * @author jack
     * @date 2026-04-22
     */
    List<String> pageFirstMileDeliveryIds(TmsAsyncTaskRecordDTO.PushParamsDTO params);

    /**
     * 统计可下推分摊的总条数
     * @param params 查询参数
     * @return 总条数
     * @author jack
     * @date 2026-04-22
     */
    int countFirstMileDeliveryIds(TmsAsyncTaskRecordDTO.PushParamsDTO params);
}
