package com.example.courseproject.Projections;
import com.example.courseproject.model.Items;
import org.springframework.data.rest.core.config.Projection;
@Projection(types = Items.class)
public interface ItemProjection {
    Long getId();
    String getJson();
}
