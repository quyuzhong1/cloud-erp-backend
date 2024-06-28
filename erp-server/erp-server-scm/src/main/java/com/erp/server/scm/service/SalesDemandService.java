package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.SalesDemandDTO;
import com.erp.model.scm.dto.SalesDemandDetailDTO;
import com.erp.model.scm.entity.SalesDemandEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 销售需求主表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-15
 */
public interface SalesDemandService extends SuperService<SalesDemandEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/15 16:48
     * @param dto
     * @return PagingVO<List<SalesDemandDTO.listDTO>>
     */
    PagingVO<SalesDemandDTO.ListDTO> paging(PagingDTO<SalesDemandDTO.SearchParamDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/15 17:35
     * @param dto
     * @return Boolean
     */
    String add(SalesDemandDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/15 17:35
     * @param dto
     * @return Boolean
     */
    Boolean update(SalesDemandDTO.UpdateDTO dto);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/3/15 17:44
     * @param id
     * @return ScmSalesDemandDTO
     */
    SalesDemandDTO.ViewDTO view(String id);
    /**
     * @description: 批量作废
     * @author Will
     * @date: 2023/3/15 17:51
     * @param ids
     * @return Boolean
     */
    Boolean invalid(List<String> ids,String reason);
    /**
     * @description: 审核
     * @author Will
     * @date: 2023/3/15 17:54
    * @param entity
    * @param type
    * @param comment
    * @param isNeedProcess
     */
    BatchResultDTO approve(SalesDemandEntity entity, String type, String comment, Boolean isNeedProcess);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/3/15 17:59
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/15 18:01
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(SalesDemandDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/15 18:19
     * @param entity
     * @return Boolean
     */
    BatchResultDTO disApprove(SalesDemandEntity entity);

    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 11:21
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/3/16 16:10
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/3/17 12:18
     * @param excelFile
     * @param response
     * @return  SalesDemandDetailDTO.ImportDTO
     */
    SalesDemandDetailDTO.ImportDTO importFile(MultipartFile excelFile,List<String> skuIds, HttpServletResponse response);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/3/17 13:00
     * @param dto
     * @return Boolean
     */
    Boolean addAndSubmit(SalesDemandDTO.AddDTO dto);
    /**
     * @description: 修改并提交
     * @author Will
     * @date: 2023/3/24 18:11
     * @param dto
     * @return Boolean
     */
    Boolean updateAndSubmit(SalesDemandDTO.UpdateDTO dto);
    /**
     * @description: 
     * @author Will
     * @date: 2023/3/27 11:12
     * @return List<SalesDemandCountDTO>
     */
    List<ListStatusCountDTO.SalesDemandCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 下推备货申请单保存
     * @author Will
     * @date: 2023/5/22 18:17
     * @param list
     * @return Boolean
     */
    Boolean generateSalesDemand(ValidList<SalesDemandDTO.GenerateSalesDemandDTO> list);

    /**
     * 方法说明
     * @author yl
     * @date 2023-05-29 16:40
     * @param soIds
     * @return java.lang.Integer
     */
    Integer getPushDownBySourceIds(List<String> soIds);

}
