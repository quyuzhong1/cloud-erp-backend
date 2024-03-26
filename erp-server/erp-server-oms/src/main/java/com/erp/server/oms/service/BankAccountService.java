package com.erp.server.oms.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.BankAccountDTO;
import com.erp.model.oms.entity.BankAccountEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * <p>
 * 银行账号 服务类
 * </p>
 *
 * @author zhangchunlin
 * @since 2023-07-04
 */
public interface BankAccountService extends SuperService<BankAccountEntity> {

    /**
     * 根据银行账号获取
     * @param bankAccountNo
     * @return
     */
    BankAccountEntity findByAccountNo(String bankAccountNo);

    /**
     * 根据组织id获取银行账号
     * @param orgId
     * @return
     */
    List<BankAccountEntity> findByOrgId(String orgId);


    /**
     * 根据组织id和银行账号获取
     * @param orgId
     * @param bankAccountNo
     * @return
     */
    List<BankAccountEntity> findByOrgIdAndAccountNo(String orgId, String bankAccountNo);

    /**
     * 根据组织id和银行名称获取到收款账户信息
     * @param orgId
     * @param accountName
     * @return
     */
    BankAccountEntity findByOrgIdAndAccountName(String orgId, String accountName);

    /**
     * 导入金蝶银行账号信息
     * @author yl
     * @date 2023-07-19 10:04
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 根据账号获取对应数据
     * @author yl
     * @date 2023-10-26 15:01
     * @param receiveAccountList
     * @return java.util.List<com.erp.model.oms.entity.BankAccountEntity>
     */
    List<BankAccountEntity> listByAccountNameList(List<String> receiveAccountList);

    /**
     * 分页获取银行账号信息
     * @description
     * @param dto
     * @return
     * @date 2024-03-06 14:45
     * @author Lambda
     */
    PagingVO<BankAccountDTO.PagingViewDTO> paging(PagingDTO<BankAccountDTO.PagingParamDTO> dto);

    BankAccountDTO.ViewDTO view(String id);

    /**
     * 添加银行账号
     * @description
     * @param dto
     * @return
     * @date 2024-03-06 15:58
     * @author Lambda
     */
    Boolean add(BankAccountDTO.AddDTO dto);
}
