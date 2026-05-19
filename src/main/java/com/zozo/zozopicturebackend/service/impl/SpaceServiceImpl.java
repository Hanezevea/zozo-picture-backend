package com.zozo.zozopicturebackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zozo.zozopicturebackend.exception.BusinessException;
import com.zozo.zozopicturebackend.exception.ErrorCode;
import com.zozo.zozopicturebackend.exception.ThrowUtils;
import com.zozo.zozopicturebackend.model.dto.space.SpaceAddRequest;
import com.zozo.zozopicturebackend.model.dto.space.SpaceQueryRequest;
import com.zozo.zozopicturebackend.model.entity.Space;
import com.zozo.zozopicturebackend.model.entity.User;
import com.zozo.zozopicturebackend.model.enums.SpaceLevelEnum;
import com.zozo.zozopicturebackend.model.vo.SpaceVO;
import com.zozo.zozopicturebackend.model.vo.UserVO;
import com.zozo.zozopicturebackend.service.SpaceService;
import com.zozo.zozopicturebackend.mapper.SpaceMapper;
import com.zozo.zozopicturebackend.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 75526
 * @description 针对表【space(空间)】的数据库操作Service实现
 * @createDate 2026-05-19 16:07:53
 */
@Service
public class SpaceServiceImpl extends ServiceImpl<SpaceMapper, Space>
        implements SpaceService {


    @Resource
    private UserService userService;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Override
    public long addSpace(SpaceAddRequest spaceAddRequest, User loginUser) {
        //1.实体类转化，校验参数
        Space space = new Space();
        BeanUtils.copyProperties(spaceAddRequest, space);
        //设置默认值
        if(StrUtil.isBlank(spaceAddRequest.getSpaceName())){
            space.setSpaceName("默认空间");
        }
        if(spaceAddRequest.getSpaceLevel() == null){
            space.setSpaceLevel(SpaceLevelEnum.COMMON.getValue());
        }
        //2.填充空间大小
        this.fillSpaceBySpaceLevel(space);
        //3.校验空间参数
        this.validSpace(space, true);
        Long userId = loginUser.getId();
        space.setUserId(userId);
        //4.权限校验
        if(SpaceLevelEnum.COMMON.getValue() != space.getSpaceLevel() && !userService.isAdmin(loginUser)){
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限创建指定空间");
        }

        //5.对用户进行加锁
        String lock =String.valueOf(userId).intern();
        synchronized (lock) {
            Long newSpaceId= transactionTemplate.execute(status -> {

                //判断用户是否已存在空间
                boolean exists = this.lambdaQuery().eq(Space::getUserId, userId)
                        .exists();
                ThrowUtils.throwIf(exists, ErrorCode.OPERATION_ERROR, "每个用户仅有一个空间");
                //写入数据库
                boolean result = this.save(space);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
                //返回写入的id
                return space.getId();
            });
            return newSpaceId;
        }
    }

    @Override
    public void validSpace(Space space, boolean add) {
        ThrowUtils.throwIf(space == null, ErrorCode.PARAMS_ERROR);
        //从对象中取值
        String spaceName = space.getSpaceName();
        Integer spaceLevel = space.getSpaceLevel();
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(spaceLevel);

        //如果是创建时校验
        if (add) {
            if (StrUtil.isBlank(spaceName)) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称不能为空");
            }
            if (spaceLevel == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间级别不能为空");
            }
        }
        //修改数据时要改空间级别
        if (spaceLevel != null && spaceLevelEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间不存在");
        }
        if (StrUtil.isNotBlank(spaceName) && spaceName.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "空间名称过长");
        }


    }

    @Override
    public SpaceVO getSpaceVO(Space space, HttpServletRequest request) {
        // 对象转封装类
        SpaceVO spaceVO = SpaceVO.objToVo(space);
        // 关联查询用户信息
        Long userId = space.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            spaceVO.setUser(userVO);
        }
        return spaceVO;
    }

    @Override
    public Page<SpaceVO> getSpaceVOPage(Page<Space> spacePage, HttpServletRequest request) {
        List<Space> spaceList = spacePage.getRecords();
        Page<SpaceVO> spaceVOPage = new Page<>(spacePage.getCurrent(), spacePage.getSize(), spacePage.getTotal());
        if (CollUtil.isEmpty(spaceList)) {
            return spaceVOPage;
        }
        // 对象列表 => 封装对象列表
        List<SpaceVO> spaceVOList = spaceList.stream().map(SpaceVO::objToVo).collect(Collectors.toList());
        // 1. 关联查询用户信息
        Set<Long> userIdSet = spaceList.stream().map(Space::getUserId).collect(Collectors.toSet());
        //1=> user1  , 2=> user2
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        spaceVOList.forEach(spaceVO -> {
            Long userId = spaceVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            spaceVO.setUser(userService.getUserVO(user));
        });
        spaceVOPage.setRecords(spaceVOList);
        return spaceVOPage;
    }

    @Override
    public QueryWrapper<Space> getQueryWrapper(SpaceQueryRequest spaceQueryRequest) {
        QueryWrapper<Space> queryWrapper = new QueryWrapper<>();
        if (spaceQueryRequest == null) {
            return queryWrapper;
        }
        //从对象中取值
        Long id = spaceQueryRequest.getId();
        Long userId = spaceQueryRequest.getUserId();
        String spaceName = spaceQueryRequest.getSpaceName();
        Integer spaceLevel = spaceQueryRequest.getSpaceLevel();
        String sortOrder = spaceQueryRequest.getSortOrder();
        String sortField = spaceQueryRequest.getSortField();

        //拼接查询对象
        queryWrapper.eq(ObjUtil.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjUtil.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjUtil.isNotEmpty(spaceLevel), "spaceLevel", spaceLevel);
        queryWrapper.like(StrUtil.isNotBlank(spaceName), "spaceName", spaceName);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }



    @Override
    public void fillSpaceBySpaceLevel(Space space) {
        // 根据空间级别，自动填充限额
        SpaceLevelEnum spaceLevelEnum = SpaceLevelEnum.getEnumByValue(space.getSpaceLevel());
        if (spaceLevelEnum != null) {
            long maxSize = spaceLevelEnum.getMaxSize();
            if (space.getMaxSize() == null) {
                space.setMaxSize(maxSize);
            }
            long maxCount = spaceLevelEnum.getMaxCount();
            if (space.getMaxCount() == null) {
                space.setMaxCount(maxCount);
            }
        }
    }

}




