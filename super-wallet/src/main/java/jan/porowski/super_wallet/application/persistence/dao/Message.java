package jan.porowski.super_wallet.application.persistence.dao;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.UUID;

public sealed interface Message permits EventMessage, SnapshotMessage {

    UUID id();
    boolean published();
}
