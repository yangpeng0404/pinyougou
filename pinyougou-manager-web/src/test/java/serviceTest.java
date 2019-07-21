import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.sellergoods.service.TbBrandService;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:spring/springmvc.xml")
public class serviceTest {

      @Reference
      private TbBrandService tbBrandService;

        /*@Test
        public void  findPafe(){
            PageInfo<TbBrand> page = tbBrandService.findPage(1, 2);
            List<TbBrand> list = page.getList();
            System.out.println(list);
        }*/
}
