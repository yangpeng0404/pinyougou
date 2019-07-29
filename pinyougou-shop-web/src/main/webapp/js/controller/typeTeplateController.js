var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        brandPages:15,
        brandPageNo:1,
        specPages:15,
        specPageNo:1,
        list:[],
        brandList:[],
        specList:[],
        entity:{specIds:[],brandIds:[],customAttributeItems:[{text:''}]},
        specIdsStr:'',
        brandIdsStr:'',
        customAttributeItemsStr:'',
        ids:[],
        searchEntity:{}
    },
    methods: {
        saveCust:function () {
            this.customAttributeItemsStr = '';
            for (var i = 0; i < this.entity.customAttributeItems.length; i++) {
                if (i == 0) {
                    this.customAttributeItemsStr += this.entity.customAttributeItems[i].text;
                }else {
                    this.customAttributeItemsStr += "," + this.entity.customAttributeItems[i].text;
                }
            }
        },
        addRow:function () {
            //this.list.push({specName:'',option:''});
            this.entity.customAttributeItems.push({text:''});
        },
        addBrand:function (id,text) {
            for (var i = 0; i < this.entity.brandIds.length; i++) {
                if (this.entity.brandIds[i].id == id) {
                    return;
                }
            }

            this.entity.brandIds.push({'id':id,'text':text});
            if (this.brandIdsStr == '' || this.brandIdsStr == undefined) {
                this.brandIdsStr += (text + "");
            }else {
                this.brandIdsStr += ","+text
            }

        },
        addSpec:function (id,text) {
            for (var i = 0; i < this.entity.specIds.length; i++) {
                if (this.entity.specIds[i].id == id) {
                    return;
                }
            }
            this.entity.specIds.push({'id':id,'text':text})
            if (this.specIdsStr == '' || this.brandIdsStr == undefined) {
                this.specIdsStr += (text + "");
            }else {
                this.specIdsStr += ","+text
            }
        },
        searchSpecList:function (curPage) {
            axios.post('/specification/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.specList=response.data.list;

                //当前页
                app.specPageNo=curPage;
                //总页数
                app.specPages=response.data.pages;
            });
        },
        searchBrandList:function (curPage) {
            axios.post('/brand/search.shtml?pageNo='+curPage,this.searchEntity).then(function (response) {
                //获取数据
                app.brandList=response.data.list;

                //当前页
                app.brandPageNo=curPage;
                //总页数
                app.brandPages=response.data.pages;
            });
        },
        //该方法只要不在生命周期的
        add:function () {
            axios.post('/typeTemplate/add.shtml',this.entity).then(function (response) {
                console.log(response);
                alert(response.data.message);

            }).catch(function (error) {
                console.log("1231312131321");
            });
        }
    },
    //钩子函数 初始化了事件和
    created: function () {
        this.searchBrandList(1);
        this.searchSpecList(1);
    }

})
