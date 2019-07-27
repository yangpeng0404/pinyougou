var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        goods:{},
        items:{},
        seckillGoods:{},
        entity:{goods:{},items:{},seckillGoods:{}},
        ids:[],
        specList:[],//规格的数据列表 格式：[{id:1,text:"网络",options:[{},{}]}]
        seckillGoodsList:[],//商品的列表 变量
        itemList:[],//物品分类的列表 变量
        brandTextList:[],//品牌的列表
        searchEntity:{},
        start_time:{},
        end_time:{}

    },
    methods: {
        addSeckillGoods:function () {
            //获取stock_count库存,判断秒杀数量和库存数量关系,大于则提示,不提交
            var stockCount = this.items.num;
            var num = this.seckillGoods.num;
            if(num == null || num == undefined || num==''){
                alert("请输入秒杀商品数量");
                return;
            }
            if (stockCount<num) {
                alert("秒杀商品数量必须小于库存数");
                return;
            }else {
                this.seckillGoods.stockCount = stockCount;
            }
            //价格和折扣价格,前者必须大于等于后者,赋值price
            var price = this.items.price;
            var costprice= this.seckillGoods.costPrice;

            if(costprice == null || costprice == undefined || costprice==''){
                alert("请输入秒杀价格");
                return;
            }
            if (price<costprice) {
                alert("秒杀价格必须小于原价");
                return;
            }else {
                this.seckillGoods.price = price;
            }
            //赋值small_pic,从items当中
            this.seckillGoods.smallPic = this.items.image;
            //赋值title
            this.seckillGoods.title = this.items.title;
            //获取item_id
            this.seckillGoods.itemId = this.items.id;
            //获取goods_id
            this.seckillGoods.goodsId = this.goods.id;

            //获取富文本编辑框值
            this.seckillGoods.introduction=editor.html();

            axios.post('/seckillGoods/addSeckillGoods.shtml',this.seckillGoods).then(function (response) {
                if(response.data.success){
                    alert("添加成功");

                }else {
                    alert("添加失败")
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });


        },
        searchList:function (curPage) {
            axios.post('/goods/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.list=response.data.list;

                //当前页
                app.pageNo=curPage;
                //总页数
                app.pages=response.data.pages;
            });
        },
        //查询所有品牌列表
        findGoodsBySellerId:function () {
            console.log(app);
            axios.get('/seckillGoods/findGoodsBySellerId.shtml').then(function (response) {
                //注意：this 在axios中就不再是 vue实例了。
                app.seckillGoodsList=response.data;
            }).catch(function (error) {

            })
        },
         findPage:function () {
            var that = this;
            axios.get('/goods/findPage.shtml',{params:{
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
            //添加商品（3个表）
            //通过kindediter的方法 获取 html代码，赋值给变量introduction
            this.entity.seckillGoods.introduction=editor.html();
            axios.post('/goods/add.shtml',this.entity).then(function (response) {
                if(response.data.success){
                    alert("添加成功")
                   window.location.href="goods.html";
                }else {
                    alert("添加失败")
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        update:function () {
            //获取富文本的值 赋值给变量
            this.entity.goodsDesc.introduction=editor.html();
            axios.post('/goods/update.shtml',this.entity).then(function (response) {
                console.log(response);
                if(response.data.success){
                    window.location.href="goods.html";
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        save:function () {
            if(this.entity.goods.id!=null){//如果是更新
                this.update();
            }else{
                this.add();
            }
        },
        findOne:function (id) {
            axios.get('/goods/findOne/'+id+'.shtml').then(function (response) {
                app.entity=response.data;

              //  获取介绍信息 赋值给富文本编辑器
               editor.html(app.entity.goodsDesc.introduction);

               //转json
                app.entity.goodsDesc.itemImages=JSON.parse(app.entity.goodsDesc.itemImages);
               app.entity.goodsDesc.customAttributeItems=JSON.parse( app.entity.goodsDesc.customAttributeItems);
               app.entity.goodsDesc.specificationItems=JSON.parse( app.entity.goodsDesc.specificationItems);

                //将SKU列表中的规格的数据转成JSON

                var itemList = app.entity.itemList;//[{spec:\{\},price:0.01},{}]

                for(var i=0;i<itemList.length;i++){
                    var obj = itemList[i];//  {spec:\{\},price:0.01}
                    obj.spec=JSON.parse(obj.spec);
                }


            }).catch(function (error) {

            });
        },
        dele:function () {
            axios.post('/goods/delete.shtml',this.ids).then(function (response) {
                console.log(response);
                if(response.data.success){
                    app.searchList(1);
                }
            }).catch(function (error) {
                console.log("1231312131321");
            });
        },
        //   <input type="text" name="usenrame"> ----->pojo  username
        //方法 是用于图片上传使用 点击上传的按钮的时候调用
        uploadFile:function () {
            //创建一个表单的对象
            var formData=new FormData();

            //添加字段    formData.append('file'           ==> <input type="file"  name="file" value="文件本身">
            //            file.files[0]    第一个file 指定的时候 标签中的id   后面的files[0] 表示获取 选中的第一张文件 对象。File
            formData.append('file', file.files[0]);

            axios({
                url: 'http://localhost:9110/upload/uploadFile.shtml',
                //数据  表单数据
                data: formData,
                method: 'post',
                //设置表单提交的数据类型
                headers: {
                    'Content-Type': 'multipart/form-data'
                },
                //开启跨域请求携带相关认证信息
                withCredentials:true
            }).then(function (response) {
                //文件上传成功
                if(response.data.success){
                    console.log(response.data.message);
                    app.image_entity.url=response.data.message;
                }else{
                    //上传失败
                    alert(response.data.message);
                }
            })
        },
        //向数组中添加 图片对象
        addImageEntity:function () {
            this.entity.goodsDesc.itemImages.push(this.image_entity);
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
        //函数的作用：是就是点击的时候调用  改变 变量specificationItems的值
        /**
         *
         * @param specName  网络
         * @param specValue  移动4G
         */
        updateChecked:function ($event,specName,specValue) {

            var obj = this.searchObjectByKey(this.entity.goodsDesc.specificationItems,'attributeName',specName);//{"attributeValue":["移动3G"],"attributeName":"网络"}

            //1.如果有对象 直接设置对象里面的属性值

            if(obj!=null){

                if($event.target.checked){
                     //如果 是勾选  添加数据
                    //向数组属性中添加规格的选项值
                    obj.attributeValue.push(specValue);
                }else{
                    //如果取消勾选  删除数据

                    obj.attributeValue.splice(obj.attributeValue.indexOf(specValue),1);

                    //如果 删除完了数据
                    if( obj.attributeValue.length==0){
                        this.entity.goodsDesc.specificationItems.splice(this.entity.goodsDesc.specificationItems.indexOf(obj),1);
                    }

                }




            }else{
                //2.如果没有对象  直接添加对象

                this.entity.goodsDesc.specificationItems.push(
                    {"attributeValue":[specValue],"attributeName":specName} );
            }
        },
        //作用 就是 从specificationItems变量中 根据 规格的名称来 查询 是否都有对象
        /**
         *
         * @param list  指定的是要搜索的对象数组
         * @param specName  要 找的属性的名称
         * @param key   要找的属性的名称 对应的值
         * @returns {*}
         */
        searchObjectByKey:function (list,specName,key) {
            /*[
                {"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
                {"attributeValue":["16G","32G"],"attributeName":"机身内存"}
            ]*/
            var specItems = list;

            for(var i=0;i<specItems.length;i++){
                var obj = specItems[i];//{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}
                if(key==obj[specName]){
                    return obj;
                }
            }

            return null;

        },
        //点击复选框的时候 调用生成 sku列表的的变量
        createList:function () {
             //1.定义初始化的值
            this.entity.itemList=[{'spec':{},'price':0,'num':0,'status':'0','isDefault':'0'}];

            //2.循环遍历 specificationItems

            /**
             * [
             {"attributeValue":["移动3G","移动4G"],"attributeName":"网络"},
             {"attributeValue":["16G","32G"],"attributeName":"机身内存"}
             ]
             *
             */
            var specificationItems=this.entity.goodsDesc.specificationItems;
            for(var i=0;i<specificationItems.length;i++){
                //3.获取 规格的名称 和规格选项的值 拼接 返回一个最新的SKU的列表
                var obj = specificationItems[i]; //{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}
                this.entity.itemList=this.addColumn(this.entity.itemList,obj.attributeName,obj.attributeValue);
            }
        },

        //.获取 规格的名称 和规格选项的值 拼接 返回一个最新的SKU的列表 方法
        /**
         *
         * @param list
         * @param columnName  网络
         * @param columnValue  [移动3G,移动4G]
         */
        addColumn: function (list, columnName, columnValue) {
            var newList=[];

            for (var i = 0; i < list.length; i++) {
                var oldRow = list[i];//   {'spec':{},'price':0,'num':0,'status':'0','isDefault':'0'}

                for (var j = 0; j < columnValue.length; j++) {
                    var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                    var value = columnValue[j];//移动3G
                    newRow.spec[columnName] = value;  //{'spec':{"网络":"移动3G"},'price':0,'num':0,'status':'0','isDefault':'0'}
                    newList.push(newRow);
                }


            }

            return newList;
        },
        isChecked:function (specName,specValue) {
            //判断  循环到的 规格选项 的值 是否 在已有的变量中存在，如果 存在就要勾选，否则就不勾选
            var specificationItems = this.entity.goodsDesc.specificationItems;//[{"attributeValue":["移动3G","移动4G"],"attributeName":"网络"}]
            var  searchObjectByKey = this.searchObjectByKey(specificationItems,"attributeName",specName);

            if(searchObjectByKey==null){
                return false;
            }
            for(var i=0;i<specificationItems.length;i++){//[]
                if(specificationItems[i].attributeValue.indexOf(specValue)!=-1){
                    return true;
                }
            }

            return false;
        }



    },
    //定义一个监听
    watch:{
        'goods':function (newval,oldval) {
            if(newval!=undefined) {
                axios.get('/seckillGoods/findItemsByGoodsId/' + newval.id + '.shtml').then(
                    function (response) {
                        //获取列表数据
                        app.itemList = response.data;
                    }
                )
            }
        }

    },

    //钩子函数 初始化了事件和
    created: function () {
        // this.findItemCat1List();

        this.findGoodsBySellerId();

        //获取URL两种的值
        // var obj= this.getUrlParam();
        // // alert(obj.name);
        // //发送请求 根据商品的ID 获取商品的数据  绑定到entity里面
        // if(obj.id != undefined){
        //     this.findOne(obj.id);
        // }
    },

    //日历插件值绑定,使用回调函数done
    mounted: function () {
    console.log(laydate)
    laydate.render({
        elem: '#test5',
        type:'datetime',
        done:function (value) {
            app.seckillGoods.startTime = value
        }
});
        laydate.render({
            elem: '#test6',
            type:'datetime',
            done:function (value) {
                app.seckillGoods.endTime = value
            }
        })
}

});
