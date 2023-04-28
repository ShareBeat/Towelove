package com.towelove.core.domain.lovealbum;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.towelove.common.core.web.domain.BaseEntity;
import com.towelove.core.domain.lovelogs.LoveLogs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 恋爱相册(LoveAlbum) 实体类
 *
 * @author 张锦标
 * @since 2023-03-26 20:42:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("love_album")
public class LoveAlbum extends BaseEntity {
    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * 男方id
     */
    private Long boyId;
    /**
     * 女方id
     */
    private Long girlId;
    /**
     * 相册标题
     */
    private String title;
    /**
     * 相恋天数
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date daysInLove;
    /**
     * 点赞人数
     */
    private Integer likesNumber;
    /**
     * 观看人数
     */
    private Integer viewsNumber;
    /**
     * 外人是否可见
     */
    private Integer canSee;
    /**
     * 开启状态
     */
    private Integer status;
    /**
     * 相册中的小照片
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<LoveLogs> loveLogs;
    /**
     * 对应的Lovelogs的id
     */
    private List<Long> loveLogsIds;
}
