package com.nonameplz.sfservice.mapper;

import com.nonameplz.sfservice.domain.po.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Noname
 * @since 2024-08-05
 */
@Mapper
public interface UsersMapper extends BaseMapper<User> {

}
