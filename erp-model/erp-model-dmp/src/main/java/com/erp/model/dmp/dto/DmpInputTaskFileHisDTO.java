package com.erp.model.dmp.dto;

import com.common.business.dto.base.SortDTO;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;
import com.common.business.dto.base.SuperDTO;
import java.time.LocalDateTime;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.NotEmpty;
import com.common.business.dto.AdvanceQueryDTO;
import java.util.Map;

/**
 * <p>
 * 拉取任务文件存储归档请求响应实体
 * </p>
 *
 * @author shukai
 * @since 2026-01-26
*/
@Data
@NoArgsConstructor
public class DmpInputTaskFileHisDTO implements Serializable {



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
        * 拉取任务id
        */
        private String mainId;

        /**
        * 文件url
        */
        private String fileUrl;

        /**
        * 解析状态：wait=待解析，finish=已解析
        */
        private String parseStatus;

        /**
        * 已解析行数
        */
        private Integer currParseCount;

        /**
        * 文件大小
        */
        private Integer fileSize;

        /**
        * 文件内容形式
        */
        private String contentType;

        /**
        * 外部系统接口转换init内部数据id
        */
        private String initConvertId;

        /**
        * 外部系统接口转换fds内部数据id
        */
        private String fdsConvertId;


        /**
        * 审核状态名称
        */
        private String approveStatusName;


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
        * 拉取任务id
        */
        private String mainId;

        /**
        * 文件url
        */
        private String fileUrl;

        /**
        * 解析状态：wait=待解析，finish=已解析
        */
        private String parseStatus;

        /**
        * 已解析行数
        */
        private Integer currParseCount;

        /**
        * 文件大小
        */
        private Integer fileSize;

        /**
        * 文件内容形式
        */
        private String contentType;

        /**
        * 外部系统接口转换init内部数据id
        */
        private String initConvertId;

        /**
        * 外部系统接口转换fds内部数据id
        */
        private String fdsConvertId;


    }

    /**
    * 新增
    */
    @Data
    @NoArgsConstructor
    public static class AddDTO extends CommonDTO {


    }

    /**
    * 修改
    */
    @Data
    @NoArgsConstructor
    public static class UpdateDTO extends CommonDTO {

        /**
        * 主键id
        */
        @NotBlank(message = "主键id不能为空")
        private String id;

    }

    @Data
    @NoArgsConstructor
    public static class CommonDTO extends SuperDTO {

        /**
        * 拉取任务id
        */
        @NotBlank(message = "拉取任务id不能为空")
        @Size(max = 19,message = "拉取任务id最大长度不能超过19位")
        private String mainId;

        /**
        * 文件url
        */
        private String fileUrl;

        /**
        * 解析状态：wait=待解析，finish=已解析
        */
        @NotBlank(message = "解析状态：wait=待解析，finish=已解析不能为空")
        @Size(max = 50,message = "解析状态：wait=待解析，finish=已解析最大长度不能超过50位")
        private String parseStatus;

        /**
        * 已解析行数
        */
        @NotNull(message = "已解析行数不能为空")
        private Integer currParseCount;

        /**
        * 文件大小
        */
        @NotNull(message = "文件大小不能为空")
        private Integer fileSize;

        /**
        * 文件内容形式
        */
        @NotBlank(message = "文件内容形式不能为空")
        @Size(max = 50,message = "文件内容形式最大长度不能超过50位")
        private String contentType;

        /**
        * 外部系统接口转换init内部数据id
        */
        private String initConvertId;

        /**
        * 外部系统接口转换fds内部数据id
        */
        private String fdsConvertId;


    }


}