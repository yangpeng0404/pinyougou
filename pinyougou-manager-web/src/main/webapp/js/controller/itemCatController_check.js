﻿var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{parentId:'0'},
        ids:[],
        searchEntity:{status:'0'},
        entity_1:{},//变量1
        entity_2:{},//变量2
        grade:1//当前等级
    },
    methods: {
        updateStatus:function (status) {
            axios.post('/itemCat/updateStatus/'+status+'.shtml',this.ids).then(function (response) {
                if(response.data.success){
                    app.ids=[];
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        searchList:function (curPage) {
            axios.post('/itemCat/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
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
            axios.get('/itemCat/findAll.shtml').then(function (response) {
                console.log(response);
                //注意：this 在axios中就不再是 vue实例了。
                app.list=response.data;

            }).catch(function (error) {

            })
        },
         findPage:function () {
            var that = this;
            axios.get('/itemCat/findPage.shtml',{params:{
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
            axios.post('/itemCat/add.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.findByParentId(app.entity.parentId);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            axios.post('/itemCat/update.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    alert('修改成功')
                    app.findByParentId(app.entity.parentId);
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
            axios.get('/itemCat/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele:function () {
            axios.post('/itemCat/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    alert('删除成功')
                    app.findByParentId(app.entity.parentId);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        findByParentId:function (parentId) {
            axios.get('/itemCat/findParentId/'+parentId+'.shtml').then(function (response) {
                app.list=response.data
            }).catch(function (error) {
                console.log("1231312131321");
            })
        },
        selectList:function (p_entity) {
            //等级一给它两空
            if(app.grade==1){
                app.entity_1={};
                app.entity_2={};
            }
            if(app.grade==2){
                app.entity_1=p_entity;
                app.entity_2={};
            }
            // 第三级就1不动，二给值
            if(app.grade==3){
                app.entity_2=p_entity;
            }
            //将当前的parentId保存
            app.entity.parentId=p_entity.id;

            //alert(JSON.stringify(app.entity_1))
            //等级赋了值 别忘了查询,它的id就是别人的父id
            app.findByParentId(p_entity.id)
        }


    },
    //钩子函数 初始化了事件和
    created: function () {
        this.searchList(1);
    }

})
