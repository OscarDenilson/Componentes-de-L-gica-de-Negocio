package mx.medinova.infrastructure;

import mx.medinova.domain.DetalleOperacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface DetalleOperacionRepository extends JpaRepository<DetalleOperacion, Long> {
    List<DetalleOperacion> findByOperacionId(String operacionId);
}

