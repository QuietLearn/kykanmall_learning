package com.mmall.pojo;

import lombok.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.Date;
import java.util.Objects;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Category {
    private Integer id;

    private Integer parentId;

    private String name;

    private Boolean status;

    private Integer sortOrder;

    private Date createTime;

    private Date updateTime;


    //对值进行hashcode算法，如果值相同，hashcode也一定相同
    /*@Override
    public int hashCode() {
        //对值进行hashcode算法，如果值相同，hashcode也一定相同
        return Objects.hash(id);
    }*/
}