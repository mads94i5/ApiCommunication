package dat3.apicommunication.repository;

import dat3.apicommunication.entity.NameInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NameInfoRepository extends JpaRepository<NameInfo, Long> {
    NameInfo findByName(String name);
}
