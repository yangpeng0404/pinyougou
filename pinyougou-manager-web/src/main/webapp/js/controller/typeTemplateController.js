var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{},
        ids:[],
        searchEntity:{},
        entity:{customAttributeItems:[{}]},//初始化
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
         findPage:function () {
            var that = this;
            axios.get('/typeTemplate/findPage.shtml',{params:{
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
            axios.post('/typeTemplate/add.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            axios.post('/typeTemplate/update.shtml',this.entity).then(function (response) {
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
            axios.get('/typeTemplate/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;
                app.entity.brandIds=JSON.parse(app.entity.brandIds);
                app.entity.customAttributeItems=JSON.parse(app.entity.customAttributeItems);
                app.entity.specIds=JSON.parse(app.entity.specIds);
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        dele:function () {
            axios.post('/typeTemplate/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        findBrands:function () {
            axios.get('/brand/findAll.shtml').then(function (response) {
                var brandList = response.data;//[{id,name}]
                for(var i=0;i<brandList.length;i++){
                    //定义一个集合接收数据
                    app.brandOptions.push({id:brandList[i].id,text:brandList[i].name});
                }
            })
        },
        findSpecs:function () {
            axios.get('/specification/findAll.shtml').then(function (response) {
                var brandList = response.data;//[{id,name}]
                for(var i=0;i<brandList.length;i++){
                    //定义一个集合接收数据
                    app.specOptions.push({id:brandList[i].id,text:brandList[i].specName});
                }
            })
        },
        addTableRow:function () {
            this.entity.customAttributeItems.push({});
        },
        removeTableRow:function (index) {
            this.entity.customAttributeItems.splice(index,1);
        },
        jsonToString:function (list,key) {
            // alert(list)
            //用于循环遍历  获取对象中的属性的值 拼接字符串,返回
            //[{"id":27,"text":"网络"},{"id":32,"text":"机身内存"}]
            //注意对象不要为空，如果为空转json对象会报错
            var listJson = JSON.parse(list)
            var str = "";
            for(var i=0;i<listJson.length;i++){
                //{"id":27,"text":"网络"}
                var obj = listJson[i];
                str+=obj[key]+",";
            }
            if(str.length>0) {
                str = str.substring(0, str.length - 1);
            }
            // var ojb = {id:1}
            //ojb.id   =1  ojb['id']=1

            return str;
        }

    },
    //钩子函数 初始化了事件和
    created: function () {
      
        this.searchList(1);

        this.findBrands();

        this.findSpecs();
    }

})
