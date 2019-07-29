var app = new Vue({
    el: "#app",
    data: {
        pages:10,
        pageNo:1,
        list:[],
        entity:{},
        seckillId:{},
        money:{},
        sellerId:{},
        payTime:{},
        searchEntity:{},
        ids:[],
        specList:[],//规格的数据列表 格式：[{id:1,text:"网络",options:[{},{}]}]
    },
    methods: {
        searchList:function (curPage) {
            axios.post('/seckillOrder/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.list=response.data.list;

                //当前页
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;

            });
        },



        //查询所有订单列表
        findAll:function () {
            console.log(app);
            axios.get('/seckillOrder/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },
    },

    //钩子函数 初始化了事件和
    created: function () {
        this.searchList(1);

    }

})
