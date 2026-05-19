package com.zozo.zozopicturebackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;


import com.baomidou.mybatisplus.extension.service.IService;
import com.zozo.zozopicturebackend.model.dto.space.SpaceAddRequest;
import com.zozo.zozopicturebackend.model.dto.space.SpaceQueryRequest;
import com.zozo.zozopicturebackend.model.entity.Space;
import com.zozo.zozopicturebackend.model.entity.User;
import com.zozo.zozopicturebackend.model.vo.SpaceVO;


import javax.servlet.http.HttpServletRequest;

/**
 * @author 75526
 * @description 针对表【space(空间)】的数据库操作Service
 * @createDate 2026-05-19 16:07:53
 */
public interface SpaceService extends IService<Space> {


    /**
     *   创建空间
     * @param spaceAddRequest
     * @param loginUser
     * @return
     */
    long addSpace(SpaceAddRequest spaceAddRequest, User loginUser);


    /**
     * 校验空间
     *
     * @param space
     */
    void validSpace(Space space, boolean add);


    /**
     * 获取空间包装类VO
     *
     * @param space
     * @param request
     * @return
     */
    SpaceVO getSpaceVO(Space space, HttpServletRequest request);

    /**
     * 获取空间分页
     *
     * @param spacePage
     * @param request
     * @return
     */
    Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request);

    /**
     * 获取空间查询条件
     *
     * @param spaceQueryRequest
     * @return
     */
    QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest);


    /**
     * 根据空间级别默认填充空间容量和大小
     * @param space
     */
    void fillSpaceBySpaceLevel(Space space);
}
