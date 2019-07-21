var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{},
        ids:[],
        seckillId:0,
        timeString:'',
        messageInfo:'',
        goodInfo:{},
        searchEntity:{}
    },
    methods: {
        /**
         *
         * @param alltime 为 时间的毫秒数。
         * @returns {string}
         */
        convertTimeString:function(alltime){
            var allsecond=Math.floor(alltime/1000);//毫秒数转成 秒数。
            var days= Math.floor( allsecond/(60*60*24));//天数
            var hours= Math.floor( (allsecond-days*60*60*24)/(60*60) );//小数数
            var minutes= Math.floor(  (allsecond -days*60*60*24 - hours*60*60)/60    );//分钟数
            var seconds= allsecond -days*60*60*24 - hours*60*60 -minutes*60; //秒数
            if(days>0){
                days=days+"天 ";
            }
            if(hours<10){
                hours="0"+hours;
            }
            if(minutes<10){
                minutes="0"+minutes;
            }
            if(seconds<10){
                seconds="0"+seconds;
            }
            return days+hours+":"+minutes+":"+seconds;
        },
        submitOrder:function () {
            axios.get('/seckillOrder/submitOrder/'+this.seckillId+'.shtml').then(
                function (response) {
                    if(response.data.success){
                        alert(response.data.message)
                        app.messageInfo=response.data.message;

                        //下单之后调用查询状态
                        app.queryStatus()
                    }else{
                        if(response.data.message=='403'){
                            //说明没有登录  去登录
                            //获取 本页面url
                            var url = window.location.href;
                            window.location.href="/page/login.shtml?url="+url;

                        }else {
                            app.messageInfo=response.data.message;

                        }
                    }
                }
            )
        },
        //倒计时
        caculate: function (alltime) {

            let clock = window.setInterval(function () {
                alltime = alltime - 1000;
                //反复被执行的函数

                app.timeString = app.convertTimeString(alltime);
                if (alltime <= 0) {
                    //取消
                    window.clearInterval(clock);
                }
            }, 1000);//相隔1000执行一次。
        },
        getGoodsById:function (id) {
            axios.post('/seckillGoods/getGoodsById.shtml?id='+id).then(function (response) {
                if(response.data){
                    app.goodInfo=response.data
                    app.caculate(response.data.time)
                }else {
                    app.goodInfo={count:'商品已售完'}
                }

            })
        },
        /*
        当点击下单，循环查询下单状态
         */
        queryStatus:function () {
            let count = 0;
            let queryOrder = window.setInterval(function () {
                count+=3;
                axios.get('/seckillOrder/queryOrderStatus.shtml').then(function (response) {
                    if(response.data.success){
                        alert("跳转到支付页面");
                        window.clearInterval(queryOrder);
                        window.location.href="pay/pay.html";
                    }else {
                        if(response.data.message=='403'){
                            //要登录
                        }else{
                            //不需要登录需要提示
                            app.messageInfo=response.data.message+"....."+count;
                            if(count>=100){
                                window.clearInterval(queryOrder);
                            }
                        }
                    }
                })
            },3000)

        }
    },
    //钩子函数 初始化了事件和
    created: function () {

        this.caculate(1000000);

        var id = this.getUrlParam().id;

        this.seckillId=id;

        this.getGoodsById(id)
    }

})
