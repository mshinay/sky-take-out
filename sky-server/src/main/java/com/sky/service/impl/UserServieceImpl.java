package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServieceImpl implements UserService {

    private static String WECHAT_LOGIN ="https://api.weixin.qq.com/sns/jscode2session";
    @Autowired
    private WeChatProperties weChatProperties;
    @Autowired
    private UserMapper userMapper;
    /**
     * 用户微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User userWeChatLogin(UserLoginDTO userLoginDTO) {
        String openid =getOpenId(userLoginDTO.getCode());
        //验证openid是否为空，为空则报错
        if(openid==null){
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        //查询是否为新用户
        User user = userMapper.getByOpenId(openid);
        //若为新用户则进行注册
        if(user==null){
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 访问微信登录接口得到openid
     * @param code
     * @return
     */
    private String getOpenId(String code){
        //访问微信登录接口，获取用户openid
        Map<String, String> paraMap = new HashMap<String, String>();
        paraMap.put("appid",weChatProperties.getAppid());
        paraMap.put("secret",weChatProperties.getSecret());
        paraMap.put("js_code",code);
        paraMap.put("grant_type", "authorization_code");
        String json= HttpClientUtil.doGet(WECHAT_LOGIN,paraMap);
        //将字符串解析为json对象
        JSONObject jsonObject = JSONObject.parseObject(json);
        String openid = jsonObject.getString("openid");
        return openid;
    }
}
