var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        ids:[],
        itemCat1List:{},//一级分类 变量
        itemCat2List:{},//二级分类 变量
        itemCat3List:{},//三级分类
        searchEntity:{},
        entity:{itemCat1List:{},itemCat2List:{},itemCat3List:{}},//初始化
        brandOptions:[],//用于接收所有的品牌
        specOptions:[]//用于接收所有的规格
    },
    methods: {
        searchList:function (curPage) {
            axios.post('/typeTemplate/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.list=response.data.list;

                //当前页
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            });
        },
        //查询所有品牌列表
        findAll:function () {
            console.log(app);
            axios.get('/typeTemplate/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },

        //{params:{
        //                 'itemCat1List':this.itemCat1List,
        //                     'itemCat2List':this.itemCat2List,
        //                     'itemCat3List':this.itemCat3List
        //             }}
        //该方法只要不在生命周期的
        add:function () {
            axios.post('/itemCat/add.shtml',this.entity
                ).then(function (response) {
               alert(response.data.message)

            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
    },
    //钩子函数 初始化了事件和
    created: function () {
      
        this.findAll();

    }

});
