package com.pinyougou.shop.security;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * @作者:pengge
 * @时间:2019/06/26 17:27
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    @Reference
    private SellerService sellerService;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //username是从页面传过来的，密码是框架中自动匹配
        //判断用户名是从数据库中查询，查到了就可以差不到就失败
        //这个findOne 就是通过id查，sellerId 就是用户名
        TbSeller tbSeller = sellerService.findOne(username);
        if(tbSeller==null){
            return null;
        }


        //1：从数据库中拿到密码  "{noop}"为加密
        String password=tbSeller.getPassword();

        // 2：在数据库中查询当前用户的角色信息
        String roleName = "ROLE_USER";

        String status = tbSeller.getStatus();
        //3：审核状态
        if(!"1".equals(status)){
            //未审核 就是 账号不可以用
            return null;
        }
        //返回一个user,AuthorityUtils的作用是将字符串转为list
        //交给框架自动匹配
        User user = new User(username,password , AuthorityUtils.commaSeparatedStringToAuthorityList(roleName));
        return user;
    }
}
