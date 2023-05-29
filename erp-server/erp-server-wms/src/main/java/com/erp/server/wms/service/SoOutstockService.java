package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SoInfoDTO;
import com.erp.model.wms.dto.SoOutstockDTO;
import com.erp.model.wms.entity.SoOutstockDetailEntity;
import com.erp.model.wms.entity.SoOutstockEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售订单出库单 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-05-10
 */
public interface SoOutstockService extends SuperService<SoOutstockEntity> {

    /**
     * 根据来源id查询出库表
     * @Author Luo_WG
     * @Date 2023/5/12 12:18
     * @param ids
     * @return java.util.List<com.erp.model.oms.entity.SoOutstockEntity>
     **/
    List<SoOutstockEntity> listBySourceId(List<String> ids);

    /**
     * 销售订单ids获取销售出库单主表信息
     * @Author Luo_WG
     * @Date 2023/5/25 15:42
     * @param soIds
     * @return java.util.List<com.erp.model.wms.entity.SoOutstockEntity>
     **/
    List<SoOutstockEntity> listBySoIds(@RequestBody List<String> soIds);

    /**
     * 添加销售出库单
     * @author yl
     * @date 2023-05-19 9:50
     * @param dto
     * @return java.lang.String
     */
    String add(SoOutstockDTO.AddDTO dto);

    /**
     * 批量提交
     * @author yl
     * @date 2023-05-19 10:34
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submit(List<String> ids);

    /**
     * 新增并提交
     * @author yl
     * @date 2023-05-19 10:42
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(SoOutstockDTO.AddDTO dto);

    
    /**
     * 销售出库单详情
     * @author yl
     * @date 2023-05-19 10:45
     * @param id
     * @return com.erp.model.wms.dto.SoOutstockDTO.ViewDTO
     */
    SoOutstockDTO.ViewDTO view(String id);

    /**
     * 审核
     * @author yl
     * @date 2023-05-19 11:42
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 反审核
     * @author yl
     * @date 2023-05-19 12:10
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean disApprove(BaseIdsDTO.IdsDTO dto);

    /**
     * 撤销流程
     * @author yl
     * @date 2023-05-19 12:13
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 删除销售出库单
     * @author yl
     * @date 2023-05-19 12:16
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean delete(List<String> ids);

    
    /**
     * 作废
     * @author yl
     * @date 2023-05-19 14:16
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean invalid(List<String> ids,String remark);

    /**
     * 获取tab
     * @author yl
     * @date 2023-05-19 14:23
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.TabListDTO>
     */
    List<SoOutstockDTO.TabListDTO> tabList();

    
    /**
     * 分页列表
     * @author yl
     * @date 2023-05-22 8:56
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoOutstockDTO.PagingViewDTO>
     */
    PagingVO<SoOutstockDTO.PagingViewDTO> paging(PagingDTO<SoOutstockDTO.PagingParamDTO> dto);

    
    /**
     * 导出销售出库单
     * @author yl
     * @date 2023-05-22 11:41
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(SoOutstockDTO.ExportDTO dto, HttpServletResponse response);

    
    /**
     * 修改销售出库单
     * @author yl
     * @date 2023-05-22 17:58
     * @param dto
     * @return java.lang.String
     */
    String updateSoOutstock(SoOutstockDTO.UpdateDTO dto);

    /**
     * 修改并提交
     * @author yl
     * @date 2023-05-22 19:04
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(SoOutstockDTO.UpdateDTO dto);

    
    /**
     * 销售出库单保存下推单据
     * @author yl
     * @date 2023-05-23 14:30
     * @param resultList
     * @return java.lang.Boolean
     */
    Boolean addPushDownNo(List<SoOutstockDTO.GenerateSoOutstockViewDTO> resultList);

    
    /**
     * 销售订单获取销售出库单的数据
     * @author yl
     * @date 2023-05-23 18:37
     * @param
     * @return java.util.List<com.erp.model.wms.dto.SoOutstockDTO.SoRefDTO>
     */
    List<SoOutstockDTO.SoRefDTO> listSoRefSoOutstockBySoId(String soId);

    /**
     * 保存销售订单下推销售出库单
     * @author yl
     * @date 2023-05-25 15:02
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean generateSoSave(ValidList<SoInfoDTO.GenerateDeliveryView> dto);
}
