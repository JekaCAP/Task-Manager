package hw.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTaskStatusRequest {

    private String status;

    private int version;

    public UpdateTaskStatusRequest(String status, int version){
        this.status = status;
        this.version = version;
    }

}
