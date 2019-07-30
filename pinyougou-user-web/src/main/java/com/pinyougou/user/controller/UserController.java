package com.pinyougou.user.controller;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.car.service.CartService;
import com.pinyougou.common.utils.CookieUtil;
import com.pinyougou.common.utils.PhoneFormatCheckUtils;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.Cart;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojogroup.UserOrder;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;

import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.Error;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/user")
public class UserController {

	@Reference
	private UserService userService;

	@Reference
	private OrderService orderService;

	@Reference
	private CartService cartService;


	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbUser> findAll(){			
		return userService.findAll();
	}


	@RequestMapping("/addpayLog")
	public Result addpayLog(Long orderId) {
		try {
			orderService.addPayLog(orderId);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}

	
	
	@RequestMapping("/findPage")
    public PageInfo<TbUser> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize) {
        return userService.findPage(pageNo, pageSize);
    }

	/**
	 * 发送短信验证码
	 * @param phone
	 * @return
	 */
	@RequestMapping("/sendCode")
	public Result sendCode(String phone){
		//判断手机号格式
		if(!PhoneFormatCheckUtils.isPhoneLegal(phone)){
			return new Result(false, "手机号格式不正确");
		}
		try {
			userService.createSmsCode(phone);//生成验证码
			return new Result(true, "验证码发送成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(true, "验证码发送失败");
		}
	}
	/**
	 * 增加
	 * @param user
	 * @return
	 */
	@RequestMapping("/add/{smsCode}")
	public Result add(@RequestBody TbUser user,
					  @PathVariable(value = "smsCode") String smscode,
					  BindingResult bindingResult//这个是校验的结果

	){
		try {
			//先校验
			if(bindingResult.hasErrors()){
				Result result = new Result(false,"失败");
				List<FieldError> fieldErrors = bindingResult.getFieldErrors();
				for (FieldError fieldError : fieldErrors) {
					result.getErrorsList().add(new Error(fieldError.getField(),fieldError.getDefaultMessage()));
				}
				return result;
			}
			//如果没有错误

			//验证码错误直接返回结果
			boolean checkSmsCode = userService.checkSmsCode(user.getPhone(), smscode);
			if(checkSmsCode==false){
				return new Result(false, "验证码输入错误！");
			}
			//把一些不能为空的设置进去
			user.setCreated(new Date());//创建日期
			user.setUpdated(new Date());//修改日期
			String password = DigestUtils.md5Hex(user.getPassword());//对密码加密
			user.setPassword(password);

			//使用继承的add方法
			userService.add(user);

			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param user
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody TbUser user){
		try {
			userService.update(user);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne/{id}")
	public TbUser findOne(@PathVariable(value = "id") Long id){
		return userService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(@RequestBody Long[] ids){
		try {
			userService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
	

	@RequestMapping("/search")
    public PageInfo<TbUser> findPage(@RequestParam(value = "pageNo", defaultValue = "1", required = true) Integer pageNo,
                                      @RequestParam(value = "pageSize", defaultValue = "10", required = true) Integer pageSize,
                                      @RequestBody TbUser user) {
        return userService.findPage(pageNo, pageSize, user);
    }



	/**
	 * 根据用户 id查询相对应订单
	 * @return 返回一个订单集合
	 */
	@RequestMapping("/findOrderList")
	public List<UserOrder> findOrderList(@RequestBody TbOrder order){
		//查询当前登录用户
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		order.setUserId(username);

		List<UserOrder> userOrders = orderService.findOrderByUser(order);

		return userOrders;
	}


	/**
	 * 通过用户名查找用户
	 * @return
	 */
	@RequestMapping("/findUserByUsername")
	public TbUser findUserByUsername() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		TbUser user = userService.findUserByUsername(username);

		return user;

	}


	//我的足迹
	@RequestMapping("/footmark")
	public List footmark(){
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return userService.footmark(username);
	}




	//我的收藏添加购物车
	@RequestMapping("/addGoodsToCartList")
	public Result addGoodsToCartList(Long itemId,Integer num){
		try {

			String username = SecurityContextHolder.getContext().getAuthentication().getName();

			List<Cart> cartList=new ArrayList<>();

			cartList = cartService.addGoodsToCartList(cartList, itemId, num);
			//未登录
			if("anonymousUser".equals(username)) {

				//保存购物列表
				String jsonString = JSON.toJSONString(cartList);

			}else {

				cartService.saveCartListToRedis(username, cartList);
			}
			return new Result(true, "添加成功!");
		}catch (RuntimeException e){
			//提示用户详细的信息
			return new Result(false, e.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
		}
		return new Result(false, "添加失败!");
	}

}
