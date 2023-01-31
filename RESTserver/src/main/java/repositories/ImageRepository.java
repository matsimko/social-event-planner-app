package repositories;

import javax.enterprise.context.ApplicationScoped;

import domain.ImageEntity;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class ImageRepository implements PanacheRepository<ImageEntity> {

}
