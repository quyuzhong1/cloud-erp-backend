package com.erp.server.wms.service;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.inventory.InitStockDTO;
import com.erp.model.wms.dto.inventory.InitStockDetailDTO;
import com.erp.model.wms.entity.InitStockEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 期初库存表 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-05-10
 */
public interface InitStockService extends SuperService<InitStockEntity> {

    /**
     * 分页列表
     * @param pagingParamDTO
     * @return
     */
    PagingVO<InitStockDTO.ListDTO> paging(PagingDTO<InitStockDTO.SearchParamDTO> pagingParamDTO);


    /**
     * 查看详情
     * @param id
     * @return
     */
    InitStockDTO.ViewDTO view(String id);

    /**
     * 导出Excel
     * @param param
     */
    void exportExcel(InitStockDTO.ExportSearchParamDTO param, HttpServletResponse response);

    /**
     * @description: 导入
     * @param response
     * @return InitStockDetailDTO.ImportDTO
     */
    InitStockDetailDTO.ImportDTO importFile(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 新增
     * @param dto
     * @return
     */
    String add(InitStockDTO.AddDTO dto);

    /**
     * 修改
     * @param dto
     */
    void update(InitStockDTO.UpdateDTO dto);

    /**
     * 批量提交
     * @param ids
     * @return
     */
    void submit(List<String> ids);

    /**
     * 新增并提交
     * @param dto
     */
    void addAndSubmit(InitStockDTO.AddDTO dto);

    /**
     * 修改并提交
     * @param dto
     */
    void updateAndSubmit(InitStockDTO.UpdateDTO dto);

    /**
     * 批量审核
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 删除
     * @param ids
     */
    void delete(List<String> ids);

    /**
     * 反审核
     * @param ids
     */
    void disApprove(List<String> ids);

    /**
     * 作废
     * @param ids
     * @param remark
     */
    void invalid(List<String> ids, String remark);

    /**
     * 删除
     * @param ids
     */
    void cancel(List<String> ids);

    /**
     * 下载模板
     * @param response
     */
    void downloadTemplate(HttpServletResponse response);



}
