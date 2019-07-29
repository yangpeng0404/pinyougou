﻿var app = new Vue({
    el: "#app",
    data: {
        pages:10,
        pageNo:1,
        list:[],
        entity:{},
        goodsId:{},
        title:{},
        smallPic:{},
        price:{},
        startTime:{},
        endTime:{},
        num:{},
        status:{},
        stockCount:{},
        searchEntity:{},
        ids:[]
        },
    methods: {
        searchList:function (curPage) {
            axios.post('/seckillGoods/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.list=response.data.list;

                //当前页
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            });
        },
        updateStatus:function (status) {
            axios.post('/seckillGoods/updateStatus.shtml?status='+status,this.ids).then(function (response) {
                if(response.data.success){
                    window.location.reload();
                    alert("成功")
                }
            });
        },
        //查询所有品牌列表
        findAll:function () {
            console.log(app);
            axios.get('/seckillGoods/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },
         findPage:function () {
            var that = this;
            axios.get('/seckillGoods/findPage.shtml',{params:{
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
            axios.post('/seckillGoods/add.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    window.location.reload()
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            axios.post('/seckillGoods/update.shtml',this.entity).then(function (response) {
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
            axios.get('/seckillGoods/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele:function () {
            axios.post('/seckillGoods/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        }



    },
    //钩子函数 初始化了事件和
    created: function () {
      
        this.searchList(1);

    }

})
