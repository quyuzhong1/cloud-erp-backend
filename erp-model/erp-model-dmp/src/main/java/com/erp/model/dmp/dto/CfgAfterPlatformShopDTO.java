package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 请求响应实体
 * </p>
 *
 * @author wtr
 * @since 2026-03-03
*/
@Data
@NoArgsConstructor
public class CfgAfterPlatformShopDTO implements Serializable {



     /**
     * 状态统计
     */
     @Data
     @NoArgsConstructor
     @AllArgsConstructor
     public static class TabListDTO {

         /**
         * 类型
         */
         private String tabFlag;

         /**
         * 数量
         */
         private Integer count;

     }


     /**
     * 分页列表查询参数
     */
     @Data
     @NoArgsConstructor
     public static class PagingParamDTO extends SortDTO {

         /**
         * 页面高级查询
         */
         private List<AdvanceQueryDTO> advanceQueryDTOList;

        /**
            * sqlMap 默认key default
        */
        private Map<String,String> sqlMap;

     }


    /**
    * 分页列表
    */
    @Data
    @NoArgsConstructor
    public static class ListDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 平台编码
        */
        private String dictPlatform;

        /**
        * 平台名称
        */
        private String platformName;

        /**
         * 店铺list
         */
        private List<ShopInfoDTO> shopInfoDTOList;

        /**
         * 售后人员list
         */
        private List<CsAgentDTO> csAgentDTOList;

        /**
        * 创建时间
        */
        private LocalDateTime createTime;

        /**
        * 创建人名称
        */
        private String createUserName;

    }


    /**
    * 导出Excel
    */
    @Data
    @NoArgsConstructor
    public static class ExportDTO extends PagingParamDTO {
        /**
        * 勾选的id集合
        */
        private List<String> ids;
    }

    /**
    * 详情
    */
    @Data
    @NoArgsConstructor
    public static class ViewDTO {

        /**
        * 主键id
        */
        private String  id;

        /**
        * 平台编码
        */
        private String dictPlatform;

        /**
        * 平台名称
        */
        private String platformName;

        /**
        * 店铺id json
        */
        private String shopJson;

        /**
        * 售后人员id json
        */
        private String csAgentJson;


    }

    @Data
    @NoArgsConstructor
    public static class ShopInfoDTO{
        /**
         * 店铺id
         */
        private String id;

        /**
         * 店铺名称
         */
        private String name;
    }

    @Data
    @NoArgsConstructor
    public static class CsAgentDTO{
        /**
         * 售后人员id
         */
        private String id;

        /**
         * 售后人员名称
         */
        private String name;
    }

    /**
    * 保存
    */
    @Data
    @NoArgsConstructor
    public static class SaveDTO  {

        private List<AfterPlatfromShopDTO> afterPlatfromShopDTOList;

        private List<String> deleteIdList;
    }

    @Data
    @NoArgsConstructor
    public static class AfterPlatfromShopDTO extends CommonDTO {

        /**
         * 主键id
         */
        private String id;

        /**
         * 店铺list
         */
        private List<String> shopIdList;

        /**
         * 售后人员list
         */
        @NotNull(message = "售后人员idList不能为空")
        private List<String> csAgentIdList;
    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 平台编码
        */
        @NotBlank(message = "平台编码不能为空")
        @Size(max = 255,message = "平台编码最大长度不能超过255位")
        private String dictPlatform;

        /**
        * 平台名称
        */
        @NotBlank(message = "平台名称不能为空")
        @Size(max = 255,message = "平台名称最大长度不能超过255位")
        private String platformName;

    }

    @Data
    @NoArgsConstructor
    public static class ShopJsonDTO {

        private List<Shop> shops;
    }

    @Data
    @NoArgsConstructor
    public static class Shop {

        private String id;

        private String name;
    }

    @Data
    @NoArgsConstructor
    public static class CsAgentJsonDTO {

        private List<CsAgent> csAgents;
    }

    @Data
    @NoArgsConstructor
    public static class CsAgent {

        private String id;

        private String name;
    }
}