package entity;

import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationOption;

import java.io.Serializable;
import java.util.List;

/**
 * @作者:pengge
 * @时间:2019/06/24 14:02
 */
public class Specification implements Serializable {

    private TbSpecification specification;//一个规格
    private List<TbSpecificationOption> optionList;//多个规格选项

    @Override
    public String toString() {
        return "Specification{" +
                "specification=" + specification +
                ", optionList=" + optionList +
                '}';
    }

    public TbSpecification getSpecification() {
        return specification;
    }

    public void setSpecification(TbSpecification specification) {
        this.specification = specification;
    }

    public List<TbSpecificationOption> getOptionList() {
        return optionList;
    }

    public void setOptionList(List<TbSpecificationOption> optionList) {
        this.optionList = optionList;
    }
}
