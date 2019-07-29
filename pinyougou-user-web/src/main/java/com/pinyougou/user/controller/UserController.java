package com.pinyougou.user.controller;
import java.util.Date;
import java.util.List;

import com.pinyougou.common.utils.PhoneFormatCheckUtils;
import com.pinyougou.user.service.UserService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.validation.BindingResult;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbUser;

import com.github.pagehelper.PageInfo;
import entity.Result;
import entity.Error;
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




	@RequestMapping("/addToMyList")
	@CrossOrigin(origins="http://localhost:9107",allowCredentials="true")//注解方式
	public Result addToMyList(Long itemId, Integer num, HttpServletRequest request, HttpServletResponse response) {
		try {
			//find方法主要是做获取，add要做添加，也要判断是否登录
			String name = SecurityContextHolder.getContext().getAuthentication().getName();
			if ("anonymousUser".equals(name)) {
				//为登录
				List<Cart> cartList = findCartList(request,response);//获取购物车列表
				cartList = cartService.addGoodsToCartList(cartList, itemId, num);
				CookieUtil.setCookie(request, response, "cartList", JSON.toJSONString(cartList), 3600 * 24, "UTF-8");
			} else {
				//以登录
				List<Cart> cartList = findCartList(request,response);//获取购物车列表
				cartList = cartService.addGoodsToCartList(cartList,itemId,num);
				cartService.saveCartListToRedis(name,cartList);
			}
			return new Result(true, "添加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "添加失败");
		}
	}

	@RequestMapping("/findCartList")
	public List<Cart> findCartList(HttpServletRequest request,HttpServletResponse response) {

		//考虑是否登录
		String username = SecurityContextHolder.getContext().getAuthentication().getName();

		if ("anonymousUser".equals(username)) {
			//说明是匿名登录，就是未登录
			String cartListString = CookieUtil.getCookieValue(request, "cartList", "UTF-8");
			//如果cookie中没有的话，就给他一个空，但是不能为null
			if (StringUtils.isEmpty(cartListString)) {
				cartListString = "[]";
			}
			List<Cart> cookieCartList = JSON.parseArray(cartListString, Cart.class);
			return cookieCartList;

		} else {
			//是登录状态
			//操作redis
			List<Cart> cartListFromRedis = cartService.findCartListFromRedis(username);
			if (cartListFromRedis == null) {
				cartListFromRedis=new ArrayList<Cart>();
			}
			//如果走到这里，那么代表是登录了，不管是第几次登陆，这个时候就要合并
			//获取cookCar
			String cookieValue = CookieUtil.getCookieValue(request, "cartList","UTF-8");
			if(StringUtils.isEmpty(cookieValue)){
				cookieValue="[]";
			}
			List<Cart> cookieCarList = JSON.parseArray(cookieValue, Cart.class);
			//何必购物车
			List<Cart> cartListConmm = cartService.commMarge(cookieCarList,cartListFromRedis);
			//将新的car存入redis
			cartService.saveCartListToRedis(username,cartListConmm);
			//清除 cookie中的car
			CookieUtil.deleteCookie(request,response,"cartList");
			if (cartListConmm==null){
				cartListConmm= new ArrayList<Cart>();
			}
			//返回最新的car
			return cartListConmm;
		}
	}
}
