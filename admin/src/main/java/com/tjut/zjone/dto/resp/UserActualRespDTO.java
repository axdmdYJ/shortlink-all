package com.tjut.zjone.dto.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

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


        @TableField(exist = false)
        private static final long serialVersionUID = 1L;


    }
