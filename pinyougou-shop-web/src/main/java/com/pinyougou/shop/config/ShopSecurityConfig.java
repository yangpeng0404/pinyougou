package com.pinyougou.shop.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * @作者:pengge
 * @时间:2019/06/26 14:50
 */
@EnableWebSecurity
public class ShopSecurityConfig extends WebSecurityConfigurerAdapter {


    @Autowired
    private UserDetailsService userDetailsService;


    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 1：配置公开资源以及拦截资源，以及角色绑定
     * 2：配置登录
     * 3：配置推出登录
     * 4：防盗链以及同源配置
     * 注：这里配置的是商家登录，安全认证
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.authorizeRequests()
                .antMatchers("/css/**","/img/**","/js/**","/plugins/**","/*.html","/seller/add.shtml").permitAll()
                //设置所有的其他请求都需要认证通过即可 也就是用户名和密码正确即可不需要其他的角色
                .anyRequest().authenticated();
        super.configure(http);

        //设置表单登录
        http.formLogin()
                .loginPage("/shoplogin.html")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/admin/index.html",true)
                .failureUrl("/login?error");
        //退出登录
        http.logout().logoutUrl("/logout").invalidateHttpSession(true);

        //关闭CSRF
        http.csrf().disable();

        //开启同源iframe 可以访问策略
        http.headers().frameOptions().sameOrigin();
    }

    /**
     *登录认证以及授予角色
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
       /* auth.inMemoryAuthentication().withUser("jack")
                .password("{noop}123").roles("admin");*/

       //自定义配置认证
       auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);

    }
}
