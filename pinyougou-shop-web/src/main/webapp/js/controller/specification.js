var app = new Vue({
    el: "#app",
    data: {
        pages:15,
        pageNo:1,
        list:[],
        entity:{optionOrder:[{specName:'',option:''}]},
        ids:[],
        searchEntity:{}
    },
    methods: {
        addRow:function () {
            //this.list.push({specName:'',option:''});
            this.entity.optionOrder.push({specName:'',option:''});
        },
        //该方法只要不在生命周期的
        add:function () {
            axios.post('/specification/add.shtml',this.entity).then(function (response) {
                console.log(response);
                    alert(response.data.message)

            }).catch(function (error) {
                console.log("1231312131321");
            });
        }
    },
    //钩子函数 初始化了事件和
    created: function () {
      


    }

})
