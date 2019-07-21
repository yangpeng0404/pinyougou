var app = new Vue({
    el: "#app",
    data: {
        cartList: [],
        totalMoney:0,//总金额
        totalNum:0,//总数量
        username:'',
        addressList: [],
        address:{},
        order:{'paymentType':'1'}
    },
    methods: {
        //添加一个方法
        submitOrder: function () {
            //设置值,并且渲染
            //因为地址上是有地址，电话，收件人，不用填写
            this.$set(this.order,'receiverAreaName',this.address.address);
            this.$set(this.order,'receiverMobile',this.address.mobile);
            this.$set(this.order,'receiver',this.address.contact);
            alert(JSON.stringify(this.order))
            axios.post('/order/add.shtml', this.order).then(
                function (response) {
                    if(response.data.success){
                        //跳转到支付页面
                        window.location.href="pay.html";
                    }else{
                        alert(response.data.message);
                    }
                }
            )
        },
        findCartList: function () {
            axios.get('/cart/findCartList.shtml').then(function (response) {
                //获取购物车列表数据
                app.cartList = response.data;
                app.totalMoney=0;
                app.totalNum=0;
                let  cartListAll=response.data;

                for(let i=0;i<cartListAll.length;i++){
                    let cart = cartListAll[i];
                    for(let j=0;j<cart.orderItemList.length;j++){
                        app.totalNum+=cart.orderItemList[j].num;
                        app.totalMoney+=cart.orderItemList[j].totalFee;
                    }
                }
            });
        },
        selectType:function (type) {
            this.$set(this.order,'paymentType',type);
            //this.order.paymentType=type;
        },

        /**
         * 向已有的购物车添加商品
         * @param itemId
         * @param num
         */
        addGoodsToCartList:function (itemId,num) {
            axios.get('/cart/addGoodsToCartList.shtml', {
                params: {
                    itemId:itemId,
                    num:num
                }
            }).then(function (response) {
                if(response.data.success){
                    //添加成功
                    app.findCartList();
                }else{
                    //添加失败
                    alert(response.data.message);
                }
            });
        },
        getUserName:function () {
            axios.get('/login/name.shtml').then(function (response) {
                if('anonymousUser'!=response.data){
                    app.username=response.data
                }else {
                    app.username='品优购'
                }
            })
        },
        /**
         * 找到adresslist
         */
        findAddressList:function () {
            axios.get('/address/findAddressListByUserId.shtml').then(function (response) {
                app.addressList=response.data;
                for(var i=0;i<app.addressList.length;i++){
                    if(app.addressList[i].isDefault=='1'){
                        app.address=app.addressList[i];
                        break;
                    }
                }
            });
        },
        selectAddress:function (address) {
            this.address=address;
        },
        isSelectedAddress:function (address) {
            if(address==this.address){
                return true;
            }
            return false;
        },
    },
    //钩子函数 初始化了事件和
    created: function () {
        //初始化拿到 购物车
        this.findCartList();

        this.getUserName();

        //如果url是去到订单选项biao就调用这个方法
        //上面两个方法，登录和未登录都能调用，这个是肯定会登录
        if(window.location.href.indexOf("getOrderInfo.html")!=-1)
            this.findAddressList();
    }

})