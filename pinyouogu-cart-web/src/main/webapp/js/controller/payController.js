var app = new Vue({
    el:"#app",
    data:{
        payObject:{},//封装支付的金额 二维码连接 交易订单号
        showMoney:''
    },
    methods:{
        createNative:function () {
          axios.get('/pay/createNative.shtml').then(function (response) {
              //如果有数据
              if (response.data) {
                 app.payObject= response.data
                  //将金额转换为元
                  app.payObject.total_fee=app.payObject.total_fee/100

                  //生成二维码
                  var qr = new QRious({
                      element:document.getElementById('qrious'),
                      size:250,
                      level:'H',
                      value:app.payObject.code_url
                  });
                 //合成之后开始调用检测方法
                  app.queryPayStatus(app.payObject.out_trade_no)
              }
          })
        },
        queryPayStatus:function (out_trade_no) {
            axios.get('/pay/queryPayStatus.shtml?out_trade_no='+out_trade_no).then(function (response) {
                //有数据
                if(response.data){
                    if(response.data.success){
                        //支付成功
                        window.location.href="paysuccess.html?money="+app.payObject.total_fee;
                    }else{
                        if(response.data.message=='超时'){
                            alert("超时，点击刷新二维码")
                            app.createNative();//刷新二维码
                        }else {
                            //支付失败
                            window.location.href="payfail.html";
                        }
                    }
                }else{
                    alert("错误");
                }

            })
        }
    },
    //钩子函数
    created:function () {
        //页面一加载就应当调用
        this.createNative();

        var obj = this.getUrlParam();

        this.showMoney = obj.money
    }

})