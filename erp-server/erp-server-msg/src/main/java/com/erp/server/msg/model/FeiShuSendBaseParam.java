package com.erp.server.msg.model;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;



/**
 * @Classname: FeishuMsgSendDTO
 * @Description: 飞书发送消息实体
 * @CreateTime: 2023-04-19  19:02
 * @Author: zhangchunlin
 */
@NoArgsConstructor
@Data
@AllArgsConstructor
public class FeiShuSendBaseParam extends BaseNoticeMsgParam {

    /**
     * content
     */
    @JSONField(name = "content")
    private ContentDTO content;


    /**
     * ContentDTO
     */
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class ContentDTO {
        /**
         * text
         */
        @JSONField(name = "text")
        private String text;
        /**
         * post
         */
        @JSONField(name = "post")
        private PostDTO post;
        /**
         * shareChatId
         */
        @JSONField(name = "share_chat_id")
        private String shareChatId;
        /**
         * imageKey
         */
        @JSONField(name = "image_key")
        private String imageKey;

        //---------------config、elements、header为卡片节点--------------------
        /**
         * config
         */
        @JSONField(name = "config")
        private CardDTO.ConfigDTO config;
        /**
         * elements
         */
        @JSONField(name = "elements")
        private List<CardDTO.ElementsDTO> elements;
        /**
         * header
         */
        @JSONField(name = "header")
        private CardDTO.HeaderDTO header;


        /**
         * PostDTO
         */
        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class PostDTO {
            /**
             * zhCn
             */
            @JSONField(name = "zh_cn")
            private ZhCnDTO zhCn;

            /**
             * ZhCnDTO
             */
            @NoArgsConstructor
            @Data
            @AllArgsConstructor
            @Builder
            public static class ZhCnDTO {
                /**
                 * title
                 */
                @JSONField(name = "title")
                private String title;
                /**
                 * content
                 */
                @JSONField(name = "content")
                private List<List<ContentDTO.PostDTO.ZhCnDTO.PostContentDTO>> content;

                /**
                 * ContentDTO
                 */
                @NoArgsConstructor
                @Data
                @AllArgsConstructor
                @Builder
                public static class PostContentDTO {
                    /**
                     * tag
                     */
                    @JSONField(name = "tag")
                    private String tag;
                    /**
                     * text
                     */
                    @JSONField(name = "text")
                    private String text;
                    /**
                     * href
                     */
                    @JSONField(name = "href")
                    private String href;
                    /**
                     * userId
                     */
                    @JSONField(name = "user_id")
                    private String userId;
                }
            }
        }
    }

    /**
     * CardDTO
     */
    @NoArgsConstructor
    @Data
    @AllArgsConstructor
    @Builder
    public static class CardDTO {
        /**
         * config
         */
        @JSONField(name = "config")
        private ConfigDTO config;
        /**
         * elements
         */
        @JSONField(name = "elements")
        private List<ElementsDTO> elements;
        /**
         * header
         */
        @JSONField(name = "header")
        private HeaderDTO header;

        /**
         * ConfigDTO
         */
        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class ConfigDTO {
            /**
             * wideScreenMode
             */
            @JSONField(name = "wide_screen_mode")
            private Boolean wideScreenMode;
            /**
             * enableForward
             */
            @JSONField(name = "enable_forward")
            private Boolean enableForward;
        }

        /**
         * HeaderDTO
         */
        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class HeaderDTO {
            /**
             * title
             */
            @JSONField(name = "title")
            private TitleDTO title;

            /**
             * TitleDTO
             */
            @NoArgsConstructor
            @Data
            @AllArgsConstructor
            @Builder
            public static class TitleDTO {

                /**
                 * tag
                 */
                @JSONField(name = "tag")
                private String tag;

                /**
                 * content
                 */
                @JSONField(name = "content")
                private String content;

            }
        }

        /**
         * ElementsDTO
         */
        @NoArgsConstructor
        @Data
        @AllArgsConstructor
        @Builder
        public static class ElementsDTO {
            /**
             * tag
             */
            @JSONField(name = "tag")
            private String tag;

            /**
             * layout
             */
            @JSONField(name = "layout")
            private String layout;

            /**
             * fields
             */
            @JSONField(name = "fields")
            private List<FieldsDTO> fields;
            /**
             * actions
             */
            @JSONField(name = "actions")
            private List<ActionsDTO> actions;

            /**
             * FieldsDTO
             */
            @NoArgsConstructor
            @Data
            @AllArgsConstructor
            public static class FieldsDTO {
                /**
                 * isShort
                 */
                @JSONField(name = "is_short")
                private Boolean  isShort;
                /**
                 * text
                 */
                @JSONField(name = "text")
                private TextDTO text;

                /**
                 * TextDTO
                 */
                @NoArgsConstructor
                @AllArgsConstructor
                @Data
                public static class TextDTO {

                    /**
                     * tag
                     */
                    @JSONField(name = "tag")
                    private String tag;

                    /**
                     * content
                     */
                    @JSONField(name = "content")
                    private String content;

                }

            }

            /**
             * ActionsDTO
             */
            @NoArgsConstructor
            @Data
            @AllArgsConstructor
            @Builder
            public static class ActionsDTO {
                /**
                 * tag
                 */
                @JSONField(name = "tag")
                private String tag;
                /**
                 * text
                 */
                @JSONField(name = "text")
                private TextDTO text;
                /**
                 * url
                 */
                @JSONField(name = "url")
                private String url;
                /**
                 * type
                 */
                @JSONField(name = "type")
                private String type;

                /**
                 * value
                 */
                @JSONField(name = "value")
                private ValueDTO value;



                /**
                 * TextDTO
                 */
                @NoArgsConstructor
                @Data
                public static class TextDTO {

                    /**
                     * content
                     */
                    @JSONField(name = "content")
                    private String content;

                    /**
                     * tag
                     */
                    @JSONField(name = "tag")
                    private String tag;

                }

                /**
                 * ValueDTO
                 */
                @Data
                @NoArgsConstructor
                public static class ValueDTO {

                    /**
                     * chosen
                     */
                    @JSONField(name = "chosen")
                    private String chosen;

                }
            }
        }
    }

}
