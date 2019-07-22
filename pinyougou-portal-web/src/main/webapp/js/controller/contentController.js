var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{},
        ids:[],
        keywords:'',
        searchEntity:{},
        itemCat1List:[],//一级分类的列表 变量
        itemCat2List:[],//二级分类的列表 变量
        contentMap:{contentList:[],contentList2:[]}
    },
    methods: {
        searchList:function (curPage) {
            axios.post('/content/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
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
            axios.get('/content/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },
         findPage:function () {
            var that = this;
            axios.get('/content/findPage.shtml',{params:{
                pageNo:this.pageNo
            }}).then(function (response) {
                console.log(app);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data.list;
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            }).catch(function (error) {

            })
        },
        //该方法只要不在生命周期的
        add:function () {
            axios.post('/content/add.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            axios.post('/content/update.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        save:function () {
            if(this.entity.id!=null){
                this.update();
            }else{
                this.add();
            }
        },
        findOne:function (id) {
            axios.get('/content/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele:function () {
            axios.post('/content/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        findByCategoryId:function (categoryId) {
            axios.get('/content/findByCategoryId/'+categoryId+'.shtml').then(function (response) {
                app.contentMap.contentList=response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        findByCategoryId2:function (categoryId) {
            axios.get('/content/findByCategoryId/'+categoryId+'.shtml').then(function (response) {
                app.contentMap.contentList2=response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        //获取一级分类的类别的方法
        findItemCat1List:function () {
            axios.get('/itemCat/findParentId/0.shtml').then(
                function (response) {
                    //获取列表数据
                    app.itemCat1List=response.data;
                }
            )
        },
        doSearch:function () {
            window.location.href="http://localhost:9104/search.html?keywords="+encodeURIComponent(this.keywords);
        }

    },
    watch: {
        //监听变量：entity.goods.category1Id 的变化  触发 一个函数 发送请求 获取 一级分类的下的二级分类的列表
        'entity.goods.category1Id': function (newval, oldval) {
            if (newval != undefined) {
                axios.get('/itemCat/findParentId/' + newval + '.shtml').then(
                    function (response) {
                        //获取列表数据
                        app.itemCat2List = response.data;
                    }
                )
            }
        }
    },
    //钩子函数 初始化了事件和
    created: function () {

        this.findItemCat1List();
        this.findByCategoryId(1);
        this.findByCategoryId2(3);
    }

})
