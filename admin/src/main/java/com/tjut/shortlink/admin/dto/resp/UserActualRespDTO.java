package com.tjut.shortlink.admin.dto.resp;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.Date;

@Data
public class UserActualRespDTO {
        /**
         * ID
         */
        @TableId(type = IdType.AUTO)
        private Long id;

        /**
         * 用户名
         */
        private String username;

        /**
         * 手机号
         */
        private String phone;

        /**
         * 邮箱
         */
        private String mail;

        /**
         * 创建时间
         */
        @TableField(fill = FieldFill.INSERT)
        private Date createTime;

        /**
         * 修改时间
         */
        @TableField(fill = FieldFill.INSERT)
        private Date updateTime;

        /**
         * 删除标识 0：未删除 1：已删除
         */
        @TableField(fill = FieldFill.INSERT)
        private Integer delFlag;
        @TableField(exist = false)
        private static final long serialVersionUID = 1L;


    }
