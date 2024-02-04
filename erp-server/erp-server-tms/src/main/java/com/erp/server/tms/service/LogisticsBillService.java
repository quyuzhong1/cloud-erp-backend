package com.erp.server.tms.service;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.tms.entity.LogisticsBillEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.vo.response.CancelResponseVO;
import com.erp.model.tms.vo.response.InterceptResponseVO;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 物流单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-11-09
 */
public interface LogisticsBillService extends SuperService<LogisticsBillEntity> {

    /**
    * 新增
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean add(LogisticsBillDTO.AddDTO dto);

    /**
    * 修改
    * @author lambda
    * @date: 2023-11-09
    * @param dto
    * @return
    */
    Boolean update(LogisticsBillDTO.UpdateDTO dto);

    /**
     * 根据来源id查询物流单
     * @Author Luo_WG
     * @Date 2023/11/9 19:40
     * @param sourceIds
     * @return java.util.List<com.erp.model.tms.entity.LogisticsBillEntity>
     **/
    List<LogisticsBillEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 销售出库单反审核后
     * 删除 物流单
     *@parms dto
     *@return
     *@author yl
     *@date 2023-11-24
     */
    Boolean remove(LogisticsBillDTO.RemoveDTO dto);

    /**
     * 新增物流单
     * @Author Luo_WG
     * @Date 2023/11/9 18:04
     * @param addDTOList
     * @return java.lang.Boolean
     **/
    Boolean logisticsBillBatchSave(List<LogisticsBillDTO.AddDTO> addDTOList);

    /**
     * 根据来源id查询物流信息及跟踪号
     * @Author Luo_WG
     * @Date 2023/11/10 9:07
     * @param sourceIdList
     * @return java.util.List<com.erp.model.tms.dto.LogisticsBillDTO.LogisticsBillVo>
     **/
    List<LogisticsBillDTO.LogisticsBillVo> listLogisticsBillVoBySourceIds(List<String> sourceIdList);

    /**
     * @description: 根据出库单号查询
     * @author Will
     * @date: 2023/11/14 19:32
     * @param outstockCodeList
     * @return List<LogisticsBillEntity>
     */
    List<LogisticsBillEntity> listByOutstockCodeList(List<String> outstockCodeList);
    
    /**
     * tab 列表
     *@parms
     *@return 
     *@author yl
     *@date 2023-11-15
     */
    List<LogisticsBillDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 分页
     * @author yl
     * @date 2023-11-16 14:01
     * @param dto
     * @return 
     */
    PagingVO<LogisticsBillDTO.PagingVO> paging(PagingDTO<LogisticsBillDTO.PagingParamDTO> dto);

    /**
     * 导出
     *@parms
     *@return
     *@author yl
     *@date 2023-11-16
     */
    Boolean exportExcel(LogisticsBillDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 生成物流单
     *@parms dto
     *@return
     *@author yl
     *@date 2023-11-23
     */
    LogisticsBillDTO.GenerateBillResultDTO generateBill(LogisticsBillDTO.GenerateBillDTO dto);

    /**
     * 根据销售出单ids获取物流单
     *
     *@parms outstockIdList
     *@return
     *@author yl
     *@date 2023-11-24
     */
    List<LogisticsBillEntity> listByOutstockIdList(List<String> outstockIdList);

    /**
     * 取消物流单
     * @author yl
     * @date 2023-12-08 11:55
     * @param dto
     * @return 
     */
    ApiResult<CancelResponseVO> cancelBill(LogisticsBillDTO.CancelBillDTO dto);

    /**
     * 拦截物流单
     * @param dto
     * @return
     */
    ApiResult<InterceptResponseVO> interceptBill(LogisticsBillDTO.CancelBillDTO dto);

    /**
     * 根据物流单号查询物流单
     * @Author Luo_WG
     * @Date 2023/12/14 15:44
     * @param trackNo
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    LogisticsBillDTO.BaseDTO getBaseByTrackNo(String trackNo);

    /**
     * 根据物流跟踪单号查询物流单详情
     * @Author Luo_WG
     * @Date 2023/12/14 15:45
     * @param trackNoList
     * @return com.erp.model.tms.dto.LogisticsBillDTO.BaseDTO
     **/
    List<LogisticsBillDTO.BaseDTO> listLogisticsBillByTransportNos(List<String> trackNoList);

    /**
     * 打印物流面单/配货单
     * @Author Luo_WG
     * @Date 2023/12/20 14:34
     * @param list
     * @return java.util.List<com.erp.model.oms.dto.SoB2cDTO.WaybillDTO>
     **/
    List<SoB2cDTO.WaybillDTO> printLogisticsWaybill(List<LogisticsBillDTO.PrintLogisticsWaybillDTO> list);

    List<BatchResultDTO> updateBatchTrackNo(List<LogisticsBillDTO.BatchUpdateTrackNoDTO> batchUpdateTrackNoDTOList, Boolean isAdd);

    Map<String, List<String>> mapTrackNoAndSoOutId(List<String> ids);

    List<String> listSoOutIdByQuery(AdvanceQueryContainer advanceQueryContainer);
}
