  var app = new Vue({

      el:"#app",
      data:{
          username:"pengge",

      },
      methods:{
          loadUserName:function () {
              axios.get('/login/getName.shtml').then(function (response) {
                  app.username=response.data;
              })
          }

      },
      created:function () {
          this.loadUserName();
      }

  })