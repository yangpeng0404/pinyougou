var app = new Vue({
    el: "#app",
    data: {
        num: 1,//商品的购买数量
        //初始化规格也就是默认的sku规格
        specificationItems: JSON.parse(JSON.stringify(skuList[0].spec)),//定义一个变量用于存储规格的数据
        sku: skuList[0]
    },
    methods: {
        addNum: function (num) {
            num = parseInt(num);
            this.num += num;//加或者减
            if (this.num <= 1) {
                this.num = 1;
            }
        },
        selectSpecifcation: function (name, value) {
            //设置值
            this.$set(this.specificationItems, name, value);
            this.search();
        },
        isSelected: function (name, value) {
            if (this.specificationItems[name] == value) {
                return true;
            } else {
                return false;
            }
        },
        search: function () {
            for (var i = 0; i < skuList.length; i++) {
                var object = skuList[i];
                if (JSON.stringify(this.specificationItems) == JSON.stringify(skuList[i].spec)) {
                    console.log(object);
                    this.sku = object;
                    break;
                }
            }
        },
        addGoodsToCartList: function () {
            //跨域请求,发一个添加一个商品的请求，参数是skuid和num
            //但是设计到跨越请求，携带参数以及cookie要在目标系统配置允许
            axios.get('http://localhost:9107/cart/addGoodsToCartList.shtml',
                {
                    params: {
                        itemId: this.sku.id,
                        num: this.num
                    },
                    withCredentials: true//携带cookie
                }
            ).then(function (response) {
                if (response.data.success) {
                    //添加购物车成功
                    window.location.href = "http://localhost:9107/cart.html";
                } else {
                    //添加购物车失败
                    alert(response.data.message);
                }

            })
        },
        //用户--足迹
        footmark:function () {

            axios.post('/page/footmark.shtml?URL='+location.href)
        }
    },

    //钩子函数 初始化了事件和
    created: function () {
        this.footmark();
    }

})